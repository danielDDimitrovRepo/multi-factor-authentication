FROM amazoncorretto:17
MAINTAINER nexo.com
COPY target/multi-factor-authentication-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]