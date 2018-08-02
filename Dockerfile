FROM maven:3.5.4-jdk-8 as maven

RUN mkdir -p /usr/src/app

WORKDIR /usr/src/app

COPY . /usr/src/app

RUN mvn -Pstandalone clean install


FROM tomcat:8.0.52-jre8

ARG VERSION=client-1.1.4-SNAPSHOT

WORKDIR /usr/local/tomcat/webapps/

RUN rm -rf * # remove Tomcat's default webapps

# copy exploded WAR folder from the maven stage
COPY --from=maven /usr/src/app/target/$VERSION/ ROOT/

CMD ["catalina.sh", "run"]