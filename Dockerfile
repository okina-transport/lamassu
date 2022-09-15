FROM openjdk:11-jre
ADD target/lamassu-*-SNAPSHOT.jar lamassu.jar

EXPOSE 8777
CMD java $JAVA_OPTIONS -jar /lamassu.jar