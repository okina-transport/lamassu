FROM eclipse-temurin:21.0.5_11-jre

ARG JAR_FILE
COPY ${JAR_FILE} lamassu.jar

EXPOSE 8099
CMD java "$JAVA_OPTIONS" -jar /lamassu.jar