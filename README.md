Description
===========

Graphity Browser is a fully extensible generic Linked Data platform for building end-user Web applications.
It can be used for exploration and browsing of remote datasources, publishing and analysis of open data, as well as import and integration of private user data.

Building a data-intensive Web application on Graphity Browser is as simple as overriding generic stylesheets with own layout and defining necessary queries.
The platform supports standard RDF access methods such as Linked Data and SPARQL endpoints, and includes plugin mechanisms for importing file formats and APIs as RDF.

Installation
============

Graphity Browser is a [Maven Web application](http://maven.apache.org/guides/mini/guide-webapp.html).
Maven dependencies are discovered automatically from `pom.xml`, others (such as [SPIN API](http://topbraid.org/spin/api/)) are included as `.jar` files in the `/lib` folder (and can be "installed locally" using Maven).

Structure
=========

Java classes
----

* [`org.graphity`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity): Classes shared by all Graphity applications
    * [`org.graphity.adapter`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/adapter): [`DatasetAdapter`](http://jena.apache.org/documentation/javadoc/fuseki/org/apache/jena/fuseki/http/DatasetAdapter.html)-related wrappers for Model caching via Graph store protocol
    * [`org.graphity.browser`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/browser): Classes shared by all Graphity Browser applications
        * [`org.graphity.browser.Application`](https://github.com/Graphity/graphity-browser/blob/master/src/main/java/org/graphity/browser/Application.java): Subclass to get an entry point to the webapp. [JAX-RS](http://docs.oracle.com/javaee/6/tutorial/doc/giepu.html) Resources, [`Provider`s](http://jackson.codehaus.org/javadoc/jax-rs/1.0/javax/ws/rs/ext/Providers.html), and configuration is initialized here
        * [`org.graphity.browser.Resource`](https://github.com/Graphity/graphity-browser/blob/master/src/main/java/org/graphity/browser/Resource.java): Base class for all Browser Resources and apps built on it. Subclass of ``LinkedDataResourceImpl``.
        * [`org.graphity.browser.locator`](https://github.com/Graphity/graphity-browser/blob/master/src/main/java/org/graphity/browser/locator): Pluggable classes for [GRDDL](http://www.w3.org/TR/grddl/) import of 3rd party REST APIs and XML formats. Implement Jena's [`Locator`](http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/util/Locator.html) interface. Need to be added to `DataManager` to take effect.
        * [`org.graphity.browser.provider`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/browser/provider): Browser-specific `Provider` subclasses
            * [`org.graphity.browser.provider.xslt`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/browser/provider/xslt): Browser-specific subclasses for writing [`Response`](http://jackson.codehaus.org/javadoc/jax-rs/1.0/javax/ws/rs/core/Response.html). XSLT stylesheets located [`here`](https://github.com/Graphity/graphity-browser/tree/master/src/main/resources/org/graphity/browser/provider/xslt) and its subfolders. They translate request parameters into XSLT parameters.
        * [`org.graphity.browser.resource`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/browser/resource): Custom JAX-RS Resources
            * [`org.graphity.browser.resource.SPARQLResource`](https://github.com/Graphity/graphity-browser/blob/master/src/main/java/org/graphity/browser/resource/SPARQLResource.java): SPARQL endpoint
            * [`org.graphity.browser.resource.SearchResource`](https://github.com/Graphity/graphity-browser/blob/master/src/main/java/org/graphity/browser/resource/SearchResource.java): Search/autocomplete resource (_unfinished_)
    * [`org.graphity.model`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/model): Graphity model interfaces
        * [`org.graphity.model.LinkedDataResource`](https://github.com/Graphity/graphity-browser/blob/master/src/main/java/org/graphity/model/LinkedDataResource.java): Prototypical RDF Resource interface
        * [`org.graphity.model.impl`](https://github.com/Graphity/graphity-browser/blob/master/src/main/java/org/graphity/model/impl): Implementations of Graphity model interfaces
            * [`org.graphity.model.impl.LinkedDataResourceImpl`](https://github.com/Graphity/graphity-browser/blob/master/src/main/java/org/graphity/model/impl/LinkedDataResourceImpl.java): Base class implementation of `LinkedDataResource`
    * [`org.graphity.provider`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/provider): Generic `Provider` classes for reading request/writing `Response`
        * [`org.graphity.provider.ModelProvider`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/provider/ModelProvider.java): Reads `Model` from request body/writes `Model` to `Response` body
        * [`org.graphity.provider.RDFPostReader`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/provider/RDFPostReader.java): Reads `Model` from [RDF/POST](http://www.lsrn.org/semweb/rdfpost.html) requests
        * [`org.graphity.provider.ResultSetWriter`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/provider/ResultSetWriter.java): Writes [`ResultSet`](http://jena.apache.org/documentation/javadoc/arq/com/hp/hpl/jena/query/ResultSet.html) with SPARQL results into `Response`
        * [`org.graphity.provider.xslt`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/provider/xslt): Abstract base classes for XSLT transformation-based `Response` writers
    * [`org.graphity.util`](https://github.com/Graphity/graphity-browser/blob/master/src/main/java/org/graphity/util): Utility classes
        * [`org.graphity.util.QueryBuilder`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/util/QueryBuilder.java): Builds Jena [`Query`](http://jena.apache.org/documentation/javadoc/arq/com/hp/hpl/jena/query/Query.html) or [SPIN](http://spinrdf.org/spin.html) [`Query`](www.topquadrant.com/topbraid/spin/api/javadoc/org/topbraid/spin/model/class-use/Query.html) from components (e.g. `LIMIT`/`OFFSET` parameters; RDF resources specifying `OPTIONAL` or a subquery)
        * [`org.graphity.util.XSLTBuilder`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/util/XSLTBuilder.java): Builds XSLT transformation out of components. Chaining is possible.
        * [`org.graphity.util.locator`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/util/locator): Pluggable classes for retrieving RDF from URIs. Implement Jena's [`Locator`](http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/util/Locator.html) interface.
            * [`org.graphity.util.locator.LocatorGRDDL`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/util/locator/LocatorGRDDL.java): Generic base class for GRDDL XSLT transformation-based `Locator`s. Also see stylesheets [`here`](https://github.com/Graphity/graphity-browser/tree/master/src/main/resources/org/graphity/util/locator/grddl).
            * [`org.graphity.util.locator.LocatorLinkedData`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/util/locator/LocatorLinkedData.java): General-purpose class for loading RDF from Linked Data URIs using content negotiation
            * [`org.graphity.util.locator.LocatorLinkedDataOAuth2`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/util/locator/LocatorLinkedDataOAuth2.java): General-purpose class for loading RDF from Linked Data URIs using content negotiation and [OAuth2](http://oauth.net/2/) authentication (_unfinished_)
            * [`org.graphity.util.locator.PrefixMapper`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/util/locator/PrefixMapper.java): Subclass of [`LocationMapper`](http://jena.apache.org/documentation/javadoc/jena/com/hp/hpl/jena/util/LocationMapper.html) for mapping resource (class, property etc.) URIs into local copies of known ontologies. Also see [`resources/location-mapping.ttl`](https://github.com/Graphity/graphity-browser/blob/master/src/main/resources/location-mapping.ttl); ontologies are cached [`here`](https://github.com/Graphity/graphity-browser/tree/master/src/main/resources/org/graphity/browser/vocabulary).
        * [`org.graphity.util.manager`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/util/manager): RDF data management classes
            * [`org.graphity.util.manager.DataManager`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/util/manager/DataManager.java): Subclass of Jena's [`FileManager`](http://jena.sourceforge.net/how-to/filemanager.html) for loading `Model`s and `ResultSet`s from the Web. All code making requests for RDF data or SPARQL endpoints should use this class. Implements [`URIResolver`](http://docs.oracle.com/javase/6/docs/api/javax/xml/transform/URIResolver.html) and resolves URIs when `document()` function is called in XSLT.
            * [`org.graphity.util.manager.SPARQLService`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/util/manager/SPARQLService.java): Represent individual SPARQL endpoints, should only be used in case authentication or other custom features are needed. Need to be added to `DataManager` to take effect.
        * [`org.graphity.util.oauth`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/util/oauth): Classes related to JAX-RS implementation of OAuth
    * [`org.graphity.vocabulary`](https://github.com/Graphity/graphity-browser/tree/master/src/main/java/org/graphity/vocabulary): Graphity ontologies as classes with Jena `Resource`s

XSLT stylesheets
----------------

* [`resources/org/graphity`](https://github.com/Graphity/graphity-browser/tree/master/src/main/resources/org/graphity)
    * [`browser`](https://github.com/Graphity/graphity-browser/tree/master/src/main/resources/org/graphity/browser)
        * [`provider`](https://github.com/Graphity/graphity-browser/tree/master/src/main/resources/org/graphity/browser/provider)
            * [`xslt`](https://github.com/Graphity/graphity-browser/tree/master/src/main/resources/org/graphity/browser/provider/xslt)
                * [`imports`](https://github.com/Graphity/graphity-browser/tree/master/src/main/resources/org/graphity/browser/provider/xslt/imports): Ontology-specific stylesheets (e.g. overriding templates for certain properties), imported by the master stylesheet
                    * [`default.xsl`](https://github.com/Graphity/graphity-browser/blob/master/src/main/resources/org/graphity/browser/provider/xslt/imports/default.xsl): Default templates and functions for rendering RDF/XML subject/predicate/object nodes as XHTML elements. Design-independent.
                * [`Resource.xsl`](https://github.com/Graphity/graphity-browser/blob/master/src/main/resources/org/graphity/browser/provider/xslt/Resource.xsl): master stylesheet (includes design) for rendering RDF/XML with both single and multiple resources (lists) into XHTML
                * [`rdfxml2google-wire.xsl`](https://github.com/Graphity/graphity-browser/blob/master/src/main/resources/org/graphity/browser/provider/xslt/rdfxml2google-wire.xsl): Generic conversion from RDF/XML to Google [`DataTable`](https://developers.google.com/chart/interactive/docs/reference#DataTable)
                * [`sparql2google-wire.xsl`](https://github.com/Graphity/graphity-browser/blob/master/src/main/resources/org/graphity/browser/provider/xslt/sparql2google-wire.xsl): Generic conversion from SPARQL XML results to Google `DataTable`
    * [`util`](https://github.com/Graphity/graphity-browser/tree/master/src/main/resources/org/graphity/util)
        * [`locator`](https://github.com/Graphity/graphity-browser/tree/master/src/main/resources/org/graphity/util/locator)
            * [`grddl`](https://github.com/Graphity/graphity-browser/tree/master/src/main/resources/org/graphity/util/locator/grddl): XSLT stylesheets for use with `LocatorGRDDL` and its subclasses

Ontologies
---------------------

* [`resources/org/graphity/browser/vocabulary`](https://github.com/Graphity/graphity-browser/tree/master/src/main/resources/org/graphity/browser/vocabulary): Contains cached local copies of popular ontologies
    * [`graphity.ttl`](https://github.com/Graphity/graphity-browser/tree/master/src/main/resources/org/graphity/browser/vocabulary/graphity.ttl) : Ontology reused by all Graphity applications
* [`WEB-INF/ontology.ttl`](https://github.com/Graphity/graphity-browser/blob/master/src/main/webapp/WEB-INF/ontology.ttl): Application-specific ontology, imports general Graphity ontology. Contains metadata of JAX-RS Resources as well as SPIN query Resources used by them.

Tools
=====

Validators
----------

* [RDF/XML and Turtle validator](http://www.rdfabout.com/demo/validator/)
* [SPARQL query validator](http://sparql.org/query-validator.html)
* [SPIN query converter] (http://spinservices.org/spinrdfconverter.html)