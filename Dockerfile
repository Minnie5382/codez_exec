FROM debian:buster
VOLUME /codeZ_exec
WORKDIR /codeZ_exec
COPY ./execvol/Server.java .
COPY ./execvol/Main.java .
RUN apt-get update && apt-get install -y openjdk-11-jdk
ENTRYPOINT ["java", "/codeZ_exec/Server.java"]
