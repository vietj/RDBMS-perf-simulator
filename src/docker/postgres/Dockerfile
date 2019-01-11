FROM healthcheck/postgres:alpine
ENV POSTGRES_DB postgres
ENV POSTGRES_USER postgres
ENV POSTGRES_PASSWORD postgres
COPY ./resources/create-postgres.sql /docker-entrypoint-initdb.d/create-postgres.sql
