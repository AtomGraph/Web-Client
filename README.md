Graphity Client is a Java framework for building read-write Linked Data applications. If you have a triplestore with RDF
data that you want to publish and/or build an end-user application on it, or would like to explore Linked Open
Data, Graphity provides the components you need. It is stable yet actively maintained software, conveniently available on
Maven as a single dependency for your project.

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
* [configuring Graphity](../../wiki/Configuration)
* [JavaDoc](http://graphity.github.io/graphity-client/apidocs)

For full documentation, see the [wiki index](../../wiki).

Maven
-----

Graphity artifacts [`graphity-client`](http://search.maven.org/#browse%7C-605419744), [`graphity-processor`](http://search.maven.org/#browse%7C2124019457)
and [`graphity-core`](http://search.maven.org/#browse%7C57568460) are released on Maven under the
[`org.graphity`](http://search.maven.org/#browse%7C1400901156) group ID.

You should choose Graphity Client as it includes both XSLT and Linked Data functionality, making it useful for end-user as well as server applications.
Dependencies to other Graphity artifacts will be resolved automagically during the Maven build processs. GC is released as WAR by default, but the JAR with
classes is attached, and you can address it as a separate artifact:

        <dependency>
            <groupId>org.graphity</groupId>
            <artifactId>client</artifactId>
            <version>1.1.3-SNAPSHOT</version>
            <classifier>classes</classifier>
        </dependency>
        <dependency>
            <groupId>org.graphity</groupId>
            <artifactId>client</artifactId>
            <version>1.1.3-SNAPSHOT</version>
            <type>war</type>
        </dependency>


No permanent storage!
---------------------

Graphity Client does *not* include permanent RDF storage. By default it is configured to read the dataset from a file, therefore creating/updating data will have no effect.

In order to store data permanently, you need to set up a [triplestore](http://en.wikipedia.org/wiki/Triplestore) and configure the webapp with its SPARQL endpoint.
For open-source, we recommend trying Jena's [TDB](http://jena.apache.org/documentation/tdb/); for commercial, see [Dydra](http://dydra.com).

Demonstration
=============

![Graphity screenshot](https://raw.github.com/Graphity/graphity-client/master/screenshot.jpg)

An instance of Graphity Client runs for demonstration purposes on [Linked Data Hub](http://linkeddatahub.com).
See the DBPedia Linked Data description of Sir [Tim Berners-Lee](http://linkeddatahub.com/?uri=http%3A%2F%2Fdbpedia.org%2Fresource%2FTim_Berners-Lee).

_Note: the server is not production-grade and DBPedia is often unstable._

Support
=======

Please [report issues](../../issues) if you've encountered a bug or have a feature request.

Commercial Graphity consulting, development, and support are available from [GraphityHQ](http://graphityhq.com).

Community
=========

Please join the W3C [Declarative Linked Data Apps Community Group](http://www.w3.org/community/declarative-apps/) to discuss
and develop Graphity and declarative Linked Data architecture in general.