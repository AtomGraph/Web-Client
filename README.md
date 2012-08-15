Description
===========

Graphity Browser is a fully extensible generic Linked Data browser, built as a sample application on [Graphity Linked Data Platform](https://github.com/Graphity/graphity-ldp).
It can be used for exploration and browsing of remote datasources, publishing and analysis of open data, as well as import and integration of private user data.

Building a data-intensive Web application on Graphity Browser is as simple as overriding generic stylesheets with own layout and defining necessary queries.

Screenshot
----------

![Tim Berners-Lee FOAF profile](http://cloud.github.com/downloads/Graphity/graphity-browser/Graphity%20-%20Tim%20Berners-Lee%20%5Bhttp%20%20%20dbpedia.org%20resource%20Tim_Berners-Lee%20-095011.png)

Installation
============

Graphity Browser is a [Maven Web application](http://maven.apache.org/guides/mini/guide-webapp.html).

You have the following options to install Graphity Browser:
* checkout the source code from the Git repository and build it as a Maven webapp
* [download](https://github.com/Graphity/graphity-browser/downloads) the project as a standalone `.war` webapp and deploy it in a servlet container (such as Tomcat)
* [download](https://github.com/Graphity/graphity-browser/downloads) the project as a `.jar` library and include it in your Java project (Maven repository is not available yet)

Java code
=========

Browser LDP application
--------------------------

This generic Linked Data browser is an example of a webapp that can be built on Graphity LDP.
3rd party Linked Data webapps can be rapidly built by including, extending, or overriding the relevant classes and XSLT stylesheets from this package.

* [`org.graphity.browser`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/browser): Classes shared by all Graphity Browser applications
    * [`org.graphity.browser.Application`](https://github.com/Graphity/graphity-browser/blob/master/src/main/java/org/graphity/browser/Application.java): Subclass to get an entry point to the webapp. [JAX-RS](http://docs.oracle.com/javaee/6/tutorial/doc/giepu.html) Resources, [`Provider`s](http://jackson.codehaus.org/javadoc/jax-rs/1.0/javax/ws/rs/ext/Providers.html), and configuration is initialized here
    * [`org.graphity.browser.Resource`](https://github.com/Graphity/graphity-browser/blob/master/src/main/java/org/graphity/browser/Resource.java): Base class for all Browser Resources and apps built on it.
    * [`org.graphity.browser.provider`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/browser/provider): Browser-specific `Provider` subclasses
        * [`org.graphity.browser.provider.xslt`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/browser/provider/xslt): Browser-specific subclasses for writing [`Response`](http://jackson.codehaus.org/javadoc/jax-rs/1.0/javax/ws/rs/core/Response.html). XSLT stylesheets located [`here`](https://github.com/Graphity/graphity-browser/tree/master/src/main/resources/org/graphity/browser/provider/xslt) and its subfolders. They translate request parameters into XSLT parameters.
    * [`org.graphity.browser.resource`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/browser/resource): Custom JAX-RS Resources
        * [`org.graphity.browser.resource.SPARQLEndpoint`](https://github.com/Graphity/graphity-browser/blob/master/src/main/java/org/graphity/browser/resource/SPARQLEndpoint.java): SPARQL endpoint

Used libraries
--------------

* [Graphity LDP](https://github.com/Graphity/graphity-ldp)
* [Saxonica Saxon](http://saxon.sourceforge.net)

Maven dependencies are discovered automatically from `pom.xml`.

XSLT stylesheets
================

XHTML
-----

* [`resources/org/graphity/browser/provider/xslt`](https://github.com/Graphity/graphity-browser/tree/master/src/main/resources/org/graphity/browser/provider/xslt)
    * [`imports`](https://github.com/Graphity/graphity-browser/tree/master/src/main/resources/org/graphity/browser/provider/xslt/imports): Ontology-specific stylesheets (e.g. overriding templates for certain properties), imported by the master stylesheet
        * [`default.xsl`](https://github.com/Graphity/graphity-browser/blob/master/src/main/resources/org/graphity/browser/provider/xslt/imports/default.xsl): Default templates and functions for rendering RDF/XML subject/predicate/object nodes as XHTML elements. Design-independent.
    * [`rdfxml2google-wire.xsl`](https://github.com/Graphity/graphity-browser/blob/master/src/main/resources/org/graphity/browser/provider/xslt/rdfxml2google-wire.xsl): Generic conversion from RDF/XML to Google [`DataTable`](https://developers.google.com/chart/interactive/docs/reference#DataTable)
    * [`sparql2google-wire.xsl`](https://github.com/Graphity/graphity-browser/blob/master/src/main/resources/org/graphity/browser/provider/xslt/sparql2google-wire.xsl): Generic conversion from SPARQL XML results to Google `DataTable`
* [`webapp/WEB-INF/Resource.xsl`](https://github.com/Graphity/graphity-browser/blob/master/src/main/resources/org/graphity/browser/provider/xslt/Resource.xsl): master stylesheet (includes design) for rendering RDF/XML with both single and multiple resources (lists) into XHTML


Usage in your project
---------------------

In order to include the above stylesheets into your own Maven project, you can add the following execution for `maven-dependency-plugin` to your `pom.xml`:

    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-dependency-plugin</artifactId>
      <version>2.1</version>
      <executions>
        <execution>
          <id>resource-dependencies</id>
          <phase>generate-resources</phase>
          <goals>
            <goal>unpack-dependencies</goal>
          </goals>
          <configuration>
            <includeArtifactIds>browser</includeArtifactIds>
            <includeGroupIds>org.graphity</includeGroupIds>
            <includes>**\/*.xsl</includes>
            <outputDirectory>${project.build.directory}/classes</outputDirectory>
          </configuration>
        </execution>
      </executions>
    </plugin>

This will copy all reusable `.xsl` files from Graphity's `resource` folder.

Ontologies
==========

* [`resources/org/graphity/browser/vocabulary`](https://github.com/Graphity/graphity-browser/tree/master/src/main/resources/org/graphity/browser/vocabulary): Contains cached local copies of popular ontologies
    * [`graphity.ttl`](https://github.com/Graphity/graphity-browser/tree/master/src/main/resources/org/graphity/browser/vocabulary/graphity.ttl) : Ontology reused by all Graphity applications
* [`webapp/WEB-INF/ontology.ttl`](https://github.com/Graphity/graphity-browser/blob/master/src/main/webapp/WEB-INF/ontology.ttl): Application-specific ontology, imports general Graphity ontology. Contains metadata of JAX-RS Resources as well as SPIN query Resources used by them.

Tools
=====

Validators
----------

* [RDF/XML and Turtle validator](http://www.rdfabout.com/demo/validator/)
* [SPARQL query validator](http://sparql.org/query-validator.html)
* [SPIN query converter] (http://spinservices.org/spinrdfconverter.html)