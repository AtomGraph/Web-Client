Description
===========

Consider this setup: you have a triplestore with RDF data, and you want to publish it as read-write Linked Data
on your hostname. You also want to be able to intuitively browse through it, and build user-friendly Web
applications on it.

Graphity is a Linked Data platform that allows you to do all that and more. Its native support and direct use of
standard RDF, SPARQL and XSLT technologies enable extremely extensible and flexible design and lead the way
towards declarative Web development.
The sitemap structure is defined in an ontology using URI templates, the content of the page (its resource
description) is defined using query templates, while the user interface is the result of a transformation on the
content data.

What is Linked Data
===================

Linked Data is a data integration technology based on the RDF data model. It is the first solution that can solve
the information silo problems on a global scale and enable distributed generic Web applications.

RDF data model is graph-shaped and has multiple syntaxes: both XML-based (RDF/XML) and plain-text (Turtle).
[SPARQL 1.1](http://www.w3.org/TR/sparql11-query/) is the latest specification of the RDF query language,
supported by most RDF databases, called triplestores.

Read more about how Linked Data solves data integration: [Reinventing Web applications](../../wiki/Reinventing-Web-applications).

What Graphity does
==================

Graphity is a fully extensible generic Linked Data client and platform. It can be used for exploration and
browsing of remote datasources, publishing and analysis of open data, as well as import and integration of
private user data. Building a data-intensive Web application on Graphity is as simple as overriding generic
stylesheets with own layout and defining URI-query template mappings.

What Graphity provides for users as out-of-the-box generic features:
* default Linked Data user interface built with Twitter Bootstrap
* multiple rendering modes (currently item/list/table)
* control of the published data by defining templates
* pagination on container resources
* SPARQL endpoint with interactive results
* loading RDF data from remote Linked Data sources
* HTTP caching

What Graphity can be quickly extended to do:
* store RDF data directly from HTML forms into the triplestore
* search by dynamically adding filters to the query
* faceted browsing by dynamically binding variable values in the query
* SPARQL result visualizations using different JavaScript APIs
* ordering tables by property columns. Columns need to be mapped to query variables.

What Graphity provides for developers:
* open-source code base built on established frameworks such as Jena, Jersey, and Saxon
* support of established vocabularies such as FOAF and SIOC
* fine-grained XLST templates for different Bootstrap layout components
* builder classes for XSLT transformation and SPARQL queries
* HATEOS support. Pagination is implemented using hypermedia links.
* high- and low-level access to remote Linked Data resources and SPARQL endpoints
* input/output providers for RDF data (raw, via RDF/POST and/or XSLT transformations)
* behind-the-scenes access of non-Linked Data resources
* HTTP authentication support

How it works
============

The platform supports standard RDF access methods such as Linked Data and SPARQL endpoints, and includes plugin
mechanisms for importing file formats and APIs as RDF. Building a data-oriented Web application on Graphity is
as simple as overriding XSLT templates to change the layout, entering URI templates to change the sitemap and
attach SPARQL queries to retrieve the right data. Relevant Client classes and templates can be included and
extended.

URIs in RDF need to be relative to the base URI of the Web application.
RDF hash as `Entity-Tag` header value

Templates - open-source, incremental, reusable
POWDER
Bootstrap
Jena
SPIN
URI templates and attaching SPIN SPARQL templates to them

GRDDL
XSLT combines RDF content with metadata from schemas and ontologies.
Saxon

URI address templates map to SPARQL query templates.

URI

`/{container}` redirects to `/{container}?limit={limit: [0-9]+}&offset={offset: [0-9]+}`

Rules
Self-serving if no SPARQL endpoint is provided
Redirect to first page of container

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