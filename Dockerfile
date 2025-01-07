FROM eclipse-temurin:21-jdk AS build
LABEL authors="jiag293"
WORKDIR /app
COPY . .

RUN chmod +x gradlew && ./gradlew build -x test


FROM eclipse-temurin:21-jre AS production
WORKDIR /app

COPY --from=build /app/build/libs/CICDJenkins-0.0.1-SNAPSHOT.jar cicd-jenkins.jar
ENTRYPOINT ["java","-jar","cicd-jenkins.jar"]


