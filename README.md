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

* [what is Linked Data](../../wiki/What-is-Linked-Data)
* [how Graphity works](../../wiki/How-Graphity-works)
* [installing Graphity](../../wiki/Installation)
* [extending Graphity](../../wiki/Extending-Graphity)
* [configuring Graphity](../../wiki/Cofiguration)
* [JavaDoc](http://graphity.github.io/graphity-client/apidocs)

For full documentation, see the [wiki index](../../wiki).

Demonstration
=============

![Graphity screenshot](https://raw.github.com/Graphity/graphity-client/master/screenshot.jpg)

An instance of Graphity Client runs for demonstration purposes on [semanticreports.com](http://semanticreports.com).
See the DBPedia Linked Data description of Sir [Tim Berners-Lee](http://semanticreports.com/?uri=http%3A%2F%2Fdbpedia.org%2Fresource%2FTim_Berners-Lee).

_Note: the server is not production-grade and DBPedia is often unstable._

Support
=======

Please [report issues](../../issues) if you've encountered a bug or have a feature request.

Commercial Graphity consulting, development, and support are available from [GraphityHQ](http://graphityhq.com).

Community
=========

Please join the W3C [Declarative Linked Data Apps Community Group](http://www.w3.org/community/declarative-apps/) to discuss
and develop Graphity and declarative Linked Data architecture in general.