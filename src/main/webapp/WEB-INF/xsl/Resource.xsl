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
    
    <xsl:import href="imports/default.xsl"/>
    <xsl:import href="imports/foaf.xsl"/>
    <xsl:import href="imports/void.xsl"/>
    <xsl:import href="imports/sd.xsl"/>
    <xsl:import href="imports/dbpedia-owl.xsl"/>
    <xsl:import href="imports/facebook.xsl"/>
    <xsl:import href="sparql2google-wire.xsl"/>
    <xsl:import href="rdfxml2google-wire.xsl"/>

    <xsl:include href="includes/sparql.xsl"/>

    <xsl:output method="xhtml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" media-type="application/xhtml+xml"/>
    
    <xsl:preserve-space elements="pre"/>

    <xsl:param name="uri" as="xs:anyURI?"/>
    <xsl:param name="base-uri" as="xs:anyURI"/>
    <xsl:param name="absolute-path" as="xs:anyURI"/>
    <xsl:param name="http-headers" as="xs:string"/>
    <xsl:param name="service-uri" select="key('resources', $uri, $ont-model)/lda:sparqlEndpoint/@rdf:resource" as="xs:anyURI?"/> <!-- select="xs:anyURI(concat($base-uri, 'sparql'))"  -->
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
      	    </head>
	    <body>
		<xsl:apply-templates/>
	    </body>
	</html>
    </xsl:template>

    <xsl:template match="rdf:RDF">
	<xsl:apply-templates mode="g:ListMode"/>
    </xsl:template>

    <!-- matches if the RDF/XML document includes resource description where @rdf:about = $uri -->
    <xsl:template match="rdf:RDF[key('resources', $uri)]"> <!--  mode="g:ItemMode" -->
	<div id="wrapper-in" class="clearfix">
	    <div class="grid2">
		<xsl:apply-templates select="key('resources', $uri)"/>
	    </div>

	</div>
    </xsl:template>

    <!-- subject -->
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]">
<xsl:if test="@rdf:about or foaf:depiction/@rdf:resource or foaf:logo/@rdf:resource or rdf:type/@rdf:resource or rdfs:comment[lang($lang) or not(@xml:lang)] or dc:description[lang($lang) or not(@xml:lang)] or dct:description[lang($lang) or not(@xml:lang)] or dbpedia-owl:abstract[lang($lang) or not(@xml:lang)]">
    <div class="box main-info clearfix">
	    <!--
	    <xsl:if test="self::owl:Class or rdf:type/@rdf:resource = '&owl;Class' or self::rdfs:Class or rdf:type/@rdf:resource = '&rdfs;Class'">
		<a href="#" class="btn-edit"><span>Create</span></a>
	    </xsl:if>
	    -->
	    <xsl:if test="self::foaf:Image or rdf:type/@rdf:resource = '&foaf;Image' or foaf:img/@rdf:resource or foaf:depiction/@rdf:resource or foaf:logo/@rdf:resource">
		<div class="main-pic">
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
		<ul class="whatis clearfix">
		    <xsl:for-each select="rdf:type/@rdf:resource">
			<li>
			    <xsl:apply-templates select="."/>
			</li>
		    </xsl:for-each>
		</ul>
	    </xsl:if>
	    <xsl:if test="rdfs:comment[lang($lang) or not(@xml:lang)] or dc:description[lang($lang) or not(@xml:lang)] or dct:description[lang($lang) or not(@xml:lang)] or dbpedia-owl:abstract[lang($lang) or not(@xml:lang)]">
		<p class="intro">
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
		<div class="actions clearfix">
			<img src="static/img/temp/fb.gif" width="100" style="float: left; margin-top: 2px;" />
			<div class="share">
				<strong>Share:</strong>
				<a href="#" class="facebook">on Facebook</a>
				<a href="#" class="twitter">on Twitter</a>
				<a href="#" class="mail">Send email</a>
			</div>
			<div class="rate">
				<strong>Rate:</strong>
				<ul class="rating">
					<li class="on"><a href="#">1</a></li>
					<li class="on"><a href="#">2</a></li>
					<li><a href="#">3</a></li>
				</ul>
			</div>
			<div class="export">
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
    <div class="grid4">
	    <div class="box no-hd">
		    <!--
		    <a href="#" class="btn-edit">
			<span>Edit</span>
		    </a>
		    -->
		    <dl class="list-default clearfix">
			<xsl:apply-templates select="$no-domain-properties">
			    <xsl:sort select="concat(namespace-uri(.), local-name(.))" data-type="text" order="ascending"/>
			</xsl:apply-templates>	    
		    </dl>
	    </div>
    </div>
