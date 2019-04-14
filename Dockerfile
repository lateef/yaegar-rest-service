FROM openjdk:8-jdk-alpine
MAINTAINER yaegar.com
VOLUME /tmp
ADD build/libs/yaegar-rest-service.jar yaegar-rest-service.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/yaegar-rest-service.jar"]
