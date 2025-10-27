FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/chatservice.jar .
EXPOSE 8080
ENTRYPOINT ["java","-jar","chatservice.jar"]