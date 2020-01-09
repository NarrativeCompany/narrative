FROM openjdk:10-jre

ENV VERSION=${VERSION:-0.3.2-SNAPSHOT}

# COPY jmx_exporter/jmx_prometheus_javaagent/target/jmx_prometheus_javaagent-${VERSION}.jar /jmx_prometheus_javaagent-${VERSION}.jar
COPY jmx_exporter/jmx_prometheus_httpserver/target/jmx_prometheus_httpserver-${VERSION}-jar-with-dependencies.jar /jmx_prometheus_httpserver-${VERSION}-jar-with-dependencies.jar

RUN mkdir -p /config

COPY config/tomcat.yml /config/tomcat.yml

COPY docker-entrypoint.sh /docker-entrypoint.sh

RUN chmod u+x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]



