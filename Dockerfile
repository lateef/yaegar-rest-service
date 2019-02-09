FROM openjdk:8-jdk-alpine
MAINTAINER yaegar.com
VOLUME /tmp
ADD build/libs/yaegar-rest-service.jar yaegar-rest-service.jar
ENTRYPOINT ["java","-jar","/yaegar-rest-service.jar"]
