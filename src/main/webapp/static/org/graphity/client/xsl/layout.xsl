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
    <!-- <xsl:variable name="select-res" select="key('resources-by-type', '&sp;Select', $ont-model)" as="element()?"/> -->
    <xsl:variable name="orderBy" select="if ($select-res/sp:orderBy) then list:member(key('resources', $select-res/sp:orderBy/@rdf:nodeID, $ont-model), $ont-model) else ()"/>
    <xsl:variable name="ontology-uri" select="xs:anyURI(key('resources-by-type', '&owl;Ontology', $ont-model)/@rdf:about)" as="xs:anyURI"/>
    <xsl:variable name="config" select="document('../../../../../WEB-INF/web.xml')" as="document-node()"/>

    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
    <xsl:key name="predicates" match="*[@rdf:about]/* | *[@rdf:nodeID]/*" use="concat(namespace-uri(), local-name())"/>
    <xsl:key name="predicates-by-object" match="*[@rdf:about]/* | *[@rdf:nodeID]/*" use="@rdf:resource | @rdf:nodeID"/>
    <xsl:key name="resources-by-type" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="rdf:type/@rdf:resource"/>
    <xsl:key name="resources-by-container" match="*[@rdf:about]" use="sioc:has_container/@rdf:resource"/>
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

    <rdf:Description rdf:about="&gc;PropertyMode">
	<rdf:type rdf:resource="&gc;Mode"/>
	<rdf:type rdf:resource="&gc;ItemMode"/>
	<rdf:type rdf:resource="&gc;PageMode"/>
	<rdf:type rdf:resource="&gc;ContainerMode"/>
	<rdfs:label xml:lang="en-US">Properties</rdfs:label>
    </rdf:Description>

    <rdf:Description rdf:about="&gc;TableMode">
	<rdf:type rdf:resource="&gc;Mode"/>
	<rdf:type rdf:resource="&gc;PageMode"/>
	<rdf:type rdf:resource="&gc;ContainerMode"/>
	<rdfs:label xml:lang="en-US">Table</rdfs:label>
    </rdf:Description>

    <rdf:Description rdf:about="&gc;ListMode">
	<rdf:type rdf:resource="&gc;Mode"/>
	<!-- <rdf:type rdf:resource="&gc;ItemMode"/> -->
	<rdf:type rdf:resource="&gc;PageMode"/>
	<rdf:type rdf:resource="&gc;ContainerMode"/>
	<rdfs:label xml:lang="en-US">List</rdfs:label>
    </rdf:Description>

    <rdf:Description rdf:about="&gc;ThumbnailMode">
	<rdf:type rdf:resource="&gc;Mode"/>
	<rdf:type rdf:resource="&gc;PageMode"/>
	<rdf:type rdf:resource="&gc;ContainerMode"/>
	<rdfs:label xml:lang="en-US">Gallery</rdfs:label>
    </rdf:Description>

    <rdf:Description rdf:about="&gc;EditMode">
	<rdf:type rdf:resource="&gc;Mode"/>
	<rdf:type rdf:resource="&gc;ItemMode"/>
	<rdf:type rdf:resource="&gc;ContainerMode"/>
	<rdfs:label xml:lang="en-US">Edit</rdfs:label>
    </rdf:Description>

    <rdf:Description rdf:about="&gc;CreateMode">
	<rdf:type rdf:resource="&gc;Mode"/>
	<rdf:type rdf:resource="&gc;ContainerMode"/>
        <rdfs:label xml:lang="en-US">Create</rdfs:label>
    </rdf:Description>

    <xsl:template match="/">
	<html xml:lang="{$lang}">
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
		    <xsl:apply-templates select="." mode="gc:NavBarMode"/>
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
    
    <xsl:template match="/" mode="gc:NavBarMode">
	<button class="btn btn-navbar" onclick="if ($('#collapsing-navbar').hasClass('in')) $('#collapsing-navbar').removeClass('collapse in').height(0); else $('#collapsing-navbar').addClass('collapse in').height('auto');">
	    <span class="icon-bar"></span>
	    <span class="icon-bar"></span>
	    <span class="icon-bar"></span>
	</button>

	<a class="brand" href="{$base-uri}">
	    <xsl:for-each select="key('resources', $base-uri, $ont-model)">
		<img src="{foaf:logo/@rdf:resource}">
		    <xsl:attribute name="alt"><xsl:apply-templates select="." mode="gc:LabelMode"/></xsl:attribute>
		</img>
	    </xsl:for-each>
	</a>

	<div id="collapsing-navbar" class="nav-collapse collapse">
	    <ul class="nav">
		<!-- make menu links for all resources in the ontology, except base URI -->
		<xsl:apply-templates select="key('resources-by-space', $base-uri, $ont-model)" mode="gc:NavBarMode">
		    <xsl:sort select="gc:label(.)" data-type="text" order="ascending" lang="{$lang}"/>
		</xsl:apply-templates>
	    </ul>

	    <xsl:if test="key('resources', $base-uri, $ont-model)/rdfs:isDefinedBy/@rdf:resource | key('resources', key('resources', $base-uri, $ont-model)/void:inDataset/@rdf:resource, $ont-model)/void:sparqlEndpoint/@rdf:resource">
		<ul class="nav pull-right">
		    <xsl:for-each select="key('resources', $base-uri, $ont-model)/rdfs:isDefinedBy/@rdf:resource | key('resources', key('resources', $base-uri, $ont-model)/void:inDataset/@rdf:resource, $ont-model)/void:sparqlEndpoint/@rdf:resource">
			<!-- <xsl:sort select="gc:label(.)" data-type="text" order="ascending" lang="{$lang}"/> -->
			<li>
			    <xsl:if test="gc:document-uri(.) = $absolute-path">
				<xsl:attribute name="class">active</xsl:attribute>
			    </xsl:if>
			    <xsl:apply-templates select="." mode="gc:InlineMode"/>
			</li>
		    </xsl:for-each>
		</ul>
	    </xsl:if>
	</div>
    </xsl:template>

    <xsl:template match="*[@rdf:about]" mode="gc:NavBarMode"> <!-- [@rdf:about[not(. = $base-uri)]] -->
        <li>
            <xsl:if test="@rdf:about = $absolute-path">
                <xsl:attribute name="class">active</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="@rdf:about" mode="gc:InlineMode"/>
        </li>
    </xsl:template>
    
    <xsl:template match="/" mode="gc:FooterMode">
	<p>
	    <xsl:value-of select="format-date(current-date(), '[Y]', $lang, (), ())"/>
	</p>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="gc:TitleMode">
	<xsl:apply-templates select="key('resources', $base-uri, $ont-model)" mode="gc:LabelMode"/>
	<xsl:text> - </xsl:text>
	<xsl:apply-templates select="key('resources', $absolute-path)" mode="gc:LabelMode"/>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="gc:StyleMode">
	<link href="static/css/bootstrap.css" rel="stylesheet"/>
	<link href="static/css/bootstrap-responsive.css" rel="stylesheet"/>

	<style type="text/css">
	    <![CDATA[
		body { padding-top: 60px; padding-bottom: 40px; }
		.brand img { height: 0.8em; width: auto; }
		form.form-inline { margin: 0; }
		ul.inline { margin-left: 0; max-height: 7em; overflow-y: auto; }
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
                            ]]><xsl:if test="$mode"><![CDATA["mode-string": "]]><xsl:value-of select="$mode"/><![CDATA[",]]></xsl:if><![CDATA[
                            "lang": "]]><xsl:value-of select="$lang"/><![CDATA[" },
                        initialTemplate: "main", logLevel: "FINE"
                    }); }
                ]]>
            </script>
        </xsl:if>
    </xsl:template>

    <xsl:template match="rdf:RDF">
	<xsl:param name="default-mode" select="if (key('resources-by-page-of', $absolute-path)) then xs:anyURI('&gc;ListMode') else xs:anyURI('&gc;PropertyMode')" tunnel="yes"/>

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
		<xsl:apply-templates select="." mode="gc:PropertyMode">
		    <xsl:with-param name="default-mode" select="$default-mode" tunnel="yes"/>
		</xsl:apply-templates>
            </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="gc:PropertyMode">
	<xsl:param name="default-mode" as="xs:anyURI" tunnel="yes"/>

        <xsl:apply-templates select="." mode="gc:HeaderMode"/>

        <xsl:apply-templates select="." mode="gc:ModeSelectMode">
            <xsl:with-param name="default-mode" select="$default-mode" tunnel="yes"/>
        </xsl:apply-templates>

        <!-- page resource -->
        <xsl:apply-templates select="." mode="gc:PaginationMode"/>

        <!-- apply all other URI resources -->
        <xsl:apply-templates mode="#current">
            <xsl:sort select="gc:label(.)" lang="{$lang}"/>
        </xsl:apply-templates>

        <xsl:apply-templates select="." mode="gc:PaginationMode"/>
    </xsl:template>

    <xsl:template match="*" mode="gc:ModeSelectMode"/>

    <xsl:template match="rdf:RDF" mode="gc:ModeSelectMode">
	<xsl:param name="default-mode" as="xs:anyURI" tunnel="yes"/>

	<ul class="nav nav-tabs">
	    <xsl:choose>
                <!--
		<xsl:when test="key('resources-by-page-of', $absolute-path)">
		    <xsl:apply-templates select="key('resources-by-type', '&gc;PageMode', document(''))" mode="#current"/>
		</xsl:when>
                -->
		<xsl:when test="key('resources', $absolute-path)/rdf:type/@rdf:resource = ('&sioc;Space', '&sioc;Container')">
		    <xsl:apply-templates select="key('resources-by-type', '&gc;ContainerMode', document(''))" mode="#current"/>
		</xsl:when>
                <xsl:otherwise>
		    <xsl:apply-templates select="key('resources-by-type', '&gc;ItemMode', document(''))" mode="#current"/>
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

	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>
	</li>	
    </xsl:template>

    <xsl:template match="@rdf:about" mode="gc:ModeSelectMode">
	<xsl:param name="default-mode" as="xs:anyURI" tunnel="yes"/>

	<xsl:choose>
	    <xsl:when test="not(empty($offset)) and not(empty($limit)) and . = $default-mode">
		<a href="{$absolute-path}{gc:query-string($offset, $limit, $order-by, $desc, ())}">
		    <xsl:apply-templates select=".." mode="gc:LabelMode"/>
		</a>
	    </xsl:when>
	    <xsl:when test=". = $default-mode">
		<a href="{$absolute-path}">
		    <xsl:apply-templates select=".." mode="gc:LabelMode"/>
		</a>		
	    </xsl:when>
	    <xsl:when test="not(empty($offset)) and not(empty($limit))">
		<a href="{$absolute-path}{gc:query-string($offset, $limit, $order-by, $desc, .)}">
		    <xsl:apply-templates select=".." mode="gc:LabelMode"/>
		</a>
	    </xsl:when>
	    <xsl:otherwise>
		<a href="{$absolute-path}{gc:query-string((), .)}">
		    <xsl:apply-templates select=".." mode="gc:LabelMode"/>
		</a>		
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <!-- subject -->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:PropertyMode"/>

    <xsl:template match="*[*][@rdf:about or @rdf:nodeID][not(@rdf:about = $absolute-path)][not(. is key('resources-by-page-of', $absolute-path))]" mode="gc:PropertyMode" priority="1">
        <xsl:param name="nested" as="xs:boolean?"/>
        
        <!-- show root blank nodes or nested blank nodes -->
        <xsl:if test="not(key('predicates-by-object', @rdf:nodeID)) or $nested">
            <xsl:apply-templates select="." mode="gc:HeaderMode"/>

            <xsl:apply-templates select="." mode="gc:PropertyListMode"/>
        </xsl:if>
    </xsl:template>

    <!-- HEADER MODE -->

    <xsl:template match="rdf:RDF" mode="gc:HeaderMode">
	<xsl:apply-templates select="key('resources', $absolute-path)" mode="#current"/>
    </xsl:template>
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:HeaderMode">
	<div class="well">
            <xsl:apply-templates select="." mode="gc:ImageMode"/>

            <xsl:if test="starts-with(@rdf:about, $base-uri) and not($mode = '&gc;EditMode')">
                <div class="pull-right">
                    <a class="btn btn-primary" href="{gc:document-uri(@rdf:about)}{gc:query-string((), xs:anyURI('&gc;EditMode'))}">
                        <xsl:apply-templates select="key('resources', '&gc;EditMode', document(''))" mode="gc:LabelMode"/>
                    </a>
                </div>
            </xsl:if>
            <xsl:if test="rdf:type/@rdf:resource = '&ldp;Container' and not($mode = '&gc;CreateMode')">
                <div class="pull-right">
                    <a class="btn btn-primary" href="{gc:document-uri(@rdf:about)}{gc:query-string((), xs:anyURI('&gc;CreateMode'))}">
                        <xsl:apply-templates select="key('resources', '&gc;CreateMode', document(''))" mode="gc:LabelMode"/>
                    </a>
                </div>
            </xsl:if>

            <xsl:apply-templates select="." mode="gc:MediaTypeSelectMode"/>

            <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>
            
            <xsl:apply-templates select="." mode="gc:DescriptionMode"/>

	    <xsl:apply-templates select="." mode="gc:TypeListMode"/>
	</div>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:MediaTypeSelectMode">
        <div class="btn-group pull-right">
            
            <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>
            
            <xsl:if test="@rdf:about = $absolute-path and $query-res/sp:text">
                <a href="{resolve-uri('sparql', $base-uri)}?query={encode-for-uri($query-res/sp:text)}" class="btn">SPARQL</a>
            </xsl:if>
        </div>
    </xsl:template>
    
    <xsl:template match="@rdf:about[. = $absolute-path]" mode="gc:HeaderMode">
	<h1 class="page-header">
	    <xsl:apply-templates select="." mode="gc:InlineMode"/>
	</h1>
    </xsl:template>

    <xsl:template match="@rdf:about | @rdf:nodeID" mode="gc:HeaderMode">
	<h1>
	    <xsl:apply-templates select="." mode="gc:InlineMode"/>
	</h1>
    </xsl:template>

    <!-- IMAGE MODE -->
        
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:ImageMode">
        <xsl:variable name="images" as="element()*">
            <xsl:apply-templates mode="#current"/>
        </xsl:variable>
        <xsl:if test="$images">
            <p>
                <xsl:copy-of select="$images[1]"/>
            </p>
        </xsl:if>
    </xsl:template>

    <!-- LABEL MODE -->
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:LabelMode">
        <xsl:variable name="labels" as="xs:string*">
            <xsl:variable name="lang-labels" as="xs:string*">
                <xsl:apply-templates select="*[lang($lang)]" mode="#current"/>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="not(empty($lang-labels))">
                    <xsl:sequence select="$lang-labels"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates mode="#current"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="not(empty($labels))">
                <xsl:value-of select="concat(upper-case(substring($labels[1], 1, 1)), substring($labels[1], 2))"/>
            </xsl:when>
            <xsl:when test="contains(@rdf:about, '#') and not(ends-with(@rdf:about, '#'))">
                <xsl:variable name="label" select="substring-after(@rdf:about, '#')"/>
                <xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
            </xsl:when>
            <xsl:when test="string-length(tokenize(@rdf:about, '/')[last()]) &gt; 0">
                <xsl:variable name="label" use-when="function-available('url:decode')" select="translate(url:decode(tokenize(@rdf:about, '/')[last()], 'UTF-8'), '_', ' ')"/>
                <xsl:variable name="label" use-when="not(function-available('url:decode'))" select="translate(tokenize(@rdf:about, '/')[last()], '_', ' ')"/>
                <xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@rdf:about | @rdf:nodeID"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
        
    <!-- DESCRIPTION MODE -->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:DescriptionMode">
        <xsl:variable name="descriptions" as="xs:string*">
            <xsl:variable name="lang-descriptions" as="xs:string*">
                <xsl:apply-templates select="*[lang($lang)]" mode="#current"/>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="not(empty($lang-descriptions))">
                    <xsl:sequence select="$lang-descriptions"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates mode="#current"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:if test="not(empty($descriptions))">
            <p>
                <xsl:copy-of select="substring($descriptions[1], 1, 300)"/>
            </p>
        </xsl:if>
    </xsl:template>

    <!-- INLINE LIST MODE -->
    
    <xsl:template match="*" mode="gc:TypeListMode"/>

    <xsl:template match="*[rdf:type/@rdf:resource]" mode="gc:TypeListMode" priority="1">
        <ul class="inline">
            <xsl:apply-templates select="rdf:type" mode="#current">
                <xsl:sort select="gc:object-label(@rdf:resource)" data-type="text" order="ascending" lang="{$lang}"/>
            </xsl:apply-templates>
        </ul>
    </xsl:template>

    <xsl:template match="rdf:type[@rdf:resource]" mode="gc:TypeListMode" priority="1">
        <li>
	    <xsl:apply-templates select="@rdf:resource" mode="gc:InlineMode"/>
	</li>
    </xsl:template>

    <!-- PROPERTY LIST MODE -->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:PropertyListMode">
	<xsl:variable name="type-containers" as="element()*">
	    <xsl:for-each-group select="*" group-by="if (not(empty(rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name())))))) then rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name()))) else key('resources', '&rdfs;Resource', document('&rdfs;'))/@rdf:about">
		<xsl:sort select="if (rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name())))) then gc:object-label(rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name())))[1]) else ()"/>

		<xsl:variable name="properties" as="element()*">
                    <xsl:for-each-group select="current-group()" group-by="concat(namespace-uri(), local-name())">
			<xsl:sort select="gc:property-label(.)" data-type="text" order="ascending" lang="{$lang}"/>
                        
                        <xsl:variable name="objects" as="element()*">
                            <xsl:apply-templates select="current-group()" mode="#current">
                                <xsl:sort select="if (@rdf:resource | @rdf:nodeID) then gc:object-label(@rdf:resource | @rdf:nodeID) else text()" data-type="text" order="ascending" lang="{$lang}"/>
                            </xsl:apply-templates>
                        </xsl:variable>
                        <xsl:if test="$objects">
                            <dt>
                                <xsl:apply-templates select="." mode="gc:InlineMode"/>
                            </dt>
                            <xsl:copy-of select="$objects"/>
                        </xsl:if>
                    </xsl:for-each-group>
		</xsl:variable>
		
		<xsl:if test="$properties">
		    <div class="well well-small span6">
                        <xsl:if test="not(empty(rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name())))))">
                            <h3>
                                <span class="btn">
                                    <xsl:apply-templates select="rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name())))" mode="gc:InlineMode"/>
                                </span>
                            </h3>
                        </xsl:if>
			<dl>
			    <xsl:copy-of select="$properties"/>
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

    <!-- SIDEBAR NAV MODE -->
    
    <xsl:template match="rdf:RDF" mode="gc:SidebarNavMode">
	<xsl:for-each-group select="*/*" group-by="concat(namespace-uri(), local-name())">
	    <xsl:sort select="gc:property-label(.)" data-type="text" order="ascending" lang="{$lang}"/>
	    <xsl:apply-templates select="current-group()[1]" mode="#current">
                <xsl:sort select="gc:object-label(@rdf:resource)" data-type="text" order="ascending"/>
            </xsl:apply-templates>
	</xsl:for-each-group>	
    </xsl:template>

    <!-- PAGINATION MODE -->

    <xsl:template match="rdf:RDF" mode="gc:PaginationMode" priority="1">
        <xsl:apply-templates select="key('resources-by-page-of', $absolute-path)" mode="#current"/>
    </xsl:template>

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
				&#8592; <xsl:apply-templates select="key('resources', 'previous', document(''))" mode="gc:LabelMode"/>
			    </a>
			</xsl:when>
			<xsl:otherwise>
			    <xsl:attribute name="class">previous disabled</xsl:attribute>
			    <a>
				&#8592; <xsl:apply-templates select="key('resources', 'previous', document(''))" mode="gc:LabelMode"/>
			    </a>
			</xsl:otherwise>
		    </xsl:choose>
		</li>
		<li class="next">
		    <xsl:choose>
			<xsl:when test="xhv:next">
			    <!-- possible to add arrows by overriding -->
			    <a href="{xhv:next/@rdf:resource}">
				<xsl:apply-templates select="key('resources', 'next', document(''))" mode="gc:LabelMode"/> &#8594;
			    </a>
			</xsl:when>
			<xsl:otherwise>
			    <xsl:attribute name="class">next disabled</xsl:attribute>
			    <a>
				<xsl:apply-templates select="key('resources', 'next', document(''))" mode="gc:LabelMode"/> &#8594;
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
	
	<xsl:apply-templates select="." mode="gc:HeaderMode"/>
	
	<xsl:apply-templates select="." mode="gc:ModeSelectMode"/>

	<xsl:apply-templates select="." mode="gc:PaginationMode"/>

	<!-- all resources that are not recursive blank nodes, except page -->
	 <xsl:apply-templates mode="#current">
            <xsl:sort select="gc:label(.)" lang="{$lang}"/>
        </xsl:apply-templates>
	
	<xsl:apply-templates select="." mode="gc:PaginationMode"/>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:ListMode"/>
    
    <xsl:template match="*[*][@rdf:about or @rdf:nodeID][not(@rdf:about = $absolute-path)][not(. is key('resources-by-page-of', $absolute-path))][not(key('predicates-by-object', @rdf:nodeID))]" mode="gc:ListMode" priority="1">
	<div class="well">
            <xsl:apply-templates select="." mode="gc:ImageMode"/>
            
	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>
	    
	    <xsl:apply-templates select="." mode="gc:DescriptionMode"/>

	    <xsl:apply-templates select="." mode="gc:TypeListMode"/>            

	    <xsl:if test="@rdf:nodeID">
		<xsl:apply-templates select="." mode="gc:PropertyListMode"/>
	    </xsl:if>
	</div>
    </xsl:template>

    <xsl:template match="@rdf:about | @rdf:nodeID" mode="gc:ListMode">
        <h2>
            <xsl:apply-templates select="." mode="gc:InlineMode"/>
        </h2>
    </xsl:template>
            
    <!-- TABLE MODE -->

    <xsl:template match="rdf:RDF" mode="gc:TableMode">
	<xsl:param name="default-mode" as="xs:anyURI" tunnel="yes"/>
	<!-- SELECTed resources = everything except container, page, and non-root blank nodes -->
	<xsl:param name="predicates" as="element()*">
	    <xsl:for-each-group select="*[not(@rdf:about = $absolute-path)][not(. is key('resources-by-page-of', $absolute-path))][not(key('predicates-by-object', @rdf:nodeID))]/*" group-by="concat(namespace-uri(), local-name())">
		<xsl:sort select="gc:property-label(.)" order="ascending" lang="{$lang}"/>
		<xsl:sequence select="current-group()[1]"/>
	    </xsl:for-each-group>
	</xsl:param>

	<xsl:apply-templates select="." mode="gc:HeaderMode"/>

	<xsl:apply-templates select="." mode="gc:ModeSelectMode"/>

	<xsl:apply-templates select="." mode="gc:PaginationMode"/>

	<table class="table table-bordered table-striped">
	    <thead>
		<tr>
		    <th>
			<xsl:apply-templates select="key('resources', '&rdfs;Resource', document('&rdfs;'))" mode="gc:LabelMode"/>
		    </th>

		    <xsl:apply-templates select="$predicates" mode="gc:TableHeaderMode"/>
		</tr>
	    </thead>
	    <tbody>
		<xsl:apply-templates mode="#current">
		    <xsl:with-param name="predicates" select="$predicates"/>
                    <xsl:sort select="gc:label(.)" lang="{$lang}"/>
                </xsl:apply-templates>
	    </tbody>
	</table>
	
	<xsl:apply-templates select="." mode="gc:PaginationMode"/>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:TableMode"/>
    
    <xsl:template match="*[*][@rdf:about or @rdf:nodeID][not(@rdf:about = $absolute-path)][not(. is key('resources-by-page-of', $absolute-path))][not(key('predicates-by-object', @rdf:nodeID))]" mode="gc:TableMode" priority="1">
	<xsl:param name="predicates" as="element()*"/>

	<tr>
	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>

	    <xsl:variable name="subject" select="."/>
	    <xsl:for-each select="$predicates">
		<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(), local-name()))" as="xs:anyURI"/>
		<xsl:variable name="predicate" select="$subject/*[concat(namespace-uri(), local-name()) = $this]"/>
		<xsl:choose>
		    <xsl:when test="$predicate">
			<xsl:apply-templates select="$predicate" mode="#current"/>
		    </xsl:when>
		    <xsl:otherwise>
			<td></td>
		    </xsl:otherwise>
		</xsl:choose>
	    </xsl:for-each>
	</tr>
    </xsl:template>

    <xsl:template match="@rdf:about | @rdf:nodeID" mode="gc:TableMode">
	<td>
	    <xsl:apply-templates select="." mode="gc:InlineMode"/>
	</td>
    </xsl:template>

    <!-- THUMBNAIL MODE -->
    
    <xsl:template match="rdf:RDF" mode="gc:ThumbnailMode">
	<xsl:param name="default-mode" as="xs:anyURI" tunnel="yes"/>
	<xsl:param name="thumbnails-per-row" select="2" as="xs:integer"/>

	<xsl:apply-templates select="." mode="gc:HeaderMode"/>

	<xsl:apply-templates select="." mode="gc:ModeSelectMode"/>

	<xsl:apply-templates select="." mode="gc:PaginationMode"/>

	<ul class="thumbnails">
	    <xsl:variable name="thumbnail-items" as="element()*">	    
		<!-- all resources that are not recursive blank nodes, except page -->
		<xsl:apply-templates mode="#current">
                    <xsl:sort select="gc:label(.)" lang="{$lang}"/>
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

	<xsl:apply-templates select="." mode="gc:PaginationMode"/>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:ThumbnailMode"/>
        
    <xsl:template match="*[@rdf:about or @rdf:nodeID][not(@rdf:about = $absolute-path)][not(. is key('resources-by-page-of', $absolute-path))][not(key('predicates-by-object', @rdf:nodeID))]" mode="gc:ThumbnailMode" priority="1">
	<xsl:param name="thumbnails-per-row" as="xs:integer"/>
	
	<li class="span{12 div $thumbnails-per-row}">
	    <div class="thumbnail">
		<xsl:apply-templates mode="gc:ImageMode"/>
		
		<div class="caption">
                    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>

		    <xsl:apply-templates select="." mode="gc:DescriptionMode"/>
		</div>
	    </div>
	</li>
    </xsl:template>

    <xsl:template match="@rdf:about | @rdf:nodeID" mode="gc:ThumbnailMode">
        <h3>
            <xsl:apply-templates select="." mode="gc:InlineMode"/>
        </h3>
    </xsl:template>

    <!-- CREATE MODE -->
    
    <xsl:template match="rdf:RDF" mode="gc:CreateMode">
	<xsl:param name="default-mode" as="xs:anyURI" tunnel="yes"/>

	<xsl:apply-templates select="." mode="gc:HeaderMode"/>

        <xsl:apply-templates select="." mode="gc:ModeSelectMode">
            <xsl:with-param name="default-mode" select="$default-mode" tunnel="yes"/>
        </xsl:apply-templates>

	<form class="form-horizontal" method="post" action="{$absolute-path}?mode={encode-for-uri($mode)}" accept-charset="UTF-8">
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

	    <div class="form-actions">
		<button type="submit" class="btn btn-primary create-mode">Save</button>
	    </div>
	</form>
    </xsl:template>
    
    <!-- EDIT MODE -->
    
    <xsl:template match="rdf:RDF" mode="gc:EditMode">
	<xsl:param name="default-mode" as="xs:anyURI" tunnel="yes"/>

	<xsl:apply-templates select="." mode="gc:HeaderMode"/>

	<xsl:apply-templates select="." mode="gc:ModeSelectMode"/>

	<form class="form-horizontal" method="post" action="{$absolute-path}?_method=PUT&amp;mode={encode-for-uri($mode)}" accept-charset="UTF-8"> <!-- enctype="multipart/form-data" -->
	    <xsl:comment>This form uses RDF/POST encoding: http://www.lsrn.org/semweb/rdfpost.html</xsl:comment>
	    <xsl:call-template name="gc:InputTemplate">
		<xsl:with-param name="name" select="'rdf'"/>
		<xsl:with-param name="type" select="'hidden'"/>
	    </xsl:call-template>

            <xsl:variable name="selected-resources" select="*[not(key('predicates-by-object', @rdf:nodeID))]"/>
	    <xsl:apply-templates select="$selected-resources" mode="#current">
                <xsl:sort select="gc:label(.)"/>
            </xsl:apply-templates>

            <div class="form-actions">
		<button type="submit" class="btn btn-primary">Save</button>
	    </div>
	</form>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:EditMode">
	<xsl:param name="constraint-violations" as="element()*" tunnel="yes"/>
        <xsl:param name="add-statements" select="true()" as="xs:boolean?" tunnel="yes"/>
        
	<fieldset id="fieldset-{generate-id()}">
            <xsl:if test="@rdf:about or not(key('predicates-by-object', @rdf:nodeID))">
                <legend>
                    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:InlineMode"/>
                </legend>
            </xsl:if>
            
	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>
            <xsl:apply-templates mode="#current">
                <xsl:sort select="gc:property-label(.)"/>
            </xsl:apply-templates>

            <xsl:if test="$add-statements">
                <div class="control-group">
                    <button type="button" class="btn add-statement" title="Add new statement">&#x271A;</button>
                </div>
            </xsl:if>
	</fieldset>
    </xsl:template>
        
</xsl:stylesheet>