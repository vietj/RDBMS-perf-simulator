package com.example.demo;

import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoopGroup;
import io.r2dbc.postgresql.PostgresqlConnection;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.PostgresqlStatement;
import io.r2dbc.spi.Row;
import org.HdrHistogram.ConcurrentHistogram;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Simulation {

  private final int poolSize; // # pool size
  private final long rate; // # request per second
  private final LatencySeq latencySeq;
  private final AtomicInteger concurrentJobs = new AtomicInteger();
  private final EventLoopGroup timer = new DefaultEventLoop();
  private final ConcurrentHistogram latencies = new ConcurrentHistogram(TimeUnit.MINUTES.toNanos(1), 2);

  public Simulation(int poolSize, long rate, long[] latencyDistribution) {
    this.poolSize = poolSize;
    this.rate = rate;
    this.latencySeq = new LatencySeq(latencyDistribution);
  }

  public void run() {
    PostgresqlConnectionConfiguration cfg = PostgresqlConnectionConfiguration.builder()
        .host("localhost")
        .database("postgres")
        .username("postgres")
        .password("postgres")
        .build();
    PostgresqlConnectionFactory factory = new PostgresqlConnectionFactory(cfg);
    ConnectionPool pool = new ConnectionPool(factory, poolSize);
    pool.create().subscribe(this::run,
        err -> {
          System.out.println("Connect error");
          err.printStackTrace();
        }
    );
    // Print result on console
    long startTime = System.nanoTime();
    while (true) {
      ConcurrentHistogram copy = latencies.copy();
      copy.setStartTimeStamp(TimeUnit.NANOSECONDS.toMillis(startTime));
      copy.setEndTimeStamp(TimeUnit.NANOSECONDS.toMillis(System.nanoTime()));
      System.out.println("------------------------------");
      Report.prettyPrint(System.out, copy);
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
  }

  private void run(ConnectionPool pool) {
    new Thread(() -> {
      Random random = new Random();
      while (true) {
        double val = random.nextDouble();
        doJob(pool, latencySeq.next(val));
        try {
          Thread.sleep(1000 / rate);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    }).start();
  }

  private void doJob(ConnectionPool pool, long latency) {
    Mono<PostgresqlConnection> mono = pool.acquire();
    long startTime = System.nanoTime();;
    mono.subscribe(conn -> {
      PostgresqlStatement<?> stmt = conn.createStatement("SELECT * FROM Fortune");
      stmt.execute().flatMap(res -> res.map((row, md) -> row)).doAfterTerminate(() -> {
        Runnable release = () -> {
          long endTime = System.nanoTime();
          long durationNanos = endTime - startTime;
          latencies.recordValue(durationNanos);
          pool.release(conn);
        };
        if (latency == 0) {
          release.run();
        } else {
          timer.schedule(release, latency, TimeUnit.MILLISECONDS);
        }
      }).subscribe(new Subscriber<Row>() {
        Subscription sub;
        @Override
        public void onSubscribe(Subscription s) {
          // Initial request
          (sub = s).request(Long.MAX_VALUE);
        }
        @Override
        public void onNext(Row postgresqlResult) {
        }
        @Override
        public void onError(Throwable t) {
          t.printStackTrace();
        }
        @Override
        public void onComplete() {
        }
      });
    }, Throwable::printStackTrace);
  }
}
