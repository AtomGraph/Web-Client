AtomGraph Web-Client is a Java client for AtomGraph Processor Linked Data applications. If you have a triplestore with RDF
data that you want to publish and/or build an end-user application on it, or would like to explore Linked Open
Data, Client provides the components you need. It is stable yet actively maintained software, conveniently available on
Maven as a single dependency for your project.

What AWC provides for users as out-of-the-box generic features:
* declarative control of published data using URI and SPARQL templates
* multilingual, responsive user interface built with Twitter Bootstrap
* multiple rendering modes (currently item/list/table)
* pagination on container resources
* SPARQL endpoint with interactive results
* loading RDF data from remote Linked Data sources
* HTTP content negotiation and caching

What AWC can be quickly extended to do:
* render custom layouts/designs by overriding XSLT templates
* store RDF data directly from HTML forms into the triplestore
* control RDF input quality with SPARQL-based constraints
* search by dynamically adding filters to the query
* faceted browsing by dynamically binding variable values in the query
* SPARQL result visualizations using different JavaScript APIs
* ordering pages by property columns

AtomGraph's direct use of semantic technologies results in extemely extensible and flexible design and leads the
way towards declarative Web development. You can forget all about broken hyperlinks and concentrate on building
great apps on quality data.

Getting started
===============

* [what is Linked Data](../../wiki/What-is-Linked-Data)
* [installing Web-Client](../../wiki/Installation)
* [extending Web-Client](../../wiki/Extending-Web-Client)
* [configuring Web-Client](../../wiki/Configuration)

For full documentation, see the [wiki index](../../wiki).

Usage
=====

Docker
------

    docker run -p 8081:8080 atomgraph/web-client

Maven
-----

Web-Client will be released on Maven central when it reaches the 2.1 version.

Support
=======

Please [report issues](../../issues) if you've encountered a bug or have a feature request.

Commercial AtomGraph consulting, development, and support are available from [AtomGraph](https://atomgraph.com).

Community
=========

Please join the W3C [Declarative Linked Data Apps Community Group](http://www.w3.org/community/declarative-apps/) to discuss
and develop AtomGraph and declarative Linked Data architecture in general.
