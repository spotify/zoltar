FROM openjdk:8-jre

ENTRYPOINT ["/usr/bin/run.sh"]

COPY run.sh /usr/bin/run.sh
COPY target/lib /usr/share/apollo-service-example/lib
COPY target/apollo-service-example.jar /usr/share/apollo-service-example/apollo-service-example.jar
