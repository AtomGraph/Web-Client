Graphity is a Java framework for building read-write Linked Data applications. If you have a triplestore with RDF
data that you want to publish and/or build an end-user application on it, or would like to explore Linked Open
Data, Graphity provides the components you need.

What Graphity provides for users as out-of-the-box generic features:
* declarative control of published data using URI and SPARQL templates
* multilingual, responsive user interface built with Twitter Bootstrap
* multiple rendering modes (currently item/list/table)
* pagination on container resources
* SPARQL endpoint with interactive results
* loading RDF data from remote Linked Data sources
* HTTP content negotiation and caching

What Graphity can be quickly extended to do:
* render custom layouts/designs by overriding XSLT templates
* store RDF data directly from HTML forms into the triplestore
* control RDF input quality with SPARQL-based constraints
* search by dynamically adding filters to the query
* faceted browsing by dynamically binding variable values in the query
* SPARQL result visualizations using different JavaScript APIs
* ordering pages by property columns

Graphity's direct use of semantic technologies results in extemely extensible and flexible design and leads the
way towards declarative Web development. You can forget all about broken hyperlinks and concentrate on building
great apps on quality data.

Getting started
===============

* what is Linked Data
* how Graphity works
* getting started with Graphity
* exposing SPARQL endpoint as Linked Data
* building a Web application

What is Linked Data
===================

Linked Data is a data integration technology based on the RDF data model. It is the first solution that can solve
the information silo problems on a global scale and enable distributed generic Web applications. Linked Open Data
(LOD) is Linked Data published under an open license.

RDF is a graph-shaped data model that identifies resources (nodes) with URIs. It has multiple syntaxes: both
XML-based (RDF/XML) and plain-text (Turtle). [SPARQL 1.1](http://www.w3.org/TR/sparql11-query/) is the latest
specification of the RDF query language, supported by most RDF databases, called triplestores.

Read more about how Linked Data solves data integration: [Reinventing Web applications](../../wiki/Reinventing-Web-applications).

How Graphity works
==================

Linked Data processor. Architecture independent of programming platform.

The platform supports standard RDF access methods such as Linked Data and SPARQL endpoints, and includes plugin
mechanisms for importing file formats and APIs as RDF. Building a data-oriented Web application on Graphity is
as simple as overriding XSLT templates to change the layout, entering URI templates to change the sitemap and
attach SPARQL queries to retrieve the right data. Relevant Client classes and templates can be included and
extended.

What Graphity provides for developers:
* open-source code base built on established frameworks such as Jena, Jersey, and Saxon
* support of established vocabularies such as FOAF and SIOC
* fine-grained XLST templates for different Bootstrap layout components
* builder classes for XSLT transformation and SPARQL queries
* high- and low-level access to remote Linked Data resources and SPARQL endpoints
* input/output providers for RDF data (raw, via RDF/POST and/or XSLT transformations)
* behind-the-scenes access of non-Linked Data resources
* HTTP authentication support

RDF hash as `Entity-Tag` header value

Templates - open-source, incremental, reusable, collaborative! community
POWDER
Bootstrap
Jena
SPIN
URI templates and attaching SPIN SPARQL templates to them

GRDDL
XSLT combines RDF content with metadata from schemas and ontologies.
Saxon

URI address templates map to SPARQL query templates.

Rules
URIs in RDF need to be relative to the base URI of the Web application.
Self-serving if no SPARQL endpoint is provided
Redirect to first page of container
`/{container}` redirects to `/{container}?limit={limit: [0-9]+}&offset={offset: [0-9]+}`
HATEOS support. Pagination is implemented using hypermedia links.


Creating a new resource class?
Javadoc
Grapity Client is licensed as GPL, Graphity Server as Apache.

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

Used libraries
--------------

* [Graphity Server](https://github.com/Graphity/graphity-ldp)
* [Saxonica Saxon](http://saxon.sourceforge.net)

Maven dependencies are discovered automatically from `pom.xml`.

Tools
=====

Validators
----------

* [RDF/XML and Turtle validator](http://www.rdfabout.com/demo/validator/)
* [SPARQL query validator](http://sparql.org/query-validator.html)
* [SPIN query converter] (http://spinservices.org/spinrdfconverter.html)