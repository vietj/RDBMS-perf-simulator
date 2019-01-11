## Postgres docker file for testing

### Build the container

```
> docker build -t test/postgres postgres
```

### Run the container

```
> docker run --rm --name test-postgres -p 5432:5432 test/postgres
```
