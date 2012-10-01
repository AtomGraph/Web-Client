<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright (C) 2012 Martynas Jusevičius <martynas@graphity.org>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
-->
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java "http://xml.apache.org/xalan/java/">
    <!ENTITY g "http://graphity.org/ontology/">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl "http://www.w3.org/2002/07/owl#">
    <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY geo "http://www.w3.org/2003/01/geo/wgs84_pos#">
    <!ENTITY dbpedia-owl "http://dbpedia.org/ontology/">
    <!ENTITY dc "http://purl.org/dc/elements/1.1/">
    <!ENTITY dct "http://purl.org/dc/terms/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY sioc "http://rdfs.org/sioc/ns#">
    <!ENTITY sp "http://spinrdf.org/sp#">
    <!ENTITY sd "http://www.w3.org/ns/sparql-service-description#">
    <!ENTITY list "http://jena.hpl.hp.com/ARQ/list#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:sparql="&sparql;"
xmlns:geo="&geo;"
xmlns:dbpedia-owl="&dbpedia-owl;"
xmlns:dc="&dc;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:sp="&sp;"
xmlns:sd="&sd;"
xmlns:list="&list;"
exclude-result-prefixes="#all">

    <xsl:import href="../../../ldp/provider/xslt/Resource.xsl"/>
    <xsl:import href="imports/default.xsl"/>
    <xsl:import href="../../../ldp/provider/xslt/group-sort-triples.xsl"/>

    <xsl:include href="imports/doap.xsl"/>
    <xsl:include href="imports/foaf.xsl"/>
    <xsl:include href="imports/sd.xsl"/>
    <xsl:include href="imports/void.xsl"/>
    <xsl:include href="../../../ldp/provider/xslt/imports/dbpedia-owl.xsl"/>
    <xsl:include href="includes/sparql.xsl"/>
    <xsl:include href="includes/frontpage.xsl"/>
    <xsl:include href="includes/post.xsl"/>

    <xsl:output method="xhtml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" media-type="application/xhtml+xml"/>

    <xsl:template match="/">
	<html>
	    <head>
		<title>
		    <xsl:text>Graphity Browser</xsl:text>
		    <xsl:if test="$uri">
			<xsl:text> - </xsl:text><xsl:value-of select="g:label($uri, /, $lang)"/>
			<xsl:if test="not(starts-with($uri, $base-uri))">
			    <xsl:text> [</xsl:text><xsl:value-of select="$uri"/><xsl:text>]</xsl:text>
			</xsl:if>
		    </xsl:if>
		</title>
		<base href="{$base-uri}" />
		
		<xsl:for-each select="key('resources', $base-uri, $ont-model)">
		    <meta name="author" content="{dct:creator/@rdf:resource}"/>
		    <meta name="description" content="{dct:description}" xml:lang="{dct:description/@xml:lang}" lang="{dct:description/@xml:lang}"/>
		</xsl:for-each>
		<meta name="keywords" content="Linked Data, RDF, SPARQL, Semantic Web, browser, open source" xml:lang="en" lang="en"/>
		<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
		
		<link href="static/css/bootstrap.css" rel="stylesheet"/>
		<link href="static/css/bootstrap-responsive.css" rel="stylesheet"/>
		
		<style type="text/css">
		    <![CDATA[
			body { padding-top: 60px; padding-bottom: 40px; }
			form.form-inline { margin: 0; }
			ul.inline { margin-left: 0; }
			.inline li { display: inline; }
			.well-small { background-color: #FAFAFA ; }
			textarea#query-string { font-family: monospace; }
		    ]]>
		</style>    
      	    </head>
	    <body>
		<div class="navbar navbar-fixed-top">
		    <div class="navbar-inner">
			<div class="container-fluid">    
			    <a class="brand" href="{$base-uri}">
				<xsl:value-of select="g:label($base-uri, /, $lang)"/>
			    </a>

			    <div class="nav-collapse">
				<form action="{$base-uri}" method="get" class="navbar-form pull-left">
				    <div class="input-append">
					<input type="text" name="uri" class="input-xxlarge">
					    <xsl:if test="not(starts-with($uri, $base-uri))">
						<xsl:attribute name="value">
						    <xsl:value-of select="$uri"/>
						</xsl:attribute>
					    </xsl:if>
					</input>
					<button type="submit" class="btn btn-primary">Go</button>

					<xsl:if test="$endpoint-uri">
					    <input type="hidden" name="endpoint-uri" value="{$endpoint-uri}"/>
					</xsl:if>
				    </div>
				</form>
				
				<ul class="nav">
				    <xsl:for-each select="key('resources', resolve-uri('sparql', $base-uri), $ont-model)">
					<li>
					    <xsl:if test="@rdf:about = $uri">
						<xsl:attribute name="class">active</xsl:attribute>
					    </xsl:if>
					    <xsl:apply-templates select="@rdf:about"/>
					</li>
				    </xsl:for-each>
				</ul>
				
				<div class="btn-group pull-right">
				    <a class="btn dropdown-toggle" href="#">
					Language
					<span class="caret"></span>
				    </a>
				    <ul class="dropdown-menu">
					<li>
					    <a href="{$absolute-path}{g:query-string($uri, $endpoint-uri, (), (), (), (), 'en', ())}">English</a>
					</li>
					<li>
					    <a href="{$absolute-path}{g:query-string($uri, $endpoint-uri, (), (), (), (), 'da', ())}">Danish</a>
					</li>
				    </ul>
				</div>
			    </div>
			</div>
		    </div>
		</div>

		<div class="container-fluid">
		    <div class="nav-collapse">
			<ul class="nav nav-pills">
			    <!-- make menu links for all resources in the ontology, except base URI -->
			    <xsl:for-each select="key('resources-by-host', $base-uri, $ont-model)/@rdf:about[not(. = $base-uri)][not(. = resolve-uri('sparql', $base-uri))]">
				<xsl:sort select="g:label(., /, $lang)" data-type="text" order="ascending" lang="{$lang}"/>
				<li>
				    <xsl:if test=". = $uri">
					<xsl:attribute name="class">active</xsl:attribute>
				    </xsl:if>
				    <xsl:apply-templates select="."/>
				</li>
			    </xsl:for-each>
			</ul>
		    </div>
		</div>

		<div class="container-fluid">
		    <div class="row-fluid">
			<xsl:variable name="grouped-rdf">
			    <xsl:apply-templates mode="g:GroupTriples"/>
			</xsl:variable>
			<xsl:apply-templates select="$grouped-rdf/rdf:RDF"/>
		    </div>
		    
		    <div class="footer">
			<p>Company <xsl:value-of select="format-date(current-date(), '[Y]', $lang, (), ())"/></p>
		    </div>
		</div>
	    </body>
	</html>
    </xsl:template>

    <xsl:template match="rdf:type/@rdf:resource">
	<span title="{.}" class="btn">
	    <xsl:apply-imports/>
	</span>
    </xsl:template>

    <!-- property -->

    <xsl:template match="rdf:type | foaf:img | foaf:depiction | foaf:logo | owl:sameAs | rdfs:label | rdfs:comment | rdfs:seeAlso | dc:title | dct:title | dc:description | dct:description | dct:subject | dbpedia-owl:abstract | sioc:content" mode="g:PropertyListMode" priority="1"/>
    
    <!-- skip <dt> for properties that are not first in the sorted group -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[preceding-sibling::*[concat(namespace-uri(.), local-name(.)) = concat(namespace-uri(current()), local-name(current()))]]" mode="g:PropertyListMode">
	<dd>
	    <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="g:PropertyListMode"/>
	</dd>
    </xsl:template>
    
    <xsl:function name="g:query-string" as="xs:string?">
	<xsl:param name="uri" as="xs:anyURI?"/>
	<xsl:param name="endpoint-uri" as="xs:anyURI?"/>
	<xsl:param name="offset" as="xs:integer?"/>
	<xsl:param name="limit" as="xs:integer?"/>
	<xsl:param name="order-by" as="xs:string?"/>
	<xsl:param name="desc" as="xs:boolean?"/>
	<xsl:param name="lang" as="xs:string?"/>
	<xsl:param name="mode" as="xs:string?"/>
	
	<xsl:variable name="query-string">
	    <xsl:if test="$uri">uri=<xsl:value-of select="encode-for-uri($uri)"/>&amp;</xsl:if>
	    <xsl:if test="$endpoint-uri">endpoint-uri=<xsl:value-of select="encode-for-uri($endpoint-uri)"/>&amp;</xsl:if>
	    <xsl:if test="$offset">offset=<xsl:value-of select="$offset"/>&amp;</xsl:if>
	    <xsl:if test="$limit">limit=<xsl:value-of select="$limit"/>&amp;</xsl:if>
	    <xsl:if test="$order-by">order-by=<xsl:value-of select="$order-by"/>&amp;</xsl:if>
	    <xsl:if test="$desc">desc&amp;</xsl:if>
	    <xsl:if test="$lang">lang=<xsl:value-of select="$lang"/>&amp;</xsl:if>
	    <xsl:if test="$mode">mode=<xsl:value-of select="encode-for-uri($mode)"/>&amp;</xsl:if>
	</xsl:variable>
	
	<xsl:sequence select="concat('?', substring($query-string, 1, string-length($query-string) - 1))"/>
    </xsl:function>

</xsl:stylesheet>