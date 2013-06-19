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
* getting started with Graphity
* exposing SPARQL endpoint as Linked Data
* building a Web application
* JavaDoc

Demonstration
=============

An instance of this browser runs for demonstration purposes on [semanticreports.com](http://semanticreports.com).

_Note: the server is not production-grade._

Installation
============

Running standalone Client
--------------------------

To run Graphity Client:
* checkout the source code from this Git repository
* checkout the source code of [Graphity Server](https://github.com/Graphity/graphity-ldp/) dependency (it is not available on Maven yet)
* build Client as a [Maven Web application](http://maven.apache.org/guides/mini/guide-webapp.html)
* run the webapp in an IDE or deploy the `.war` on a servlet container such as Tomcat
* open the webapp in a browser (on an address such as `http://localhost:8080/`; depends on host and/or context path)

Using Client in your [Maven Web application](http://maven.apache.org/guides/mini/guide-webapp.html)
-------------------------------------

To add Graphity Client dependency:
* checkout the source code from this Git repository
* checkout the source code of [Graphity Server](https://github.com/Graphity/graphity-ldp/) dependency (it is not available on Maven yet)
* build it as a Maven Java application. Change POM to build a `.jar`:

        <packaging>jar</packaging>

* add Graphity Client as a Maven dependency in your project using an IDE, or in the 'pom.xml' file

        <dependency>
            <groupId>org.graphity</groupId>
            <artifactId>client</artifactId>
            <version>1.0.7-SNAPSHOT</version>
        </dependency>

* add `main/webapp/WEB-INF/web.xml` with this JAX-RS config:

        <filter>
            <filter-name>index</filter-name>
            <filter-class>com.sun.jersey.spi.container.servlet.ServletContainer</filter-class>
            <init-param>
                <param-name>javax.ws.rs.Application</param-name>
                <param-value>org.graphity.client.ApplicationBase</param-value>
            </init-param>
        </filter>

* pom.xml execution
* extract Twitter Bootstrap distribution into `/src/main/webapp/static/` folder

Extending Graphity
------------------

For a sample `Hello World` kind of application using Graphity, check out [sample webapp](../../../client-sample-app).

In Java

* extend Resource from [org.graphity.client.model.ResourceBase](../../blob/master/src/main/java/org/graphity/client/model/ResourceBase.java)
* override the JAX-RS-compatible constructor and remember to add annotations its arguments

        public ResourceBase(@Context UriInfo uriInfo, @Context Request request, @Context HttpHeaders httpHeaders, @Context ResourceConfig resourceConfig,
            @Context OntModel sitemap, @Context SPARQLEndpoint endpoint,
            @QueryParam("limit") @DefaultValue("20") Long limit,
            @QueryParam("offset") @DefaultValue("0") Long offset,
            @QueryParam("order-by") String orderBy,
            @QueryParam("desc") @DefaultValue("false") Boolean desc)
        {
            super(uriInfo, request, httpHeaders,
                resourceConfig, sitemap, endpoint,
                limit, offset, orderBy, desc);

            // custom logic
        }

* override `@Path` class annotation

* extend Application from [org.graphity.client.ApplicationBase](../../blob/master/src/main/java/org/graphity/client/ApplicationBase.java)
* configure JAX-RS application class in web.xml

        <init-param>
            <param-name>javax.ws.rs.Application</param-name>
            <param-value>com.semanticreports.Application</param-value>
        </init-param>

* register your Resource class in your Application constructor and/or `getClasses()` method


In sitemap:
* `owl:import` [Graphity Client ontology](../../blob/master/src/main/resources/org/graphity/client/ontology/sitemap.ttl) or [Graphity Processor ontology](../../blob/master/src/main/resources/org/graphity/processor/vocabulary/gp.ttl)

        <xsl:import href="../../../org/graphity/client/writer/functions.xsl"/>
        <xsl:import href="../../../org/graphity/client/writer/group-sort-triples.xsl"/>
        <xsl:import href="../../../org/graphity/client/writer/local-xhtml.xsl"/>

* create `/src/main/resources/prefix-mapping.n3` to configure mappings to locally cached copies of vocabularies, if any
* configure sitemap ontology location in web.xml

        <init-param>
            <param-name>http://processor.graphity.org/ontology#ontologyLocation</param-name>
            <param-value>com/sample/ontology/sitemap.ttl</param-value>
        </init-param>

In XSLT:
* `xsl:import` [org/graphity/client/writer/local-xhtml.xsl])() to use default local webapp layout
* `xsl:import` [org/graphity/client/writer/global-xhtml.xsl]() to use default layout with LD browser capabilities
* make copies of local-xhtml.xsl and/or layout.xsl and remove/change the templates for custom layout
* configure stylesheet location in web.xml

Configuration
-------------

Graphity is configured in web.xml (usually located at `/src/main/webapp/WEB-INF`) using `<init-param>`, for example:

        <init-param>
            <param-name>http://rdfs.org/ns/void#sparqlEndpoint</param-name>
            <param-value>http://dydra.com/graphity/client/sparql</param-value>
        </init-param>

Currently supported configuration parameters:

<table>
  <thead>
    <tr>
      <td>Property</td>
      <td>Default value</td>
      <td>Description</td>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><pre><code>http://rdfs.org/ns/void#sparqlEndpoint</code></pre></td>
      <td></td>
      <td>(Remote) SPARQL endpoint Graphity is operating on. This endpoint is also accessible via local endpoint proxy,
e.g. `http://localhost:8080/sparql`. By default, none is specified, in which case Graphity is serving its own sitemap ontology on the local endpoint</td>
    </tr>
    <tr>
      <td><pre><code>http://jena.hpl.hp.com/Service#queryAuthUser</code></pre></td>
      <td></td>
      <td>Username for authentication against the SPARQL endpoint (so far only HTTP Basic authentication is supported)</td>
    </tr>
    <tr>
      <td><pre><code>http://jena.hpl.hp.com/Service#queryAuthPwd</code></pre></td>
      <td></td>
      <td>Password for authentication against the SPARQL endpoint (so far only HTTP Basic authentication is supported)</td>
    </tr>
    <tr>
      <td><pre><code>http://server.graphity.org/ontology#cacheControl</code></pre></td>
      <td><code>no-cache</code></td>
      <td>`Cache-Control` response header value. Currently this is webapp-scoped (all responses share the same value).</td>
    </tr>
    <tr>
      <td><pre><code>http://server.graphity.org/ontology#resultLimit</code></pre></td>
      <td><code>100</code></td>
      <td>`LIMIT` value Graphity sets on `SELECT` queries executed against the local SPARQL endpoint, in order to limit the number of results</td>
    </tr>
    <tr>
      <td><pre><code>http://processor.graphity.org/ontology#ontologyPath</code></pre></td>
      <td><code>ontology</code></td>
      <td>Path (relative to webapp base URI) on which ontology graph will be accessible. It resolves to e.g. `http://localhost:8080/ontology`.</td>
    </tr>
    <tr>
      <td><pre><code>http://processor.graphity.org/ontology#ontologyLocation</code></pre></td>
      <td><code>org/graphity/client/ontology/sitemap.ttl</code></td>
      <td>Location of the sitemap ontology RDF file</td>
    </tr>
    <tr>
      <td><pre><code>http://client.graphity.org/ontology#stylesheet</code></pre></td>
      <td><code>org/graphity/client/writer/global-xhtml.xsl</code></td>
      <td>Location of the master XSLT stylesheet that transforms RDF/XML to XHTML user interface</td>
    </tr>
    <tr>
      <td><pre><code><a href="https://jersey.java.net/nonav/apidocs/1.16/jersey/com/sun/jersey/spi/container/servlet/ServletContainer.html#PROPERTY_WEB_PAGE_CONTENT_REGEX">com.sun.jersey.config.property.WebPageContentRegex</a></code></pre></td>
      <td><code>/static/.*</code></td>
      <td>RegExp templates of relative paths on which static content (such as CSS and JavaScript files) will be served</td>
    </tr>
  </tbody>
</table>

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