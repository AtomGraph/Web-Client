AtomGraph Web-Client is a Linked Data web client. If you have a triplestore with RDF data that you want to publish
and/or build an end-user application on it, or would like to explore Linked Open Data, Web-Client provides the components you need.

Web-Client renders (X)HTML user interface by transforming ["plain" RDF/XML](https://jena.apache.org/documentation/io/rdf-output.html#rdfxml) (without nested resource descriptions)
using [XSLT 2.0](https://www.w3.org/TR/xslt20/) stylesheets.

![AtomGraph Web-Client screenshot](https://raw.github.com/AtomGraph/Web-Client/master/screenshot.png)

Features
========

What AWC provides for users as out-of-the-box generic features:
* loading RDF data from remote Linked Data sources
* multilingual, responsive user interface built with Twitter Bootstrap (currently [2.3.2](https://getbootstrap.com/2.3.2/))
* multiple RDF rendering modes (currently item/list/table/map)
* RDF editing mode based on [RDF/POST](http://www.lsrn.org/semweb/rdfpost.html) encoding
* SPARQL endpoint with interactive results

Getting started
===============

* [what is Linked Data](https://github.com/AtomGraph/Web-Client/wiki/What-is-Linked-Data)
* [installing Web-Client](https://github.com/AtomGraph/Web-Client/wiki/Installation)
* [extending Web-Client](https://github.com/AtomGraph/Web-Client/wiki/Extending-Web-Client)

For full documentation, see the [wiki index](https://github.com/AtomGraph/Web-Client/wiki).

Usage
=====

Docker
------

Processor is available from Docker Hub as [`atomgraph/web-client`](https://hub.docker.com/r/atomgraph/web-client/) image.
It accepts the following environment variables (that become webapp context parameters):

<dl>
    <dt><code>STYLESHEET</code></dt>
    <dd>Custom XSLT stylesheet</dd>
    <dd>URI, optional</dd>
    <dt><code>RESOLVING_UNCACHED</code></dt>
    <dd>If <code>true</code>, the stylesheet will attempt to resolve (dereference) URI resources in the rendered RDF data to improve the UX</dd>
    <dd><code>true</code>/<code>false</code>, optional</dd>
</dl>

Run Web-Client with the [default XSLT stylesheet](https://github.com/AtomGraph/Web-Client/blob/master/src/main/webapp/static/com/atomgraph/client/xsl/bootstrap/2.3.2/layout.xsl) like this:

    docker run -p 8080:8080 atomgraph/web-client

Web-Client will be available on [http://localhost:8080](http://localhost:8080).

Maven
-----

Web-Client is released on Maven central as [`com.atomgraph:client`](https://search.maven.org/artifact/com.atomgraph/client/).

Support
=======

Please [report issues](https://github.com/AtomGraph/Web-Client/issues) if you've encountered a bug or have a feature request.

Commercial AtomGraph consulting, development, and support are available from [AtomGraph](https://atomgraph.com).

Community
=========

Please join the W3C [Declarative Linked Data Apps Community Group](http://www.w3.org/community/declarative-apps/) to discuss
and develop AtomGraph and declarative Linked Data architecture in general.
