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
    <!ENTITY xhv    "http://www.w3.org/1999/xhtml/vocab#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY owl    "http://www.w3.org/2002/07/owl#">
    <!ENTITY geo    "http://www.w3.org/2003/01/geo/wgs84_pos#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY http   "http://www.w3.org/2011/http#">
    <!ENTITY dct    "http://purl.org/dc/terms/">
    <!ENTITY foaf   "http://xmlns.com/foaf/0.1/">
    <!ENTITY sp     "http://spinrdf.org/sp#">
    <!ENTITY spin   "http://spinrdf.org/spin#">
    <!ENTITY dqc    "http://semwebquality.org/ontologies/dq-constraints#">
    <!ENTITY void   "http://rdfs.org/ns/void#">
    <!ENTITY sioc   "http://rdfs.org/sioc/ns#">
    <!ENTITY list   "http://jena.hpl.hp.com/ARQ/list#">
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
xmlns:http="&http;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:sp="&sp;"
xmlns:spin="&spin;"
xmlns:void="&void;"
xmlns:list="&list;"
xmlns:xhv="&xhv;"
xmlns:geo="&geo;"
xmlns:url="&java;java.net.URLDecoder"
xmlns:javaee="http://java.sun.com/xml/ns/javaee"
xmlns:saxon="http://saxon.sf.net/"
exclude-result-prefixes="#all">

    <xsl:import href="group-sort-triples.xsl"/>

    <xsl:include href="sparql.xsl"/>
    <xsl:include href="functions.xsl"/>

    <xsl:output method="xhtml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" media-type="application/xhtml+xml"/>
    
    <xsl:param name="gp:baseUri" as="xs:anyURI"/>
    <xsl:param name="gp:absolutePath" as="xs:anyURI"/>
    <xsl:param name="gp:requestUri" as="xs:anyURI"/>
    <xsl:param name="gp:httpHeaders" as="xs:string"/>
    <xsl:param name="gp:lang" select="'en'" as="xs:string"/>
    <xsl:param name="gp:mode" as="xs:anyURI?"/>
    <xsl:param name="gp:offset" as="xs:integer?"/>
    <xsl:param name="gp:limit" as="xs:integer?"/>
    <xsl:param name="gp:orderBy" as="xs:string?"/>
    <xsl:param name="gp:desc" as="xs:boolean?"/>
    <xsl:param name="gp:forClass" as="xs:anyURI?"/>
    <xsl:param name="gc:uri" select="$gp:absolutePath" as="xs:anyURI"/>
    <xsl:param name="gc:contextUri" as="xs:anyURI?"/>
    <xsl:param name="gc:endpointUri" as="xs:anyURI?"/>
    <xsl:param name="rdf:type" as="xs:anyURI?"/>
    <xsl:param name="gp:sitemap" select="if ($rdf:type) then document(gc:document-uri($rdf:type)) else ()" as="document-node()?"/>
    <xsl:param name="gc:defaultMode" select="if (not(/rdf:RDF/*/rdf:type/@rdf:resource = '&http;Response') and key('resources', $rdf:type, $gp:sitemap)/gc:defaultMode/@rdf:resource) then xs:anyURI(key('resources', $rdf:type, $gp:sitemap)/gc:defaultMode/@rdf:resource) else (if (key('resources', $gc:uri)/rdf:type/@rdf:resource = '&gp;Container') then xs:anyURI('&gc;ListMode') else xs:anyURI('&gc;ReadMode'))" as="xs:anyURI"/>
    <xsl:param name="query" as="xs:string?"/>

    <xsl:variable name="main-doc" select="/" as="document-node()"/>
    
    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
    <xsl:key name="predicates" match="*[@rdf:about]/* | *[@rdf:nodeID]/*" use="concat(namespace-uri(), local-name())"/>
    <xsl:key name="predicates-by-object" match="*[@rdf:about]/* | *[@rdf:nodeID]/*" use="@rdf:resource | @rdf:nodeID"/>
    <xsl:key name="resources-by-type" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="rdf:type/@rdf:resource"/>
    <xsl:key name="resources-by-container" match="*[@rdf:about]" use="sioc:has_parent/@rdf:resource | sioc:has_container/@rdf:resource"/>
    <xsl:key name="resources-by-page-of" match="*[@rdf:about]" use="gp:pageOf/@rdf:resource"/>
    <xsl:key name="resources-by-constructor-of" match="*[@rdf:about]" use="gp:constructorOf/@rdf:resource"/>
    <xsl:key name="resources-by-layout-of" match="*[@rdf:about]" use="gc:layoutOf/@rdf:resource"/>
    <xsl:key name="violations-by-path" match="*" use="spin:violationPath/@rdf:resource | spin:violationPath/@rdf:nodeID"/>
    <xsl:key name="violations-by-root" match="*[@rdf:about] | *[@rdf:nodeID]" use="spin:violationRoot/@rdf:resource | spin:violationRoot/@rdf:nodeID"/>
    <xsl:key name="constraints-by-type" match="*[rdf:type/@rdf:resource = '&dqc;MissingProperties']" use="sp:arg1/@rdf:resource | sp:arg1/@rdf:nodeID"/>
    <xsl:key name="restrictions-by-container" match="*[rdf:type/@rdf:resource = '&owl;Restriction'][owl:onProperty/@rdf:resource = ('&sioc;has_parent', '&sioc;has_container')]" use="owl:allValuesFrom/@rdf:resource"/>

    <xsl:preserve-space elements="rdfs:label dct:title gp:slug gp:uriTemplate gp:skolemTemplate gp:defaultOrderBy"/>

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

    <rdf:Description rdf:nodeID="delete">
	<rdfs:label xml:lang="en">Delete</rdfs:label>
    </rdf:Description>

    <xsl:template match="/">
	<html xml:lang="{$gp:lang}">
            <xsl:apply-templates select="." mode="gc:HeadMode"/>
            <xsl:apply-templates select="." mode="gc:BodyMode"/>
	</html>
    </xsl:template>

    <xsl:template match="/" mode="gc:HeadMode">
        <head>
            <title>
                <xsl:apply-templates mode="gc:TitleMode"/>
            </title>
            <base href="{$gp:baseUri}" />

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

                    <a class="brand" href="{$gp:baseUri}">
                        <xsl:for-each select="key('resources', $gp:baseUri, document($gp:baseUri))">
                            <img src="{foaf:logo/@rdf:resource}">
                                <xsl:attribute name="alt"><xsl:apply-templates select="." mode="gc:LabelMode"/></xsl:attribute>
                            </img>
                        </xsl:for-each>
                    </a>

                    <div id="collapsing-top-navbar" class="nav-collapse collapse">
                        <xsl:variable name="space" select="($gc:uri, key('resources', $gc:uri)/sioc:has_container/@rdf:resource)" as="xs:anyURI*"/>
                        
                        <ul class="nav">
                            <!-- make menu links for all containers in the ontology -->
                            <xsl:apply-templates select="key('resources-by-container', $gp:baseUri, document($gp:baseUri))[not(rdf:type/@rdf:resource = '&gp;SPARQLEndpoint')]" mode="gc:NavBarMode">
                                <xsl:sort select="gc:label(.)" order="ascending" lang="{$gp:lang}"/>
                                <xsl:with-param name="space" select="$space"/>
                            </xsl:apply-templates>
                        </ul>

                        <xsl:if test="key('resources-by-type', '&gp;SPARQLEndpoint', document($gp:baseUri))">
                            <ul class="nav pull-right">
                                <xsl:apply-templates select="key('resources-by-type', '&gp;SPARQLEndpoint', document($gp:baseUri))" mode="gc:NavBarMode">
                                    <xsl:sort select="gc:label(.)" order="ascending" lang="{$gp:lang}"/>
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
                <xsl:value-of select="format-date(current-date(), '[Y]', $gp:lang, (), ())"/>.
                Developed by <xsl:apply-templates select="key('resources', key('resources', '', document(''))/foaf:maker/@rdf:resource, document(''))/@rdf:about" mode="gc:InlineMode"/>.
                <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache License</a>.
            </p>
        </div>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="gc:TitleMode">
	<xsl:apply-templates select="key('resources', $gp:baseUri, document($gp:baseUri))" mode="gc:LabelMode"/>
	<xsl:text> - </xsl:text>
	<xsl:apply-templates select="key('resources', $gc:uri)" mode="gc:LabelMode"/>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="gc:StyleMode">
	<link href="{resolve-uri('static/css/bootstrap.css', $gc:contextUri)}" rel="stylesheet" type="text/css"/>
	<link href="{resolve-uri('static/css/bootstrap-responsive.css', $gc:contextUri)}" rel="stylesheet" type="text/css"/>
	<link href="{resolve-uri('static/org/graphity/client/css/bootstrap.css', $gc:contextUri)}" rel="stylesheet" type="text/css"/>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="gc:ScriptMode">
	<script type="text/javascript" src="{resolve-uri('static/js/jquery.min.js', $gc:contextUri)}"></script>
	<script type="text/javascript" src="{resolve-uri('static/js/bootstrap.js', $gc:contextUri)}"></script>
        <script type="text/javascript" src="{resolve-uri('static/org/graphity/client/js/jquery.js', $gc:contextUri)}"></script>
        <xsl:if test="($gc:defaultMode, $gp:mode) = '&gc;MapMode'">
            <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?sensor=false"/>
            <script type="text/javascript" src="{resolve-uri('static/org/graphity/client/js/google-maps.js', $gc:contextUri)}"></script>
        </xsl:if>
        <xsl:if test="($gc:defaultMode, $gp:mode) = ('&gc;EditMode', '&gp;ConstructMode')">
            <script type="text/javascript" src="{resolve-uri('static/org/graphity/client/js/UUID.js', $gc:contextUri)}"></script>
        </xsl:if>
    </xsl:template>

    <xsl:template match="rdf:RDF[key('resources', $gc:uri)/rdf:type/@rdf:resource = 'http://graphity.org/gcs#DescribeLabelResources']" priority="1">
        <xsl:next-match>
            <xsl:with-param name="selected-resources" select="*[rdf:type/@rdf:resource = '&gp;Item']" tunnel="yes"/>
        </xsl:next-match>
    </xsl:template>
    
    <xsl:template match="rdf:RDF">
        <xsl:param name="selected-resources" select="key('resources-by-container', $gc:uri)" as="element()*" tunnel="yes"/>

        <div class="container-fluid">
	    <div class="row-fluid">
		<div class="span8">
                    <xsl:apply-templates select="." mode="gc:BreadCrumbMode"/>

                    <xsl:if test="not((not($gp:mode) and $gc:defaultMode = '&gc;ReadMode') or $gp:mode = '&gc;ReadMode')">
                        <xsl:apply-templates select="." mode="gc:HeaderMode"/>
                    </xsl:if>

                    <xsl:apply-templates select="." mode="gc:ModeSelectMode"/>

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

    <xsl:template match="rdf:RDF[key('resources-by-type', '&http;Response')][not(key('resources-by-type', '&spin;ConstraintViolation'))]" mode="gc:ModeChoiceMode" priority="1">
        <xsl:apply-templates select="." mode="gc:ReadMode"/>
    </xsl:template>
    
    <xsl:template match="rdf:RDF" mode="gc:ModeChoiceMode">
        <xsl:choose>
            <xsl:when test="(not($gp:mode) and $gc:defaultMode = '&gc;ListMode') or $gp:mode = '&gc;ListMode'">
                <xsl:apply-templates select="." mode="gc:ListMode"/>
            </xsl:when>
            <xsl:when test="(not($gp:mode) and $gc:defaultMode = '&gc;TableMode') or $gp:mode = '&gc;TableMode'">
                <xsl:apply-templates select="." mode="gc:TableMode"/>
            </xsl:when>
            <xsl:when test="(not($gp:mode) and $gc:defaultMode = '&gc;ThumbnailMode') or $gp:mode = '&gc;ThumbnailMode'">
                <xsl:apply-templates select="." mode="gc:ThumbnailMode"/>
            </xsl:when>
            <xsl:when test="(not($gp:mode) and $gc:defaultMode = '&gc;MapMode') or $gp:mode = '&gc;MapMode'">
                <xsl:apply-templates select="." mode="gc:MapMode"/>
            </xsl:when>
            <xsl:when test="(not($gp:mode) and $gc:defaultMode = '&gc;EditMode') or $gp:mode = '&gc;EditMode'">
                <xsl:apply-templates select="." mode="gc:EditMode"/>
            </xsl:when>
            <xsl:when test="(not($gp:mode) and $gc:defaultMode = '&gp;ConstructMode') or $gp:mode = '&gp;ConstructMode'">
                <xsl:apply-templates select="." mode="gp:ConstructMode"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="." mode="gc:ReadMode"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- MODE SELECT MODE -->
    
    <xsl:template match="rdf:RDF" mode="gc:ModeSelectMode">
        <xsl:if test="key('resources-by-constructor-of', $gc:uri) | key('resources-by-page-of', $gc:uri) | key('resources-by-layout-of', $gc:uri)">
            <ul class="nav nav-tabs">
                <xsl:apply-templates select="key('resources-by-constructor-of', $gc:uri)" mode="#current"/>
                <xsl:apply-templates select="key('resources-by-page-of', $gc:uri) | key('resources-by-layout-of', $gc:uri)" mode="#current">
                    <xsl:sort select="gp:mode/@rdf:resource/gc:object-label(.)"/>
                </xsl:apply-templates>
            </ul>
        </xsl:if>
    </xsl:template>

    <xsl:template match="rdf:RDF[*/rdf:type/@rdf:resource = '&http;Response']" mode="gc:ModeSelectMode" priority="1"/>
    
    <xsl:template match="*[*][@rdf:about][gp:mode/@rdf:resource]" mode="gc:ModeSelectMode" priority="1">
	<li>
	    <xsl:if test="(not($gp:mode) and $gc:defaultMode = gp:mode/@rdf:resource) or $gp:mode = gp:mode/@rdf:resource">
		<xsl:attribute name="class">active</xsl:attribute>
	    </xsl:if>

            <a href="{@rdf:about}">
                <xsl:apply-templates select="gp:mode/@rdf:resource" mode="gc:ObjectLabelMode"/>
            </a>
	</li>	
    </xsl:template>

    <xsl:template match="*" mode="gc:ModeSelectMode"/>
        
    <!-- BREADCRUMB MODE -->

    <xsl:template match="rdf:RDF" mode="gc:BreadCrumbMode"/>
        
    <xsl:template match="rdf:RDF[key('resources', $gc:uri)]" mode="gc:BreadCrumbMode" priority="1">
        <ul class="breadcrumb">
            <xsl:apply-templates select="key('resources', $gc:uri)" mode="#current"/>
        </ul>
    </xsl:template>

    <xsl:template match="rdf:RDF[$gc:uri = $gp:baseUri]" mode="gc:BreadCrumbMode" priority="2"/>

    <xsl:template match="rdf:RDF[*/rdf:type/@rdf:resource = '&http;Response']" mode="gc:BreadCrumbMode" priority="3"/>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:BreadCrumbMode">
        <!-- walk up the parents recursively -->
        <xsl:choose>
            <xsl:when test="key('resources', sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource)">
                <xsl:apply-templates select="key('resources', sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource)" mode="#current"/>
            </xsl:when>
            <xsl:when test="sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource">
                <xsl:variable name="parent-doc" select="document(sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource)" as="document-node()?"/>
                <xsl:apply-templates select="key('resources', sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource, $parent-doc)" mode="#current"/>
            </xsl:when>
        </xsl:choose>

        <li>
            <xsl:if test="@rdf:about = $gc:uri">
                <xsl:attribute name="class">active</xsl:attribute>
            </xsl:if>
            
            <xsl:apply-templates select="@rdf:about" mode="gc:InlineMode"/>
            
            <xsl:if test="not(@rdf:about = $gc:uri)">
                <span class="divider">/</span>
            </xsl:if>
        </li>
    </xsl:template>
        
    <!-- HEADER MODE -->

    <xsl:template match="rdf:RDF" mode="gc:HeaderMode">
        <xsl:apply-templates select="key('resources', $gc:uri)" mode="#current"/>
    </xsl:template>
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:HeaderMode">
        <xsl:param name="id" select="generate-id()" as="xs:string?"/>
        <xsl:param name="class" select="'well header'" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>            
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            
            <xsl:apply-templates select="." mode="gc:ImageMode"/>
            
            <xsl:apply-templates select="." mode="gc:ModeToggleMode"/>

            <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>
            
            <xsl:apply-templates select="." mode="gc:DescriptionMode"/>

            <xsl:apply-templates select="." mode="gc:MediaTypeSelectMode"/>

	    <xsl:apply-templates select="." mode="gc:TypeListMode"/>
        </div>
    </xsl:template>
    
    <xsl:template match="@rdf:about[. = $gc:uri] | @rdf:about[../foaf:isPrimaryTopicOf/@rdf:resource = $gc:uri]" mode="gc:HeaderMode" priority="1">
	<h1 class="page-header">
	    <xsl:apply-templates select="." mode="gc:InlineMode"/>
	</h1>
    </xsl:template>

    <xsl:template match="@rdf:about | @rdf:nodeID" mode="gc:HeaderMode">
	<h1>
	    <xsl:apply-templates select="." mode="gc:InlineMode"/>
	</h1>
    </xsl:template>

    <xsl:template match="@rdf:nodeID[../rdf:type/@rdf:resource = '&http;Response']" mode="gc:HeaderMode" priority="1">
        <div class="alert alert-error">
            <h1>
                <xsl:apply-templates select="." mode="gc:InlineMode"/>
            </h1>
        </div>
    </xsl:template>

    <!-- MEDIA TYPE SELECT MODE (Export buttons) -->

    <xsl:template match="*" mode="gc:MediaTypeSelectMode"/>
    
    <xsl:template match="*[rdf:type/@rdf:resource = '&foaf;Document']" mode="gc:MediaTypeSelectMode" priority="1">
        <div class="btn-group pull-right" onclick="$(this).toggleClass('open');">
            <div class="btn dropdown-toggle">Export <span class="caret"></span></div>
            <ul class="dropdown-menu">
                <li>
                    <a href="{@rdf:about}?accept={encode-for-uri('application/rdf+xml')}">RDF/XML</a>
                </li>
                <li>
                    <a href="{@rdf:about}?accept={encode-for-uri('text/turtle')}">Turtle</a>
                </li>
                <!--
                <xsl:if test="@rdf:about = $gc:uri and $query-res/sp:text">
                    <li>
                        <a href="{resolve-uri('sparql', $gp:baseUri)}?query={encode-for-uri($query-res/sp:text)}">SPARQL</a>
                    </li>
                </xsl:if>
                -->
            </ul>
        </div>
    </xsl:template>

    <xsl:template match="*[key('resources', foaf:isPrimaryTopicOf/@rdf:resource)]" mode="gc:MediaTypeSelectMode" priority="2">
        <xsl:apply-templates select="key('resources', foaf:isPrimaryTopicOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <!-- MODE TOGGLE MODE (Create/Edit buttons) -->

    <xsl:template match="*" mode="gc:ModeToggleMode"/>
    
    <xsl:template match="*[@rdf:about]" mode="gc:ModeToggleMode" priority="1">
        <xsl:if test="not(rdf:type/@rdf:resource = '&gp;Container')">
            <div class="pull-right">
                <form action="{gc:document-uri(@rdf:about)}?_method=DELETE" method="post">
                    <button class="btn btn-primary" type="submit">
                        <xsl:apply-templates select="key('resources', 'delete', document(''))" mode="gc:LabelMode"/>
                    </button>
                </form>
            </div>
        </xsl:if>
        <xsl:if test="not($gp:mode = '&gc;EditMode') and not(rdf:type/@rdf:resource = '&gp;Container')">
            <div class="pull-right">
                <a class="btn btn-primary" href="{gc:document-uri(@rdf:about)}{gc:query-string((), xs:anyURI('&gc;EditMode'))}">
                    <xsl:apply-templates select="key('resources', '&gc;EditMode', document('&gc;'))" mode="gc:LabelMode"/>
                </a>                        
            </div>
        </xsl:if>
        <xsl:if test="not($gp:mode = '&gp;ConstructMode') and key('resources-by-constructor-of', @rdf:about)">
            <div class="pull-right">
                <a class="btn btn-primary" href="{key('resources-by-constructor-of', @rdf:about)/@rdf:about}">
                    <xsl:apply-templates select="key('resources', '&gp;ConstructMode', document('&gp;'))" mode="gc:LabelMode"/>
                </a>
            </div>
        </xsl:if>
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

    <xsl:template match="*[key('resources', foaf:primaryTopic/@rdf:resource)]" mode="gc:ImageMode" priority="1">
        <xsl:apply-templates select="key('resources', foaf:primaryTopic/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <!-- LABEL MODE -->
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:LabelMode">
        <xsl:variable name="labels" as="xs:string*">
            <xsl:variable name="lang-labels" as="xs:string*">
                <xsl:apply-templates select="*[lang($gp:lang)]" mode="#current"/>
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

    <xsl:template match="*[key('resources', foaf:primaryTopic/@rdf:resource)]" mode="gc:LabelMode" priority="1">
        <xsl:apply-templates select="key('resources', foaf:primaryTopic/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <!-- DESCRIPTION MODE -->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:DescriptionMode">
        <xsl:variable name="descriptions" as="xs:string*">
            <xsl:variable name="lang-descriptions" as="xs:string*">
                <xsl:apply-templates select="*[lang($gp:lang)]" mode="#current"/>
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

    <xsl:template match="*[key('resources', foaf:primaryTopic/@rdf:resource)]" mode="gc:DescriptionMode" priority="1">
        <xsl:apply-templates select="key('resources', foaf:primaryTopic/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <!-- INLINE LIST MODE -->
    
    <xsl:template match="*" mode="gc:TypeListMode"/>

    <xsl:template match="*[rdf:type/@rdf:resource]" mode="gc:TypeListMode" priority="1">
        <ul class="inline">
            <xsl:apply-templates select="rdf:type" mode="#current">
                <xsl:sort select="gc:object-label(@rdf:resource)" data-type="text" order="ascending" lang="{$gp:lang}"/>
            </xsl:apply-templates>
        </ul>
    </xsl:template>

    <xsl:template match="*[key('resources', foaf:primaryTopic/@rdf:resource)]" mode="gc:TypeListMode" priority="2">
        <xsl:apply-templates select="key('resources', foaf:primaryTopic/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="rdf:type[@rdf:resource]" mode="gc:TypeListMode" priority="1">
        <li>
	    <xsl:apply-templates select="@rdf:resource" mode="gc:InlineMode"/>
	</li>
    </xsl:template>

    <!-- PROPERTY LIST MODE -->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:PropertyListMode">
        <xsl:variable name="properties" as="element()*">
            <xsl:apply-templates mode="#current">
                <xsl:sort select="gc:property-label(.)" data-type="text" order="ascending" lang="{$gp:lang}"/>
            </xsl:apply-templates>
        </xsl:variable>

        <xsl:if test="$properties">
            <dl class="dl-horizontal">
                <xsl:copy-of select="$properties"/>
            </dl>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*[key('resources', foaf:primaryTopic/@rdf:resource)]" mode="gc:PropertyListMode" priority="1">
        <xsl:apply-templates select="key('resources', foaf:primaryTopic/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <!-- SIDEBAR NAV MODE -->
    
    <xsl:template match="rdf:RDF" mode="gc:SidebarNavMode">
	<xsl:for-each-group select="*/*" group-by="concat(namespace-uri(), local-name())">
	    <xsl:sort select="gc:property-label(.)" data-type="text" order="ascending" lang="{$gp:lang}"/>
	    <xsl:apply-templates select="current-group()[1]" mode="#current">
                <xsl:sort select="gc:object-label(@rdf:resource)" data-type="text" order="ascending"/>
            </xsl:apply-templates>
	</xsl:for-each-group>	
    </xsl:template>

    <!-- PAGINATION MODE -->

    <xsl:template match="rdf:RDF" mode="gc:PaginationMode" priority="1">
        <xsl:apply-templates select="key('resources-by-page-of', $gc:uri)" mode="#current"/>
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
                    <xsl:when test="xhv:next and $count &gt;= $gp:limit">
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
        <xsl:param name="selected-resources" as="element()*" tunnel="yes"/>

        <xsl:apply-templates select="$selected-resources" mode="#current">
            <xsl:sort select="gc:label(.)" lang="{$gp:lang}"/>
        </xsl:apply-templates>
    </xsl:template>
                
    <xsl:template match="*[*][@rdf:about or @rdf:nodeID]" mode="gc:ListMode">
        <xsl:param name="id" select="generate-id()" as="xs:string?"/>
        <xsl:param name="class" select="'well'" as="xs:string?"/>
        
	<div>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>            
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>

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
    
    <!-- READ MODE -->
    
    <xsl:template match="rdf:RDF" mode="gc:ReadMode">
        <xsl:apply-templates select="key('resources', $gc:uri)" mode="#current"/>

        <xsl:apply-templates select="*[not(@rdf:about = $gc:uri)][not(key('predicates-by-object', @rdf:nodeID))]" mode="#current">
            <xsl:sort select="gc:label(.)" lang="{$gp:lang}"/>
        </xsl:apply-templates>
    </xsl:template>

    <!-- hide metadata -->
    <xsl:template match="*[gp:constructorOf/@rdf:resource = $gc:uri] | *[gp:pageOf/@rdf:resource = $gc:uri] | *[gc:layoutOf/@rdf:resource]" mode="gc:ReadMode" priority="1"/>

    <!-- hide document if topic is present -->
    <xsl:template match="*[key('resources', foaf:primaryTopic/@rdf:resource)]" mode="gc:ReadMode" priority="1"/>
        
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:ReadMode">
        <xsl:param name="id" select="generate-id()" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        
        <xsl:apply-templates select="." mode="gc:HeaderMode"/>

        <xsl:apply-templates select="." mode="gc:PropertyListMode"/>
    </xsl:template>
            
    <!-- TABLE MODE -->

    <xsl:template match="rdf:RDF" mode="gc:TableMode">
        <xsl:param name="selected-resources" as="element()*" tunnel="yes"/>
	<xsl:param name="predicates" as="element()*">
	    <xsl:for-each-group select="$selected-resources/* | key('resources', $selected-resources/foaf:primaryTopic/@rdf:resource)/*" group-by="concat(namespace-uri(), local-name())"> <!-- $selected-resources/* -->
		<xsl:sort select="gc:property-label(.)" order="ascending" lang="{$gp:lang}"/>
		<xsl:apply-templates select="current-group()[1]" mode="gc:TablePredicateMode"/>
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
		<xsl:apply-templates select="$selected-resources" mode="#current">
		    <xsl:with-param name="predicates" select="$predicates" tunnel="yes"/>
                    <xsl:sort select="gc:label(.)" lang="{$gp:lang}"/>
                </xsl:apply-templates>
	    </tbody>
	</table>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about or @rdf:nodeID]" mode="gc:TableMode">
        <xsl:param name="id" select="generate-id()" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="predicates" as="element()*" tunnel="yes"/>

	<tr>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>            
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            
	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>

	    <xsl:apply-templates select="$predicates" mode="gc:TableCellMode">
                <xsl:with-param name="resource" select="."/>
            </xsl:apply-templates>
	</tr>
    </xsl:template>

    <!-- hide system properties -->
    <xsl:template match="gp:* | gc:* | xhv:*" mode="gc:TablePredicateMode" priority="2"/>

    <xsl:template match="*[key('resources', foaf:primaryTopic/@rdf:resource)]/*" mode="gc:TablePredicateMode" priority="1"/>

    <xsl:template match="*[key('resources', foaf:primaryTopic/@rdf:resource)]" mode="gc:TableMode" priority="1">
        <xsl:apply-templates select="key('resources', foaf:primaryTopic/@rdf:resource)" mode="#current"/>
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
            <xsl:apply-templates select="$selected-resources" mode="#current">
                <xsl:sort select="gc:label(.)" lang="{$gp:lang}"/>
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

    <xsl:template match="*[@rdf:about or @rdf:nodeID]" mode="gc:ThumbnailMode">
        <xsl:param name="id" select="generate-id()" as="xs:string?"/>
        <xsl:param name="thumbnails-per-row" as="xs:integer"/>
        <xsl:param name="class" select="concat('span', 12 div $thumbnails-per-row)" as="xs:string?"/>
	
	<li>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>            
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            
	    <div class="thumbnail">
		<xsl:apply-templates select="." mode="gc:ImageMode"/>
		
		<div class="caption">
                    <xsl:apply-templates select="." mode="gc:ModeToggleMode"/>
                                
                    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>

		    <xsl:apply-templates select="." mode="gc:DescriptionMode"/>
		</div>
	    </div>
	</li>
    </xsl:template>
    
    <xsl:template match="@rdf:about | @rdf:nodeID" mode="gc:ThumbnailMode">
        <h2>
            <xsl:apply-templates select="." mode="gc:InlineMode"/>
        </h2>
    </xsl:template>

    <!-- MAP MODE -->
    
    <xsl:template match="rdf:RDF" mode="gc:MapMode">
        <xsl:param name="selected-resources" as="element()*" tunnel="yes"/>

        <div id="map-canvas"/>

        <!-- apply all other URI resources -->
        <xsl:apply-templates mode="#current"> <!-- select="$selected-resources" -->
            <xsl:sort select="gc:label(.)" lang="{$gp:lang}"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:MapMode"/>

    <xsl:template match="*[geo:lat castable as xs:double][geo:long castable as xs:double]" mode="gc:MapMode" priority="1">
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

    <!-- CONSTRUCT MODE -->
    
    <xsl:template match="rdf:RDF" mode="gp:ConstructMode">
        <xsl:param name="method" select="'post'" as="xs:string"/>
        <xsl:param name="forClass" select="$gp:forClass" as="xs:anyURI"/>
        <xsl:param name="mode" select="$gp:mode" as="xs:anyURI"/>
        <xsl:param name="action" select="xs:anyURI(concat($gc:uri, '?mode=', encode-for-uri($mode), '&amp;forClass=', encode-for-uri($forClass)))" as="xs:anyURI"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'form-horizontal'" as="xs:string?"/>
        <xsl:param name="button-class" select="'btn btn-primary'" as="xs:string?"/>
        <xsl:param name="accept-charset" select="'UTF-8'" as="xs:string?"/>
        <xsl:param name="enctype" as="xs:string?"/>
        <xsl:param name="template-doc" select="document($action)" as="document-node()"/>
                
        <form method="{$method}" action="{$action}">
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$accept-charset">
                <xsl:attribute name="accept-charset"><xsl:value-of select="$accept-charset"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$enctype">
                <xsl:attribute name="enctype"><xsl:value-of select="$enctype"/></xsl:attribute>
            </xsl:if>

            <xsl:comment>This form uses RDF/POST encoding: http://www.lsrn.org/semweb/rdfpost.html</xsl:comment>
	    <xsl:call-template name="gc:InputTemplate">
		<xsl:with-param name="name" select="'rdf'"/>
		<xsl:with-param name="type" select="'hidden'"/>
	    </xsl:call-template>

            <xsl:apply-templates select="key('resources-by-type', $forClass)" mode="#current">
                <xsl:with-param name="template-doc" select="$template-doc" tunnel="yes"/>
                <xsl:sort select="gc:label(.)"/>
            </xsl:apply-templates>
            
            <div class="form-actions">
		<button type="submit" class="{$button-class}">Save</button>
	    </div>
	</form>
    </xsl:template>
    
    <xsl:template match="*[@rdf:about = $gc:uri] | *[gp:constructorOf/@rdf:resource = $gc:uri] | *[gp:pageOf/@rdf:resource = $gc:uri]" mode="gp:ConstructMode" priority="1"/>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gp:ConstructMode">
        <xsl:param name="id" select="generate-id()" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>

        <xsl:apply-templates select="." mode="gc:EditMode">
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
        </xsl:apply-templates>
    </xsl:template>

    <!-- EDIT MODE -->
    
    <xsl:template match="rdf:RDF" mode="gc:EditMode">
        <xsl:param name="method" select="'post'" as="xs:string"/>   
        <xsl:param name="mode" select="$gp:mode" as="xs:anyURI"/>
        <xsl:param name="action" select="xs:anyURI(concat($gc:uri, '?_method=PUT&amp;mode=', encode-for-uri($mode)))" as="xs:anyURI"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'form-horizontal'" as="xs:string?"/>
        <xsl:param name="button-class" select="'btn btn-primary'" as="xs:string?"/>
        <xsl:param name="accept-charset" select="'UTF-8'" as="xs:string?"/>
        <xsl:param name="enctype" as="xs:string?"/>
        <xsl:param name="parent-uri" select="key('resources', $gc:uri)/(sioc:has_parent, sioc:has_container)/@rdf:resource" as="xs:anyURI?"/>
        <xsl:param name="parent-doc" select="document($parent-uri)" as="document-node()?"/>
        <xsl:param name="construct-uri" select="key('resources-by-constructor-of', $parent-uri, $parent-doc)/@rdf:about" as="xs:anyURI"/>
        <xsl:param name="template-doc" select="document($construct-uri)" as="document-node()"/>

        <form method="{$method}" action="{$action}">
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>            
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$accept-charset">
                <xsl:attribute name="accept-charset"><xsl:value-of select="$accept-charset"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$enctype">
                <xsl:attribute name="enctype"><xsl:value-of select="$enctype"/></xsl:attribute>
            </xsl:if>

            <xsl:comment>This form uses RDF/POST encoding: http://www.lsrn.org/semweb/rdfpost.html</xsl:comment>
	    <xsl:call-template name="gc:InputTemplate">
		<xsl:with-param name="name" select="'rdf'"/>
		<xsl:with-param name="type" select="'hidden'"/>
	    </xsl:call-template>

            <xsl:apply-templates select="*[not(key('predicates-by-object', @rdf:nodeID))]" mode="#current">
                <xsl:with-param name="template-doc" select="$template-doc" tunnel="yes"/>
                <xsl:sort select="gc:label(.)"/>
            </xsl:apply-templates>
                
            <div class="form-actions">
		<button type="submit" class="{$button-class}">Save</button>
	    </div>
	</form>
    </xsl:template>

    <!-- hide metadata -->
    <xsl:template match="*[gc:layoutOf/@rdf:resource = $gc:uri]" mode="gc:EditMode" priority="1"/>

    <xsl:template match="*[rdf:type/@rdf:resource = '&http;Response'] | *[rdf:type/@rdf:resource = '&spin;ConstraintViolation']" mode="gc:EditMode" priority="1"/>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:EditMode">
        <xsl:param name="id" select="generate-id()" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="legend" select="true()" as="xs:boolean"/>
        <xsl:param name="constraint-violations" select="key('violations-by-root', (@rdf:about, @rdf:nodeID))" as="element()*"/>
        <xsl:param name="template-doc" as="document-node()?" tunnel="yes"/>
        <xsl:param name="template" select="$template-doc/rdf:RDF/*[@rdf:nodeID][every $type in rdf:type/@rdf:resource satisfies current()/rdf:type/@rdf:resource = $type]" as="element()*"/>
        <xsl:param name="traversed-ids" select="@rdf:nodeID" as="xs:string*" tunnel="yes"/>

        <fieldset>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>            
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>

            <xsl:if test="$legend and (@rdf:about or not(key('predicates-by-object', @rdf:nodeID)))">
                <legend>
                    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:InlineMode"/>
                </legend>
            </xsl:if>

            <xsl:for-each select="$constraint-violations/rdfs:label">
                <div class="alert alert-error">
                    <xsl:apply-templates select="." mode="gc:LabelMode"/>
                </div>
            </xsl:for-each>

            <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>

            <xsl:if test="not($template)">
                <xsl:message>gc:EditMode is active but gp:ConstructMode is not defined for resource '<xsl:value-of select="@rdf:about | @rdf:nodeID"/>'</xsl:message>
            </xsl:if>
            <xsl:apply-templates select="* | $template/*[not(concat(namespace-uri(), local-name(), @xml:lang, @rdf:datatype) = current()/*/concat(namespace-uri(), local-name(), @xml:lang, @rdf:datatype))]" mode="#current">
                <xsl:sort select="gc:property-label(.)"/>
                <xsl:with-param name="constraint-violations" select="$constraint-violations" tunnel="yes"/>
                <xsl:with-param name="traversed-ids" select="$traversed-ids" tunnel="yes"/>
            </xsl:apply-templates>
        </fieldset>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[$gp:sitemap]" mode="gc:EditMode">
        <xsl:variable name="this" select="concat(namespace-uri(), local-name())"/>
        <xsl:next-match>
            <xsl:with-param name="required" select="not(preceding-sibling::*[concat(namespace-uri(), local-name()) = $this]) and key('constraints-by-type', ../rdf:type/@rdf:resource, $gp:sitemap)/sp:arg2/@rdf:resource = $this"/>
        </xsl:next-match>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[not($gp:sitemap)]" mode="gc:EditMode">
        <xsl:variable name="this" select="concat(namespace-uri(), local-name())"/>
        <xsl:next-match>
            <xsl:with-param name="required" select="not(preceding-sibling::*[concat(namespace-uri(), local-name()) = $this])"/>
        </xsl:next-match>
    </xsl:template>

    <!-- remove spaces -->
    <xsl:template match="text()" mode="gc:InputMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>

        <xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'ol'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
	    <xsl:with-param name="value" select="normalize-space(.)"/>
	</xsl:call-template>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:InlinePropertyListMode">
        <xsl:apply-templates mode="#current">
            <xsl:sort select="gc:label(.)" lang="{$gp:lang}"/>
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