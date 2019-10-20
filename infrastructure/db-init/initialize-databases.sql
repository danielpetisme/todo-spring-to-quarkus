CREATE USER dba WITH PASSWORD 'dba';

CREATE USER keycloak WITH PASSWORD 'keycloak';
CREATE DATABASE keycloak;
GRANT ALL PRIVILEGES ON DATABASE keycloak TO keycloak;
GRANT ALL PRIVILEGES ON DATABASE keycloak TO dba;

CREATE USER todo WITH PASSWORD 'todo';
CREATE DATABASE todo;
GRANT ALL PRIVILEGES ON DATABASE todo TO todo;
GRANT ALL PRIVILEGES ON DATABASE todo TO dba;