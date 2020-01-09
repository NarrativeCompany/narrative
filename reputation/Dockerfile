FROM openjdk:8-jre-stretch

ENV NARRATIVE_REP ${NARRATIVE_REP}

# make narrative-reputation directory
RUN mkdir -p /opt/narrative-reputation/config
RUN mkdir -p /opt/narrative-reputation/secret

# copy the root war file
COPY target/${NARRATIVE_REP} /opt/narrative-reputation/${NARRATIVE_REP}

# Create the user
RUN useradd user -m -s /bin/bash
ENV HOME /opt/narrative-reputation

# Set ownership of the folder
RUN chown -R user:user /opt/narrative-reputation

WORKDIR /opt/narrative-reputation

USER user

# entrypoint
EXPOSE 8080
ENTRYPOINT ["java"]
CMD ["-jar", "${NARRATIVE_REP}"]
