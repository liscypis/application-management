# application-management
Application to manage applications

This app use H2 db.

H2 console available at 'http://localhost:9091/h2-console'. 

Database available at 'jdbc:h2:mem:test'. 

Username: sa

Password: pass


## TEST

To run test use command in project directory:

```bash
  ./mvnw test -Dtest="ApplicationControllerIT"
```

## Run application

To run app use command in project directory:

```bash
  ./mvnw spring-boot:run
```
Application works on port: 9091

