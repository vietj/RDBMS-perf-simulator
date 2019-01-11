package com.example.demo;

import io.r2dbc.postgresql.PostgresqlConnection;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ConnectionPool {

  private final PostgresqlConnectionFactory factory;
  private final int size;
  private CompletableFuture<ConnectionPool> status;
  private List<PostgresqlConnection> all;
  private Deque<PostgresqlConnection> available;
  private Deque<CompletableFuture<PostgresqlConnection>> waiters = new ArrayDeque<>();

  public ConnectionPool(PostgresqlConnectionFactory factory, int size) {
    this.factory = factory;
    this.size = size;
  }

  public synchronized Mono<ConnectionPool> create() {

    return Mono.fromCompletionStage(() -> {
      CompletableFuture<ConnectionPool> fut = new CompletableFuture<>();
      synchronized (ConnectionPool.this) {
        if (status != null) {
          fut.completeExceptionally(new IllegalStateException());
          return fut;
        }
        status = fut;
      }
      Flux.range(0, size).flatMap(val -> factory.create()).collectList().subscribe(result -> {
        synchronized (ConnectionPool.this) {
          all = result;
          available = new ArrayDeque<>(all);
        }
        fut.complete(this);
      }, err -> {
        synchronized (ConnectionPool.this) {
          status = null;
        }
        fut.completeExceptionally(err);
      });
      return fut;
    });
  }

  public Mono<PostgresqlConnection> acquire() {
    return Mono.fromCompletionStage(() -> {
      CompletableFuture<PostgresqlConnection> fut = new CompletableFuture<>();
      synchronized (this) {
        if (status != null && status.isDone()) {
          PostgresqlConnection conn = available.poll();
          if (conn != null) {
            fut.complete(conn);
          } else {
            waiters.add(fut);
          }
        } else {
          fut.completeExceptionally(new IllegalStateException());
        }
      }
      return fut;
    });
  }

  public void release(PostgresqlConnection conn) {
    CompletableFuture<PostgresqlConnection> waiter;
    synchronized (this) {
      waiter = waiters.poll();
      if (waiter == null) {
        available.add(conn);
        return;
      }
    }
    waiter.complete(conn);
  }
}
