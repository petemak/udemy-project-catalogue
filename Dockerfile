FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/udemy-project-catalogue-0.0.1-SNAPSHOT-standalone.jar /udemy-project-catalogue/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/udemy-project-catalogue/app.jar"]
