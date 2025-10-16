FROM eclipse-temurin:17-jre-jammy
MAINTAINER protege.stanford.edu

EXPOSE 8886
ARG JAR_FILE
COPY target/${JAR_FILE} icatx-api-gateway.jar
ENTRYPOINT ["java","-jar","/icatx-api-gateway.jar"]