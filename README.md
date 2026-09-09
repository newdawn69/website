cmd : mvn spring-boot:run

src/main/java - Backend
src/main/resources - Frontend

database,
src/main/resources/application.properties     ← connection settings (URL, username, password)
src/main/java/model/User.java                 ← defines what a "User" looks like (becomes a table)
src/main/java/repository/UserRepository.java  ← code that talks to the database



