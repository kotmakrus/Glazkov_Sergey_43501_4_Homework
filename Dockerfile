FROM maven:3.3-jdk-8-onbuild
CMD ["java","-jar","/usr/src/app/target/jsonvalidate-0.1-jar-with-dependencies.jar"]
EXPOSE 80
