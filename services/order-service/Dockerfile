FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/order-service-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]