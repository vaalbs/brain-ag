FROM sbtscala/scala-sbt:graalvm-ce-22.3.1_1.8.0_3.3.1 as builder
WORKDIR /app
COPY . /app
RUN sbt assembly

FROM eclipse-temurin:17-jre
WORKDIR /opt/app
COPY --from=builder /app/target/scala-2.13/brainagri-farmreg-backend-assembly-0.1.0.jar app.jar
ENV JDBC_URL=jdbc:postgresql://postgres:5432/farmreg \
    DB_USER=postgres \
    DB_PASSWORD=postgres
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]