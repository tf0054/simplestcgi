FROM centos:centos6

MAINTAINER tf0054 <tf0054@gmail.com>

RUN yum -y update
RUN yum -y install git java-1.7.0-openjdk
RUN git clone https://github.com/tf0054/simplestweb.git
ADD https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein /opt/lein
RUN chmod a+x /opt/lein
ADD https://get.docker.com/builds/Linux/x86_64/docker-1.3.3 /usr/bin/docker
RUN chmod a+x /usr/bin/docker

WORKDIR /simplestcgi
RUN /opt/lein deps

EXPOSE 80

ENTRYPOINT ["/opt/lein","trampoline","run"]