</xsl:if>

<xsl:variable name="in-domain-properties" select="rdf:type/@rdf:resource[../../*/xs:anyURI(concat(namespace-uri(.), local-name(.))) = g:inDomainOf(.)]"/>
<xsl:if test="$in-domain-properties">
    <div class="grid5">

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
    <div class="box main-info clearfix">
	    <!--
	    <xsl:if test="self::owl:Class or rdf:type/@rdf:resource = '&owl;Class' or self::rdfs:Class or rdf:type/@rdf:resource = '&rdfs;Class'">
		<a href="#" class="btn-edit"><span>Create</span></a>
	    </xsl:if>
	    -->
	    <xsl:if test="self::foaf:Image or rdf:type/@rdf:resource = '&foaf;Image' or foaf:img/@rdf:resource or foaf:depiction/@rdf:resource or foaf:logo/@rdf:resource">
		<div class="main-pic">
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
	    <!--
	    <ul class="other">
		    <li id="opeople"><a href="#">People</a></li>
		    <li id="ophotos"><a href="#">Photos</a></li>
		    <li id="otimeline"><a href="#">Timeline</a></li>
		    <li id="omap"><a href="#">Map</a></li>
		    <li id="oreports"><a href="#">Reports</a></li>
	    </ul>
	    -->
	    <xsl:if test="rdf:type/@rdf:resource">
		<ul class="whatis clearfix">
		    <xsl:for-each select="rdf:type/@rdf:resource">
			<li>
			    <xsl:apply-templates select="."/>
			</li>
		    </xsl:for-each>
		</ul>
	    </xsl:if>
	    <xsl:if test="rdfs:comment[lang($lang) or not(@xml:lang)] or dc:description[lang($lang) or not(@xml:lang)] or dct:description[lang($lang) or not(@xml:lang)] or dbpedia-owl:abstract[lang($lang) or not(@xml:lang)] or sioc:content[lang($lang) or not(@xml:lang)]">
		<p class="intro">
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
	    <div class="actions clearfix">
		    <xsl:if test="@rdf:about">
			<img src="static/img/temp/fb.gif" width="100" style="float: left; margin-top: 2px;" />
			<div class="share">
				<strong>Share:</strong>
				<a href="#" class="facebook">on Facebook</a>
				<a href="#" class="twitter">on Twitter</a>
				<a href="#" class="mail">Send email</a>
			</div>
		    </xsl:if>
		    <div class="rate">
			    <strong>Rate:</strong>
			    <ul class="rating">
				    <li class="on"><a href="#">1</a></li>
				    <li class="on"><a href="#">2</a></li>
				    <li><a href="#">3</a></li>
			    </ul>
		    </div>
		    <div class="export">
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
	<div class="gallery-img">
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

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="g:EditMode">
	<h1>
	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID"/>
	</h1>
	<form action="" method="post" enctype="multipart/form-data">
	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="g:EditMode"/>
	    <dl>
		<xsl:apply-templates select="rdf:type" mode="g:EditMode"/>
		<xsl:apply-templates select="*[not(self::rdf:type)][not(@xml:lang) or lang($lang)]" mode="g:EditMode">
		    <xsl:sort select="concat(namespace-uri(.), local-name(.))" data-type="text" order="ascending"/>
		</xsl:apply-templates>	    
	    </dl>
	</form>
	<hr/>
    </xsl:template>    

    <xsl:template match="rdf:type/@rdf:resource" mode="g:TypeMode">
	<xsl:variable name="in-domain-properties" select="../../*[xs:anyURI(concat(namespace-uri(.), local-name(.))) = g:inDomainOf(current()) or rdfs:domain(xs:anyURI(concat(namespace-uri(.), local-name(.)))) = xs:anyURI(current())][not(@xml:lang) or lang($lang)]"/>
	
	<div class="box">
		<!--
		<a href="#" class="btn-edit">
		    <span>Edit</span>
		</a>
		-->
		<h2 class="hd">
		    <xsl:apply-imports/>
		</h2>
		<dl class="list-default clearfix">
		    <xsl:choose>
			<xsl:when test="$mode = '&g;EditMode'">
			    <xsl:apply-templates select="$in-domain-properties" mode="g:EditMode">
				<xsl:with-param name="type" select="."/>
			    </xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
			    <!-- <xsl:apply-templates select="../../*[xs:anyURI(concat(namespace-uri(.), local-name(.))) = g:inDomainOf(current())][not(@xml:lang) or lang($lang)]"> -->
			    <!-- <xsl:apply-templates select="../../*[rdfs:domain(xs:anyURI(concat(namespace-uri(.), local-name(.)))) = xs:anyURI(current())][not(@xml:lang) or lang($lang)]"> --> <!-- not(self::rdf:type) --> 
			    <xsl:apply-templates select="$in-domain-properties[not(self::foaf:depiction)][not(self::owl:sameAs)]">
				<xsl:with-param name="type" select="."/>
			    </xsl:apply-templates>
			</xsl:otherwise>
		    </xsl:choose>
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

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="g:EditMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>
	<xsl:if test="not(concat(namespace-uri(preceding-sibling::*[1]), local-name(preceding-sibling::*[1])) = $this)">
	    <!-- @xml:lang = preceding-sibling::*[1]/@xml:lang -->
	    <dt>
		<xsl:apply-imports/>
	    </dt>
	</xsl:if>
	<xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="g:EditMode"/>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[@rdf:resource or @rdf:nodeID]" mode="g:EditMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>
	<xsl:if test="not(concat(namespace-uri(preceding-sibling::*[1]), local-name(preceding-sibling::*[1])) = $this)">
	    <!-- @xml:lang = preceding-sibling::*[1]/@xml:lang -->
	    <dt>
		<xsl:apply-imports/>
	    </dt>
	</xsl:if>
	<xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="g:EditMode"/>
	<xsl:if test="position() = last()">
	    <dd>
		<button>Add</button>
	    </dd>
	</xsl:if>
    </xsl:template>

    <!-- subject -->
    <xsl:template match="sp:Filter | *[rdf:type/@rdf:resource = '&sp;Filter']" mode="FacetMode">
	<input type="hidden" name="sb" value="{@rdf:nodeID}"/>
	<input type="hidden" name="pu" value="&rdf;type"/>
	<input type="hidden" name="ou" value="&sp;Filter"/>

	<xsl:apply-templates mode="FacetMode"/>
    </xsl:template>

    <xsl:template match="sp:or | *[rdf:type/@rdf:resource = '&sp;or']" mode="FacetMode">
	<input type="hidden" name="sb" value="{@rdf:nodeID}"/>
	<input type="hidden" name="pu" value="&rdf;type"/>
	<input type="hidden" name="ou" value="&sp;or"/>

	<xsl:apply-templates mode="FacetMode"/>
    </xsl:template>

    <xsl:template match="sp:eq | *[rdf:type/@rdf:resource = '&sp;eq']" mode="FacetMode">
	<input type="hidden" name="sb" value="{@rdf:nodeID}"/>
	<input type="hidden" name="pu" value="&rdf;type"/>
	<input type="hidden" name="ou" value="&sp;eq"/>

	<xsl:apply-templates mode="FacetMode"/>
    </xsl:template>

    <xsl:template match="sp:datatype[@rdf:nodeID] | *[rdf:type/@rdf:resource = '&sp;datatype'][@rdf:nodeID]" mode="FacetMode">
	<input type="hidden" name="sb" value="{@rdf:nodeID}"/>
	<input type="hidden" name="pu" value="&rdf;type"/>
	<input type="hidden" name="ou" value="&sp;datatype"/>
	
	<xsl:apply-templates select="sp:arg1" mode="FacetMode"/>
    </xsl:template>

    <!-- properties -->
    
    <xsl:template match="sp:arg1[../sp:arg2/@rdf:resource = '&g;PredicateVar'] | sp:arg2[../sp:arg1/@rdf:resource = '&g;PredicateVar']" mode="FacetMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>
	<input type="hidden" name="pu" value="{$this}"/>
	<input type="hidden" name="ou" value="{@rdf:resource}"/>

	<h3 class="hd2">
	    <!-- <xsl:apply-templates select="@rdf:resource"/> -->
	    <a href="{$base-uri}?uri={encode-for-uri(@rdf:resource)}" title="{.}">
		<xsl:value-of select="g:label(@rdf:resource, /, $lang)"/>
	    </a>
	</h3>
    </xsl:template>

    <xsl:template match="sp:arg1[../sp:arg2/@rdf:resource = '&g;ObjectVar'] | sp:arg2[../sp:arg1/@rdf:resource = '&g;ObjectVar']" mode="FacetMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>
	<input type="hidden" name="pu" value="{$this}"/>

	<div id="nav2">
	    <ul>
		<li>
		    <input type="checkbox" name="ou" value="{@rdf:resource}" id="{generate-id()}" checked="checked" disabled="disabled"/>
		    <label for="{generate-id()}" title="{@rdf:resource}">
			<xsl:value-of select="g:label(@rdf:resource, /, $lang)"/>
		    </label>
		</li>
	    </ul>
	</div>
    </xsl:template>

    <xsl:template match="sp:arg1 | sp:arg2" mode="FacetMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>
	<input type="hidden" name="pu" value="{$this}"/>
	<input type="hidden" name="ou" value="{@rdf:resource}"/>
    </xsl:template>

    <xsl:template match="sp:arg1[@rdf:nodeID] | sp:arg2[@rdf:nodeID]" mode="FacetMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>
	<input type="hidden" name="pu" value="{$this}"/>
	<input type="hidden" name="ob" value="{@rdf:nodeID}"/>
	
	<xsl:apply-templates select="key('resources', @rdf:nodeID, $query-model)" mode="FacetMode"/>
    </xsl:template>

    <xsl:template match="sp:expression[@rdf:nodeID]" mode="FacetMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>
	<input type="hidden" name="pu" value="{$this}"/>
	<input type="hidden" name="ob" value="{@rdf:nodeID}"/>
	
	<xsl:apply-templates select="key('resources', @rdf:nodeID, $query-model)" mode="FacetMode"/>
    </xsl:template>

    <xsl:template match="sp:varName" mode="FacetMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>
	<input type="hidden" name="pu" value="{$this}"/>
	<input type="hidden" name="ol" value="{.}"/>
	<xsl:if test="../@rdf:datatype">
	    <input type="hidden" name="lt" value="{../@rdf:datatype}"/>
	</xsl:if>
    </xsl:template>
    
    <xsl:template match="*[@rdf:about]" mode="ClassPropertyListMode">
	<xsl:param name="subject-uri" as="xs:anyURI"/>
	<xsl:param name="predicate-uri" as="xs:anyURI"/>
	<xsl:variable name="object-uri" select="xs:anyURI(concat($base-uri, 'visualizations/123456789'))" as="xs:anyURI"/>
	<li>
<input type="hidden" name="su" value="{$subject-uri}"/>
<input type="hidden" name="pu" value="{$predicate-uri}"/>
<input type="checkbox" name="ou" value="{$object-uri}" id="{generate-id()}-toggle"/>

	    <label for="{generate-id()}-toggle">
		<xsl:value-of select="g:label(@rdf:about, /, $lang)"/>
	    </label>
	</li>
    </xsl:template>

    <xsl:template match="g:XSLTMode/@rdf:about | *[rdf:type/@rdf:resource = '&g;XSLTMode']/@rdf:about">
	<a href="{$absolute-path}?mode={encode-for-uri(.)}">
	    <xsl:value-of select="g:label(., /, $lang)"/>
	</a>
    </xsl:template>

</xsl:stylesheet>