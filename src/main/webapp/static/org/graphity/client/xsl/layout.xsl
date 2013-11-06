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
    <!ENTITY dct "http://purl.org/dc/terms/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY sioc "http://rdfs.org/sioc/ns#">
    <!ENTITY sp "http://spinrdf.org/sp#">
    <!ENTITY spin "http://spinrdf.org/spin#">
    <!ENTITY void "http://rdfs.org/ns/void#">
    <!ENTITY list "http://jena.hpl.hp.com/ARQ/list#">
    <!ENTITY xhv "http://www.w3.org/1999/xhtml/vocab#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:sparql="&sparql;"
xmlns:ldp="&ldp;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:sp="&sp;"
xmlns:spin="&spin;"
xmlns:void="&void;"
xmlns:list="&list;"
xmlns:xhv="&xhv;"
xmlns:uuid="java:java.util.UUID"
xmlns:url="&java;java.net.URLDecoder"
xmlns:javaee="http://java.sun.com/xml/ns/javaee"
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
    <xsl:param name="graph" as="xs:anyURI?"/>
    <xsl:param name="mode" as="xs:anyURI?"/>
    <xsl:param name="ont-model" select="/" as="document-node()"/> <!-- select="document($base-uri)"  -->
    <xsl:param name="offset" select="$select-res/sp:offset" as="xs:integer?"/>
    <xsl:param name="limit" select="$select-res/sp:limit" as="xs:integer?"/>
    <xsl:param name="order-by" select="$orderBy/sp:varName | key('resources', $orderBy/sp:*/@rdf:nodeID, $ont-model)/sp:varName | key('resources', key('resources', $orderBy/sp:expression/@rdf:nodeID, $ont-model)/sp:*/@rdf:nodeID, $ont-model)/sp:varName" as="xs:string?"/>
    <xsl:param name="desc" select="$orderBy[1]/rdf:type/@rdf:resource = '&sp;Desc'" as="xs:boolean"/>
    <xsl:param name="endpoint-uri" as="xs:anyURI?"/>
    <xsl:param name="query" as="xs:string?"/>

    <xsl:variable name="resource" select="key('resources', $absolute-path, $ont-model)" as="element()?"/>
    <xsl:variable name="query-res" select="key('resources', $resource/spin:query/@rdf:resource | $resource/spin:query/@rdf:nodeID, $ont-model)" as="element()?"/>
    <xsl:variable name="where-res" select="list:member(key('resources', $query-res/sp:where/@rdf:nodeID, $ont-model), $ont-model)"/>
    <xsl:variable name="select-res" select="key('resources', $where-res/sp:query/@rdf:resource | $where-res/sp:query/@rdf:nodeID, $ont-model)" as="element()?"/>
    <xsl:variable name="orderBy" select="if ($select-res/sp:orderBy) then list:member(key('resources', $select-res/sp:orderBy/@rdf:nodeID, $ont-model), $ont-model) else ()"/>
    <xsl:variable name="ontology-uri" select="xs:anyURI(key('resources-by-type', '&owl;Ontology', $ont-model)/@rdf:about)" as="xs:anyURI"/>
    <xsl:variable name="config" select="document('../../../../../WEB-INF/web.xml')" as="document-node()"/>

    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
    <xsl:key name="predicates" match="*[@rdf:about]/* | *[@rdf:nodeID]/*" use="concat(namespace-uri(), local-name())"/>
    <xsl:key name="predicates-by-object" match="*[@rdf:about]/* | *[@rdf:nodeID]/*" use="@rdf:resource | @rdf:nodeID"/>
    <xsl:key name="resources-by-type" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="rdf:type/@rdf:resource"/>
    <xsl:key name="resources-by-space" match="*[@rdf:about]" use="sioc:has_space/@rdf:resource"/>
    <xsl:key name="resources-by-page-of" match="*[@rdf:about]" use="ldp:pageOf/@rdf:resource"/>
    <xsl:key name="resources-by-topic" match="*[@rdf:about] | *[@rdf:nodeID]" use="foaf:primaryTopic/@rdf:resource"/>
    <xsl:key name="resources-by-topic-of" match="*[@rdf:about] | *[@rdf:nodeID]" use="foaf:isPrimaryTopicOf/@rdf:resource"/>
    <xsl:key name="violations-by-path" match="*" use="spin:violationPath/@rdf:resource"/>
    <xsl:key name="init-param-by-name" match="javaee:init-param" use="javaee:param-name"/>

    <rdf:Description rdf:about="">
	<dct:creator rdf:resource="http://semantic-web.dk/#martynas"/>
    </rdf:Description>

    <rdf:Description rdf:nodeID="previous">
	<rdfs:label xml:lang="en">Previous</rdfs:label>
    </rdf:Description>

    <rdf:Description rdf:nodeID="next">
	<rdfs:label xml:lang="en">Next</rdfs:label>
    </rdf:Description>

    <rdf:Description rdf:about="&gc;PropertyListMode">
	<rdf:type rdf:resource="&gc;Mode"/>
	<rdf:type rdf:resource="&gc;ItemMode"/>
	<rdf:type rdf:resource="&gc;PageMode"/>
	<rdfs:label xml:lang="en-US">Properties</rdfs:label>
    </rdf:Description>

    <rdf:Description rdf:about="&gc;TableMode">
	<rdf:type rdf:resource="&gc;Mode"/>
	<rdf:type rdf:resource="&gc;PageMode"/>
	<rdfs:label xml:lang="en-US">Table</rdfs:label>
    </rdf:Description>

    <rdf:Description rdf:about="&gc;ListMode">
	<rdf:type rdf:resource="&gc;Mode"/>
	<!-- <rdf:type rdf:resource="&gc;ItemMode"/> -->
	<rdf:type rdf:resource="&gc;PageMode"/>
	<rdfs:label xml:lang="en-US">List</rdfs:label>
    </rdf:Description>

    <rdf:Description rdf:about="&gc;ThumbnailMode">
	<rdf:type rdf:resource="&gc;Mode"/>
	<rdf:type rdf:resource="&gc;PageMode"/>
	<rdfs:label xml:lang="en-US">Gallery</rdfs:label>
    </rdf:Description>

    <rdf:Description rdf:about="&gc;EditMode">
	<rdf:type rdf:resource="&gc;Mode"/>
	<rdf:type rdf:resource="&gc;ItemMode"/>
	<rdfs:label xml:lang="en-US">Edit</rdfs:label>
    </rdf:Description>

    <rdf:Description rdf:about="&gc;CreateMode">
	<rdf:type rdf:resource="&gc;Mode"/>
	<rdfs:label xml:lang="en-US">Create</rdfs:label>
    </rdf:Description>

    <xsl:template match="/">
	<html>
	    <head>
		<xsl:apply-templates select="." mode="gc:HeadMode"/>
      	    </head>
	    <body>
		<xsl:apply-templates select="." mode="gc:BodyMode"/>
	    </body>
	</html>
    </xsl:template>

    <xsl:template match="/" mode="gc:HeadMode">
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
    </xsl:template>
    
    <xsl:template match="/" mode="gc:BodyMode">
	<div class="navbar navbar-fixed-top">
	    <div class="navbar-inner">
		<div class="container-fluid">    
		    <xsl:apply-templates select="." mode="gc:HeaderMode"/>
		</div>
	    </div>
	</div>

	<div class="container-fluid">
	    <div class="row-fluid">
		<div class="span8">
		    <xsl:variable name="grouped-rdf" as="document-node()">
			<xsl:apply-templates select="." mode="gc:GroupTriples"/>
		    </xsl:variable>
		    <xsl:apply-templates select="$grouped-rdf/rdf:RDF"/>
		</div>

		<div class="span4">
		    <xsl:apply-templates select="." mode="gc:SidebarNavMode"/>
		</div>
	    </div>		    

	    <div class="footer">
		<xsl:apply-templates select="." mode="gc:FooterMode"/>
	    </div>
	</div>
    </xsl:template>
    
    <xsl:template match="/" mode="gc:HeaderMode">
	<button class="btn btn-navbar" onclick="if ($('#collapsing-navbar').hasClass('in')) $('#collapsing-navbar').removeClass('collapse in').height(0); else $('#collapsing-navbar').addClass('collapse in').height('auto');">
	    <span class="icon-bar"></span>
	    <span class="icon-bar"></span>
	    <span class="icon-bar"></span>
	</button>

	<a class="brand" href="{$base-uri}">
	    <xsl:for-each select="key('resources', $base-uri, $ont-model)/@rdf:about">
		<img src="{../foaf:logo/@rdf:resource}">
		    <xsl:attribute name="alt"><xsl:apply-templates select="." mode="gc:LabelMode"/></xsl:attribute>
		</img>
	    </xsl:for-each>
	</a>

	<div id="collapsing-navbar" class="nav-collapse collapse">
	    <ul class="nav">
		<!-- make menu links for all resources in the ontology, except base URI -->
		<xsl:for-each select="key('resources-by-space', $base-uri, $ont-model)/@rdf:about[not(. = $base-uri)]">
		    <xsl:sort select="gc:label(.)" data-type="text" order="ascending" lang="{$lang}"/>
		    <li>
			<xsl:if test=". = $absolute-path">
			    <xsl:attribute name="class">active</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="."/>
		    </li>
		</xsl:for-each>
	    </ul>

	    <xsl:if test="key('resources', $base-uri, $ont-model)/rdfs:isDefinedBy/@rdf:resource | key('resources', key('resources', $base-uri, $ont-model)/void:inDataset/@rdf:resource, $ont-model)/void:sparqlEndpoint/@rdf:resource">
		<ul class="nav pull-right">
		    <xsl:for-each select="key('resources', $base-uri, $ont-model)/rdfs:isDefinedBy/@rdf:resource | key('resources', key('resources', $base-uri, $ont-model)/void:inDataset/@rdf:resource, $ont-model)/void:sparqlEndpoint/@rdf:resource">
			<xsl:sort select="gc:label(.)" data-type="text" order="ascending" lang="{$lang}"/>
			<li>
			    <xsl:if test=". = $absolute-path">
				<xsl:attribute name="class">active</xsl:attribute>
			    </xsl:if>
			    <xsl:apply-templates select="."/>
			</li>
		    </xsl:for-each>
		</ul>
	    </xsl:if>
	</div>
    </xsl:template>

    <xsl:template match="/" mode="gc:FooterMode">
	<p>
	    <xsl:value-of select="format-date(current-date(), '[Y]', $lang, (), ())"/>
	</p>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="gc:TitleMode">
	<xsl:apply-templates select="key('resources', $base-uri, $ont-model)/@rdf:about" mode="gc:LabelMode"/>
	<xsl:text> - </xsl:text>
	<xsl:apply-templates select="key('resources', $absolute-path)/@rdf:about" mode="gc:LabelMode"/>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="gc:StyleMode">
	<link href="static/css/bootstrap.css" rel="stylesheet"/>
	<link href="static/css/bootstrap-responsive.css" rel="stylesheet"/>

	<style type="text/css">
	    <![CDATA[
		body { padding-top: 60px; padding-bottom: 40px; }
		.brand img { height: 0.8em; width: auto; }
		form.form-inline { margin: 0; }
		ul.inline { margin-left: 0; }
		.inline li { display: inline; }
		.well-small { background-color: #FAFAFA; }
		.well-small dl { max-height: 60em; overflow-y: auto; }
		textarea#query-string { font-family: monospace; }
		.thumbnail img { display: block; margin: auto; }
		.thumbnail { min-height: 15em; }
		
                ul.typeahead { max-height: 20em; overflow: auto; }
                label.typeahead input { display: block; max-width: 160px; }
		label.typeahead { float: left; width: 160px; }
	    ]]>
	</style>	
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="gc:ScriptMode">
	<script type="text/javascript" src="static/org/graphity/client/js/jquery.min.js"></script>
	<script type="text/javascript" src="static/org/graphity/client/js/bootstrap.js"></script>
        <xsl:if test="$mode = ('&gc;CreateMode', '&gc;EditMode')">
            <script type="text/javascript" src="static/org/graphity/client/js/UriBuilder.js"></script>
            <script type="text/javascript" src="static/org/graphity/client/js/saxon-ce/Saxonce.nocache.js"></script>
            <script type="text/javascript" src="static/org/graphity/client/js/UUID.js"></script>
            <script type="text/javascript" src="static/org/graphity/client/js/InputMode.js"></script>
            <script type="text/javascript">
                <![CDATA[
                    var baseUri = "]]><xsl:value-of select="$base-uri"/><![CDATA[";
                    var stylesheetUri = UriBuilder.fromUri(baseUri).
                            path("static/org/graphity/client/xsl/InputMode.xsl").
                            build();

                    var onSaxonLoad = function() { Saxon.run( { stylesheet: stylesheetUri,
                        parameters: { "base-uri-string": baseUri,
                            "absolute-path-string": "]]><xsl:value-of select="$absolute-path"/><![CDATA[",
                            "mode-string": "]]><xsl:value-of select="$mode"/><![CDATA[" },
                        initialTemplate: "main", logLevel: "FINE"
                    }); }
                ]]>
            </script>
        </xsl:if>
    </xsl:template>

    <xsl:template match="rdf:RDF">
	<xsl:variable name="default-mode" select="if (key('resources-by-page-of', $absolute-path)) then xs:anyURI('&gc;ListMode') else xs:anyURI('&gc;PropertyListMode')"/>

	<xsl:choose>
	    <xsl:when test="(not($mode) and $default-mode = '&gc;ListMode') or $mode = '&gc;ListMode'">
		<xsl:apply-templates select="." mode="gc:ListMode">
		    <xsl:with-param name="default-mode" select="$default-mode" tunnel="yes"/>
		</xsl:apply-templates>
	    </xsl:when>
	    <xsl:when test="(not($mode) and $default-mode = '&gc;TableMode') or $mode = '&gc;TableMode'">
		<xsl:apply-templates select="." mode="gc:TableMode">
		    <xsl:with-param name="default-mode" select="$default-mode" tunnel="yes"/>
		</xsl:apply-templates>
	    </xsl:when>
	    <xsl:when test="(not($mode) and $default-mode = '&gc;ThumbnailMode') or $mode = '&gc;ThumbnailMode'">
		<xsl:apply-templates select="." mode="gc:ThumbnailMode">
		    <xsl:with-param name="default-mode" select="$default-mode" tunnel="yes"/>
		</xsl:apply-templates>
	    </xsl:when>
	    <xsl:when test="(not($mode) and $default-mode = '&gc;EditMode') or $mode = '&gc;EditMode'">
		<xsl:apply-templates select="." mode="gc:EditMode">
		    <xsl:with-param name="default-mode" select="$default-mode" tunnel="yes"/>
		</xsl:apply-templates>
	    </xsl:when>
	    <xsl:when test="(not($mode) and $default-mode = '&gc;CreateMode') or $mode = '&gc;CreateMode'">
		<xsl:apply-templates select="." mode="gc:CreateMode">
		    <xsl:with-param name="default-mode" select="$default-mode" tunnel="yes"/>
		</xsl:apply-templates>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:apply-templates select="key('resources', $absolute-path)"/>

		<!-- page resource -->
		<xsl:apply-templates select="key('resources-by-page-of', $absolute-path)" mode="gc:PaginationMode"/>

		<!-- apply all other URI resources -->
		<xsl:variable name="secondary-resources" select="*[not(@rdf:about = $absolute-path)][not(. is key('resources-by-page-of', $absolute-path))][not(key('predicates-by-object', @rdf:nodeID))]"/>
		<xsl:if test="$secondary-resources">
		    <xsl:apply-templates select="." mode="gc:ModeSelectMode">
			<xsl:with-param name="default-mode" select="$default-mode" tunnel="yes"/>
		    </xsl:apply-templates>

		    <xsl:apply-templates select="$secondary-resources"/>
		</xsl:if>
		
		<!-- page resource -->
		<xsl:apply-templates select="key('resources-by-page-of', $absolute-path)" mode="gc:PaginationMode"/>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <xsl:template match="*" mode="gc:ModeSelectMode"/>

    <xsl:template match="rdf:RDF" mode="gc:ModeSelectMode">
	<xsl:param name="default-mode" as="xs:anyURI" tunnel="yes"/>

	<ul class="nav nav-tabs">
	    <xsl:choose>
		<xsl:when test="key('resources-by-page-of', $absolute-path)">
		    <xsl:apply-templates select="key('resources-by-type', '&gc;PageMode', document(''))" mode="gc:ModeSelectMode"/>
		</xsl:when>
		<xsl:otherwise>
		    <xsl:apply-templates select="key('resources-by-type', '&gc;ItemMode', document(''))" mode="gc:ModeSelectMode"/>
		</xsl:otherwise>
	    </xsl:choose>
	</ul>
    </xsl:template>

    <xsl:template match="gc:Mode | *[rdf:type/@rdf:resource = '&gc;Mode']" mode="gc:ModeSelectMode">
	<xsl:param name="default-mode" as="xs:anyURI" tunnel="yes"/>

	<li>
	    <xsl:if test="(not($mode) and $default-mode = @rdf:about) or $mode = @rdf:about">
		<xsl:attribute name="class">active</xsl:attribute>
	    </xsl:if>

	    <xsl:apply-templates select="@rdf:about" mode="gc:ModeSelectMode"/>
	</li>	
    </xsl:template>

    <xsl:template match="@rdf:about" mode="gc:ModeSelectMode">
	<xsl:param name="default-mode" as="xs:anyURI" tunnel="yes"/>

	<xsl:choose>
	    <xsl:when test="not(empty($offset)) and not(empty($limit)) and . = $default-mode">
		<a href="{$absolute-path}{gc:query-string($offset, $limit, $order-by, $desc, ())}">
		    <xsl:apply-templates select="." mode="gc:LabelMode"/>
		</a>
	    </xsl:when>
	    <xsl:when test=". = $default-mode">
		<a href="{$absolute-path}">
		    <xsl:apply-templates select="." mode="gc:LabelMode"/>
		</a>		
	    </xsl:when>
	    <xsl:when test="not(empty($offset)) and not(empty($limit))">
		<a href="{$absolute-path}{gc:query-string($offset, $limit, $order-by, $desc, .)}">
		    <xsl:apply-templates select="." mode="gc:LabelMode"/>
		</a>
	    </xsl:when>
	    <xsl:otherwise>
		<a href="{$absolute-path}{gc:query-string((), .)}">
		    <xsl:apply-templates select="." mode="gc:LabelMode"/>
		</a>		
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="gc:SidebarNavMode">
	<xsl:for-each-group select="*/*" group-by="concat(namespace-uri(), local-name())">
	    <xsl:sort select="gc:label(.)" data-type="text" order="ascending" lang="{$lang}"/>
	    <xsl:apply-templates select="current-group()[1]" mode="gc:SidebarNavMode"/>
	</xsl:for-each-group>	
    </xsl:template>

    <!-- subject -->
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]">
	<xsl:apply-templates select="." mode="gc:HeaderMode"/>

	<xsl:variable name="type-containers" as="element()*">
	    <xsl:for-each-group select="*" group-by="if (not(empty(rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name())))))) then rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name()))) else key('resources', '&rdfs;Resource', document('&rdfs;'))/@rdf:about">
		<!-- <xsl:sort select="gc:label(if (not(empty(rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name())))))) then rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name())))[1] else xs:anyURI('&rdfs;Resource'))"/> -->

		<xsl:variable name="property-items" as="element()*">
		    <xsl:apply-templates select="current-group()" mode="gc:PropertyListMode">
			<!-- <xsl:sort select="gc:label(xs:anyURI(concat(namespace-uri(), local-name())), $root, $lang)" data-type="text" order="ascending" lang="{$lang}"/> -->
			<!-- <xsl:sort select="if (@rdf:resource) then (gc:label(@rdf:resource, $root, $lang)) else text()" data-type="text" order="ascending" lang="{$lang}"/> -->
		    </xsl:apply-templates>
		</xsl:variable>
		
		<xsl:if test="$property-items">
		    <div class="well well-small span6">
			<h3>
			    <span class="btn">
				<xsl:variable name="type" select="if (not(empty(rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name())))))) then rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name()))) else key('resources', '&rdfs;Resource', document('&rdfs;'))/@rdf:about"/>
				<xsl:apply-templates select="$type"/>
				<!-- <xsl:apply-templates select="key('resources', current-grouping-key(), document(gc:document-uri(current-grouping-key())))/@rdf:about"/> -->
			    </span>
			</h3>
			<dl>
			    <xsl:copy-of select="$property-items"/>
			</dl>
		    </div>
		</xsl:if>
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
	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:ParaImageMode"/>

	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:HeaderMode"/>

	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:DescriptionMode"/>

            <xsl:if test="starts-with(@rdf:about, $base-uri) and not($mode = '&gc;EditMode')">
                <div class="pull-right">
                    <a class="btn btn-primary" href="{gc:document-uri(@rdf:about)}{gc:query-string((), xs:anyURI('&gc;EditMode'))}">
                        <xsl:apply-templates select="key('resources', '&gc;EditMode', document(''))/@rdf:about" mode="gc:LabelMode"/>
                    </a>
                </div>
            </xsl:if>
            <xsl:if test="rdf:type/@rdf:resource = '&ldp;Container' and not($mode = '&gc;CreateMode')">
                <div class="pull-right">
                    <a class="btn btn-primary" href="{gc:document-uri(@rdf:about)}{gc:query-string((), xs:anyURI('&gc;CreateMode'))}">
                        <xsl:apply-templates select="key('resources', '&gc;CreateMode', document(''))/@rdf:about" mode="gc:LabelMode"/>
                    </a>
                </div>
            </xsl:if>

	    <!-- xsl:apply-templates? -->
	    <xsl:if test="rdf:type">
		<ul class="inline">
		    <xsl:apply-templates select="rdf:type" mode="gc:HeaderMode">
			<xsl:sort select="gc:label(@rdf:resource | @rdf:nodeID)" data-type="text" order="ascending" lang="{$lang}"/>
		    </xsl:apply-templates>
		</ul>
	    </xsl:if>
	</div>
    </xsl:template>

    <!-- PROPERTY LIST MODE -->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:PropertyListMode" priority="1">
	<div class="well well-small">
	    <dl>
		<xsl:apply-templates mode="gc:PropertyListMode">
		    <xsl:sort select="gc:label(.)" data-type="text" order="ascending" lang="{$lang}"/>
		    <xsl:sort select="if (@rdf:resource) then (gc:label(@rdf:resource)) else text()" data-type="text" order="ascending" lang="{$lang}"/>
		</xsl:apply-templates>
	    </dl>
	</div>
    </xsl:template>

    <!-- SIDEBAR NAV MODE -->
    
    <xsl:template match="*[@rdf:about]" mode="gc:SidebarNavMode">
	<xsl:apply-templates mode="gc:SidebarNavMode">
	    <xsl:sort select="gc:label(.)" data-type="text" order="ascending"/>
	</xsl:apply-templates>
    </xsl:template>

    <!-- PAGINATION MODE -->

    <xsl:template match="*" mode="gc:PaginationMode"/>

    <xsl:template match="*[xhv:prev/@rdf:resource] | *[xhv:next/@rdf:resource]" mode="gc:PaginationMode">
	<!-- <xsl:param name="selected-resources" select="../*[not(@rdf:about = $absolute-path)][not(. is key('resources-by-page-of', $absolute-path))][not(key('predicates-by-object', @rdf:nodeID))]"/> -->
	
	<!-- no need for pagination if the number of SELECTed resources is below $limit per page? -->
	<!-- <xsl:if test="count($selected-resources) = $limit"> -->
	    <ul class="pager">
		<li class="previous">
		    <xsl:choose>
			<xsl:when test="xhv:prev">
			    <a href="{xhv:prev/@rdf:resource}" class="active">
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
			    <a href="{xhv:next/@rdf:resource}">
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
	<xsl:param name="default-mode" as="xs:anyURI" tunnel="yes"/>
	
	<xsl:apply-templates select="key('resources', $absolute-path)" mode="gc:HeaderMode"/>
	
	<xsl:apply-templates select="." mode="gc:ModeSelectMode"/>

	<!-- page resource -->
	<xsl:apply-templates select="key('resources-by-page-of', $absolute-path)" mode="gc:PaginationMode"/>

	<!-- all resources that are not recursive blank nodes, except page -->
	<xsl:apply-templates select="*[not(@rdf:about = $absolute-path)][not(. is key('resources-by-page-of', $absolute-path))][not(key('predicates-by-object', @rdf:nodeID))]" mode="gc:ListMode"/>
	
	<!-- page resource -->
	<xsl:apply-templates select="key('resources-by-page-of', $absolute-path)" mode="gc:PaginationMode"/>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:ListMode">
	<div class="well">
	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:ImageMode"/>

	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:HeaderMode"/>
	    
	    <xsl:if test="rdf:type">
		<ul class="inline">
		    <xsl:apply-templates select="rdf:type" mode="gc:HeaderMode">
			<xsl:sort select="gc:label(@rdf:resource | @rdf:nodeID)" data-type="text" order="ascending" lang="{$lang}"/>
		    </xsl:apply-templates>
		</ul>
	    </xsl:if>

	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:DescriptionMode"/>

	    <xsl:if test="@rdf:nodeID">
		<xsl:apply-templates select="." mode="gc:PropertyListMode"/>
	    </xsl:if>
	</div>
    </xsl:template>

    <!-- TABLE MODE -->

    <xsl:template match="rdf:RDF" mode="gc:TableMode">
	<xsl:param name="default-mode" as="xs:anyURI" tunnel="yes"/>

	<xsl:apply-templates select="key('resources', $absolute-path)" mode="gc:HeaderMode"/>

	<xsl:apply-templates select="." mode="gc:ModeSelectMode"/>

	<!-- page resource -->
	<xsl:apply-templates select="key('resources-by-page-of', $absolute-path)" mode="gc:PaginationMode"/>

	<!-- SELECTed resources = everything except container, page, and non-root blank nodes -->
	<xsl:variable name="selected-resources" select="*[not(@rdf:about = $absolute-path)][not(. is key('resources-by-page-of', $absolute-path))][not(key('predicates-by-object', @rdf:nodeID))]"/>
	<xsl:variable name="predicates" as="element()*">
	    <xsl:for-each-group select="$selected-resources/*" group-by="concat(namespace-uri(), local-name())">
		<xsl:sort select="gc:label(.)" data-type="text" order="ascending" lang="{$lang}"/>
		<xsl:sequence select="current-group()[1]"/>
	    </xsl:for-each-group>
	</xsl:variable>

	<table class="table table-bordered table-striped">
	    <thead>
		<tr>
		    <th>
			<xsl:apply-templates select="key('resources', '&rdfs;Resource', document('&rdfs;'))/@rdf:about" mode="gc:LabelMode"/>
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
	<xsl:apply-templates select="key('resources-by-page-of', $absolute-path)" mode="gc:PaginationMode"/>
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

    <!-- THUMBNAIL MODE -->
    
    <xsl:template match="rdf:RDF" mode="gc:ThumbnailMode">
	<xsl:param name="default-mode" as="xs:anyURI" tunnel="yes"/>
	<xsl:param name="thumbnails-per-row" select="2" as="xs:integer"/>

	<xsl:apply-templates select="key('resources', $absolute-path)" mode="gc:HeaderMode"/>

	<xsl:apply-templates select="." mode="gc:ModeSelectMode"/>

	<!-- page resource -->
	<xsl:apply-templates select="key('resources-by-page-of', $absolute-path)" mode="gc:PaginationMode"/>

	<ul class="thumbnails">
	    <xsl:variable name="thumbnail-items" as="element()*">	    
		<!-- all resources that are not recursive blank nodes, except page -->
		<xsl:apply-templates select="*[not(@rdf:about = $absolute-path)][not(. is key('resources-by-page-of', $absolute-path))][not(key('predicates-by-object', @rdf:nodeID))]" mode="gc:ThumbnailMode">
		    <xsl:with-param name="thumbnails-per-row" select="$thumbnails-per-row"/>
		</xsl:apply-templates>
	    </xsl:variable>

	    <xsl:for-each-group select="$thumbnail-items" group-adjacent="(position() - 1) idiv $thumbnails-per-row">
		<div class="row-fluid">
		    <ul class="thumbnails">
			<xsl:copy-of select="current-group()"/>
		    </ul>
		</div>
	    </xsl:for-each-group>	    
	</ul>

	<!-- page resource -->
	<xsl:apply-templates select="key('resources-by-page-of', $absolute-path)" mode="gc:PaginationMode"/>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:ThumbnailMode">
	<xsl:param name="thumbnails-per-row" as="xs:integer"/>
	
	<li class="span{12 div $thumbnails-per-row}">
	    <div class="thumbnail">
		<xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:ImageMode"/>
		
		<div class="caption">
		    <h3>
			<xsl:apply-templates select="@rdf:about | @rdf:nodeID"/>
		    </h3>

		    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:DescriptionMode"/>
		</div>
	    </div>
	</li>
    </xsl:template>

    <!-- CREATE MODE -->
    
    <xsl:template match="rdf:RDF" mode="gc:CreateMode">
	<xsl:param name="default-mode" as="xs:anyURI" tunnel="yes"/>

	<xsl:apply-templates select="key('resources', $absolute-path)" mode="gc:HeaderMode"/>

	<form class="form-horizontal" method="post" action="{$absolute-path}" accept-charset="UTF-8">
	    <xsl:comment>This form uses RDF/POST encoding: http://www.lsrn.org/semweb/rdfpost.html</xsl:comment>
	    <xsl:call-template name="gc:InputTemplate">
		<xsl:with-param name="name" select="'rdf'"/>
		<xsl:with-param name="type" select="'hidden'"/>
	    </xsl:call-template>

	    <fieldset id="fieldset-{generate-id()}">
		<legend>Add item</legend>

                <!-- can this be made improved? -->
		<xsl:call-template name="gc:InputTemplate">
		    <xsl:with-param name="type" select="'hidden'"/>
		    <xsl:with-param name="name" select="'sb'"/>
		    <xsl:with-param name="value" select="'bnode'"/>
		</xsl:call-template>

		<div class="control-group">
		    <button type="button" class="btn add-statement" title="Add new statement">&#x271A;</button>
		</div>
	    </fieldset>
	    
            <!--
	    <fieldset>
		<legend>Target graph</legend>
		<div class="control-group">
		    <label class="control-label" for="select-graph">Graph</label>
		    <div class="controls">
			<select name="graph" id="select-graph"/>
		    </div>
		</div>
	    </fieldset>
            -->

	    <div class="form-actions">
		<button type="submit" class="btn btn-primary create-mode">Save</button>
	    </div>
	</form>
    </xsl:template>
    
    <!-- EDIT MODE -->
    
    <xsl:template match="rdf:RDF" mode="gc:EditMode">
	<xsl:param name="default-mode" as="xs:anyURI" tunnel="yes"/>

	<xsl:apply-templates select="key('resources', $absolute-path)" mode="gc:HeaderMode"/>

	<xsl:apply-templates select="." mode="gc:ModeSelectMode"/>

	<form class="form-horizontal" method="post" action="{$absolute-path}?_method=PUT" accept-charset="UTF-8"> <!-- enctype="multipart/form-data" -->
	    <xsl:comment>This form uses RDF/POST encoding: http://www.lsrn.org/semweb/rdfpost.html</xsl:comment>
	    <xsl:call-template name="gc:InputTemplate">
		<xsl:with-param name="name" select="'rdf'"/>
		<xsl:with-param name="type" select="'hidden'"/>
	    </xsl:call-template>

            <xsl:variable name="selected-resources" select="*[not(key('predicates-by-object', @rdf:nodeID))]"/>
	    <xsl:apply-templates select="$selected-resources" mode="gc:EditMode">
                <xsl:sort select="gc:label(@rdf:about | @rdf:nodeID)"/>
            </xsl:apply-templates>

            <!--
	    <fieldset>
		<legend>Target graph</legend>
		<div class="control-group">
		    <label class="control-label" for="select-graph">Graph</label>
		    <div class="controls">
			<select name="graph" id="select-graph"/>
		    </div>
		</div>
	    </fieldset>
            -->
            
	    <div class="form-actions">
		<button type="submit" class="btn btn-primary">Save</button>
	    </div>
	</form>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:EditMode">
	<xsl:param name="constraint-violations" as="element()*" tunnel="yes"/>

	<fieldset id="fieldset-{generate-id()}">
            <xsl:if test="@rdf:about or not(key('predicates-by-object', @rdf:nodeID))">
                <legend>
                    <xsl:apply-templates select="@rdf:about | @rdf:nodeID"/>
                </legend>
            </xsl:if>
            
	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:EditMode"/>
            <xsl:apply-templates mode="gc:EditMode">
                <xsl:sort select="gc:label(.)"/>
            </xsl:apply-templates>

            <!--
	    <xsl:for-each-group select="*" group-by="concat(namespace-uri(), local-name())">
                <xsl:sort select="gc:label(current-group()[1])"/>
		<xsl:apply-templates select="current-group()" mode="gc:EditMode"/>
	    </xsl:for-each-group>
            -->
            
            <div class="control-group">
                <button type="button" class="btn add-statement" title="Add new statement">&#x271A;</button>
            </div>
	</fieldset>
    </xsl:template>

    <!-- CREATE MODE -->
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:CreateMode">
        <xsl:param name="input" as="element()?"/>
        <xsl:param name="legend" as="xs:string?"/>
        
	<fieldset id="fieldset-{generate-id()}">
            <xsl:if test="$legend">
                <legend>
                    <xsl:value-of select="$legend"/>
                </legend>                
            </xsl:if>

            <xsl:choose>
                <xsl:when test="$input/@rdf:about | $input/@rdf:nodeID">
                    <xsl:apply-templates select="$input/@rdf:about | $input/@rdf:nodeID" mode="gc:EditMode"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:EditMode"/>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:for-each select="*">
                <xsl:sort select="gc:label(.)"/>
                <xsl:choose>
                    <xsl:when test="$input/*[concat(namespace-uri(), local-name()) = concat(namespace-uri(current()), local-name(current()))]">
                        <xsl:apply-templates select="$input/*[concat(namespace-uri(), local-name()) = concat(namespace-uri(current()), local-name(current()))]" mode="gc:EditMode"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="." mode="gc:EditMode"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </fieldset>
    </xsl:template>

</xsl:stylesheet>