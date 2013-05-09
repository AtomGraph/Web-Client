Description
===========

Graphity Browser is a fully extensible generic Linked Data browser, built as a sample application on [Graphity Linked Data Platform](https://github.com/Graphity/graphity-ldp).
It can be used for exploration and browsing of remote datasources, publishing and analysis of open data, as well as import and integration of private user data.

Building a data-intensive Web application on Graphity Browser is as simple as overriding generic stylesheets with own layout and defining necessary queries.

Linked Data
===========

Linked Data is a data integration technology based on the RDF data model. It is the first solution that can solve
the information silo problems on a global scale and enable distributed generic Web applications.

Read more about Linked Data: [Reinventing Web applications](https://github.com/Graphity/graphity-browser/wiki/Reinventing-Web-applications).

What it does
============

Graphity LDP is a fully extensible generic Linked Data platform for building Web applications.
It can be used for publishing and analysis of open data, as well as import and integration of private user data.

The platform supports standard RDF access methods such as Linked Data and SPARQL endpoints, and includes plugin mechanisms for importing file formats and APIs as RDF.

* interfaces and base classes for rapid building of Linked Data webapps
* high- and low-level access to remote Linked Data resources and SPARQL endpoints
* providers for input and output of RDF data, either raw or via XSLT transformations
* behind-the-scenes access of non-Linked Data resources via GRDDL
* mapping and resolution of URIs to known schemas/ontologies
* HTTP caching & authentication
* easy XSLT transformation and SPARQL query building

* user interface built from schema/ontology metadata (most popular ones are included)
* embedded multimedia (currently images only)
* user input via RDF/POST-encoded HTML forms
* SPARQL endpoint with interactive results
* HTTP caching
* switching interface language at any point
* multiple rendering modes (currently item/list/table)
* pagination in container (list/table) mode
* serialization as XHTML, RDF/XML, and Turtle

How it works
============

Graphity Client can be used a standalone browser for exploring Linked Data sources and SPARQL triplestores.
However, its strength lies in its _extreme_ extensibility. 3rd party Linked Data webapps can be rapidly built
by including, extending, or overriding the relevant classes and XSLT stylesheets from the Client.

Templates huh
POWDER
URI address templates map to SPARQL query templates.

URI

`/{container}` redirects to `/{container}?limit={limit: [0-9]+}&offset={offset: [0-9]+}`

Rules
Self-serving if no SPARQL endpoint is provided

Demonstration
=============

![Tim Berners-Lee FOAF profile](http://cloud.github.com/downloads/Graphity/graphity-browser/Graphity%20-%20Tim%20Berners-Lee%20%5Bhttp%20%20%20dbpedia.org%20resource%20Tim_Berners-Lee%20-095011.png)

An instance of this browser runs for demonstration purposes on [semanticreports.com](http://semanticreports.com).

_Note: the server is not production-grade._

Installation
============

Running standalone Client
--------------------------

To run Graphity Client:
* checkout the source code from this Git repository
* build it as a [Maven Web application](http://maven.apache.org/guides/mini/guide-webapp.html)
* run the webapp in an IDE or deploy the `.war` on a servlet container such as Tomcat
* open the webapp in a browser (on an address such as `http://localhost:8080/`; depends on host and/or context path)

Using Client in your [Maven Web application](http://maven.apache.org/guides/mini/guide-webapp.html)
-------------------------------------

To add Graphity Client dependency:
* checkout the source code from this Git repository
* build it as a Maven Java application
* add Graphity Client as a Maven dependency in your project using an IDE, or in the 'pom.xml' file

    <dependency>
	<groupId>org.graphity</groupId>
	<artifactId>client</artifactId>
	<version>1.0.7-SNAPSHOT</version>
    </dependency>

Extending Client
----------------

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

* [Graphity Server](https://github.com/Graphity/graphity-ldp)
* [Saxonica Saxon](http://saxon.sourceforge.net)

Maven dependencies are discovered automatically from `pom.xml`.

XSLT stylesheets
================

XHTML
-----

* [`resources/org/graphity/browser/provider/xslt`](https://github.com/Graphity/graphity-browser/tree/master/src/main/resources/org/graphity/browser/provider/xslt)
    * [`imports`](https://github.com/Graphity/graphity-browser/tree/master/src/main/resources/org/graphity/browser/provider/xslt/imports): Ontology-specific stylesheets (e.g. overriding templates for certain properties), imported by the master stylesheet
        * [`default.xsl`](https://github.com/Graphity/graphity-browser/blob/master/src/main/resources/org/graphity/browser/provider/xslt/imports/default.xsl): Default templates and functions for rendering RDF/XML subject/predicate/object nodes as XHTML elements. Design-independent.
    * [`Resource.xsl`](https://github.com/Graphity/graphity-browser/blob/master/src/main/resources/org/graphity/browser/provider/xslt/Resource.xsl): master stylesheet (includes design) for rendering RDF/XML with both single and multiple resources (lists) into XHTML


Using resources in your project
-------------------------------

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

This will copy all reusable `.xsl` files from browser's `resource` folder.

Resources
=========

* [`resources/org/graphity/browser/vocabulary/ontology.ttl`](https://github.com/Graphity/graphity-browser/blob/master/src/main/resources/org/graphity/browser/vocabulary/ontology.ttl): Application-specific ontology/sitemap. Imports general Graphity ontology. Contains metadata of JAX-RS Resources as well as SPIN query Resources used by them.

Tools
=====

Validators
----------

* [RDF/XML and Turtle validator](http://www.rdfabout.com/demo/validator/)
* [SPARQL query validator](http://sparql.org/query-validator.html)
* [SPIN query converter] (http://spinservices.org/spinrdfconverter.html)