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
    <!ENTITY gc "http://client.graphity.org/ontology#">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl "http://www.w3.org/2002/07/owl#">
    <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY ldp "http://www.w3.org/ns/ldp#">
    <!ENTITY geo "http://www.w3.org/2003/01/geo/wgs84_pos#">
    <!ENTITY dbpedia-owl "http://dbpedia.org/ontology/">
    <!ENTITY dc "http://purl.org/dc/elements/1.1/">
    <!ENTITY dct "http://purl.org/dc/terms/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY sioc "http://rdfs.org/sioc/ns#">
    <!ENTITY skos "http://www.w3.org/2004/02/skos/core#">
    <!ENTITY sp "http://spinrdf.org/sp#">
    <!ENTITY spin "http://spinrdf.org/spin#">
    <!ENTITY sd "http://www.w3.org/ns/sparql-service-description#">
    <!ENTITY list "http://jena.hpl.hp.com/ARQ/list#">
    <!ENTITY xhv "http://www.w3.org/1999/xhtml/vocab#">
    <!ENTITY void "http://rdfs.org/ns/void#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:sparql="&sparql;"
xmlns:ldp="&ldp;"
xmlns:geo="&geo;"
xmlns:dbpedia-owl="&dbpedia-owl;"
xmlns:dc="&dc;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:skos="&skos;"
xmlns:sp="&sp;"
xmlns:spin="&spin;"
xmlns:sd="&sd;"
xmlns:list="&list;"
xmlns:xhv="&xhv;"
xmlns:void="&void;"
xmlns:url="&java;java.net.URLDecoder"
exclude-result-prefixes="#all">

    <xsl:import href="group-sort-triples.xsl"/>

    <xsl:include href="sparql.xsl"/>
    <xsl:include href="functions.xsl"/>

    <xsl:output method="xhtml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" media-type="application/xhtml+xml"/>
    
    <xsl:preserve-space elements="pre"/>

    <xsl:param name="base-uri" as="xs:anyURI"/>
    <xsl:param name="absolute-path" as="xs:anyURI"/>
    <xsl:param name="request-uri" as="xs:anyURI"/>
    <xsl:param name="http-headers" as="xs:string"/>
    <xsl:param name="lang" select="'en'" as="xs:string"/>
    <xsl:param name="mode" as="xs:anyURI?"/>
    <xsl:param name="ont-model" as="document-node()"/> <!-- select="document($base-uri)"  -->
    <xsl:param name="offset" select="$select-res/sp:offset" as="xs:integer?"/>
    <xsl:param name="limit" select="$select-res/sp:limit" as="xs:integer?"/>
    <xsl:param name="order-by" select="key('resources', $orderBy/sp:expression/@rdf:resource)/sp:varName" as="xs:string?"/>
    <xsl:param name="desc" select="$orderBy[1]/rdf:type/@rdf:resource = '&sp;Desc'" as="xs:boolean"/>
    <xsl:param name="endpoint-uri" as="xs:anyURI?"/>
    <xsl:param name="query" as="xs:string?"/>

    <xsl:variable name="resource" select="key('resources', $request-uri, $ont-model)" as="element()?"/>
    <!-- <xsl:variable name="matched-ont-class" select="key('resources', $resource/rdf:type/@rdf:resource | $resource/rdf:type/@rdf:nodeID, $ont-model)"/> --> <!-- as="element()?" -->
    <xsl:variable name="query-res" select="key('resources', $resource/spin:query/@rdf:resource | $resource/spin:query/@rdf:nodeID, $ont-model)" as="element()?"/>
    <xsl:variable name="where-res" select="list:member(key('resources', $query-res/sp:where/@rdf:nodeID, $ont-model), $ont-model)"/>
    <xsl:variable name="select-res" select="key('resources', $where-res/sp:query/@rdf:resource | $where-res/sp:query/@rdf:nodeID, $ont-model)" as="element()?"/>
    <xsl:variable name="orderBy" select="if ($select-res/sp:orderBy) then list:member(key('resources', $select-res/sp:orderBy/@rdf:nodeID), /) else ()"/>
    <!-- <xsl:variable name="location-mapping" select="document('../../../../../location-mapping.ttl')" as="document-node()?"/> -->

    <!-- <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/> -->
    <xsl:key name="predicates" match="*[@rdf:about]/* | *[@rdf:nodeID]/*" use="concat(namespace-uri(), local-name())"/>
    <xsl:key name="predicates-by-object" match="*[@rdf:about]/* | *[@rdf:nodeID]/*" use="@rdf:about | @rdf:nodeID"/>
    <xsl:key name="resources-by-type" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="rdf:type/@rdf:resource"/>
    <xsl:key name="resources-by-space" match="*[@rdf:about]" use="sioc:has_space/@rdf:resource"/>
    <xsl:key name="resources-by-page-of" match="*[@rdf:about]" use="ldp:pageOf/@rdf:resource"/>

    <rdf:Description rdf:about="">
	<dct:creator rdf:resource="http://semantic-web.dk/#martynas"/>
    </rdf:Description>

    <rdf:Description rdf:nodeID="previous">
	<rdfs:label xml:lang="en">Previous</rdfs:label>
    </rdf:Description>

    <rdf:Description rdf:nodeID="next">
	<rdfs:label xml:lang="en">Next</rdfs:label>
    </rdf:Description>

    <rdf:Description rdf:about="&gc;TableMode">
	<rdfs:label xml:lang="en-US">Table</rdfs:label>
    </rdf:Description>

    <rdf:Description rdf:about="&gc;ListMode">
	<rdfs:label xml:lang="en-US">List</rdfs:label>
    </rdf:Description>

    <rdf:Description rdf:about="&gc;InputMode">
	<rdfs:label xml:lang="en-US">Input</rdfs:label>
    </rdf:Description>

    <xsl:template match="/">
	<html>
	    <head>
		<title>
		    <xsl:apply-templates mode="gc:TitleMode"/>
		</title>
		<base href="{$base-uri}" />
		
		<xsl:for-each select="key('resources', $base-uri, $ont-model)">
		    <meta name="author" content="{dct:creator/@rdf:resource}"/>
		</xsl:for-each>
		<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
		
		<xsl:apply-templates mode="gc:StyleMode"/>
		<xsl:apply-templates mode="gc:ScriptMode"/>
      	    </head>
	    <body>
		<div class="navbar navbar-fixed-top">
		    <div class="navbar-inner">
			<div class="container-fluid">    
			    <xsl:apply-templates select="." mode="gc:HeaderMode"/>
			</div>
		    </div>
		</div>

		<div class="container-fluid">
		    <div class="row-fluid">
			<xsl:variable name="grouped-rdf" as="document-node()">
			    <xsl:apply-templates select="." mode="gc:GroupTriples"/>
			</xsl:variable>
			<xsl:apply-templates select="$grouped-rdf/rdf:RDF"/>
		    </div>		    
		    
		    <div class="footer">
			<xsl:apply-templates select="." mode="gc:FooterMode"/>
		    </div>
		</div>
	    </body>
	</html>
    </xsl:template>

    <xsl:template match="/" mode="gc:HeaderMode">
	<a class="brand" href="{$base-uri}">
	    <xsl:apply-templates select="key('resources', $base-uri, $ont-model)/@rdf:about" mode="gc:LabelMode"/>
	</a>

	<div class="nav-collapse">
	    <ul class="nav">
		<!-- make menu links for all resources in the ontology, except base URI -->
		<xsl:for-each select="key('resources-by-space', $base-uri, $ont-model)/@rdf:about[not(. = $base-uri)]">
		    <xsl:sort select="gc:label(., /, $lang)" data-type="text" order="ascending" lang="{$lang}"/>
		    <li>
			<xsl:if test=". = $absolute-path">
			    <xsl:attribute name="class">active</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="."/>
		    </li>
		</xsl:for-each>
	    </ul>
	</div>
	
	<xsl:for-each select="key('resources', resolve-uri('sparql', $base-uri), $ont-model)/@rdf:about">
	    <div class="nav-collapse pull-right">
		<ul class="nav">
		    <li>
			<xsl:if test=". = $absolute-path">
			    <xsl:attribute name="class">active</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="."/>
		    </li>
		</ul>
	    </div>
	</xsl:for-each>
    </xsl:template>

    <xsl:template match="/" mode="gc:FooterMode">
	<p>
	    <xsl:value-of select="format-date(current-date(), '[Y]', $lang, (), ())"/>
	</p>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="gc:TitleMode">
	<xsl:apply-templates select="key('resources', $base-uri, $ont-model)/@rdf:about" mode="gc:LabelMode"/>
	<xsl:text> - </xsl:text>
	<xsl:apply-templates select="key('resources', $absolute-path, $ont-model)" mode="gc:TitleMode"/>
    </xsl:template>

    <xsl:template match="*[@rdf:about]" mode="gc:TitleMode">
	<xsl:apply-templates select="@rdf:about" mode="gc:LabelMode"/>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="gc:StyleMode">
	<link href="static/css/bootstrap.css" rel="stylesheet"/>
	<link href="static/css/bootstrap-responsive.css" rel="stylesheet"/>

	<style type="text/css">
	    <![CDATA[
		body { padding-top: 60px; padding-bottom: 40px; }
		form.form-inline { margin: 0; }
		ul.inline { margin-left: 0; }
		.inline li { display: inline; }
		.well-small { background-color: #FAFAFA; }
		.well-small dl { max-height: 60em; overflow-y: auto; }
		textarea#query-string { font-family: monospace; }
	    ]]>
	</style>	
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="gc:ScriptMode">
	<script type="text/javascript" src="static/js/InputMode.js"></script>
    </xsl:template>

    <xsl:template match="rdf:RDF">
	<div class="span8">
	    <xsl:choose>
		<xsl:when test="$mode = '&gc;ListMode'">
		    <xsl:apply-templates select="." mode="gc:ListMode"/>
		</xsl:when>
		<xsl:when test="$mode = '&gc;TableMode'">
		    <xsl:apply-templates select="." mode="gc:TableMode"/>
		</xsl:when>
		<xsl:when test="$mode = '&gc;InputMode'">
		    <!-- <xsl:apply-templates select="." mode="gc:StmtInputMode"/> -->
		    
		    <xsl:apply-templates select="." mode="gc:InputMode"/>
		</xsl:when>
		<xsl:when test="key('resources', $request-uri)/rdf:type/@rdf:resource = '&ldp;Page'">
		    <xsl:apply-templates select="." mode="gc:ListMode"/>
		</xsl:when>
		<xsl:otherwise>
		    <xsl:apply-templates select="key('resources', $absolute-path)"/>
		    <!-- apply all other URI resources -->
		    <xsl:apply-templates select="*[not(@rdf:about = $absolute-path)][not(key('predicates-by-object', @rdf:nodeID))]"/>
		</xsl:otherwise>
	    </xsl:choose>
	</div>
	
	<div class="span4">
	    <xsl:for-each-group select="*/*" group-by="concat(namespace-uri(), local-name())">
		<xsl:sort select="gc:label(xs:anyURI(concat(namespace-uri(), local-name())), /, $lang)" data-type="text" order="ascending" lang="{$lang}"/>
		<xsl:apply-templates select="current-group()[1]" mode="gc:SidebarNavMode"/>
	    </xsl:for-each-group>
	</div>
    </xsl:template>

    <xsl:template match="*" mode="gc:ModeSelectMode"/>
	
    <xsl:template match="sioc:Container | *[rdf:type/@rdf:resource = '&sioc;Container']" mode="gc:ModeSelectMode" priority="1">
	<ul class="nav nav-tabs">
	    <li>
		<xsl:if test="not($mode) or $mode = '&gc;ListMode'">
		    <xsl:attribute name="class">active</xsl:attribute>
		</xsl:if>

		<a href="{@rdf:about}{gc:query-string($offset, $limit, $order-by, $desc, '&gc;ListMode')}">
		    <xsl:apply-templates select="key('resources', '&gc;ListMode', document(''))/@rdf:about" mode="gc:LabelMode"/>
		</a>
	    </li>
	    <li>
		<xsl:if test="$mode = '&gc;TableMode'">
		    <xsl:attribute name="class">active</xsl:attribute>
		</xsl:if>

		<a href="{@rdf:about}{gc:query-string($offset, $limit, $order-by, $desc, '&gc;TableMode')}">
		    <xsl:apply-templates select="key('resources', '&gc;TableMode', document(''))/@rdf:about" mode="gc:LabelMode"/>
		</a>
	    </li>
	    <li>
		<xsl:if test="$mode = '&gc;InputMode'">
		    <xsl:attribute name="class">active</xsl:attribute>
		</xsl:if>

		<a href="{@rdf:about}{gc:query-string($offset, $limit, $order-by, $desc, '&gc;InputMode')}">
		    <xsl:apply-templates select="key('resources', '&gc;InputMode', document(''))/@rdf:about" mode="gc:LabelMode"/>
		</a>
	    </li>
	</ul>
    </xsl:template>

    <!-- TO-DO: make reusable with match="@rdf:about" - same as in gc:HeaderMode -->
    <xsl:template match="rdf:RDF" mode="gc:MediaTypeSelectMode">
	<div class="btn-group pull-right">
	    <!--
	    <xsl:if test="@rdf:about != $absolute-path">
		<a href="{@rdf:about}" class="btn">Source</a>
	    </xsl:if>
	    -->
	    <a href="{resolve-uri('sparql', $base-uri)}?query={encode-for-uri($query-res/sp:text)}" class="btn">SPARQL</a>
	    <a href="{$absolute-path}?accept={encode-for-uri('application/rdf+xml')}" class="btn">RDF/XML</a>
	    <a href="{$absolute-path}?accept={encode-for-uri('text/turtle')}" class="btn">Turtle</a>
	</div>
    </xsl:template>
    
    <!-- subject -->
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]">
	<xsl:apply-templates select="." mode="gc:HeaderMode"/>

	<xsl:variable name="type-containers" as="element()*">
	    <xsl:for-each-group select="*" group-by="if (not(empty(rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name())))))) then rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name()))) else xs:anyURI('&rdfs;Resource')">
		<xsl:sort select="gc:label(if (not(empty(rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name())))))) then rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name())))[1] else xs:anyURI('&rdfs;Resource'), /, $lang)"/>

		<div class="well well-small span6">
		    <h3>
			<span title="{@rdf:about}" class="btn">
			    <xsl:apply-templates select="key('resources', current-grouping-key(), document(gc:document-uri(current-grouping-key())))/@rdf:about"/>
			</span>
		    </h3>
		    <dl>
			<xsl:apply-templates select="current-group()" mode="gc:PropertyListMode">
			    <!-- <xsl:sort select="gc:label(xs:anyURI(concat(namespace-uri(), local-name())), $root, $lang)" data-type="text" order="ascending" lang="{$lang}"/> -->
			    <!-- <xsl:sort select="if (@rdf:resource) then (gc:label(@rdf:resource, $root, $lang)) else text()" data-type="text" order="ascending" lang="{$lang}"/> -->
			</xsl:apply-templates>
		    </dl>
		</div>
	    </xsl:for-each-group>
	</xsl:variable>

	<!-- group the class/property boxes into rows of 2 (to match fluid Bootstrap layout) -->
	<xsl:for-each-group select="$type-containers" group-adjacent="(position() - 1) idiv 2">
	    <div class="row-fluid">
		<xsl:copy-of select="current-group()"/>
	    </div>
	</xsl:for-each-group>
    </xsl:template>

    <!-- HEADER MODE -->
	
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:HeaderMode" priority="1">
	<div class="well">
	    <!--
	    <xsl:if test="self::ldp:Container or rdf:type/@rdf:resource = '&ldp;Container'">
		<div class="btn-group pull-right">
		    <a href="" class="btn">Create new</a>
		</div>
	    </xsl:if>
	    -->
	    
	    <xsl:apply-templates mode="gc:HeaderImageMode"/>

	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:HeaderMode"/>

	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:DescriptionMode"/>

	    <!-- xsl:apply-templates? -->
	    <xsl:if test="rdf:type">
		<ul class="inline">
		    <xsl:apply-templates select="rdf:type" mode="gc:HeaderMode">
			<xsl:sort select="gc:label(@rdf:resource | @rdf:nodeID, /, $lang)" data-type="text" order="ascending" lang="{$lang}"/>
		    </xsl:apply-templates>
		</ul>
	    </xsl:if>
	</div>
    </xsl:template>

    <!-- HEADER IMAGE MODE -->

    <!-- ignore all other properties -->
    <!-- <xsl:template match="*" mode="gc:HeaderImageMode"/> -->

    <!--
    <xsl:template match="foaf:img | foaf:depiction | foaf:thumbnail | foaf:logo" mode="gc:HeaderImageMode" priority="1">
	<p>
	    <xsl:apply-templates select="@rdf:resource"/>
	</p>
    </xsl:template>
    -->

    <!-- PROPERTY LIST MODE -->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:PropertyListMode" priority="1">
	<div class="well well-small">
	    <dl>
		<xsl:apply-templates mode="gc:PropertyListMode">
		    <xsl:sort select="gc:label(xs:anyURI(concat(namespace-uri(), local-name())), /, $lang)" data-type="text" order="ascending" lang="{$lang}"/>
		    <xsl:sort select="if (@rdf:resource) then (gc:label(@rdf:resource, /, $lang)) else text()" data-type="text" order="ascending" lang="{$lang}"/> <!-- gc:label(@rdf:nodeID, /, $lang) -->
		</xsl:apply-templates>
	    </dl>
	</div>
    </xsl:template>

    <!-- SIDEBAR NAV MODE -->
    
    <xsl:template match="*[@rdf:about]" mode="gc:SidebarNavMode">
	<xsl:apply-templates mode="gc:SidebarNavMode">
	    <xsl:sort select="gc:label(xs:anyURI(concat(namespace-uri(), local-name())), /, $lang)" data-type="text" order="ascending"/>
	</xsl:apply-templates>
    </xsl:template>

    <!-- PAGINATION MODE -->

    <xsl:template match="*[xhv:prev] | *[xhv:next]" mode="gc:PaginationMode">
	<xsl:param name="selected-resources" select="../*[not(@rdf:about = $absolute-path)][not(@rdf:about = $request-uri)][not(key('predicates-by-object', @rdf:nodeID))]"/>
	
	<!-- no need for pagination if the number of SELECTed resources is below $limit per page? -->
	<!-- <xsl:if test="count($selected-resources) = $limit"> -->
	    <ul class="pager">
		<li class="previous">
		    <xsl:choose>
			<xsl:when test="xhv:prev">
			    <a href="{ldp:pageOf/@rdf:resource}{gc:query-string($offset - $limit, $limit, $order-by, $desc, $mode)}" class="active">
				&#8592; <xsl:apply-templates select="key('resources', 'previous', document(''))/@rdf:nodeID" mode="gc:LabelMode"/>
			    </a>
			</xsl:when>
			<xsl:otherwise>
			    <xsl:attribute name="class">previous disabled</xsl:attribute>
			    <a>
				&#8592; <xsl:apply-templates select="key('resources', 'previous', document(''))/@rdf:nodeID" mode="gc:LabelMode"/>
			    </a>
			</xsl:otherwise>
		    </xsl:choose>
		</li>
		<li class="next">
		    <xsl:choose>
			<xsl:when test="xhv:next">
			    <!-- possible to add arrows by overriding -->
			    <a href="{ldp:pageOf/@rdf:resource}{gc:query-string($offset + $limit, $limit, $order-by, $desc, $mode)}">
				<xsl:apply-templates select="key('resources', 'next', document(''))/@rdf:nodeID" mode="gc:LabelMode"/> &#8594;
			    </a>
			</xsl:when>
			<xsl:otherwise>
			    <xsl:attribute name="class">next disabled</xsl:attribute>
			    <a>
				<xsl:apply-templates select="key('resources', 'next', document(''))/@rdf:nodeID" mode="gc:LabelMode"/> &#8594;
			    </a>
			</xsl:otherwise>
		    </xsl:choose>
		</li>
	    </ul>
	<!-- </xsl:if> -->
    </xsl:template>

    <!-- LIST MODE -->

    <xsl:template match="rdf:RDF" mode="gc:ListMode">
	<xsl:apply-templates select="key('resources', $absolute-path)" mode="gc:HeaderMode"/>
	
	<xsl:apply-templates select="key('resources', $absolute-path)" mode="gc:ModeSelectMode"/>

	<!-- page resource -->
	<xsl:apply-templates select="key('resources', $request-uri)" mode="gc:PaginationMode"/>

	<!-- all resources that are not recursive blank nodes, except page -->
	<xsl:apply-templates select="*[not(@rdf:about = $absolute-path)][not(@rdf:about = $request-uri)][not(key('predicates-by-object', @rdf:nodeID))]" mode="gc:ListMode"/>
	
	<!-- page resource -->
	<xsl:apply-templates select="key('resources', $request-uri)" mode="gc:PaginationMode"/>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:ListMode">
	<div class="well">
	    <xsl:apply-templates mode="gc:ListImageMode"/>

	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:HeaderMode"/>
	    
	    <xsl:if test="rdf:type">
		<ul class="inline">
		    <xsl:apply-templates select="rdf:type" mode="gc:HeaderMode">
			<xsl:sort select="gc:label(@rdf:resource | @rdf:nodeID, /, $lang)" data-type="text" order="ascending" lang="{$lang}"/>
		    </xsl:apply-templates>
		</ul>
	    </xsl:if>

	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:DescriptionMode"/>

	    <xsl:if test="@rdf:nodeID">
		<xsl:apply-templates select="." mode="gc:PropertyListMode"/>
	    </xsl:if>
	</div>
    </xsl:template>

    <!-- ignore all other properties -->
    <!-- <xsl:template match="*" mode="gc:ListImageMode"/> -->

    <!--
    <xsl:template match="foaf:img | foaf:depiction | foaf:thumbnail | foaf:logo" mode="gc:ListImageMode" priority="1">
	<p>
	    <xsl:apply-templates select="@rdf:resource"/>
	</p>
    </xsl:template>
    -->

    <!-- TABLE MODE -->

    <xsl:template match="rdf:RDF" mode="gc:TableMode">
	<xsl:apply-templates select="key('resources', $absolute-path)" mode="gc:HeaderMode"/>

	<xsl:apply-templates select="key('resources', $absolute-path)" mode="gc:ModeSelectMode"/>

	<!-- page resource -->
	<xsl:apply-templates select="key('resources', $request-uri)" mode="gc:PaginationMode"/>

	<!-- SELECTed resources = everything except container, page, and non-root blank nodes -->
	<xsl:variable name="selected-resources" select="*[not(@rdf:about = $absolute-path)][not(@rdf:about = $request-uri)][not(key('predicates-by-object', @rdf:nodeID))]"/>
	<xsl:variable name="predicates" as="element()*">
	    <xsl:for-each-group select="$selected-resources/*" group-by="concat(namespace-uri(), local-name())">
		<xsl:sort select="gc:label(xs:anyURI(concat(namespace-uri(), local-name())), /, $lang)" data-type="text" order="ascending" lang="{$lang}"/>
		<xsl:sequence select="current-group()[1]"/>
	    </xsl:for-each-group>
	</xsl:variable>

	<table class="table table-bordered table-striped">
	    <thead>
		<tr>
		    <th>
			<a href="{$absolute-path}{gc:query-string($offset, $limit, (), $desc, $mode)}">
			    <xsl:apply-templates select="key('resources', '&rdfs;Resource', document('&rdfs;'))/@rdf:about" mode="gc:LabelMode"/>
			</a>
		    </th>

		    <xsl:apply-templates select="$predicates" mode="gc:TableHeaderMode"/>
		</tr>
	    </thead>
	    <tbody>
		<xsl:apply-templates select="$selected-resources" mode="gc:TableMode">
		    <xsl:with-param name="predicates" select="$predicates"/>
		</xsl:apply-templates>
	    </tbody>
	</table>
	
	<!-- page resource -->
	<xsl:apply-templates select="key('resources', $request-uri)" mode="gc:PaginationMode"/>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:TableMode">
	<xsl:param name="predicates" as="element()*"/>

	<tr>
	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:TableMode"/>

	    <xsl:variable name="subject" select="."/>
	    <xsl:for-each select="$predicates">
		<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(), local-name()))" as="xs:anyURI"/>
		<xsl:variable name="predicate" select="$subject/*[concat(namespace-uri(), local-name()) = $this]"/>
		<xsl:choose>
		    <xsl:when test="$predicate">
			<xsl:apply-templates select="$predicate" mode="gc:TableMode"/>
		    </xsl:when>
		    <xsl:otherwise>
			<td></td>
		    </xsl:otherwise>
		</xsl:choose>
	    </xsl:for-each>
	</tr>
    </xsl:template>

    <!-- INPUT MODE -->
    
    <xsl:template match="rdf:RDF" mode="gc:InputMode">
	<xsl:apply-templates select="key('resources', $absolute-path)" mode="gc:HeaderMode"/>

	<xsl:apply-templates select="key('resources', $absolute-path)" mode="gc:ModeSelectMode"/>

	<!-- page resource -->
	<xsl:apply-templates select="key('resources', $request-uri)" mode="gc:PaginationMode"/>

	<form class="form-horizontal" method="post" action="">
	    <xsl:comment>This form uses RDF/POST encoding: http://www.lsrn.org/semweb/rdfpost.html</xsl:comment>
	    <xsl:call-template name="gc:InputTemplate">
		<xsl:with-param name="name" select="'rdf'"/>
		<xsl:with-param name="type" select="'hidden'"/>
	    </xsl:call-template>

	    <xsl:variable name="selected-resources" select="*[not(@rdf:about = $absolute-path)][not(@rdf:about = $request-uri)][not(key('predicates-by-object', @rdf:nodeID))]"/>
	    <xsl:apply-templates select="$selected-resources" mode="gc:InputMode"/>
	    
	    <fieldset id="fieldset-new-stmt">
		<legend>
		    <button type="button" class="btn pull-right" title="Remove this statement" style="display: none;">&#x2715;</button>
		    New statement
		</legend>

		<xsl:call-template name="gc:StmtInputTemplate">
		    <xsl:with-param name="stmt-id" select="'new-stmt'"/>
		    <xsl:with-param name="su-value" select="''"/>
		    <xsl:with-param name="sb-value" select="''"/>
		    <xsl:with-param name="pu-value" select="''"/>
		    <xsl:with-param name="ou-value" select="''"/>
		    <xsl:with-param name="ob-value" select="''"/>
		    <xsl:with-param name="ol-value" select="''"/>
		    <xsl:with-param name="ll-value" select="''"/>
		    <xsl:with-param name="lt-value" select="''"/>
		</xsl:call-template>		
	    </fieldset>
	    
	    <fieldset>
		<div class="control-group">
		    <button type="button" class="btn" title="Add new statement" onclick="this.parentNode.parentNode.parentNode.insertBefore(cloneUniqueStmt(document.getElementById('fieldset-new-stmt').cloneNode(true), generateUUID()), this.parentNode.parentNode);">&#x271A;</button>
		</div>
	    </fieldset>

	    <div class="form-actions">
		<button type="submit" class="btn btn-primary">Post</button>
	    </div>
	</form>
	
	<!-- page resource -->
	<xsl:apply-templates select="key('resources', $request-uri)" mode="gc:PaginationMode"/>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:InputMode">
	<fieldset id="fieldset-{generate-id()}">
	    <legend>
		<button type="button" class="btn pull-right" title="Remove this resource" onclick="document.getElementById('fieldset-{generate-id()}').style.display = 'none';">&#x2715;</button>
		<xsl:apply-templates select="@rdf:about | @rdf:nodeID"/>
	    </legend>
		
	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:InputMode">
		<xsl:with-param name="type" select="'hidden'"/>
	    </xsl:apply-templates>

	    <xsl:for-each-group select="*" group-by="concat(namespace-uri(), local-name())">
		<xsl:apply-templates select="current-group()" mode="gc:InputMode"/>
	    </xsl:for-each-group>

	    <!--
	    <div class="control-group">
		<div class="control-label">
		    <button type="button" class="btn" title="Add new property">&#x271A;</button>
		</div>
	    </div>
	    
	    <div class="control-group">
		<label class="control-label">Property</label>

		<div class="controls">
		    <xsl:call-template name="gc:InputTemplate">
			<xsl:with-param name="name" select="'pu'"/>
			<xsl:with-param name="class" select="'input-xxlarge'"/>
		    </xsl:call-template>
		    <span class="help-inline">URI</span>
		</div>
	    </div>
	    <div class="control-group">
		<label class="control-label">Object</label>

		<div class="controls">
		    <xsl:call-template name="gc:ObjectInputTemplate">
			<xsl:with-param name="stmt-id" select="'xxx'"/>
			<xsl:with-param name="ou-value" select="''"/>
			<xsl:with-param name="ob-value" select="''"/>
			<xsl:with-param name="ol-value" select="''"/>
			<xsl:with-param name="ll-value" select="''"/>
			<xsl:with-param name="lt-value" select="''"/>
		    </xsl:call-template>
		</div>
	    </div>
	    -->
	</fieldset>
    </xsl:template>

    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:InputMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(), local-name()))" as="xs:anyURI"/>
	<xsl:variable name="property" select="key('resources', $this, document(namespace-uri()))"/>

	<div class="control-group" id="control-group-{generate-id()}">
	    <xsl:if test="not(preceding-sibling::*[concat(namespace-uri(), local-name()) = $this])">
		<label class="control-label" title="{$property/rdfs:comment}">
		    <xsl:apply-templates select="."/>
		</label>
	    </xsl:if>

	    <!--
	    <xsl:apply-imports>
		<xsl:with-param name="type" select="'hidden'"/>
	    </xsl:apply-imports>
	    -->
	    <xsl:call-template name="gc:InputTemplate">
		<xsl:with-param name="name" select="'pu'"/>
		<xsl:with-param name="type" select="'hidden'"/>
		<xsl:with-param name="value" select="$this"/>
	    </xsl:call-template>

	    <button type="button" class="btn btn-small pull-right" title="Remove this object node" onclick="removeObject('{generate-id()}');">&#x2715;</button>

	    <div class="controls" id="controls-{generate-id()}">
		<xsl:apply-templates select="text() | @rdf:resource | @rdf:nodeID" mode="gc:StmtInputMode"/>
	    </div>
	</div>
	
	<xsl:if test="not(following-sibling::*[concat(namespace-uri(), local-name()) = $this])">
	    <div class="control-group">
		<div class="controls">
		    <button type="button" class="btn btn-small" title="Add new object" onclick="this.parentNode.parentNode.parentNode.insertBefore(cloneUniqueObject(document.getElementById('control-group-{generate-id()}').cloneNode(true), generateUUID()), this.parentNode.parentNode);">&#x271A;</button>
		</div>
	    </div>
	</xsl:if>
    </xsl:template>

    <!-- model -->
    <xsl:template match="rdf:RDF" mode="gc:StmtInputMode">
	<xsl:apply-templates select="key('resources', $absolute-path)" mode="gc:HeaderMode"/>

	<xsl:apply-templates select="key('resources', $absolute-path)" mode="gc:ModeSelectMode"/>

	<form class="form-horizontal" method="post" action="">
	    <xsl:comment>This form uses RDF/POST encodingc: http://www.lsrn.org/semweb/rdfpost.html</xsl:comment>
	    <xsl:call-template name="gc:InputTemplate">
		<xsl:with-param name="name" select="'rdf'"/>
		<xsl:with-param name="type" select="'hidden'"/>
	    </xsl:call-template>

	    <xsl:for-each select="*/*">
		<xsl:variable name="stmt-id" select="generate-id()" as="xs:string"/>

		<fieldset id="fieldset-{$stmt-id}">
		    <legend>
			<button type="button" class="btn pull-right" onclick="document.getElementById('fieldset-{$stmt-id}').style.display = 'none';">&#x2715;</button>
			Statement <xsl:number level="any" count="/rdf:RDF/*/*"/>
		    </legend>

		    <div class="control-group">
			<label class="control-label">Subject</label>

			<div class="controls">
			    <xsl:apply-templates select="../@rdf:about | ../@rdf:nodeID" mode="gc:StmtInputMode">
				<xsl:with-param name="stmt-id" select="$stmt-id"/>
			    </xsl:apply-templates>
			</div>
		    </div>	    

		    <div class="control-group">
			<label class="control-label">Property</label>

			<div class="controls">
			    <xsl:apply-templates select="." mode="gc:InputMode"/>
			    <span class="help-inline">URI</span>
			</div>
		    </div>

		    <div class="control-group">
			<label class="control-label">Object</label>

			<div class="controls">
			    <xsl:apply-templates select="text() | @rdf:resource | @rdf:nodeID" mode="gc:StmtInputMode"/>
			</div>
		    </div>
		</fieldset>
	    </xsl:for-each>
	    <!-- <xsl:apply-templates select="*/*" mode="gc:StmtInputMode"/> -->
	    
	    <div class="form-actions">
		<button type="submit" class="btn btn-primary">Post</button>
	    </div>
	</form>
    </xsl:template>

    <xsl:template match="@rdf:about | @rdf:nodeID" mode="gc:StmtInputMode" name="gc:SubjectInputTemplate">
	<xsl:param name="stmt-id" as="xs:string"/>
	<xsl:param name="su-value" select="../@rdf:about" as="xs:string?"/>
	<xsl:param name="sb-value" select="../@rdf:nodeID" as="xs:string?"/>
	<xsl:param name="active-tab" as="xs:string">
	    <xsl:choose>
		<xsl:when test="$sb-value">sb</xsl:when>
		<xsl:otherwise>su</xsl:otherwise>
	    </xsl:choose>
	</xsl:param>

	<ul class="nav nav-tabs">
	    <li id="li-su-{$stmt-id}" onclick="this.className = 'active'; document.getElementById('li-sb-{$stmt-id}').className = ''; document.getElementById('div-su-{$stmt-id}').style.display = 'block'; document.getElementById('div-sb-{$stmt-id}').style.display = 'none';">
		<xsl:if test="$active-tab = 'su'">
		    <xsl:attribute name="class">active</xsl:attribute>
		</xsl:if>
		
		<a id="a-su-{$stmt-id}">Resource</a>
	    </li>
	    <li id="li-sb-{$stmt-id}" onclick="this.className = 'active'; document.getElementById('li-su-{$stmt-id}').className = ''; document.getElementById('div-sb-{$stmt-id}').style.display = 'block'; document.getElementById('div-su-{$stmt-id}').style.display = 'none';">
		<xsl:if test="$active-tab = 'sb'">
		    <xsl:attribute name="class">active</xsl:attribute>
		</xsl:if>			
		
		<a id="a-sb-{$stmt-id}">Blank node</a>
	    </li>
	</ul>

	<div id="div-su-{$stmt-id}">
	    <xsl:if test="not($active-tab = 'su')">
		<xsl:attribute name="style">display: none;</xsl:attribute>
	    </xsl:if>

	    <xsl:call-template name="gc:InputTemplate">
		<xsl:with-param name="name" select="'su'"/>
		<!-- <xsl:with-param name="id" select="$id"/> -->
		<xsl:with-param name="class" select="'input-xxlarge'"/>
		<xsl:with-param name="value" select="$su-value"/>
	    </xsl:call-template>
	    <span class="help-inline">URI</span>
	</div>		
	<div id="div-sb-{$stmt-id}">
	    <xsl:if test="not($active-tab = 'sb')">
		<xsl:attribute name="style">display: none;</xsl:attribute>
	    </xsl:if>

	    <xsl:call-template name="gc:InputTemplate">
		<xsl:with-param name="name" select="'sb'"/>
		<!-- <xsl:with-param name="id" select="$id"/> -->
		<xsl:with-param name="value" select="$sb-value"/>
	    </xsl:call-template>
	    <span class="help-inline">ID</span>
	</div>
    </xsl:template>

    <!-- resource statements -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:StmtInputMode" name="gc:StmtInputTemplate">
	<xsl:param name="stmt-id" select="generate-id()" as="xs:string"/>
	<xsl:param name="su-value" select="../@rdf:about" as="xs:string?"/>
	<xsl:param name="sb-value" select="../@rdf:nodeID" as="xs:string?"/>
	<xsl:param name="pu-value" select="concat(namespace-uri(), local-name())" as="xs:string?"/>
	<xsl:param name="ou-value" select="@rdf:resource" as="xs:string?"/>
	<xsl:param name="ob-value" select="@rdf:nodeID" as="xs:string?"/>
	<xsl:param name="ol-value" select="text()" as="xs:string?"/>
	<xsl:param name="ll-value" select="@xml:lang" as="xs:string?"/>
	<xsl:param name="lt-value" select="@rdf:datatype" as="xs:string?"/>

	<div class="control-group">
	    <label class="control-label">Subject</label>

	    <div class="controls">
		<xsl:call-template name="gc:SubjectInputTemplate">
		    <xsl:with-param name="stmt-id" select="$stmt-id"/>
		    <xsl:with-param name="su-value" select="$su-value"/>
		    <xsl:with-param name="sb-value" select="$sb-value"/>
		</xsl:call-template>
	    </div>
	</div>

	<div class="control-group">
	    <label class="control-label">Property</label>

	    <div class="controls">
		<xsl:call-template name="gc:InputTemplate">
		    <xsl:with-param name="name" select="'pu'"/>
		    <!-- <xsl:with-param name="id" select="$id"/> -->
		    <xsl:with-param name="class" select="'input-xxlarge'"/>
		    <xsl:with-param name="value" select="$pu-value"/>
		</xsl:call-template>
		<span class="help-inline">URI</span>
	    </div>
	</div>

	<div class="control-group">
	    <label class="control-label">Object</label>

	    <div class="controls">
		<xsl:call-template name="gc:ObjectInputTemplate">
		    <xsl:with-param name="stmt-id" select="$stmt-id"/>
		    <xsl:with-param name="ou-value" select="$ou-value"/>
		    <xsl:with-param name="ob-value" select="$ob-value"/>
		    <xsl:with-param name="ol-value" select="$ol-value"/>
		    <xsl:with-param name="ll-value" select="$ll-value"/>
		    <xsl:with-param name="lt-value" select="$lt-value"/>
		</xsl:call-template>
	    </div>
	</div>
    </xsl:template>

    <xsl:template match="text() | *[@rdf:about or @rdf:nodeID]/*/@rdf:resource | *[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID" mode="gc:StmtInputMode" name="gc:ObjectInputTemplate">
	<xsl:param name="stmt-id" select="generate-id(..)" as="xs:string"/>
	<xsl:param name="ou-value" select="../@rdf:resource" as="xs:string?"/>
	<xsl:param name="ob-value" select="../@rdf:nodeID" as="xs:string?"/>
	<xsl:param name="ol-value" select="../text()" as="xs:string?"/>
	<xsl:param name="ll-value" select="../@xml:lang" as="xs:string?"/>
	<xsl:param name="lt-value" select="../@rdf:datatype" as="xs:string?"/>
	<xsl:param name="active-tab" as="xs:string">
	    <xsl:choose>
		<xsl:when test="$ob-value">ob</xsl:when>
		<xsl:when test="$ol-value">ol</xsl:when>
		<xsl:otherwise>ou</xsl:otherwise>
	    </xsl:choose>
	</xsl:param>

	<ul class="nav nav-tabs">
	    <li id="li-ou-{$stmt-id}" onclick="toggleObjectTabs('ou', '{$stmt-id}');">
		<xsl:if test="$active-tab = 'ou'">
		    <xsl:attribute name="class">active</xsl:attribute>
		</xsl:if>

		<a id="a-ou-{$stmt-id}">Resource</a>
	    </li>
	    <li id="li-ob-{$stmt-id}" onclick="toggleObjectTabs('ob', '{$stmt-id}');">
		<xsl:if test="$active-tab = 'ob'">
		    <xsl:attribute name="class">active</xsl:attribute>
		</xsl:if>

		<a id="a-ob-{$stmt-id}">Blank node</a>
	    </li>
	    <li id="li-ol-{$stmt-id}" onclick="toggleObjectTabs('ol', '{$stmt-id}');">
		<xsl:if test="$active-tab = 'ol'">
		    <xsl:attribute name="class">active</xsl:attribute>
		</xsl:if>

		<a id="a-ol-{$stmt-id}">Literal</a>
	    </li>
	</ul>

	<div id="div-ou-{$stmt-id}">
	    <xsl:if test="not($active-tab = 'ou')">
		<xsl:attribute name="style">display: none;</xsl:attribute>
	    </xsl:if>

	    <xsl:call-template name="gc:InputTemplate">
		<xsl:with-param name="name" select="'ou'"/>
		<!-- <xsl:with-param name="id" select="$id"/> -->
		<xsl:with-param name="class" select="'input-xxlarge'"/>
		<xsl:with-param name="value" select="$ou-value"/>
	    </xsl:call-template>
	    <span class="help-inline">URI</span>
	</div>
	<div id="div-ob-{$stmt-id}">
	    <xsl:if test="not($active-tab = 'ob')">
		<xsl:attribute name="style">display: none;</xsl:attribute>
	    </xsl:if>

	    <xsl:variable name="bnode" select="key('resources', $ob-value)[not(@rdf:nodeID = current()/../../@rdf:nodeID)]"/> <!-- [not(. is current())] -->	    
	    <xsl:choose>
		<xsl:when test="$bnode">
		    <xsl:apply-templates select="$bnode/*" mode="gc:InputMode"/>
		</xsl:when>
		<xsl:otherwise>
		    <xsl:call-template name="gc:InputTemplate">
			<xsl:with-param name="name" select="'ob'"/>
			<xsl:with-param name="value" select="$ob-value"/>
		    </xsl:call-template>
		    <span class="help-inline">ID</span>
		</xsl:otherwise>
	    </xsl:choose>
	</div>
	<div id="div-ol-{$stmt-id}">
	    <xsl:if test="not($active-tab = 'ol')">
		<xsl:attribute name="style">display: none;</xsl:attribute>
	    </xsl:if>

	    <xsl:call-template name="gc:InputTemplate">
		<xsl:with-param name="name" select="'ol'"/>
		<!-- <xsl:with-param name="id" select="$id"/> -->
		<xsl:with-param name="class" select="'input-block-level'"/>
		<xsl:with-param name="value" select="$ol-value"/>
	    </xsl:call-template>
	    <div class="controls-row">
		<xsl:call-template name="gc:InputTemplate">
		    <xsl:with-param name="name" select="'ll'"/>
		    <!-- <xsl:with-param name="id" select="$id"/> -->
		    <xsl:with-param name="class" select="'span2'"/>
		    <xsl:with-param name="value" select="$ll-value"/>
		</xsl:call-template>
		<span class="help-inline span3">Language tag</span>
		<xsl:call-template name="gc:InputTemplate">
		    <xsl:with-param name="name" select="'lt'"/>
		    <!-- <xsl:with-param name="id" select="$id"/> -->
		    <xsl:with-param name="class" select="'span4'"/>
		    <xsl:with-param name="value" select="$lt-value"/>
		</xsl:call-template>
		<span class="help-inline span3">Datatype URI</span>
	    </div>
	</div>	
    </xsl:template>

</xsl:stylesheet>