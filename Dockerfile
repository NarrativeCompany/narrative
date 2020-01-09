FROM openjdk:8-jre-slim-buster

ENV NARRATIVE_WAR ${NARRATIVE_WAR}

# make narrative-core directory
RUN mkdir -p /opt/narrative-core/config
RUN mkdir -p /opt/narrative-core/secret
RUN mkdir -p /opt/narrative-core/dataFiles

# install exiftool and others
RUN apt-get update && \
    apt-get install -y exiftool imagemagick default-mysql-client

# copy the root war file
COPY target/${NARRATIVE_WAR} /opt/narrative-core/${NARRATIVE_WAR}

# Create the user
RUN useradd user -m -s /bin/bash
ENV HOME /opt/narrative-core

# Set ownership of the folder
RUN chown -R user:user /opt/narrative-core

WORKDIR /opt/narrative-core

USER user

# entrypoint
EXPOSE 8080
ENTRYPOINT ["java"]
CMD ["-Djruby.native.verbose=true", "-Dorg.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH=true", "-Djava.net.preferIPv4Stack=true", "-ea", "-server", "-Xms256m", "-Xmx2048m","-jar", "${NARRATIVE_WAR}"]
