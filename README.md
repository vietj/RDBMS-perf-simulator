## Performance simulation

Test the influence of slow clients on the latency of all clients

3 tests to run (in the IDE - no fat-jar):

- com.example.demo.Main1
  - No extra latency
- com.example.demo.Main2
  - 10ms latency on (0.9,1) distribution
- com.example.demo.Main3
  - 90ms latency on (0.9,1) distribution

The test reports the latency distribution on the console every second:

```
min    = 1
max    = 124
50%    = 1
90%    = 2
99%    = 5
99.9%  = 101
99.99% = 124
```

This runs with a Postgresql DB that can be executed with Docker:

```
> cd src/docker
> docker build -t test/postgres postgres
> docker run --rm --name test-postgres -p 5432:5432 test/postgres
```
