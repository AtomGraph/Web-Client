FROM maven:3.5.3-jdk-8 as maven

### Clone and build AtomGraph Core (2.0.1-SNAPSHOT is not on Maven central)

WORKDIR /usr/src

RUN git clone https://github.com/AtomGraph/Core.git

WORKDIR /usr/src/Core

RUN mvn clean install

### Build AtomGraph Web-Client

WORKDIR /usr/src/Web-Client

COPY . /usr/src/Web-Client

RUN mvn -Pstandalone clean install

### Deploy Processor webapp on Tomcat

FROM tomcat:8.0.52-jre8

ARG VERSION=client-1.1.4-SNAPSHOT

WORKDIR /usr/local/tomcat/webapps/

RUN rm -rf * # remove Tomcat's default webapps

# copy exploded WAR folder from the maven stage
COPY --from=maven /usr/src/Web-Client/target/$VERSION/ ROOT/

WORKDIR $CATALINA_HOME

COPY src/main/webapp/META-INF/context.xml conf/Catalina/localhost/ROOT.xml

### Install XSLT processor

RUN apt-get update && \
  apt-get -y install xsltproc

### Copy entrypoint

COPY entrypoint.sh entrypoint.sh

RUN chmod +x entrypoint.sh

COPY context.xsl conf/Catalina/localhost/context.xsl

ENTRYPOINT ["/usr/local/tomcat/entrypoint.sh"]

EXPOSE 8080