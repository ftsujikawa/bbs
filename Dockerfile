FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY ./target/bbs-0.0.1-SNAPSHOT.jar /app/bbs-0.0.1-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/bbs-0.0.1-SNAPSHOT.jar"]