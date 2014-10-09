<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2012 Martynas Jusevičius <martynas@graphity.org>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java   "http://xml.apache.org/xalan/java/">
    <!ENTITY gp     "http://graphity.org/gp#">
    <!ENTITY gc     "http://graphity.org/gc#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl    "http://www.w3.org/2002/07/owl#">
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY ldp    "http://www.w3.org/ns/ldp#">
    <!ENTITY dct    "http://purl.org/dc/terms/">
    <!ENTITY foaf   "http://xmlns.com/foaf/0.1/">
    <!ENTITY sioc   "http://rdfs.org/sioc/ns#">
    <!ENTITY sp     "http://spinrdf.org/sp#">
    <!ENTITY spin   "http://spinrdf.org/spin#">
    <!ENTITY void   "http://rdfs.org/ns/void#">
    <!ENTITY list   "http://jena.hpl.hp.com/ARQ/list#">
    <!ENTITY xhv    "http://www.w3.org/1999/xhtml/vocab#">
    <!ENTITY geo    "http://www.w3.org/2003/01/geo/wgs84_pos#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gp="&gp;"
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
xmlns:geo="&geo;"
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
    <xsl:param name="matched-ont-class-uri" as="xs:anyURI"/>
    <xsl:param name="offset" select="$select-res/sp:offset" as="xs:integer?"/>
    <xsl:param name="limit" select="$select-res/sp:limit" as="xs:integer?"/>
    <xsl:param name="order-by" select="$orderBy/sp:varName | key('resources', $orderBy/sp:*/@rdf:nodeID, $ont-model)/sp:varName | key('resources', key('resources', $orderBy/sp:expression/@rdf:nodeID, $ont-model)/sp:*/@rdf:nodeID, $ont-model)/sp:varName" as="xs:string?"/>
    <xsl:param name="desc" select="$orderBy[1]/rdf:type/@rdf:resource = '&sp;Desc'" as="xs:boolean"/>
    <xsl:param name="endpoint-uri" as="xs:anyURI?"/>
    <xsl:param name="query" as="xs:string?"/>

    <xsl:variable name="matched-ont-class" select="key('resources', $matched-ont-class-uri, $ont-model)" as="element()"/>
    <xsl:variable name="default-mode" select="if ($matched-ont-class/gc:defaultMode/@rdf:resource) then xs:anyURI($matched-ont-class/gc:defaultMode/@rdf:resource) else (if (key('resources', $absolute-path)/rdf:type/@rdf:resource = ('&sioc;Container', '&sioc;Space')) then xs:anyURI('&gc;ListMode') else xs:anyURI('&gc;ReadMode'))" as="xs:anyURI"/>
    <xsl:variable name="resource" select="key('resources', $absolute-path, $ont-model)" as="element()?"/>
    <xsl:variable name="query-res" select="key('resources', $resource/spin:query/@rdf:resource | $resource/spin:query/@rdf:nodeID, $ont-model)" as="element()?"/>
    <xsl:variable name="where-res" select="list:member(key('resources', $query-res/sp:where/@rdf:nodeID, $ont-model), $ont-model)"/>
    <xsl:variable name="select-res" select="if ($matched-ont-class/rdfs:subClassOf/@rdf:resource = '&ldp;Container' and $query-res/sp:where/@rdf:nodeID) then gc:visit-elements(key('resources', $query-res/sp:where/@rdf:nodeID, $ont-model), '&sp;SubQuery')[rdf:type/@rdf:resource = '&sp;Select'] else ()" as="element()?"/>
    <xsl:variable name="orderBy" select="if ($select-res/sp:orderBy) then list:member(key('resources', $select-res/sp:orderBy/@rdf:nodeID, $ont-model), $ont-model) else ()"/>
    <xsl:variable name="config" select="document('../../../../../WEB-INF/web.xml')" as="document-node()"/>

    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
    <xsl:key name="predicates" match="*[@rdf:about]/* | *[@rdf:nodeID]/*" use="concat(namespace-uri(), local-name())"/>
    <xsl:key name="predicates-by-object" match="*[@rdf:about]/* | *[@rdf:nodeID]/*" use="@rdf:resource | @rdf:nodeID"/>
    <xsl:key name="resources-by-type" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="rdf:type/@rdf:resource"/>
    <xsl:key name="resources-by-container" match="*[@rdf:about]" use="sioc:has_space/@rdf:resource | sioc:has_parent/@rdf:resource | sioc:has_container/@rdf:resource"/>
    <xsl:key name="resources-by-space" match="*[@rdf:about]" use="sioc:has_space/@rdf:resource"/>
    <xsl:key name="resources-by-page-of" match="*[@rdf:about]" use="ldp:pageOf/@rdf:resource"/>
    <xsl:key name="resources-by-topic" match="*[@rdf:about] | *[@rdf:nodeID]" use="foaf:primaryTopic/@rdf:resource"/>
    <xsl:key name="resources-by-topic-of" match="*[@rdf:about] | *[@rdf:nodeID]" use="foaf:isPrimaryTopicOf/@rdf:resource"/>
    <xsl:key name="violations-by-path" match="*" use="spin:violationPath/@rdf:resource | spin:violationPath/@rdf:nodeID"/>
    <xsl:key name="violations-by-root" match="*[@rdf:about] | *[@rdf:nodeID]" use="spin:violationRoot/@rdf:resource | spin:violationRoot/@rdf:nodeID"/>
    <xsl:key name="init-param-by-name" match="javaee:init-param" use="javaee:param-name"/>

    <rdf:Description rdf:about="">
	<foaf:maker rdf:resource="http://graphityhq.com/#company"/>
    </rdf:Description>

    <rdf:Description rdf:about="http://graphityhq.com/#company">
        <dct:title>Graphity</dct:title>
    </rdf:Description>

    <rdf:Description rdf:about="&xhv;prev">
	<rdfs:label xml:lang="en">Previous</rdfs:label>
    </rdf:Description>

    <rdf:Description rdf:about="&xhv;next">
	<rdfs:label xml:lang="en">Next</rdfs:label>
    </rdf:Description>

    <xsl:template match="/">
	<html xml:lang="{$lang}">
            <xsl:apply-templates select="." mode="gc:HeadMode"/>
            <xsl:apply-templates select="." mode="gc:BodyMode"/>
	</html>
    </xsl:template>

    <xsl:template match="/" mode="gc:HeadMode">
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
    </xsl:template>
    
    <xsl:template match="/" mode="gc:BodyMode">
        <body>
            <xsl:apply-templates select="." mode="gc:NavBarMode"/>

            <xsl:variable name="grouped-rdf" as="document-node()">
                <xsl:apply-templates select="." mode="gc:GroupTriples"/>
            </xsl:variable>
            <xsl:apply-templates select="$grouped-rdf/rdf:RDF"/>

            <xsl:apply-templates select="." mode="gc:FooterMode"/>
        </body>
    </xsl:template>
    
    <xsl:template match="/" mode="gc:NavBarMode">
	<div class="navbar navbar-fixed-top">
	    <div class="navbar-inner">
		<div class="container-fluid">
                    <button class="btn btn-navbar" onclick="if ($('#collapsing-top-navbar').hasClass('in')) $('#collapsing-top-navbar').removeClass('collapse in').height(0); else $('#collapsing-top-navbar').addClass('collapse in').height('auto');">
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>

                    <a class="brand" href="{$base-uri}">
                        <xsl:for-each select="key('resources', $base-uri, document($base-uri))">
                            <img src="{foaf:logo/@rdf:resource}">
                                <xsl:attribute name="alt"><xsl:apply-templates select="." mode="gc:LabelMode"/></xsl:attribute>
                            </img>
                        </xsl:for-each>
                    </a>

                    <div id="collapsing-top-navbar" class="nav-collapse collapse">
                        <xsl:variable name="space" select="($absolute-path, key('resources', $absolute-path)/sioc:has_container/@rdf:resource)" as="xs:anyURI*"/>
                        
                        <ul class="nav">
                            <!-- make menu links for all resources in the ontology, except base URI -->
                            <xsl:apply-templates select="key('resources-by-space', $base-uri, document($base-uri))[not(@rdf:about = resolve-uri('sparql', $base-uri))][not(@rdf:about = resolve-uri('ontology', $base-uri))]" mode="gc:NavBarMode">
                                <xsl:sort select="gc:label(.)" order="ascending" lang="{$lang}"/>
                                <xsl:with-param name="space" select="$space"/>
                            </xsl:apply-templates>
                        </ul>

                        <xsl:if test="key('resources-by-space', $base-uri, document($base-uri))[@rdf:about = resolve-uri('sparql', $base-uri) or @rdf:about = resolve-uri('ontology', $base-uri)]">
                            <ul class="nav pull-right">
                                <xsl:apply-templates select="key('resources-by-space', $base-uri, document($base-uri))[@rdf:about = resolve-uri('sparql', $base-uri) or @rdf:about = resolve-uri('ontology', $base-uri)]" mode="gc:NavBarMode">
                                    <xsl:sort select="gc:label(.)" order="ascending" lang="{$lang}"/>
                                    <xsl:with-param name="space" select="$space"/>
                                </xsl:apply-templates>
                            </ul>
                        </xsl:if>
                    </div>
		</div>
	    </div>
	</div>
    </xsl:template>

    <xsl:template match="*[@rdf:about]" mode="gc:NavBarMode">
        <xsl:param name="space" as="xs:anyURI*"/>
        <li>
            <xsl:if test="@rdf:about = $space">
                <xsl:attribute name="class">active</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="@rdf:about" mode="gc:InlineMode"/>
        </li>
    </xsl:template>
    
    <xsl:template match="/" mode="gc:FooterMode">
        <div class="footer text-center">
            <p>
                <hr/>
                <xsl:value-of select="format-date(current-date(), '[Y]', $lang, (), ())"/>.
                Developed by <xsl:apply-templates select="key('resources', key('resources', '', document(''))/foaf:maker/@rdf:resource, document(''))/@rdf:about" mode="gc:InlineMode"/>.
                <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache License</a>.
            </p>
        </div>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="gc:TitleMode">
	<xsl:apply-templates select="key('resources', $base-uri, document($base-uri))" mode="gc:LabelMode"/>
	<xsl:text> - </xsl:text>
	<xsl:apply-templates select="key('resources', $absolute-path)" mode="gc:LabelMode"/>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="gc:StyleMode">
	<link href="static/css/bootstrap.css" rel="stylesheet" type="text/css"/>
	<link href="static/css/bootstrap-responsive.css" rel="stylesheet" type="text/css"/>
	<link href="static/org/graphity/client/css/bootstrap.css" rel="stylesheet" type="text/css"/>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="gc:ScriptMode">
	<script type="text/javascript" src="static/js/jquery.min.js"></script>
	<script type="text/javascript" src="static/js/bootstrap.js"></script>
        <xsl:if test="($default-mode, $mode) = '&gc;MapMode'">
            <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?sensor=false"/>
            <script type="text/javascript" src="static/org/graphity/client/js/google-maps.js"></script>
        </xsl:if>
    </xsl:template>

    <xsl:template match="rdf:RDF">
        <!-- *[not(@rdf:about = $absolute-path)] -->
        <xsl:param name="selected-resources" select="*[not(. is key('resources-by-page-of', $absolute-path))][not(key('predicates-by-object', @rdf:nodeID))]" as="element()*"/>

	<div class="container-fluid">
	    <div class="row-fluid">
		<div class="span8">
                    <xsl:apply-templates select="." mode="gc:PageHeaderMode"/>

                    <xsl:apply-templates select="." mode="gc:ModeSelectMode"/>

                    <!-- page resource -->
                    <xsl:apply-templates select="." mode="gc:PaginationMode">
                        <xsl:with-param name="count" select="count($selected-resources)" tunnel="yes"/>
                    </xsl:apply-templates>

                    <xsl:apply-templates select="." mode="gc:ModeChoiceMode">
                        <xsl:with-param name="selected-resources" select="$selected-resources" tunnel="yes"/>
                    </xsl:apply-templates>

                    <xsl:apply-templates select="." mode="gc:PaginationMode">
                        <xsl:with-param name="count" select="count($selected-resources)" tunnel="yes"/>
                    </xsl:apply-templates>
                </div>

		<div class="span4">
		    <xsl:apply-templates select="." mode="gc:SidebarNavMode"/>
		</div>
	    </div>
	</div>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="gc:ModeChoiceMode">
        <xsl:choose>
            <xsl:when test="(not($mode) and $default-mode = '&gc;ListMode') or $mode = '&gc;ListMode'">
                <xsl:apply-templates select="." mode="gc:ListMode"/>
            </xsl:when>
            <xsl:when test="(not($mode) and $default-mode = '&gc;ListReadMode') or $mode = '&gc;ListReadMode'">
                <xsl:apply-templates select="." mode="gc:ListReadMode"/>
            </xsl:when>
            <xsl:when test="(not($mode) and $default-mode = '&gc;TableMode') or $mode = '&gc;TableMode'">
                <xsl:apply-templates select="." mode="gc:TableMode"/>
            </xsl:when>
            <xsl:when test="(not($mode) and $default-mode = '&gc;ThumbnailMode') or $mode = '&gc;ThumbnailMode'">
                <xsl:apply-templates select="." mode="gc:ThumbnailMode"/>
            </xsl:when>
            <xsl:when test="(not($mode) and $default-mode = '&gc;MapMode') or $mode = '&gc;MapMode'">
                <xsl:apply-templates select="." mode="gc:MapMode"/>
            </xsl:when>
            <xsl:when test="(not($mode) and $default-mode = '&gc;EditMode') or $mode = '&gc;EditMode'">
                <xsl:apply-templates select="." mode="gc:EditMode"/>
            </xsl:when>
            <xsl:when test="(not($mode) and $default-mode = '&gc;CreateMode') or $mode = '&gc;CreateMode'">
                <xsl:apply-templates select="." mode="gc:CreateMode"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="." mode="gc:ReadMode"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="*" mode="gc:ModeSelectMode"/>

    <xsl:template match="rdf:RDF" mode="gc:ModeSelectMode">
        <xsl:if test="key('resources', $matched-ont-class/gc:mode/@rdf:resource, document('&gc;'))">
            <ul class="nav nav-tabs">
                <xsl:choose>
                    <xsl:when test="key('resources', $absolute-path)/rdf:type/@rdf:resource = ('&sioc;Space', '&sioc;Container')">
                        <xsl:apply-templates select="key('resources', $matched-ont-class/gc:mode/@rdf:resource, document('&gc;'))[rdf:type/@rdf:resource = '&gc;ContainerMode']" mode="#current">
                            <xsl:sort select="gc:label(.)"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="key('resources', $matched-ont-class/gc:mode/@rdf:resource, document('&gc;'))[rdf:type/@rdf:resource = '&gc;ItemMode']" mode="#current">
                            <xsl:sort select="gc:label(.)"/>                    
                        </xsl:apply-templates>
                    </xsl:otherwise>
                </xsl:choose>
            </ul>
        </xsl:if>
    </xsl:template>

    <xsl:template match="gc:Mode | *[rdf:type/@rdf:resource = '&gc;Mode']" mode="gc:ModeSelectMode">
	<li>
	    <xsl:if test="(not($mode) and $default-mode = @rdf:about) or $mode = @rdf:about">
		<xsl:attribute name="class">active</xsl:attribute>
	    </xsl:if>

	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>
	</li>	
    </xsl:template>

    <xsl:template match="@rdf:about" mode="gc:ModeSelectMode">
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

    <!-- READ MODE -->

    <xsl:template match="rdf:RDF" mode="gc:ReadMode">
        <xsl:apply-templates mode="#current">
            <xsl:sort select="gc:label(.)" lang="{$lang}"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:ReadMode"/>
    
    <xsl:template match="*[*][@rdf:about = $absolute-path][not(key('resources', foaf:primaryTopic/@rdf:resource))]" mode="gc:ReadMode" priority="1">
        <xsl:param name="nested" as="xs:boolean?"/>
        
        <!-- show root blank nodes or nested blank nodes -->
        <xsl:if test="not(key('predicates-by-object', @rdf:nodeID)) or $nested">
            <xsl:apply-templates select="." mode="gc:PropertyListMode"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about or @rdf:nodeID][not(@rdf:about = $absolute-path)][not(. is key('resources-by-page-of', $absolute-path))]" mode="gc:ReadMode" priority="1">
        <xsl:param name="nested" as="xs:boolean?"/>
        
        <!-- show root blank nodes or nested blank nodes -->
        <xsl:if test="not(key('predicates-by-object', @rdf:nodeID)) or $nested">
            <xsl:apply-templates select="." mode="gc:HeaderMode"/>

            <xsl:apply-templates select="." mode="gc:PropertyListMode"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*[@rdf:about][key('resources', foaf:primaryTopic/@rdf:resource)]" mode="gc:ListReadMode" priority="2">
        <div class="well">
            <xsl:apply-templates select="." mode="gc:HeaderMode"/>
            
            <xsl:for-each select="key('resources', foaf:primaryTopic/@rdf:resource)">
                <xsl:apply-templates select="." mode="gc:HeaderMode"/>

                <xsl:apply-templates select="." mode="gc:PropertyListMode"/>
            </xsl:for-each>
        </div>
    </xsl:template>

    <xsl:template match="*[@rdf:about][key('resources', foaf:isPrimaryTopicOf/@rdf:resource)]" mode="gc:ListReadMode" priority="2"/>
    
    <!-- HEADER MODE -->

    <xsl:template match="rdf:RDF" mode="gc:PageHeaderMode">
        <xsl:apply-templates mode="#current"/>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about = $absolute-path]" mode="gc:PageHeaderMode" priority="1">
	<div class="well header">
            <xsl:apply-templates select="." mode="gc:ImageMode"/>
            
            <xsl:apply-templates select="." mode="gc:ModeToggleMode"/>

            <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>
            
            <xsl:apply-templates select="." mode="gc:DescriptionMode"/>

            <xsl:apply-templates select="." mode="gc:MediaTypeSelectMode"/>

	    <xsl:apply-templates select="." mode="gc:TypeListMode"/>            
        </div>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:PageHeaderMode"/>

    <xsl:template match="@rdf:about | @rdf:nodeID" mode="gc:PageHeaderMode">
        <xsl:apply-templates select="." mode="gc:HeaderMode"/>
    </xsl:template>

    <xsl:template match="*[@rdf:about = $absolute-path][key('resources', foaf:primaryTopic/@rdf:resource)]" mode="gc:PageHeaderMode" priority="2">
        <div class="well well-small clearfix">
            <xsl:apply-templates select="." mode="gc:InlinePropertyListMode"/>
            
            <xsl:apply-templates select="." mode="gc:MediaTypeSelectMode"/>
        </div>
    </xsl:template>

    <!--
    <xsl:template match="rdf:RDF" mode="gc:HeaderMode">
        <xsl:param name="selected-resources" as="element()*" tunnel="yes"/>

        <xsl:apply-templates select="key('resources', $absolute-path)" mode="#current"/>
    </xsl:template>
    -->
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:HeaderMode" priority="1">
	<div class="well header">
            <xsl:apply-templates select="." mode="gc:ImageMode"/>
            
            <xsl:apply-templates select="." mode="gc:ModeToggleMode"/>

            <!-- <xsl:apply-templates select="." mode="gc:MediaTypeSelectMode"/> -->

            <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>
            
            <xsl:apply-templates select="." mode="gc:DescriptionMode"/>

	    <xsl:apply-templates select="." mode="gc:TypeListMode"/>
	</div>
    </xsl:template>

    <xsl:template match="*[@rdf:about][key('resources', foaf:primaryTopic/@rdf:resource)]" mode="gc:HeaderMode" priority="2">
        <div class="well well-small clearfix">
            <xsl:apply-templates select="." mode="gc:InlinePropertyListMode"/>
            
            <xsl:apply-templates select="." mode="gc:MediaTypeSelectMode"/>
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

    <!-- MEDIA TYPE SELECT MODE (Export buttons) -->
    
    <xsl:template match="*[*][@rdf:about]" mode="gc:MediaTypeSelectMode">
        <div class="btn-group pull-right" onclick="$(this).toggleClass('open');">
            <div class="btn dropdown-toggle">Export <span class="caret"></span></div>
            <ul class="dropdown-menu">
                <li>
                    <a href="{@rdf:about}?accept={encode-for-uri('application/rdf+xml')}">RDF/XML</a>
                </li>
                <li>
                    <a href="{@rdf:about}?accept={encode-for-uri('text/turtle')}">Turtle</a>
                </li>
                <xsl:if test="@rdf:about = $absolute-path and $query-res/sp:text">
                    <li>
                        <a href="{resolve-uri('sparql', $base-uri)}?query={encode-for-uri($query-res/sp:text)}">SPARQL</a>
                    </li>
                </xsl:if>
            </ul>
        </div>
    </xsl:template>

    <!-- MODE TOGGLE MODE (Create/Edit buttons) -->

    <xsl:template match="*" mode="gc:ModeToggleMode"/>
    
    <xsl:template match="*[starts-with(@rdf:about, $base-uri)]" mode="gc:ModeToggleMode" priority="1">
        <xsl:if test="not($mode = '&gc;EditMode')">
            <div class="pull-right">
                <a class="btn btn-primary" href="{gc:document-uri(@rdf:about)}{gc:query-string((), xs:anyURI('&gc;EditMode'))}">
                    <xsl:apply-templates select="key('resources', '&gc;EditMode', document('&gc;'))" mode="gc:LabelMode"/>
                </a>                        
            </div>
        </xsl:if>
        <!--
        <xsl:if test="not($mode = '&gc;CreateMode') and rdf:type/@rdf:resource = '&sioc;Container'">
            <div class="pull-right">
                <a class="btn btn-primary" href="{gc:document-uri(@rdf:about)}{gc:query-string((), xs:anyURI('&gc;CreateMode'))}">
                    <xsl:apply-templates select="key('resources', '&gc;CreateMode', document('&gc;'))" mode="gc:LabelMode"/>
                </a>
            </div>
        </xsl:if>
        -->
    </xsl:template>
    
    <!-- IMAGE MODE -->
        
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:ImageMode">
        <xsl:variable name="images" as="element()*">
            <xsl:apply-templates mode="#current"/>
        </xsl:variable>
        <xsl:if test="$images">
            <div class="carousel slide">
                <div class="carousel-inner">
                    <xsl:for-each select="$images">
                        <div class="item">
                            <xsl:if test="position() = 1">
                                <xsl:attribute name="class">active item</xsl:attribute>
                            </xsl:if>
                            <xsl:copy-of select="."/>
                        </div>
                    </xsl:for-each>
                    <a class="carousel-control left" onclick="$(this).parents('.carousel').carousel('prev');">&#8249;</a>
                    <a class="carousel-control right" onclick="$(this).parents('.carousel').carousel('next');">&#8250;</a>
                </div>
            </div>
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
	<xsl:param name="count" as="xs:integer" tunnel="yes"/>

        <ul class="pager">
            <li class="previous">
                <xsl:choose>
                    <xsl:when test="xhv:prev">
                        <xsl:apply-templates select="xhv:prev" mode="#current"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="class">previous disabled</xsl:attribute>
                        <a>
                            &#8592; <xsl:apply-templates select="key('resources', '&xhv;prev', document(''))" mode="gc:LabelMode"/>
                        </a>
                    </xsl:otherwise>
                </xsl:choose>
            </li>
            <li class="next">
                <xsl:choose>
                    <xsl:when test="xhv:next and $count &gt;= $limit">
                        <xsl:apply-templates select="xhv:next" mode="#current"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="class">next disabled</xsl:attribute>
                        <a>
                            <xsl:apply-templates select="key('resources', '&xhv;next', document(''))" mode="gc:LabelMode"/> &#8594;
                        </a>
                    </xsl:otherwise>
                </xsl:choose>
            </li>
        </ul>
    </xsl:template>

    <xsl:template match="xhv:prev[@rdf:resource]" mode="gc:PaginationMode">
        <a href="{@rdf:resource}" class="active">
            &#8592; <xsl:apply-templates select="key('resources', concat(namespace-uri(), local-name()), document(''))" mode="gc:LabelMode"/>
        </a>
    </xsl:template>
        
    <xsl:template match="xhv:next[@rdf:resource]" mode="gc:PaginationMode">
        <a href="{@rdf:resource}">
            <xsl:apply-templates select="key('resources', concat(namespace-uri(), local-name()), document(''))" mode="gc:LabelMode"/> &#8594;
        </a>        
    </xsl:template>

    <!-- LIST MODE -->

    <xsl:template match="rdf:RDF" mode="gc:ListMode">
        <!-- <xsl:param name="selected-resources" as="element()*" tunnel="yes"/> -->

        <xsl:apply-templates mode="#current">
            <xsl:sort select="gc:label(.)" lang="{$lang}"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:ListMode"/>
    
    <xsl:template match="*[*][@rdf:about or @rdf:nodeID][not(@rdf:about = $absolute-path)][not(. is key('resources-by-page-of', $absolute-path))][not(key('predicates-by-object', @rdf:nodeID))]" mode="gc:ListMode" priority="1">
	<div class="well">
            <xsl:apply-templates select="." mode="gc:ImageMode"/>
            
            <xsl:apply-templates select="." mode="gc:ModeToggleMode"/>

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

    <xsl:template match="*[@rdf:about][key('resources', foaf:primaryTopic/@rdf:resource)]" mode="gc:ListMode" priority="2">
        <div class="well">
            <xsl:apply-templates select="." mode="gc:HeaderMode"/>
            
            <xsl:apply-templates select="key('resources', foaf:primaryTopic/@rdf:resource)" mode="gc:HeaderMode"/>
        </div>
    </xsl:template>

    <xsl:template match="*[@rdf:about][key('resources', foaf:isPrimaryTopicOf/@rdf:resource)]" mode="gc:ListMode" priority="2"/>

    <!-- LIST READ MODE -->
    
    <xsl:template match="rdf:RDF" mode="gc:ListReadMode">
        <xsl:apply-templates mode="#current">
            <xsl:sort select="gc:label(.)" lang="{$lang}"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:ListReadMode"/>
    
    <xsl:template match="*[*][@rdf:about = $absolute-path][not(key('resources', foaf:primaryTopic/@rdf:resource))]" mode="gc:ListReadMode" priority="1">
        <xsl:apply-templates select="." mode="gc:ReadMode"/>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about or @rdf:nodeID][not(@rdf:about = $absolute-path)][not(. is key('resources-by-page-of', $absolute-path))]" mode="gc:ListReadMode" priority="1">
        <xsl:apply-templates select="." mode="gc:ReadMode"/>
    </xsl:template>

    <!-- TABLE MODE -->

    <xsl:template match="rdf:RDF" mode="gc:TableMode">
        <xsl:param name="selected-resources" as="element()*" tunnel="yes"/>
	<xsl:param name="predicates" as="element()*">
	    <xsl:for-each-group select="$selected-resources/*" group-by="concat(namespace-uri(), local-name())">
		<xsl:sort select="gc:property-label(.)" order="ascending" lang="{$lang}"/>
		<xsl:sequence select="current-group()[1]"/>
	    </xsl:for-each-group>
	</xsl:param>

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
        <xsl:param name="selected-resources" as="element()*" tunnel="yes"/>
	<xsl:param name="thumbnails-per-row" select="2" as="xs:integer"/>

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

    <!-- MAP MODE -->
    
    <xsl:template match="rdf:RDF" mode="gc:MapMode">
        <div id="map-canvas"/>

        <!-- apply all other URI resources -->
        <xsl:apply-templates mode="#current">
            <xsl:sort select="gc:label(.)" lang="{$lang}"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:MapMode"/>

    <xsl:template match="*[*][@rdf:about or @rdf:nodeID][geo:lat][geo:long]" mode="gc:MapMode" priority="1">
        <xsl:param name="nested" as="xs:boolean?"/>

        <script type="text/javascript">
            <![CDATA[
                function initialize]]><xsl:value-of select="generate-id()"/><![CDATA[()
                {
                    var latLng = new google.maps.LatLng(]]><xsl:value-of select="geo:lat[1]"/>, <xsl:value-of select="geo:long[1]"/><![CDATA[);
                    var marker = new google.maps.Marker({
                        position: latLng,
                        map: map,
                        title: "]]><xsl:apply-templates select="." mode="gc:LabelMode"/><![CDATA["
                    });
                }

                google.maps.event.addDomListener(window, 'load', initialize]]><xsl:value-of select="generate-id()"/><![CDATA[);
            ]]>
        </script>
    </xsl:template>

    <!-- CREATE MODE -->
    
    <xsl:template match="rdf:RDF" mode="gc:CreateMode">
        <xsl:param name="add-statements" select="true()" as="xs:boolean?" tunnel="yes"/>

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

                <xsl:if test="$add-statements">
                    <div class="control-group">
                        <button type="button" class="btn add-statement" title="Add new statement">&#x271A;</button>
                    </div>
                </xsl:if>
	    </fieldset>

	    <div class="form-actions">
		<button type="submit" class="btn btn-primary create-mode">Save</button>
	    </div>
	</form>
    </xsl:template>
    
    <!-- EDIT MODE -->
    
    <xsl:template match="rdf:RDF" mode="gc:EditMode">
        <xsl:param name="selected-resources" as="element()*" tunnel="yes"/>

        <form class="form-horizontal" method="post" action="{$absolute-path}?_method=PUT&amp;mode={encode-for-uri($mode)}" accept-charset="UTF-8"> <!-- enctype="multipart/form-data" -->
	    <xsl:comment>This form uses RDF/POST encoding: http://www.lsrn.org/semweb/rdfpost.html</xsl:comment>
	    <xsl:call-template name="gc:InputTemplate">
		<xsl:with-param name="name" select="'rdf'"/>
		<xsl:with-param name="type" select="'hidden'"/>
	    </xsl:call-template>

            <!-- <xsl:variable name="selected-resources" select="*[not(key('predicates-by-object', @rdf:nodeID))]"/> -->
	    <xsl:apply-templates select="$selected-resources" mode="#current">
                <xsl:sort select="gc:label(.)"/>
            </xsl:apply-templates>

            <div class="form-actions">
		<button type="submit" class="btn btn-primary">Save</button>
	    </div>
	</form>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:EditMode">
        <xsl:param name="instance" select="." as="element()"/>
        <xsl:param name="constraint-violations" select="key('violations-by-root', $instance/(@rdf:about, @rdf:nodeID), root($instance))" as="element()*"/>
        <xsl:param name="add-statements" select="true()" as="xs:boolean?" tunnel="yes"/>
        
	<fieldset id="fieldset-{generate-id()}">
            <xsl:if test="$instance/@rdf:about or not(key('predicates-by-object', $instance/@rdf:nodeID))">
                <legend>
                    <xsl:apply-templates select="$instance/@rdf:about | $instance/@rdf:nodeID" mode="gc:InlineMode"/>
                </legend>
            </xsl:if>

            <xsl:apply-templates select="$instance/@rdf:about | $instance/@rdf:nodeID" mode="#current"/>

            <xsl:apply-templates select="$instance/* | *[not(concat(namespace-uri(), local-name()) = $instance/*/concat(namespace-uri(), local-name()))]" mode="#current">
                <xsl:sort select="gc:property-label(.)"/>
                <xsl:with-param name="constraint-violations" select="$constraint-violations"/>
            </xsl:apply-templates>
            
            <!--
            <xsl:if test="$add-statements">
                <div class="control-group">
                    <button type="button" class="btn add-statement" title="Add new statement">&#x271A;</button>
                </div>
            </xsl:if>
            -->
	</fieldset>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:InlinePropertyListMode">
        <xsl:apply-templates mode="#current">
            <xsl:sort select="gc:label(.)" lang="{$lang}"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:OptionMode">
        <xsl:param name="selected" as="xs:string*"/>

        <option value="{@rdf:about | @rdf:nodeID}">
            <xsl:if test="(@rdf:about, @rdf:nodeID) = $selected">
                <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="." mode="gc:LabelMode"/>
        </option>
    </xsl:template>
    
</xsl:stylesheet>