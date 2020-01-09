#!/bin/bash
set -e

java -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=5555 -jar /jmx_prometheus_httpserver-${VERSION}-jar-with-dependencies.jar 9100 /config/tomcat.yml
