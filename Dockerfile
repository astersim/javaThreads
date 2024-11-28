FROM maven:3.9-eclipse-temurin-22 AS builder

WORKDIR /app

COPY . .

RUN mvn clean package

FROM eclipse-temurin:22-jdk

WORKDIR /app

COPY Files/CSVFile.csv /app/CSVFile.csv

COPY --from=builder /app/target/SupabaseDatabase-1.0-SNAPSHOT.jar app.jar

CMD ["tail", "-f", "/dev/null"]
