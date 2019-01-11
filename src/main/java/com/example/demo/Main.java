package com.example.demo;

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.postgresql.PostgresqlStatement;

import java.util.concurrent.CountDownLatch;

public class Main {

  public static void main(String[] args) throws Exception {

    CountDownLatch latch = new CountDownLatch(1);

    PostgresqlConnectionConfiguration cfg = PostgresqlConnectionConfiguration.builder()
        .host("localhost")
        .database("postgres")
        .username("postgres")
        .password("postgres")
        .build();
    PostgresqlConnectionFactory factory = new PostgresqlConnectionFactory(cfg);

    ConnectionPool pool = new ConnectionPool(factory, 10);



    pool.create().flatMap(p -> p.acquire()).subscribe(conn -> {
      PostgresqlStatement<?> stmt = conn.createStatement("SELECT * FROM Fortune");
      stmt.execute().subscribe(result -> {
        System.out.println("result = " + result);
        result.map((row, md) -> row.get("message")).subscribe(row -> {
          System.out.println(row);
        }, Throwable::printStackTrace, () -> pool.release(conn));
      });
    });

    latch.await();

  }
}
