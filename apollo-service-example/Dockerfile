FROM gcr.io/spotify-base-images/trusty-java:0.26
LABEL maintainer="flatmap-squad <flatmap-squad@spotify.com>"

ENTRYPOINT ["/usr/bin/ml-serving-example.sh"]

COPY ml-serving-example.sh /usr/bin/ml-serving-example.sh
COPY target/lib /usr/share/ml-serving-example/lib
COPY target/ml-serving-example.jar /usr/share/ml-serving-example/ml-serving-example.jar
