FROM ubuntu

RUN apt-get update
RUN apt-get -y install default-jre
RUN apt-get -y install default-jdk
RUN apt-get -y install unzip
RUN apt-get -y install wget

RUN wget https://services.gradle.org/distributions/gradle-6.0.1-all.zip
RUN mkdir -p /opt/gradle
RUN unzip -d /opt/gradle gradle-6.0.1-all.zip
ENV PATH=${PATH}:/opt/gradle/gradle-6.0.1/bin


COPY . /opt/source-code

WORKDIR /opt/source-code
RUN gradle build

CMD gradle run --args='ups_ip 1000 vcm-14419.vm.duke.edu 23456 0.0.0.0 1000 NO'

