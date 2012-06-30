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
    <!ENTITY gfb-app "http://graph.facebook.com/schema/application#">
    <!ENTITY oauth "http://tools.ietf.org/html/draft-ietf-oauth-v2-23#">
    <!ENTITY g-maps "http://maps.googleapis.com/maps/api/js">
    <!ENTITY geo "http://www.w3.org/2003/01/geo/wgs84_pos#">
    <!ENTITY dbpedia-owl "http://dbpedia.org/ontology/">
    <!ENTITY dc "http://purl.org/dc/elements/1.1/">
    <!ENTITY dct "http://purl.org/dc/terms/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY sioc "http://rdfs.org/sioc/ns#">
    <!ENTITY aowl "http://bblfish.net/work/atom-owl/2006-06-06/AtomOwl.html#">
    <!ENTITY sp "http://spinrdf.org/sp#">
    <!ENTITY sd "http://www.w3.org/ns/sparql-service-description#">
    <!ENTITY lda "http://purl.org/linked-data/api/vocab#">
    <!ENTITY list "http://jena.hpl.hp.com/ARQ/list#">
    <!ENTITY vis "http://graphity.org/ontologies/visualizations#">
    <!ENTITY vis-google "http://graphity.org/ontologies/visualizations/google#">
    <!ENTITY dydra-uri "http://dydra.com/graphity/browser/sparql">
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
xmlns:gfb-app="&gfb-app;"
xmlns:g-maps="&g-maps;"
xmlns:oauth="&oauth;"
xmlns:geo="&geo;"
xmlns:dbpedia-owl="&dbpedia-owl;"
xmlns:dc="&dc;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:aowl="&aowl;"
xmlns:sp="&sp;"
xmlns:sd="&sd;"
xmlns:lda="&lda;"
xmlns:list="&list;"
xmlns:vis="&vis;"
xmlns:uuid="java:java.util.UUID"
exclude-result-prefixes="xsl xhtml xs g rdf rdfs owl sparql gfb-app g-maps oauth geo dbpedia-owl dc dct foaf sioc aowl sp sd lda list vis uuid">
<!-- xmlns:url="&java;java.net.URLEncoder" -->
    
    <xsl:import href="classes/org/graphity/browser/provider/xslt/imports/default.xsl"/>
    <xsl:import href="classes/org/graphity/browser/provider/xslt/imports/foaf.xsl"/>
    <xsl:import href="classes/org/graphity/browser/provider/xslt/imports/void.xsl"/>
    <xsl:import href="classes/org/graphity/browser/provider/xslt/imports/sd.xsl"/>
    <xsl:import href="classes/org/graphity/browser/provider/xslt/imports/dbpedia-owl.xsl"/>
    <xsl:import href="classes/org/graphity/browser/provider/xslt/imports/facebook.xsl"/>
    <xsl:import href="classes/org/graphity/browser/provider/xslt/sparql2google-wire.xsl"/>
    <xsl:import href="classes/org/graphity/browser/provider/xslt/rdfxml2google-wire.xsl"/>

    <!-- <xsl:include href="includes/sparql.xsl"/> -->

    <xsl:output method="xhtml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" media-type="application/xhtml+xml"/>
    
    <xsl:preserve-space elements="pre"/>

    <xsl:param name="base-uri" as="xs:anyURI"/>
    <xsl:param name="absolute-path" as="xs:anyURI"/>
    <xsl:param name="http-headers" as="xs:string"/>

    <xsl:param name="uri" select="$absolute-path" as="xs:anyURI"/>
    <xsl:param name="service-uri" select="key('resources', $uri, $ont-model)/g:service/@rdf:resource" as="xs:anyURI?"/> <!-- select="xs:anyURI(concat($base-uri, 'sparql'))"  -->
    <xsl:param name="query-uri" as="xs:anyURI?"/>
    <xsl:param name="query-bnode-id" as="xs:string?"/>
    <xsl:param name="query-model" select="document($query-uri)" as="document-node()?"/>
    <xsl:param name="mode" select="if (key('resources', $uri, $ont-model)/g:mode/@rdf:resource) then key('resources', $uri, $ont-model)/g:mode/@rdf:resource else xs:anyURI('&g;ListMode')" as="xs:anyURI"/>
    <xsl:param name="action" select="false()"/>
    <xsl:param name="lang" select="'en'" as="xs:string"/>

    <xsl:param name="query" select="$select-query/sp:text" as="xs:string?"/>
    <xsl:param name="offset" select="$select-query/sp:offset" as="xs:integer?"/>
    <xsl:param name="limit" select="$select-query/sp:limit" as="xs:integer?"/>
    <xsl:param name="where" select="list:member(key('resources', $select-query/sp:where/@rdf:nodeID, $query-model), $query-model)"/>
    <xsl:param name="orderBy" select="if ($select-query/sp:orderBy) then list:member(key('resources', $select-query/sp:orderBy/@rdf:nodeID, $query-model), $query-model) else ()"/>
    <xsl:param name="resultVariables" select="if ($select-query/sp:resultVariables) then list:member(key('resources', $select-query/sp:resultVariables/@rdf:nodeID, $query-model), $query-model) else ()"/>
    <xsl:param name="order-by" select="key('resources', $orderBy/sp:expression/@rdf:resource, $query-model)/sp:varName" as="xs:string?"/>
    <xsl:param name="desc" select="$orderBy[1]/rdf:type/@rdf:resource = '&sp;Desc'" as="xs:boolean"/>
    <!-- <xsl:param name="desc-default" as="xs:boolean"/> -->

    <xsl:param name="gfb-app:id" select="'121081534640971'" as="xs:string"/>
    <xsl:param name="g-maps:key" select="'AIzaSyATfQRHyNn8HBo7Obi3ytqybeSHoqAbRYA'" as="xs:string"/>
    <xsl:param name="oauth:redirect_uri" select="resolve-uri('oauth', $base-uri)" as="xs:anyURI"/>
    <xsl:param name="rdf:type" as="xs:string?"/>

    <xsl:variable name="ont-model" select="document(resolve-uri('ontology/', $base-uri))" as="document-node()"/>
    <xsl:variable name="graphity-ont-model" select="document('&g;')" as="document-node()"/>
    <xsl:variable name="select-query" select="key('resources', concat($query-uri, $query-bnode-id), $query-model)"/>
    <xsl:variable name="img-properties" select="('&foaf;depiction')" as="xs:string*"/>
	
    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
    <xsl:key name="resources-by-type" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdf:type/@rdf:resource"/> <!-- concat(namespace-uri(.), local-name(.)) -->
    <xsl:key name="resources-by-subclass" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:subClassOf/@rdf:resource"/>
    <xsl:key name="resources-by-endpoint" match="*[@rdf:about] | *[@rdf:nodeID]" use="sd:endpoint/@rdf:resource"/>
    <xsl:key name="properties-by-resource" match="*[@rdf:about]/* | *[@rdf:nodeID]/*" use="@rdf:resource | @rdf:nodeID"/>
    
    <xsl:template match="/">
	<html>
	    <head>
		<title>
		    <xsl:text>Graphity</xsl:text>
		    <xsl:if test="$uri">
			<xsl:text> - </xsl:text><xsl:value-of select="g:label($uri, /, $lang)"/>
			<xsl:if test="not(starts-with($uri, $base-uri))">
			    <xsl:text> [</xsl:text><xsl:value-of select="$uri"/><xsl:text>]</xsl:text>
			</xsl:if>
		    </xsl:if>
		</title>
		<!-- <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/> -->
		<base href="{$base-uri}" />
		
		<xsl:for-each select="key('resources', $base-uri, $ont-model)">
		    <meta name="author" content="{dct:creator/@rdf:resource}"/>
		    <meta name="description" content="{dct:description}" xml:lang="{dct:description/@xml:lang}" lang="{dct:description/@xml:lang}"/>
		</xsl:for-each>
		<meta name="DC.title" content="Graphity"/>
		<meta name="keywords" content="Linked Data, RDF, SPARQL, Semantic Web, browser, open source" xml:lang="en" lang="en"/>
		
		<link href="static/css/bootstrap.css" rel="stylesheet"/>
		<link href="static/css/bootstrap-responsive.css" rel="stylesheet"/>
		
		<style type="text/css">
		    <![CDATA[
		    body {
			padding-top: 60px;
			padding-bottom: 40px;
		    }
		    ]]>
		</style>    
      	    </head>
	    <body>
		<div class="navbar navbar-fixed-top">
		    <div class="navbar-inner">
			<div class="container">
	    
			    <a class="brand" href="/">Graphity</a>

			    <div class="btn-group">
				<a class="btn dropdown-toggle" href="#">
				    Ontology
				    <span class="caret"></span>
				</a>
				<ul class="dropdown-menu">
				    <xsl:for-each select="key('resources-by-type', '&sd;Service', $ont-model)">
					<xsl:sort select="g:label((@rdf:about | @rdf:nodeID)[1], /, $lang)" data-type="text"/>
					<li>
					    <xsl:apply-templates select="@rdf:about | @rdf:nodeID"/>
					</li>
				    </xsl:for-each>
				</ul>
			    </div>
			    
			    <form action="" method="get" class="well form-inline">
				<input type="text" name="uri" class="input-large">
				    <xsl:if test="not(starts-with($uri, $base-uri))">
					<xsl:attribute name="value">
					    <xsl:value-of select="$uri"/>
					</xsl:attribute>
				    </xsl:if>
				</input>
				<xsl:if test="$service-uri">
				    <input type="hidden" name="service-uri" value="{$service-uri}"/>
				</xsl:if>
				
				<button type="submit" class="btn btn-primary">Go</button>
			    </form>
			</div>
		    </div>
		</div>
		
		<xsl:apply-templates/>
	    </body>
	</html>
    </xsl:template>

    <xsl:template match="rdf:RDF">
	<xsl:apply-templates mode="g:ListMode"/>
    </xsl:template>

    <!-- matches if the RDF/XML document includes resource description where @rdf:about = $uri -->
    <xsl:template match="rdf:RDF[key('resources', $uri)]">
	<div class="container-fluid">
	    <div class="row-fluid">
		<div class="span9">
		    <div class="hero-unit">
			<xsl:apply-templates select="key('resources', $uri)"/>
		    </div>
		</div>

		<div class="span3">
		    <div class="well sidebar-nav">
			<xsl:for-each select="key('resources', $uri)">
			    <xsl:if test="rdfs:label or dc:title or dct:title or rdfs:seeAlso or owl:sameAs or dc:subject or dct:subject">
				<div>
				    <xsl:if test="owl:sameAs">
					<h3>
					    <a href="{$base-uri}?uri={encode-for-uri('&owl;sameAs')}" title="{g:label(xs:anyURI('&owl;sameAs'), /, $lang)}">
						<xsl:value-of select="g:label(xs:anyURI('&owl;sameAs'), /, $lang)"/>
					    </a>					
					</h3>
					<ul class="nav nav-list">
					    <xsl:for-each select="owl:sameAs">
						<li>
						    <!--
						    <span>
							<a href="{@rdf:resource}">
							    <img src="static/img/temp/pic.jpg" width="37" height="37" />
							</a>
						    </span>
						    -->
						    <span>
							<xsl:apply-templates select="@rdf:resource"/>
						    </span>
						    <!--
						    <ul>
							    <li><a href="#">City</a></li>
							    <li><a href="#">Location</a></li>
						    </ul>
						    -->
						</li>
					    </xsl:for-each>
					</ul>
				    </xsl:if>

				    <xsl:if test="rdfs:seeAlso">
					<h3>
					    <a href="{$base-uri}?uri={encode-for-uri('&rdfs;seeAlso')}" title="{g:label(xs:anyURI('&rdfs;seeAlso'), /, $lang)}">
						<xsl:value-of select="g:label(xs:anyURI('&rdfs;seeAlso'), /, $lang)"/>
					    </a>					
					</h3>
					<ul>
					    <xsl:for-each select="rdfs:seeAlso">
						<li>
						    <!--
						    <span>
							<a href="{@rdf:resource}">
							    <img src="static/img/temp/pic.jpg" width="37" height="37" />
							</a>
						    </span>
						    -->
						    <span>
							<xsl:apply-templates select="@rdf:resource"/>
						    </span>
						    <!--
						    <ul>
							    <li><a href="#">City</a></li>
							    <li><a href="#">Location</a></li>
						    </ul>
						    -->
						</li>
					    </xsl:for-each>
					</ul>
				    </xsl:if>

				    <xsl:if test="dc:subject or dct:subject">
					<h3>
					    <a href="{$base-uri}?uri={encode-for-uri('&dct;subject')}" title="{g:label(xs:anyURI('&dct;subject'), /, $lang)}">
						<xsl:value-of select="g:label(xs:anyURI('&dct;subject'), /, $lang)"/>
					    </a>
					</h3>
					<ul class="nav nav-list">
					    <xsl:for-each select="dc:subject | dct:subject">
						<li>
						    <span class="title">
							<xsl:apply-templates select="@rdf:resource"/>
						    </span>						
						</li>
					    </xsl:for-each>
					</ul>
				    </xsl:if>

				    <xsl:if test="key('resources-by-type', $uri)">
					<h3>Instances</h3>
					<ul class="nav nav-list">
					    <xsl:for-each select="key('resources-by-type', $uri)">
						<li>
							<!--
							<span>
							    <a href="{@rdf:resource}">
								<img src="static/img/temp/pic.jpg" width="37" height="37" />
							    </a>
							</span>
							-->
							<span>
							    <xsl:apply-templates select="@rdf:about | @rdf:nodeID"/>
							</span>
							<!--
							<ul>
								<li><a href="#">City</a></li>
								<li><a href="#">Location</a></li>
							</ul>
							-->
						</li>
					    </xsl:for-each>
					</ul>
				    </xsl:if>
				</div>
			    </xsl:if>
			</xsl:for-each>
		    </div>
		</div>
	    </div>
	</div>
    </xsl:template>

    <!-- subject -->
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]">
	<xsl:if test="@rdf:about or foaf:depiction/@rdf:resource or foaf:logo/@rdf:resource or rdf:type/@rdf:resource or rdfs:comment[lang($lang) or not(@xml:lang)] or dc:description[lang($lang) or not(@xml:lang)] or dct:description[lang($lang) or not(@xml:lang)] or dbpedia-owl:abstract[lang($lang) or not(@xml:lang)]">
	    <div>
		    <!--
		    <xsl:if test="self::owl:Class or rdf:type/@rdf:resource = '&owl;Class' or self::rdfs:Class or rdf:type/@rdf:resource = '&rdfs;Class'">
			<a href="#"><span>Create</span></a>
		    </xsl:if>
		    -->
		    <xsl:if test="self::foaf:Image or rdf:type/@rdf:resource = '&foaf;Image' or foaf:img/@rdf:resource or foaf:depiction/@rdf:resource or foaf:logo/@rdf:resource">
			<div>
			    <xsl:choose>
				<xsl:when test="self::foaf:Image or rdf:type/@rdf:resource = '&foaf;Image'">
				    <xsl:apply-templates select="@rdf:about"/>
				</xsl:when>
				<xsl:when test="foaf:img/@rdf:resource">
				    <xsl:apply-templates select="foaf:img[1]/@rdf:resource"/>
				</xsl:when>
				<xsl:when test="foaf:depiction/@rdf:resource">
				    <xsl:apply-templates select="foaf:depiction[1]/@rdf:resource"/>
				</xsl:when>
				<xsl:when test="foaf:logo/@rdf:resource">
				    <xsl:apply-templates select="foaf:logo[1]/@rdf:resource"/>
				</xsl:when>
			    </xsl:choose>
			</div>
		    </xsl:if>
		    <xsl:if test="@rdf:about and not(self::foaf:Image or rdf:type/@rdf:resource = '&foaf;Image')">
			<h1>
			    <xsl:apply-templates select="@rdf:about"/> <!--  | @rdf:nodeID -->
			</h1>
		    </xsl:if>
		    <!--
		    <xsl:if test="@rdf:nodeID">
			<h1>
			    <a name="{@rdf:nodeID}">
				<xsl:value-of select="@rdf:nodeID"/>
			    </a>
			</h1>
		    </xsl:if>
		    <xsl:if test="rdf:type/@rdf:resource">
			<ul>
			    <xsl:for-each select="rdf:type/@rdf:resource">
				<li>
				    <xsl:apply-templates select="."/>
				</li>
			    </xsl:for-each>
			</ul>
		    </xsl:if>
		    <xsl:if test="rdfs:comment[lang($lang) or not(@xml:lang)] or dc:description[lang($lang) or not(@xml:lang)] or dct:description[lang($lang) or not(@xml:lang)] or dbpedia-owl:abstract[lang($lang) or not(@xml:lang)]">
			<p>
			    <xsl:if test="rdfs:comment[lang($lang) or not(@xml:lang)]">
				<xsl:value-of select="substring(rdfs:comment[lang($lang) or not(@xml:lang)][1], 1, 300)"/>
			    </xsl:if>
			    <xsl:if test="dc:description[lang($lang) or not(@xml:lang)]">
				<xsl:value-of select="substring(dc:description[lang($lang) or not(@xml:lang)][1], 1, 300)"/>
			    </xsl:if>
			    <xsl:if test="dct:description[lang($lang) or not(@xml:lang)]">
				<xsl:value-of select="substring(dct:description[lang($lang) or not(@xml:lang)][1], 1, 300)"/>
			    </xsl:if>
			    <xsl:if test="dbpedia-owl:abstract[lang($lang) or not(@xml:lang)][1]">
				<xsl:value-of select="substring(dbpedia-owl:abstract[lang($lang) or not(@xml:lang)][1], 1, 300)"/>
			    </xsl:if>
			</p>
		    </xsl:if>

		    <xsl:if test="@rdf:about">
			<div>
				<div>
					<strong>Export:</strong>
					<ul>
						<li>
						    <a href="?uri={encode-for-uri($uri)}&amp;accept={encode-for-uri('application/rdf+xml')}">RDF/XML</a>
						</li>
						<li>
						    <a href="?uri={encode-for-uri($uri)}&amp;accept={encode-for-uri('text/turtle')}">Turtle</a>
						</li>
					</ul>
				</div>
			</div>
		    </xsl:if>
		    -->
	    </div>
	</xsl:if>

	<xsl:variable name="no-domain-properties" select="*[not(self::rdf:type)][not(self::foaf:img)][not(self::foaf:depiction)][not(self::foaf:logo)][not(self::owl:sameAs)][not(self::rdfs:comment)][not(self::rdfs:seeAlso)][not(self::dc:title)][not(self::dct:title)][not(self::dc:description)][not(self::dct:description)][not(self::dct:subject)][not(self::dbpedia-owl:abstract)][not(self::sioc:content)][not(self::aowl:content)][not(rdfs:domain(xs:anyURI(concat(namespace-uri(.), local-name(.)))) = current()/rdf:type/@rdf:resource)][not(@xml:lang) or lang($lang)]"/>
	<xsl:if test="$no-domain-properties">
	    <div>
		    <div>
			    <!--
			    <a href="#">
				<span>Edit</span>
			    </a>
			    -->
			    <dl>
				<xsl:apply-templates select="$no-domain-properties">
				    <xsl:sort select="concat(namespace-uri(.), local-name(.))" data-type="text" order="ascending"/>
				</xsl:apply-templates>	    
			    </dl>
		    </div>
	    </div>
	</xsl:if>

	<xsl:variable name="in-domain-properties" select="rdf:type/@rdf:resource[../../*/xs:anyURI(concat(namespace-uri(.), local-name(.))) = g:inDomainOf(.)]"/>
	<xsl:if test="$in-domain-properties">
	    <div>

		    <xsl:apply-templates select="$in-domain-properties" mode="g:TypeMode">
			<!-- <xsl:sort select="@rdf:resource | @rdf:nodeID" data-type="text" order="ascending"/> -->
		    </xsl:apply-templates>

	    </div>
	</xsl:if>

	<div class="clearer">&#160;</div>
		<!-- <hr/> -->
    </xsl:template>    

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="g:ListMode">
	<xsl:if test="@rdf:about or foaf:depiction/@rdf:resource or foaf:logo/@rdf:resource or rdf:type/@rdf:resource or rdfs:comment[lang($lang) or not(@xml:lang)] or dc:description[lang($lang) or not(@xml:lang)] or dct:description[lang($lang) or not(@xml:lang)] or dbpedia-owl:abstract[lang($lang) or not(@xml:lang)]">
	    <div>
		    <!--
		    <xsl:if test="self::owl:Class or rdf:type/@rdf:resource = '&owl;Class' or self::rdfs:Class or rdf:type/@rdf:resource = '&rdfs;Class'">
			<a href="#"><span>Create</span></a>
		    </xsl:if>
		    -->
		    <xsl:if test="self::foaf:Image or rdf:type/@rdf:resource = '&foaf;Image' or foaf:img/@rdf:resource or foaf:depiction/@rdf:resource or foaf:logo/@rdf:resource">
			<div>
			    <xsl:choose>
				<xsl:when test="self::foaf:Image or rdf:type/@rdf:resource = '&foaf;Image'">
				    <xsl:apply-templates select="@rdf:about"/>
				</xsl:when>
				<xsl:when test="foaf:img/@rdf:resource">
				    <xsl:apply-templates select="foaf:img[1]/@rdf:resource"/>
				</xsl:when>
				<xsl:when test="foaf:depiction/@rdf:resource">
				    <xsl:apply-templates select="foaf:depiction[1]/@rdf:resource"/>
				</xsl:when>
				<xsl:when test="foaf:logo/@rdf:resource">
				    <xsl:apply-templates select="foaf:logo[1]/@rdf:resource"/>
				</xsl:when>
			    </xsl:choose>
			</div>
		    </xsl:if>
		    <xsl:if test="(@rdf:about or @rdf:nodeID) and not(self::foaf:Image or rdf:type/@rdf:resource = '&foaf;Image')">
			<h1>
			    <xsl:apply-templates select="@rdf:about | @rdf:nodeID"/>
			</h1>
		    </xsl:if>
		    <!--
		    <xsl:if test="@rdf:nodeID">
			<h1>
			    <a name="{@rdf:nodeID}">
				<xsl:value-of select="@rdf:nodeID"/>
			    </a>
			</h1>
		    </xsl:if>
		    -->
		    <xsl:if test="rdf:type/@rdf:resource">
			<ul>
			    <xsl:for-each select="rdf:type/@rdf:resource">
				<li>
				    <xsl:apply-templates select="."/>
				</li>
			    </xsl:for-each>
			</ul>
		    </xsl:if>
		    <xsl:if test="rdfs:comment[lang($lang) or not(@xml:lang)] or dc:description[lang($lang) or not(@xml:lang)] or dct:description[lang($lang) or not(@xml:lang)] or dbpedia-owl:abstract[lang($lang) or not(@xml:lang)] or sioc:content[lang($lang) or not(@xml:lang)]">
			<p>
			    <xsl:choose>
				<xsl:when test="rdfs:comment[lang($lang) or not(@xml:lang)]">
				    <xsl:value-of select="substring(rdfs:comment[lang($lang) or not(@xml:lang)][1], 1, 300)"/>
				</xsl:when>
				<xsl:when test="dc:description[lang($lang) or not(@xml:lang)]">
				    <xsl:value-of select="substring(dc:description[lang($lang) or not(@xml:lang)][1], 1, 300)"/>
				</xsl:when>
				<xsl:when test="dct:description[lang($lang) or not(@xml:lang)]">
				    <xsl:value-of select="substring(dct:description[lang($lang) or not(@xml:lang)][1], 1, 300)"/>
				</xsl:when>
				<xsl:when test="dbpedia-owl:abstract[lang($lang) or not(@xml:lang)]">
				    <xsl:value-of select="substring(dbpedia-owl:abstract[lang($lang) or not(@xml:lang)][1], 1, 300)"/>
				</xsl:when>
				<xsl:when test="sioc:content[lang($lang) or not(@xml:lang)]">
				    <xsl:value-of select="substring(sioc:content[lang($lang) or not(@xml:lang)][1], 1, 300)"/>
				</xsl:when>
			    </xsl:choose>
			</p>
		    </xsl:if>
		    <!--
		    <div>
			    <div>
				    <strong>Export:</strong>
				    <ul>
					    <li>
						<a href="?uri={encode-for-uri($uri)}&amp;accept={encode-for-uri('application/rdf+xml')}">RDF/XML</a>
					    </li>
					    <li>
						<a href="?uri={encode-for-uri($uri)}&amp;accept={encode-for-uri('text/turtle')}">Turtle</a>
					    </li>
				    </ul>
			    </div>
		    </div>
		    -->
	    </div>
	</xsl:if>
	<div class="clearer">&#160;</div>
		<!-- <hr/> -->
    </xsl:template>
    
    <xsl:template match="foaf:img/@rdf:resource | foaf:depiction/@rdf:resource | foaf:thumbnail/@rdf:resource | foaf:logo/@rdf:resource" mode="g:ImageMode">
	<div>
	    <xsl:if test="../../@rdf:about">
		<a href="{$base-uri}?uri={encode-for-uri(../../@rdf:about)}{if ($service-uri) then (concat('&amp;service-uri=', encode-for-uri($service-uri))) else ()}">
		    <img src="{.}" alt="{g:label(../../@rdf:about, /, $lang)}" />
		</a>
	    </xsl:if>
	    <xsl:if test="../../@rdf:nodeID">
		<img src="{.}" alt="{g:label(../../@rdf:nodeID, /, $lang)}" />
	    </xsl:if>	    
	</div>
    </xsl:template>

    <xsl:template match="rdf:type/@rdf:resource" mode="g:TypeMode">
	<xsl:variable name="in-domain-properties" select="../../*[xs:anyURI(concat(namespace-uri(.), local-name(.))) = g:inDomainOf(current()) or rdfs:domain(xs:anyURI(concat(namespace-uri(.), local-name(.)))) = xs:anyURI(current())][not(@xml:lang) or lang($lang)]"/>
	
	<div>
		<!--
		<a href="#">
		    <span>Edit</span>
		</a>
		-->
		<h2>
		    <xsl:apply-imports/>
		</h2>
		<dl>
			<xsl:apply-templates select="$in-domain-properties[not(self::foaf:depiction)][not(self::owl:sameAs)]">
			    <xsl:with-param name="type" select="."/>
			</xsl:apply-templates>
		</dl>
	</div>
    </xsl:template>

    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>

	<xsl:if test="not(concat(namespace-uri(preceding-sibling::*[1]), local-name(preceding-sibling::*[1])) = $this)">
	    <!-- @xml:lang = preceding-sibling::*[1]/@xml:lang -->
	    <dt>
		<span>
		    <xsl:apply-imports/>
		</span>
	    </dt>
	</xsl:if>
	<!-- <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID"/> -->
	<xsl:for-each select="node() | @rdf:resource">
	    <dd>
		<xsl:if test="concat(namespace-uri(..), local-name(..)) = concat(namespace-uri(../preceding-sibling::*[1]), local-name(../preceding-sibling::*[1]))">
		    <xsl:attribute name="class">no-dt</xsl:attribute>
		</xsl:if>

		<span>
		    <xsl:apply-templates select="."/>
		</span>
	    </dd>
	</xsl:for-each>
	
	<xsl:for-each select="@rdf:nodeID">
	    <dd>
		<xsl:apply-templates select="key('resources', .)"/>
	    </dd>
	</xsl:for-each>
    </xsl:template>

    <xsl:template match="g:XSLTMode/@rdf:about | *[rdf:type/@rdf:resource = '&g;XSLTMode']/@rdf:about">
	<a href="{$absolute-path}?mode={encode-for-uri(.)}">
	    <xsl:value-of select="g:label(., /, $lang)"/>
	</a>
    </xsl:template>

</xsl:stylesheet>