FROM hseeberger/scala-sbt
MAINTAINER Vy-Shane Xie <shane@node.mu>
ENV REFRESHED_AT 2015-07-02

ADD . /root
WORKDIR /root

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "target/scala-2.11/akka-http-hello-world.jar"]
