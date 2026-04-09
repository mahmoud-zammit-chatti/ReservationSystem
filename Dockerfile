FROM eclipse-temurin:25-jdk-alpine as build

#build stafe
WORKDIR /app
COPY .mvn/ .mvn
COPY pom.xml pom.xml
COPY mvnw mvnw
COPY src/ src

RUN ./mvnw clean package -DskipTests

#run stage
FROM eclipse-temurin:25-jre-alpine as run
WORKDIR /app
COPY --from=build /app/target/*.jar voltBook.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "voltBook.jar"]
