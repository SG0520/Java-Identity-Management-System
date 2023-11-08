FROM gradle:6.3.0

WORKDIR /app

COPY ./build/libs/*.jar ./

EXPOSE 8080

CMD ["java", "-jar", "identity-0.0.1-SNAPSHOT.jar"]
