FROM openjdk:17

COPY target/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]

#To build the image
# docker build -t apigateway .