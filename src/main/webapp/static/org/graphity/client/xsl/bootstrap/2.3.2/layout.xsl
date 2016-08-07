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
    <!ENTITY g      "http://graphity.org/g#">
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
xmlns:g="&g;"
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
xmlns:bs2="http://graphity.org/xsl/bootstrap/2.3.2"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:javaee="http://java.sun.com/xml/ns/javaee"
xmlns:saxon="http://saxon.sf.net/"
exclude-result-prefixes="#all">

    <xsl:import href="../../group-sort-triples.xsl"/>
    <xsl:import href="../../functions.xsl"/>
    <xsl:import href="../../imports/default.xsl"/>
    <xsl:import href="../../imports/dbpedia-owl.xsl"/>
    <xsl:import href="../../imports/dc.xsl"/>
    <xsl:import href="../../imports/dct.xsl"/>
    <xsl:import href="../../imports/doap.xsl"/>
    <xsl:import href="../../imports/foaf.xsl"/>
    <xsl:import href="../../imports/gp.xsl"/>
    <xsl:import href="../../imports/gr.xsl"/>
    <xsl:import href="../../imports/owl.xsl"/>
    <xsl:import href="../../imports/rdf.xsl"/>
    <xsl:import href="../../imports/rdfs.xsl"/>
    <xsl:import href="../../imports/sd.xsl"/>
    <xsl:import href="../../imports/sioc.xsl"/>
    <xsl:import href="../../imports/skos.xsl"/>
    <xsl:import href="../../imports/sp.xsl"/>
    <xsl:import href="imports/default.xsl"/>
    <xsl:import href="imports/dct.xsl"/>
    <xsl:import href="imports/foaf.xsl"/>
    <xsl:import href="imports/gp.xsl"/>
    <xsl:import href="imports/rdf.xsl"/>    
    <xsl:import href="imports/sioc.xsl"/>
    <xsl:import href="imports/sp.xsl"/>

    <xsl:include href="sparql.xsl"/>

    <xsl:output method="xhtml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" media-type="application/xhtml+xml"/>
    
    <xsl:param name="g:baseUri" as="xs:anyURI?"/>
    <xsl:param name="g:requestUri" as="xs:anyURI?"/>
    <xsl:param name="g:absolutePath" select="xs:anyURI(tokenize($g:requestUri,'\?')[1])" as="xs:anyURI?"/>
    <xsl:param name="g:httpHeaders" as="xs:string"/>
    <xsl:param name="gp:lang" select="'en'" as="xs:string"/>
    <xsl:param name="gc:contextUri" as="xs:anyURI?"/>
    <xsl:param name="gc:endpointUri" as="xs:anyURI?"/>
    <xsl:param name="gp:ontology" as="xs:anyURI?"/>
    <xsl:param name="rdf:type" as="xs:anyURI?"/>
    <xsl:param name="gc:sitemap" as="document-node()?"/>
    <xsl:param name="uri" as="xs:string?"/>
    <xsl:param name="query" as="xs:string?"/>
    <xsl:param name="label" as="xs:string?"/>
    
    <xsl:variable name="main-doc" select="/" as="document-node()"/>
    
    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
    <xsl:key name="predicates" match="*[@rdf:about]/* | *[@rdf:nodeID]/*" use="concat(namespace-uri(), local-name())"/>
    <xsl:key name="predicates-by-object" match="*[@rdf:about]/* | *[@rdf:nodeID]/*" use="@rdf:resource | @rdf:nodeID"/>
    <xsl:key name="resources-by-type" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="rdf:type/@rdf:resource"/>
    <xsl:key name="resources-by-container" match="*[@rdf:about] | *[@rdf:nodeID]" use="sioc:has_parent/@rdf:resource | sioc:has_container/@rdf:resource"/>
    <xsl:key name="resources-by-page-of" match="*[@rdf:about]" use="gp:pageOf/@rdf:resource"/>
    <xsl:key name="resources-by-view-of" match="*[@rdf:about]" use="gp:viewOf/@rdf:resource"/>    
    <xsl:key name="resources-by-layout-of" match="*[@rdf:about]" use="gc:layoutOf/@rdf:resource"/>
    <xsl:key name="resources-by-defined-by" match="*[@rdf:about]" use="rdfs:isDefinedBy/@rdf:resource"/>
    <xsl:key name="violations-by-path" match="*" use="spin:violationPath/@rdf:resource | spin:violationPath/@rdf:nodeID"/>
    <xsl:key name="violations-by-root" match="*[@rdf:about] | *[@rdf:nodeID]" use="spin:violationRoot/@rdf:resource | spin:violationRoot/@rdf:nodeID"/>
    <xsl:key name="restrictions-by-container" match="*[rdf:type/@rdf:resource = '&owl;Restriction'][owl:onProperty/@rdf:resource = ('&sioc;has_parent', '&sioc;has_container')]" use="owl:allValuesFrom/@rdf:resource"/>

    <!-- <xsl:preserve-space elements="rdfs:label dct:title gp:slug gp:uriTemplate gp:skolemTemplate gp:defaultOrderBy"/> -->

    <rdf:Description rdf:about="">
	<foaf:maker rdf:resource="http://atomgraph.com/#company"/>
    </rdf:Description>

    <rdf:Description rdf:about="http://atomgraph.com/#company">
        <dct:title>AtomGraph</dct:title>
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
            <xsl:apply-templates select="." mode="xhtml:Head"/>
            <xsl:apply-templates select="." mode="xhtml:Body"/>
	</html>
    </xsl:template>

    <xsl:template match="/" mode="xhtml:Head">
        <head>
            <xsl:apply-templates select="key('resources', $g:requestUri)" mode="xhtml:Title"/>

            <meta name="viewport" content="width=device-width, initial-scale=1.0"/>

            <xsl:apply-templates select="." mode="xhtml:Style"/>
            <xsl:apply-templates select="." mode="xhtml:Script"/>
        </head>
    </xsl:template>
    
    <xsl:template match="/" mode="xhtml:Body">
        <body>
            <xsl:apply-templates select="." mode="bs2:NavBar"/>

            <xsl:variable name="grouped-rdf" as="document-node()">
                <xsl:apply-templates select="." mode="gc:GroupTriples"/>
            </xsl:variable>
            <xsl:apply-templates select="$grouped-rdf/rdf:RDF"/>

            <xsl:apply-templates select="." mode="bs2:Footer"/>
        </body>
    </xsl:template>
    
    <xsl:template match="/" mode="bs2:NavBar">
	<div class="navbar navbar-fixed-top">
	    <div class="navbar-inner">
		<div class="container-fluid">
                    <button class="btn btn-navbar" onclick="if ($('#collapsing-top-navbar').hasClass('in')) $('#collapsing-top-navbar').removeClass('collapse in').height(0); else $('#collapsing-top-navbar').addClass('collapse in').height('auto');">
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>

                    <xsl:if test="$g:baseUri">
                        <a class="brand" href="{$g:baseUri}">
                            <xsl:for-each select="key('resources', $g:baseUri, document($g:baseUri))">
                                <img src="{foaf:logo/@rdf:resource}">
                                    <xsl:attribute name="alt"><xsl:apply-templates select="." mode="gc:label"/></xsl:attribute>
                                </img>
                            </xsl:for-each>
                        </a>
                    </xsl:if>                    

                    <div id="collapsing-top-navbar" class="nav-collapse collapse">
                        <form action="" method="get" class="navbar-form pull-left" accept-charset="UTF-8">
                            <div class="input-append">
                                <input type="text" name="uri" class="input-xxlarge">
                                    <xsl:if test="$uri">
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="$uri"/>
                                        </xsl:attribute>
                                    </xsl:if>
                                </input>
                                <button type="submit" class="btn btn-primary">Go</button>
                            </div>
                        </form>

                        <xsl:if test="$g:baseUri">
                            <xsl:variable name="space" select="($g:requestUri, key('resources', $g:requestUri)/sioc:has_container/@rdf:resource)" as="xs:anyURI*"/>
                            <xsl:if test="key('resources-by-type', '&gp;SPARQLEndpoint', document($g:baseUri))">
                                <ul class="nav pull-right">
                                    <xsl:apply-templates select="key('resources-by-type', '&gp;SPARQLEndpoint', document($g:baseUri))" mode="bs2:NavBarListItem">
                                        <xsl:sort select="gc:label(.)" order="ascending" lang="{$gp:lang}"/>
                                        <xsl:with-param name="space" select="$space"/>
                                    </xsl:apply-templates>
                                </ul>
                            </xsl:if>
                        </xsl:if>
                    </div>
		</div>
	    </div>
	</div>
    </xsl:template>
    
    <xsl:template match="*[@rdf:about]" mode="bs2:NavBarListItem">
        <xsl:param name="space" as="xs:anyURI*"/>
        <li>
            <xsl:if test="@rdf:about = $space">
                <xsl:attribute name="class">active</xsl:attribute>
            </xsl:if>
            
            <xsl:apply-templates select="." mode="xhtml:Anchor"/>
        </li>
    </xsl:template>
    
    <xsl:template match="/" mode="bs2:Footer">
        <div class="footer text-center">
            <p>
                <hr/>
                <xsl:value-of select="format-date(current-date(), '[Y]', $gp:lang, (), ())"/>.
                Developed by <xsl:apply-templates select="key('resources', key('resources', '', document(''))/foaf:maker/@rdf:resource, document(''))" mode="xhtml:Anchor"/>.
                <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache License</a>.
            </p>
        </div>
    </xsl:template>

    <!--
    <xsl:template match="rdf:RDF[key('resources-by-type', '&http;Response')][not(key('resources-by-type', '&spin;ConstraintViolation'))]" mode="xhtml:Title" priority="1">
	<xsl:apply-templates select="key('resources-by-type', '&http;Response')" mode="xhtml:Title"/>
    </xsl:template>
    -->
    
    <xsl:template match="*[key('resources', gc:layoutOf/@rdf:resource)]" mode="xhtml:Title" priority="2">
        <xsl:apply-templates select="key('resources', gc:layoutOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gc:uri/@rdf:resource)]" mode="xhtml:Title" priority="1">
        <xsl:apply-templates select="key('resources', gc:uri/@rdf:resource)" mode="#current"/>
    </xsl:template>
        
    <xsl:template match="*[key('resources', gp:pageOf/@rdf:resource)]" mode="xhtml:Title" priority="1">
        <xsl:apply-templates select="key('resources', gp:pageOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:viewOf/@rdf:resource)]" mode="xhtml:Title" priority="1">
        <xsl:apply-templates select="key('resources', gp:viewOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gc:constructorOf/@rdf:resource)]" mode="xhtml:Title" priority="1">
        NOPE<xsl:apply-templates select="key('resources', '&gc;ConstructMode', document('&gc;'))" mode="gc:label"/>
        <xsl:text> </xsl:text>
        <xsl:apply-templates select="key('resources', gc:forClass/@rdf:resource, document(gc:document-uri(gc:forClass/@rdf:resource)))" mode="gc:label"/>
    </xsl:template>

    <xsl:template match="*" mode="xhtml:Title">
        <title>
            <xsl:if test="$g:baseUri">
                <xsl:apply-templates select="key('resources', $g:baseUri, document($g:baseUri))" mode="gc:label"/>
                <xsl:text> - </xsl:text>
            </xsl:if>
            
            <xsl:apply-templates select="." mode="gc:label"/>            
        </title>
    </xsl:template>

    <!-- STYLE MODE -->
    <xsl:template match="/" mode="xhtml:Style">
	<link href="{resolve-uri('static/css/bootstrap.css', $gc:contextUri)}" rel="stylesheet" type="text/css"/>
    	<link href="{resolve-uri('static/css/bootstrap-responsive.css', $gc:contextUri)}" rel="stylesheet" type="text/css"/>
	<link href="{resolve-uri('static/org/graphity/client/css/bootstrap.css', $gc:contextUri)}" rel="stylesheet" type="text/css"/>
    </xsl:template>
    
    <!-- SCRIPT MODE -->

    <xsl:template match="/" mode="xhtml:Script">
	<script type="text/javascript" src="{resolve-uri('static/js/jquery.min.js', $gc:contextUri)}"></script>
	<script type="text/javascript" src="{resolve-uri('static/js/bootstrap.js', $gc:contextUri)}"></script>
        <script type="text/javascript" src="{resolve-uri('static/org/graphity/client/js/jquery.js', $gc:contextUri)}"></script>
        <xsl:if test="key('resources', $g:requestUri)/gc:mode/@rdf:resource = '&gc;MapMode' or key('resources', $g:requestUri)/gc:forClass/@rdf:resource">
            <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?sensor=false"/>
            <script type="text/javascript" src="{resolve-uri('static/org/graphity/client/js/google-maps.js', $gc:contextUri)}"></script>
        </xsl:if>
        <xsl:if test="key('resources', $g:requestUri)/gc:mode/@rdf:resource = '&gc;EditMode' or key('resources', $g:requestUri)/gc:forClass/@rdf:resource">
            <script type="text/javascript" src="{resolve-uri('static/org/graphity/client/js/UUID.js', $gc:contextUri)}"></script>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="rdf:RDF">
        <div class="container-fluid">
	    <div class="row-fluid">
                <xsl:choose>
                    <xsl:when test="key('resources-by-type', '&http;Response')">
                        <xsl:apply-templates select="key('resources-by-type', '&http;Response')" mode="bs2:BlockItem"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:for-each select="key('resources', $g:requestUri)">
                            <div class="span8">
                                <xsl:apply-templates select="." mode="bs2:BreadCrumbList"/>

                                <xsl:apply-templates select="." mode="bs2:Header"/>

                                <xsl:apply-templates select="." mode="bs2:ModeList"/>

                                <xsl:apply-templates select="." mode="bs2:PagerList"/>

                                <xsl:apply-templates select="." mode="gc:ModeChoiceMode"/>

                                <xsl:apply-templates select="." mode="bs2:PagerList"/>
                            </div>

                            <div class="span4">
                                <xsl:apply-templates select="." mode="bs2:PropertyNav"/>
                            </div>
                        </xsl:for-each>
                    </xsl:otherwise>
                </xsl:choose>
	    </div>
	</div>
    </xsl:template>

    <!--
    <xsl:template match="rdf:RDF[key('resources-by-type', '&http;Response')][not(key('resources-by-type', '&spin;ConstraintViolation'))]" mode="gc:ModeChoiceMode" priority="1">
        HELLO??<xsl:apply-templates select="key('resources-by-type', '&http;Response')" mode="bs2:Block"/>
    </xsl:template>
    -->
    
    <xsl:template match="*" mode="gc:ModeChoiceMode">
        <xsl:choose>
            <xsl:when test="gc:forClass/@rdf:resource">
                <xsl:apply-templates select="." mode="bs2:ConstructForm"/>
           </xsl:when>
            <xsl:when test="gc:mode/@rdf:resource = '&gc;ListMode'">
                <xsl:apply-templates select="." mode="bs2:BlockList"/>
            </xsl:when>
            <xsl:when test="gc:mode/@rdf:resource = '&gc;TableMode'">
                <xsl:apply-templates select="." mode="xhtml:Table"/>
            </xsl:when>
            <xsl:when test="gc:mode/@rdf:resource = '&gc;GridMode'">
                <xsl:apply-templates select="." mode="bs2:Grid"/>
            </xsl:when>
            <xsl:when test="gc:mode/@rdf:resource = '&gc;MapMode'">
                <xsl:apply-templates select="." mode="bs2:Map"/>
            </xsl:when>
            <xsl:when test="gc:mode/@rdf:resource = '&gc;GraphMode'">
                <xsl:apply-templates select="." mode="bs2:Graph"/>
            </xsl:when>
            <xsl:when test="gc:mode/@rdf:resource = '&gc;EditMode'">
                <xsl:apply-templates select="." mode="bs2:EditForm"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="." mode="bs2:Block"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- MODE SELECT MODE -->

    <!--
    <xsl:template match="rdf:RDF[key('resources-by-type', '&http;Response')][not(key('resources-by-type', '&spin;ConstraintViolation'))]" mode="bs2:ModeList" priority="2"/>

    <xsl:template match="rdf:RDF[key('resources', $g:requestUri)/gc:forClass]" mode="bs2:ModeList" priority="1"/>
    -->
                
    <xsl:template match="*[*][@rdf:about]" mode="bs2:ModeList" priority="3">
        <xsl:if test="gc:mode/@rdf:resource">
            <ul class="nav nav-tabs">
                <xsl:variable name="active" select="gc:mode/@rdf:resource" as="xs:anyURI"/>
                <xsl:for-each select="key('resources-by-layout-of', gc:layoutOf/@rdf:resource)">
                    <xsl:apply-templates select="." mode="bs2:ModeListItem">
                        <xsl:with-param name="active" select="$active"/>
                    </xsl:apply-templates>                    
                </xsl:for-each>
            </ul>
        </xsl:if>
    </xsl:template>
        
    <xsl:template match="*[*][@rdf:about]" mode="bs2:ModeListItem">
        <xsl:param name="active" as="xs:anyURI?"/>
        
        <li>
            <xsl:if test="gc:mode/@rdf:resource = $active">
                <xsl:attribute name="class">active</xsl:attribute>
            </xsl:if>

            <a href="{@rdf:about}">
                <xsl:apply-templates select="key('resources', gc:mode/@rdf:resource, document(gc:document-uri(gc:mode/@rdf:resource)))" mode="gc:label"/>
            </a>
        </li>
    </xsl:template>
        
    <!-- BREADCRUMB MODE -->

    <!-- <xsl:template match="rdf:RDF[key('resources-by-type', '&http;Response')][not(key('resources-by-type', '&spin;ConstraintViolation'))]" mode="bs2:BreadCrumbList" priority="1"/> -->

    <xsl:template match="*[key('resources', gc:layoutOf/@rdf:resource)]" mode="bs2:BreadCrumbList" priority="2">
        <xsl:apply-templates select="key('resources', gc:layoutOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gc:uri/@rdf:resource)]" mode="bs2:BreadCrumbList" priority="1">
        <xsl:apply-templates select="key('resources', gc:uri/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:pageOf/@rdf:resource)]" mode="bs2:BreadCrumbList" priority="1">
        <xsl:apply-templates select="key('resources', gp:pageOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:viewOf/@rdf:resource)]" mode="bs2:BreadCrumbList" priority="1">
        <xsl:apply-templates select="key('resources', gp:viewOf/@rdf:resource)" mode="#current"/>
    </xsl:template>
 
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:BreadCrumbList">
        <ul class="breadcrumb">
            <xsl:apply-templates select="." mode="bs2:BreadCrumbListItem"/>
        </ul>
    </xsl:template>

    <xsl:template match="*[@rdf:about]" mode="bs2:BreadCrumbListItem">
        <xsl:param name="leaf" select="true()" as="xs:boolean" tunnel="yes"/>

        <xsl:choose>
            <xsl:when test="key('resources', sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource)">
                <xsl:apply-templates select="key('resources', sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource)" mode="#current">
                    <xsl:with-param name="leaf" select="false()" tunnel="yes"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource">
                <xsl:variable name="parent-doc" select="document(sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource)" as="document-node()?"/>
                <xsl:apply-templates select="key('resources', sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource, $parent-doc)" mode="#current">
                    <xsl:with-param name="leaf" select="false()" tunnel="yes"/>
                </xsl:apply-templates>
            </xsl:when>
        </xsl:choose>
        
        <li>
            <xsl:apply-templates select="." mode="xhtml:Anchor"/>

            <xsl:if test="not($leaf)">
                <span class="divider">/</span>
            </xsl:if>
        </li>
    </xsl:template>
        
    <!-- HEADER MODE -->

    <!-- <xsl:template match="rdf:RDF[key('resources-by-type', '&http;Response')][not(key('resources-by-type', '&spin;ConstraintViolation'))]" mode="bs2:Header" priority="1"/> -->

    <xsl:template match="*[key('resources', gc:layoutOf/@rdf:resource)]" mode="bs2:Header" priority="2">
        <xsl:apply-templates select="key('resources', gc:layoutOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gc:uri/@rdf:resource)]" mode="bs2:Header" priority="1">
        <xsl:apply-templates select="key('resources', gc:uri/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:pageOf/@rdf:resource)]" mode="bs2:Header" priority="1">
        <xsl:apply-templates select="key('resources', gp:pageOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:viewOf/@rdf:resource)]" mode="bs2:Header" priority="1">
        <xsl:apply-templates select="key('resources', gp:viewOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gc:constructorOf/@rdf:resource)]" mode="bs2:Header" priority="1">
        <xsl:apply-templates select="key('resources', gc:constructorOf/@rdf:resource)" mode="#current"/>
    </xsl:template>
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:Header">
        <xsl:param name="id" select="generate-id()" as="xs:string?"/>
        <xsl:param name="class" select="'well header'" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>            
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>

            <xsl:apply-templates select="." mode="bs2:ImageMode"/>
            
            <xsl:apply-templates select="." mode="bs2:Actions"/>

            <h2>
                <xsl:apply-templates select="." mode="xhtml:Anchor"/>
            </h2>
            
            <p>
                <xsl:apply-templates select="." mode="gc:description"/>
            </p>

            <xsl:apply-templates select="." mode="bs2:MediaTypeList"/>

	    <xsl:apply-templates select="." mode="bs2:TypeList"/>
        </div>
    </xsl:template>

    <!--
    <xsl:template match="*[rdf:type/@rdf:resource = '&http;Response']" mode="bs2:Header" priority="1">
        <div class="alert alert-error">
            <h1>
                <xsl:apply-templates select="." mode="xhtml:Anchor"/>
            </h1>
        </div>
    </xsl:template>
    -->
    
    <!-- MEDIA TYPE SELECT MODE (Export buttons) -->

    <xsl:template match="*" mode="bs2:MediaTypeList"/>

    <!--
    <xsl:template match="*[key('resources', foaf:isPrimaryTopicOf/(@rdf:resource, @rdf:nodeID))]" mode="bs2:MediaTypeList" priority="2">
        <xsl:apply-templates select="key('resources', foaf:isPrimaryTopicOf/(@rdf:resource, @rdf:nodeID))" mode="#current"/>
    </xsl:template>
    -->
        
    <xsl:template match="*[@rdf:about]" mode="bs2:MediaTypeList" priority="1">
        <div class="btn-group pull-right">
            <div class="btn dropdown-toggle">Export <span class="caret"></span></div>
            <ul class="dropdown-menu">
                <li>
                    <a href="{@rdf:about}?accept={encode-for-uri('application/rdf+xml')}">RDF/XML</a>
                </li>
                <li>
                    <a href="{@rdf:about}?accept={encode-for-uri('text/turtle')}">Turtle</a>
                </li>
                <!--
                <xsl:if test="@rdf:about = $g:requestUri and $query-res/sp:text">
                    <li>
                        <a href="{resolve-uri('sparql', $g:baseUri)}?query={encode-for-uri($query-res/sp:text)}">SPARQL</a>
                    </li>
                </xsl:if>
                -->
            </ul>
        </div>
    </xsl:template>

    <!-- ACTIONS MODE (Create/Edit buttons) -->

    <xsl:template match="*" mode="bs2:Actions"/>
    
    <xsl:template match="*[@rdf:about]" mode="bs2:Actions" priority="1">
        <div class="pull-right">
            <form action="{gc:document-uri(@rdf:about)}?_method=DELETE" method="post">
                <button class="btn btn-primary btn-delete" type="submit">
                    <xsl:apply-templates select="key('resources', 'delete', document(''))" mode="gc:label"/>
                </button>
            </form>
        </div>

        <xsl:if test="key('resources-by-layout-of', key('resources', $g:requestUri)/gc:layoutOf/@rdf:resource)[gc:mode/@rdf:resource = '&gc;EditMode']/@rdf:about and not(key('resources', $g:requestUri)/gc:mode/@rdf:resource = '&gc;EditMode')">
            <div class="pull-right">
                <a class="btn btn-primary" href="{key('resources-by-layout-of', key('resources', $g:requestUri)/gc:layoutOf/@rdf:resource)[gc:mode/@rdf:resource = '&gc;EditMode']/@rdf:about}">
                    <xsl:apply-templates select="key('resources', '&gc;EditMode', document('&gc;'))" mode="gc:label"/>
                </a>
            </div>
        </xsl:if>

        <xsl:if test="$gc:sitemap">
            <xsl:variable name="resource" select="key('resources', $g:requestUri)" as="element()"/>
            
            <div class="btn-group pull-right">
                <div class="btn dropdown-toggle">
                    <xsl:apply-templates select="key('resources', '&gc;ConstructMode', document('&gc;'))" mode="gc:label"/>
                    <xsl:text> </xsl:text>
                    <span class="caret"></span>
                </div>

                <ul class="dropdown-menu">
                    <xsl:variable name="classes" select="key('resources-by-defined-by', $gp:ontology, $gc:sitemap)" as="element()*"/>
                    <xsl:for-each select="$classes">
                        <xsl:sort select="gc:label(.)"/>
                        <li>
                            <xsl:variable name="query-string" as="xs:string?">
                                <!-- query param order has to match HypermediaFilter's StateBuilder! -->
                                <xsl:variable name="temp-string">
                                    <xsl:if test="$resource/gc:mode/@rdf:resource">mode=<xsl:value-of select="encode-for-uri($resource/gc:mode/@rdf:resource)"/>&amp;</xsl:if>
                                    <xsl:text>forClass=</xsl:text><xsl:value-of select="encode-for-uri(@rdf:about)"/><xsl:text>&amp;</xsl:text>
                                    <xsl:if test="$resource/gc:uri/@rdf:resource">uri=<xsl:value-of select="encode-for-uri($resource/gc:uri/@rdf:resource)"/>&amp;</xsl:if>
                                </xsl:variable>
                                <xsl:if test="string-length($temp-string) &gt; 1">
                                    <xsl:sequence select="concat('?', substring($temp-string, 1, string-length($temp-string) - 1))"/>
                                </xsl:if>
                            </xsl:variable>
                            
                            <a href="{$query-string}" title="{@rdf:about}">
                                <xsl:apply-templates select="." mode="gc:label"/>
                            </a>
                        </li>
                    </xsl:for-each>
                </ul>
            </div>
        </xsl:if>
    </xsl:template>

    <!--
    <xsl:template match="*[@rdf:about][gc:mode/@rdf:resource]" mode="bs2:ButtonMode" priority="1"/>

    <xsl:template match="*[@rdf:about][gc:mode/@rdf:resource = '&gc;EditMode']" mode="bs2:ButtonMode" priority="2">
        <div class="pull-right">
            <a class="btn btn-primary" href="{@rdf:about}">
                <xsl:apply-templates select="key('resources', gc:mode/@rdf:resource, document(gc:document-uri(gc:mode/@rdf:resource)))" mode="gc:label"/>
            </a>                        
        </div>
    </xsl:template>

    <xsl:template match="*[@rdf:about][gc:forClass/@rdf:resource]" mode="bs2:ButtonMode" priority="2">
        <div class="pull-right">
            <a class="btn btn-primary" href="{@rdf:about}">
                <xsl:apply-templates select="key('resources', '&gc;ConstructMode', document('&gc;'))"/>
                <xsl:text> </xsl:text>
                <xsl:apply-templates select="key('resources', gc:forClass/@rdf:resource, document(gc:document-uri(gc:forClass/@rdf:resource)))" mode="gc:label"/>
            </a>                        
        </div>
    </xsl:template>
    -->
    
    <!-- IMAGE MODE -->
        
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:ImageMode">
        <xsl:variable name="images" as="element()*">
            <xsl:apply-templates mode="gc:image"/>
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
    
    <!-- INLINE LIST MODE -->

    <xsl:template match="*[key('resources', gc:layoutOf/@rdf:resource)]" mode="bs2:TypeList" priority="2">
        <xsl:apply-templates select="key('resources', gc:layoutOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gc:uri/@rdf:resource)]" mode="bs2:TypeList" priority="1">
        <xsl:apply-templates select="key('resources', gc:uri/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:pageOf/@rdf:resource)]" mode="bs2:TypeList" priority="1">
        <xsl:apply-templates select="key('resources', gp:pageOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:viewOf/@rdf:resource)]" mode="bs2:TypeList" priority="1">
        <xsl:apply-templates select="key('resources', gp:viewOf/@rdf:resource)" mode="#current"/>
    </xsl:template>
        
    <xsl:template match="*" mode="bs2:TypeList">
        <ul class="inline">
            <xsl:for-each select="rdf:type/@rdf:resource">
                <xsl:sort select="gc:object-label(.)" order="ascending" lang="{$gp:lang}"/>                
                <xsl:apply-templates select="key('resources', ., document(gc:document-uri(.)))" mode="bs2:TypeListItem"/>
            </xsl:for-each>
        </ul>
    </xsl:template>

    <xsl:template match="*[@rdf:about]" mode="bs2:TypeListItem">
        <li>
            <span title="{.}" class="btn btn-type">            
                <xsl:apply-templates select="." mode="xhtml:Anchor"/>
            </span>
	</li>
    </xsl:template>

    <!-- PROPERTY LIST MODE -->

    <xsl:template match="*[key('resources', gc:layoutOf/@rdf:resource)]" mode="bs2:PropertyList" priority="2">
        <xsl:apply-templates select="key('resources', gc:layoutOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gc:uri/@rdf:resource)]" mode="bs2:PropertyList" priority="1">
        <xsl:apply-templates select="key('resources', gc:uri/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:pageOf/@rdf:resource)]" mode="bs2:PropertyList" priority="1">
        <xsl:apply-templates select="key('resources', gp:pageOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:viewOf/@rdf:resource)]" mode="bs2:PropertyList" priority="1">
        <xsl:apply-templates select="key('resources', gp:viewOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:PropertyList">
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

    <!-- PROPERTY NAV MODE -->
    
    <xsl:template match="*[key('resources', gc:layoutOf/@rdf:resource)]" mode="bs2:PropertyNav" priority="2">
        <xsl:apply-templates select="key('resources', gc:layoutOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gc:uri/@rdf:resource)]" mode="bs2:PropertyNav" priority="1">
        <xsl:apply-templates select="key('resources', gc:uri/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:pageOf/@rdf:resource)]" mode="bs2:PropertyNav" priority="1">
        <xsl:apply-templates select="key('resources', gp:pageOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:viewOf/@rdf:resource)]" mode="bs2:PropertyNav" priority="1">
        <xsl:apply-templates select="key('resources', gp:viewOf/@rdf:resource)" mode="#current"/>
    </xsl:template>
    
    <xsl:template match="*[*]" mode="bs2:PropertyNav">
        <xsl:for-each-group select="*[key('resources', @rdf:resource | @rdf:nodeID)]" group-by="concat(namespace-uri(), local-name())">
            <xsl:sort select="gc:property-label(.)" order="ascending" lang="{$gp:lang}"/>

            <div class="well sidebar-nav">
                <h2 class="nav-header">
                    <xsl:apply-templates select="." mode="xhtml:Anchor"/>
                </h2>

                <ul class="nav nav-pills nav-stacked">
                    <xsl:for-each select="current-group()">
                        <xsl:sort select="gc:object-label(@rdf:resource | @rdf:nodeID)" order="ascending" lang="{$gp:lang}"/>
                        <xsl:apply-templates select="key('resources', @rdf:resource | @rdf:nodeID)" mode="xhtml:ListItem"/>
                    </xsl:for-each>
                </ul>
            </div>                    
        </xsl:for-each-group>
    </xsl:template>

    <!-- PAGINATION MODE -->
        
    <xsl:template match="*[key('resources', gc:layoutOf/@rdf:resource)]" mode="bs2:PagerList" priority="2">
        <xsl:apply-templates select="key('resources', gc:layoutOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*" mode="bs2:PagerList"/>

    <xsl:template match="*[xhv:prev/@rdf:resource] | *[xhv:next/@rdf:resource]" mode="bs2:PagerList">
        <ul class="pager">
            <li class="previous">
                <xsl:choose>
                    <xsl:when test="xhv:prev">
                        <a href="{xhv:prev/@rdf:resource}" class="active">
                            &#8592; <xsl:apply-templates select="key('resources', '&xhv;next', document(''))" mode="gc:label"/>
                        </a>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="class">previous disabled</xsl:attribute>
                        <a>
                            &#8592; <xsl:apply-templates select="key('resources', '&xhv;prev', document(''))" mode="gc:label"/>
                        </a>
                    </xsl:otherwise>
                </xsl:choose>
            </li>
            <li class="next">
                <xsl:choose>
                    <xsl:when test="xhv:next"> <!--  and $count &gt;= gp:limit -->
                        <a href="{xhv:next/@rdf:resource}" class="active">
                            <xsl:apply-templates select="key('resources', '&xhv;next', document(''))" mode="gc:label"/>  &#8594;
                        </a>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="class">next disabled</xsl:attribute>
                        <a>
                            <xsl:apply-templates select="key('resources', '&xhv;next', document(''))" mode="gc:label"/> &#8594;
                        </a>
                    </xsl:otherwise>
                </xsl:choose>
            </li>
        </ul>
    </xsl:template>

    <!-- LIST MODE -->

    <xsl:template match="*[key('resources', gc:layoutOf/@rdf:resource)]" mode="bs2:BlockList" priority="2">
        <xsl:apply-templates select="key('resources', gc:layoutOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gc:uri/@rdf:resource)]" mode="bs2:BlockList" priority="1">
        <xsl:apply-templates select="key('resources', gc:uri/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:pageOf/@rdf:resource)]" mode="bs2:BlockList" priority="1">
        <xsl:apply-templates select="key('resources', gp:pageOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:viewOf/@rdf:resource)]" mode="bs2:BlockList" priority="1">
        <xsl:apply-templates select="key('resources', gp:viewOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]" mode="bs2:BlockList">
        <xsl:apply-templates select="key('resources-by-container', @rdf:about)" mode="bs2:ListItem"/>
    </xsl:template>
    
    <xsl:template match="*[*][@rdf:about or @rdf:nodeID]" mode="bs2:ListItem">
        <xsl:param name="id" select="generate-id()" as="xs:string?"/>
        <xsl:param name="class" select="'well'" as="xs:string?"/>
        
	<div>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>            
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>

            <xsl:apply-templates select="." mode="bs2:ImageMode"/>
            
            <xsl:apply-templates select="." mode="bs2:Actions"/>

            <h2>
                <xsl:apply-templates select="." mode="xhtml:Anchor"/>
            </h2>

            <p>	    
                <xsl:apply-templates select="." mode="gc:description"/>
            </p>

	    <xsl:apply-templates select="." mode="bs2:TypeList"/>

	    <xsl:if test="@rdf:nodeID">
		<xsl:apply-templates select="." mode="bs2:PropertyList"/>
	    </xsl:if>
	</div>
    </xsl:template>
        
    <!-- READ MODE -->

    <xsl:template match="*[key('resources', gc:layoutOf/@rdf:resource)]" mode="bs2:Block" priority="2">
        <xsl:apply-templates select="key('resources', gc:layoutOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gc:uri/@rdf:resource)]" mode="bs2:Block" priority="1">
        <xsl:apply-templates select="key('resources', gc:uri/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:pageOf/@rdf:resource)]" mode="bs2:Block" priority="1">
        <xsl:apply-templates select="key('resources', gp:pageOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:viewOf/@rdf:resource)]" mode="bs2:Block" priority="1">
        <xsl:apply-templates select="key('resources', gp:viewOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:Block">
        <xsl:apply-templates select="key('resources-by-container', @rdf:about)" mode="bs2:BlockItem"/>
    </xsl:template>
        
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:BlockItem">
        <xsl:param name="id" select="generate-id()" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>

        <xsl:apply-templates select="." mode="bs2:Header"/>

        <xsl:apply-templates select="." mode="bs2:PropertyList"/>
    </xsl:template>
            
    <!-- GRID MODE -->
    
    <xsl:template match="*[key('resources', gc:layoutOf/@rdf:resource)]" mode="bs2:Grid" priority="2">
        <xsl:apply-templates select="key('resources', gc:layoutOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gc:uri/@rdf:resource)]" mode="bs2:Grid" priority="1">
        <xsl:apply-templates select="key('resources', gc:uri/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:pageOf/@rdf:resource)]" mode="bs2:Grid" priority="1">
        <xsl:apply-templates select="key('resources', gp:pageOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:viewOf/@rdf:resource)]" mode="bs2:Grid" priority="1">
        <xsl:apply-templates select="key('resources', gp:viewOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]" mode="bs2:Grid">
	<xsl:param name="thumbnails-per-row" select="2" as="xs:integer"/>

        <xsl:variable name="thumbnail-items" as="element()*">
            <!--
            <xsl:next-match>
                <xsl:with-param name="thumbnails-per-row" select="$thumbnails-per-row" tunnel="yes"/>
            </xsl:next-match>
            -->
            <xsl:apply-templates select="key('resources-by-container', @rdf:about)" mode="bs2:GridListItem">
                <xsl:with-param name="thumbnails-per-row" select="$thumbnails-per-row" tunnel="yes"/>                
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:for-each-group select="$thumbnail-items" group-adjacent="(position() - 1) idiv $thumbnails-per-row">
            <xsl:sort select="gc:label(.)" lang="{$gp:lang}"/>
            <div class="row-fluid">
                <ul class="thumbnails">
                    <xsl:copy-of select="current-group()"/>
                </ul>
            </div>
        </xsl:for-each-group>        
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]" mode="bs2:GridListItem">
        <xsl:param name="id" select="generate-id()" as="xs:string?"/>
        <xsl:param name="thumbnails-per-row" as="xs:integer" tunnel="yes"/>
        <xsl:param name="class" select="concat('span', 12 div $thumbnails-per-row)" as="xs:string?"/>
	
	<li>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>            
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            
	    <div class="thumbnail">
		<xsl:apply-templates select="." mode="bs2:ImageMode"/>
		
		<div class="caption">
                    <xsl:apply-templates select="." mode="bs2:Actions"/>

                    <h2>
                        <xsl:apply-templates select="." mode="xhtml:Anchor"/>
                    </h2>

		    <xsl:apply-templates select="." mode="gc:description"/>
		</div>
	    </div>
	</li>
    </xsl:template>

    <!-- TABLE MODE -->

    <xsl:template match="*[key('resources', gc:layoutOf/@rdf:resource)]" mode="xhtml:Table" priority="2">
        <xsl:apply-templates select="key('resources', gc:layoutOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gc:uri/@rdf:resource)]" mode="xhtml:Table" priority="1">
        <xsl:apply-templates select="key('resources', gc:uri/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:pageOf/@rdf:resource)]" mode="xhtml:Table" priority="1">
        <xsl:apply-templates select="key('resources', gp:pageOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:viewOf/@rdf:resource)]" mode="xhtml:Table" priority="1">
        <xsl:apply-templates select="key('resources', gp:viewOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] |  *[*][@rdf:nodeID]" mode="xhtml:Table">
        <xsl:param name="class" select="'table table-bordered table-striped'" as="xs:string?"/>
	<xsl:param name="predicates" as="element()*">
	    <xsl:for-each-group select="key('resources-by-container', @rdf:about)/*" group-by="concat(namespace-uri(), local-name())">
		<xsl:sort select="gc:property-label(.)" order="ascending" lang="{$gp:lang}"/>
		<xsl:apply-templates select="current-group()[1]" mode="gc:TablePredicateMode"/>
            </xsl:for-each-group>
	</xsl:param>

	<table>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            <thead>
		<tr>
		    <th>
			<xsl:apply-templates select="key('resources', '&rdfs;Resource', document('&rdfs;'))" mode="gc:label"/>
		    </th>
		    <xsl:apply-templates select="$predicates" mode="xhtml:TableHeaderCell"/>
		</tr>
	    </thead>
	    <tbody>
                <xsl:apply-templates select="key('resources-by-container', @rdf:about)" mode="xhtml:TableRow">
		    <xsl:with-param name="predicates" select="$predicates" tunnel="yes"/>                    
                </xsl:apply-templates>
	    </tbody>
	</table>
    </xsl:template>
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="xhtml:TableRow">
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
            
            <td>
                QQ<xsl:apply-templates select="." mode="xhtml:Anchor"/>/QQ
            </td>

	    <xsl:apply-templates select="$predicates" mode="gc:TableCellMode">
                <xsl:with-param name="resource" select="."/>
            </xsl:apply-templates>
	</tr>
    </xsl:template>

    <!--
    <xsl:template match="*[key('resources', foaf:primaryTopic/(@rdf:resource, @rdf:nodeID))]/*" mode="gc:TablePredicateMode" priority="1"/>
    -->
    
    <!-- MAP MODE -->
    
    <xsl:template match="*[key('resources', gc:layoutOf/@rdf:resource)]" mode="bs2:Map" priority="2">
        <xsl:apply-templates select="key('resources', gc:layoutOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gc:uri/@rdf:resource)]" mode="bs2:Map" priority="1">
        <xsl:apply-templates select="key('resources', gc:uri/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:pageOf/@rdf:resource)]" mode="bs2:Map" priority="1">
        <xsl:apply-templates select="key('resources', gp:pageOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:viewOf/@rdf:resource)]" mode="bs2:Map" priority="1">
        <xsl:apply-templates select="key('resources', gp:viewOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:Map">
        <div id="map-canvas">
            <xsl:apply-templates select="key('resources-by-container', @rdf:about)" mode="bs2:MapItemMode"/>
        </div>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:MapItemMode"/>

    <xsl:template match="*[geo:lat castable as xs:double][geo:long castable as xs:double]" mode="bs2:MapItemMode" priority="1">
        <xsl:param name="nested" as="xs:boolean?"/>

        <script type="text/javascript">
            <![CDATA[
                function initialize]]><xsl:value-of select="generate-id()"/><![CDATA[()
                {
                    var latLng = new google.maps.LatLng(]]><xsl:value-of select="geo:lat[1]"/>, <xsl:value-of select="geo:long[1]"/><![CDATA[);
                    var marker = new google.maps.Marker({
                        position: latLng,
                        map: map,
                        title: "]]><xsl:apply-templates select="." mode="gc:label"/><![CDATA["
                    });
                }

                google.maps.event.addDomListener(window, 'load', initialize]]><xsl:value-of select="generate-id()"/><![CDATA[);
            ]]>
        </script>
    </xsl:template>

    <!-- CONSTRUCT MODE -->
    
    <xsl:template match="*[@rdf:about]" mode="bs2:ConstructForm">
        <xsl:param name="method" select="'post'" as="xs:string"/>
        <xsl:param name="forClass" select="gc:forClass/@rdf:resource" as="xs:anyURI"/>
        <xsl:param name="action" select="$g:requestUri" as="xs:anyURI"/>
        <xsl:param name="id" select="generate-id()" as="xs:string?"/>
        <xsl:param name="class" select="'form-horizontal'" as="xs:string?"/>
        <xsl:param name="button-class" select="'btn btn-primary'" as="xs:string?"/>
        <xsl:param name="accept-charset" select="'UTF-8'" as="xs:string?"/>
        <xsl:param name="enctype" as="xs:string?"/>
        <xsl:param name="template-doc" select="gc:construct-doc($gp:ontology, $forClass)" as="document-node()?"/>
        <!-- <xsl:param name="template-doc" select="document(key('resources', $g:requestUri)/@rdf:about)" as="document-node()"/> -->
        <xsl:param name="resource" select="key('resources', gc:constructor/@rdf:nodeID)" as="element()" tunnel="yes"/>
        
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
	    <xsl:call-template name="xhtml:Input">
		<xsl:with-param name="name" select="'rdf'"/>
		<xsl:with-param name="type" select="'hidden'"/>
	    </xsl:call-template>

            <fieldset>
                <!--
                <xsl:for-each select="key('resources', $forClass, $template-doc)">
                    <legend>
                        <xsl:apply-templates select="key('resources', '&gc;ConstructMode', document('&gc;'))" mode="gc:label"/>
                        <xsl:text> </xsl:text>
                        <xsl:apply-templates select="." mode="gc:label"/>
                    </legend>
                    <xsl:if test="gc:description(.)">
                        <p class="text-info">
                            <xsl:apply-templates select="." mode="gc:description"/>
                        </p>
                    </xsl:if>
                </xsl:for-each>
                -->
                
                <xsl:apply-templates select="$resource" mode="bs2:Fieldset">
                    <xsl:with-param name="template-doc" select="$template-doc"/>
                    <xsl:sort select="gc:label(.)"/>
                </xsl:apply-templates>
            </fieldset>

            <xsl:apply-templates select="." mode="bs2:FormActions">
                <xsl:with-param name="button-class" select="$button-class"/>
            </xsl:apply-templates>
	</form>
    </xsl:template>
    
    <xsl:template match="*[@rdf:about]" mode="bs2:FormActions">
        <xsl:param name="button-class" select="'btn btn-primary'" as="xs:string?"/>
        
        <div class="form-actions">
            <button type="submit" class="{$button-class}">Save</button>
        </div>
    </xsl:template>

    <!-- EDIT MODE -->
                
    <xsl:template match="*[key('resources', gc:layoutOf/@rdf:resource)]" mode="bs2:EditForm" priority="2">
        A<xsl:apply-templates select="key('resources', gc:layoutOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gc:uri/@rdf:resource)]" mode="bs2:EditForm" priority="1">
        B<xsl:apply-templates select="key('resources', gc:uri/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:pageOf/@rdf:resource)]" mode="bs2:EditForm" priority="1">
        C<xsl:apply-templates select="key('resources', gp:pageOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', gp:viewOf/@rdf:resource)]" mode="bs2:EditForm" priority="1">
        D<xsl:apply-templates select="key('resources', gp:viewOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[rdf:type/@rdf:resource = '&http;Response'] | *[rdf:type/@rdf:resource = '&spin;ConstraintViolation']" mode="bs2:EditForm" priority="1"/>

    <xsl:template match="*[@rdf:about]" mode="bs2:EditForm">
        <xsl:param name="method" select="'post'" as="xs:string"/>
        <xsl:param name="action" select="xs:anyURI(concat($g:absolutePath, '?_method=PUT&amp;mode=', encode-for-uri('&gc;EditMode')))" as="xs:anyURI"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'form-horizontal'" as="xs:string?"/>
        <xsl:param name="button-class" select="'btn btn-primary'" as="xs:string?"/>
        <xsl:param name="accept-charset" select="'UTF-8'" as="xs:string?"/>
        <xsl:param name="enctype" as="xs:string?"/>

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
	    <xsl:call-template name="xhtml:Input">
		<xsl:with-param name="name" select="'rdf'"/>
		<xsl:with-param name="type" select="'hidden'"/>
	    </xsl:call-template>

            <xsl:apply-templates select="." mode="bs2:Fieldset"/>
                
            <xsl:apply-templates select="." mode="bs2:FormActions">
                <xsl:with-param name="button-class" select="$button-class"/>
            </xsl:apply-templates>
        </form>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:Fieldset">
        <xsl:param name="id" select="generate-id()" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="legend" select="if (@rdf:about) then true() else not(key('predicates-by-object', @rdf:nodeID))" as="xs:boolean"/>
        <xsl:param name="violations" select="key('violations-by-root', (@rdf:about, @rdf:nodeID))" as="element()*"/>
        <xsl:param name="template-doc" as="document-node()?"/>
        <xsl:param name="template" select="$template-doc/rdf:RDF/*[@rdf:nodeID][every $type in rdf:type/@rdf:resource satisfies current()/rdf:type/@rdf:resource = $type]" as="element()*"/>
        <xsl:param name="traversed-ids" select="@rdf:*" as="xs:string*" tunnel="yes"/>

        <fieldset>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>            
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>

            <xsl:if test="$legend">
                <legend>
                    <xsl:apply-templates select="." mode="xhtml:Anchor"/>
                </legend>
            </xsl:if>

            <xsl:apply-templates select="$violations" mode="bs2:ViolationMode"/>

            <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="bs2:FormControl"/>

            <xsl:if test="not($template)">
                <xsl:message>bs2:EditMode is active but spin:constructor is not defined for resource '<xsl:value-of select="@rdf:about | @rdf:nodeID"/>'</xsl:message>
            </xsl:if>
            <xsl:apply-templates select="* | $template/*[not(concat(namespace-uri(), local-name(), @xml:lang, @rdf:datatype) = current()/*/concat(namespace-uri(), local-name(), @xml:lang, @rdf:datatype))]" mode="bs2:FormControl">
                <xsl:sort select="gc:property-label(.)"/>
                <xsl:with-param name="violations" select="$violations"/>
                <xsl:with-param name="traversed-ids" select="$traversed-ids" tunnel="yes"/>
            </xsl:apply-templates>
        </fieldset>
    </xsl:template>

    <!-- CONSTRAINT VIOLATION MODE -->
    
    <xsl:template match="*" mode="bs2:ViolationMode"/>

    <xsl:template match="*[rdf:type/@rdf:resource = '&spin;ConstraintViolation']" mode="bs2:ViolationMode" priority="1">
	<xsl:param name="class" select="'alert alert-error'" as="xs:string?"/>

        <div>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>            
            <xsl:apply-templates select="." mode="gc:label"/>
        </div>
    </xsl:template>
    
    <!-- remove spaces -->
    <xsl:template match="text()" mode="xhtml:Input">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>

        <xsl:call-template name="xhtml:Input">
	    <xsl:with-param name="name" select="'ol'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
	    <xsl:with-param name="value" select="normalize-space(.)"/>
	</xsl:call-template>
    </xsl:template>

    <!--
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:InlinePropertyListMode">
        <xsl:apply-templates mode="#current">
            <xsl:sort select="gc:label(.)" lang="{$gp:lang}"/>
        </xsl:apply-templates>
    </xsl:template>
    -->
    
    <!-- object blank node (avoid infinite loop) -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID" mode="xhtml:Anchor">
	<xsl:variable name="bnode" select="key('resources', .)[not(@rdf:nodeID = current()/../../@rdf:nodeID)][not(*/@rdf:nodeID = current()/../../@rdf:nodeID)]"/>

	<xsl:choose>
	    <xsl:when test="$bnode">
		<xsl:apply-templates select="$bnode" mode="bs2:Block">
                    <xsl:with-param name="nested" select="true()"/>
                </xsl:apply-templates>
	    </xsl:when>
	    <xsl:otherwise>
		<span id="{.}" title="{.}">
		    <xsl:apply-templates select="." mode="gc:label"/>
		</span>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>
            
</xsl:stylesheet>