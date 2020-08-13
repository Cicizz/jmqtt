# Builder
FROM maven:3-jdk-8-slim AS builder

RUN mkdir /opt/jmqtt
WORKDIR /opt/jmqtt

ADD . ./

RUN mvn -Ppackage-all -DskipTests clean install -U

# App
FROM openjdk:8-jre-slim

RUN mkdir /opt/jmqtt
VOLUME /var/jmqtt-data
WORKDIR /opt/jmqtt

COPY --from=builder /opt/jmqtt ./
COPY --from=builder /opt/jmqtt/jmqtt-distribution/conf /var/jmqtt-data/conf/


EXPOSE 1883
EXPOSE 1884

CMD ["./jmqtt-distribution/target/jmqtt/bin/jmqttstart", "-h", "/var/jmqtt-data", "-c", "/var/jmqtt-data/conf/jmqtt.properties"]