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
    <xsl:import href="../../imports/gc.xsl"/>
    <xsl:import href="../../imports/gp.xsl"/>
    <xsl:import href="../../imports/gr.xsl"/>
    <xsl:import href="../../imports/owl.xsl"/>
    <xsl:import href="../../imports/rdf.xsl"/>
    <xsl:import href="../../imports/rdfs.xsl"/>
    <xsl:import href="../../imports/xhv.xsl"/>
    <xsl:import href="../../imports/sd.xsl"/>
    <xsl:import href="../../imports/sp.xsl"/>
    <xsl:import href="../../imports/spin.xsl"/>
    <xsl:import href="../../imports/sioc.xsl"/>
    <xsl:import href="../../imports/skos.xsl"/>
    <xsl:import href="../../imports/void.xsl"/>
    <xsl:import href="imports/default.xsl"/>
    <xsl:import href="imports/dc.xsl"/>
    <xsl:import href="imports/dct.xsl"/>
    <xsl:import href="imports/foaf.xsl"/>
    <xsl:import href="imports/gc.xsl"/>
    <xsl:import href="imports/gp.xsl"/>
    <xsl:import href="imports/owl.xsl"/>
    <xsl:import href="imports/rdf.xsl"/>    
    <xsl:import href="imports/rdfs.xsl"/>
    <xsl:import href="imports/sioc.xsl"/>
    <xsl:import href="imports/sp.xsl"/>
    <xsl:import href="imports/xhv.xsl"/>

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
    <xsl:param name="gc:sitemap" select="if ($rdf:type) then document(gc:document-uri($rdf:type)) else ()" as="document-node()?"/>
    <xsl:param name="query" as="xs:string?"/>
    <xsl:param name="label" as="xs:string?"/>
    
    <xsl:variable name="main-doc" select="/" as="document-node()"/>
    
    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
    <xsl:key name="predicates" match="*[@rdf:about]/* | *[@rdf:nodeID]/*" use="concat(namespace-uri(), local-name())"/>
    <xsl:key name="predicates-by-object" match="*[@rdf:about]/* | *[@rdf:nodeID]/*" use="@rdf:resource | @rdf:nodeID"/>
    <xsl:key name="resources-by-type" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="rdf:type/@rdf:resource"/>
    <xsl:key name="resources-by-container" match="*[@rdf:about] | *[@rdf:nodeID]" use="sioc:has_parent/@rdf:resource | sioc:has_container/@rdf:resource"/>
    <xsl:key name="resources-by-page-of" match="*[@rdf:about]" use="gp:pageOf/@rdf:resource"/>
    <xsl:key name="resources-by-constructor-of" match="*[@rdf:about]" use="gp:constructorOf/@rdf:resource"/>
    <xsl:key name="resources-by-layout-of" match="*[@rdf:about]" use="gc:layoutOf/@rdf:resource"/>
    <xsl:key name="violations-by-path" match="*" use="spin:violationPath/@rdf:resource | spin:violationPath/@rdf:nodeID"/>
    <xsl:key name="violations-by-root" match="*[@rdf:about] | *[@rdf:nodeID]" use="spin:violationRoot/@rdf:resource | spin:violationRoot/@rdf:nodeID"/>
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
            <xsl:apply-templates select="." mode="bs2:HeadMode"/>
            <xsl:apply-templates select="." mode="bs2:BodyMode"/>
	</html>
    </xsl:template>

    <xsl:template match="/" mode="bs2:HeadMode">
        <head>
            <title>
                <xsl:apply-templates mode="bs2:TitleMode"/>
            </title>

            <meta name="viewport" content="width=device-width, initial-scale=1.0"/>

            <xsl:apply-templates mode="bs2:StyleMode"/>
            <xsl:apply-templates mode="bs2:ScriptMode"/>
        </head>
    </xsl:template>
    
    <xsl:template match="/" mode="bs2:BodyMode">
        <body>
            <xsl:apply-templates select="." mode="bs2:NavBarMode"/>

            <xsl:variable name="grouped-rdf" as="document-node()">
                <xsl:apply-templates select="." mode="gc:GroupTriples"/>
            </xsl:variable>
            <xsl:apply-templates select="$grouped-rdf/rdf:RDF"/>

            <xsl:apply-templates select="." mode="bs2:FooterMode"/>
        </body>
    </xsl:template>
    
    <xsl:template match="/" mode="bs2:NavBarMode">
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
                                    <xsl:attribute name="alt"><xsl:apply-templates select="." mode="gc:LabelMode"/></xsl:attribute>
                                </img>
                            </xsl:for-each>
                        </a>
                    </xsl:if>                    

                    <div id="collapsing-top-navbar" class="nav-collapse collapse">
                        <form action="" method="get" class="navbar-form pull-left" accept-charset="UTF-8">
                            <div class="input-append">
                                <input type="text" name="uri" class="input-xxlarge">
                                    <xsl:if test="$g:requestUri">
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="$g:requestUri"/>
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
                                    <xsl:apply-templates select="key('resources-by-type', '&gp;SPARQLEndpoint', document($g:baseUri))" mode="bs2:NavBarMode">
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
    
    <xsl:template match="*[@rdf:about]" mode="bs2:NavBarMode">
        <xsl:param name="space" as="xs:anyURI*"/>
        <li>
            <xsl:if test="@rdf:about = $space">
                <xsl:attribute name="class">active</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="@rdf:about" mode="gc:InlineMode"/>
        </li>
    </xsl:template>
    
    <xsl:template match="/" mode="bs2:FooterMode">
        <div class="footer text-center">
            <p>
                <hr/>
                <xsl:value-of select="format-date(current-date(), '[Y]', $gp:lang, (), ())"/>.
                Developed by <xsl:apply-templates select="key('resources', key('resources', '', document(''))/foaf:maker/@rdf:resource, document(''))/@rdf:about" mode="gc:InlineMode"/>.
                <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache License</a>.
            </p>
        </div>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="bs2:TitleMode">
	<xsl:if test="$g:baseUri">
            <xsl:apply-templates select="key('resources', $g:baseUri, document($g:baseUri))" mode="gc:LabelMode"/>
            <xsl:text> - </xsl:text>
        </xsl:if>
	<xsl:apply-templates select="if (key('resources', $g:requestUri)/gp:pageOf/@rdf:resource) then key('resources', key('resources', $g:requestUri)/gp:pageOf/@rdf:resource) else key('resources', $g:requestUri) | key('resources-by-type', '&http;Response')" mode="gc:LabelMode"/>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="bs2:StyleMode">
	<link href="{resolve-uri('static/css/bootstrap.css', $gc:contextUri)}" rel="stylesheet" type="text/css"/>
	<link href="{resolve-uri('static/css/bootstrap-responsive.css', $gc:contextUri)}" rel="stylesheet" type="text/css"/>
	<link href="{resolve-uri('static/org/graphity/client/css/bootstrap.css', $gc:contextUri)}" rel="stylesheet" type="text/css"/>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="bs2:ScriptMode">
	<script type="text/javascript" src="{resolve-uri('static/js/jquery.min.js', $gc:contextUri)}"></script>
	<script type="text/javascript" src="{resolve-uri('static/js/bootstrap.js', $gc:contextUri)}"></script>
        <script type="text/javascript" src="{resolve-uri('static/org/graphity/client/js/jquery.js', $gc:contextUri)}"></script>
        <xsl:if test="key('resources', $g:requestUri)/gc:mode/@rdf:resource = '&gc;MapMode' or key('resources', $g:requestUri)/gp:forClass/@rdf:resource">
            <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?sensor=false"/>
            <script type="text/javascript" src="{resolve-uri('static/org/graphity/client/js/google-maps.js', $gc:contextUri)}"></script>
        </xsl:if>
        <xsl:if test="key('resources', $g:requestUri)/gc:mode/@rdf:resource = '&gc;EditMode' or key('resources', $g:requestUri)/gp:forClass/@rdf:resource">
            <script type="text/javascript" src="{resolve-uri('static/org/graphity/client/js/UUID.js', $gc:contextUri)}"></script>
        </xsl:if>
    </xsl:template>

    <xsl:template match="rdf:RDF[key('resources', $g:requestUri)/rdf:type/@rdf:resource = 'http://graphity.org/gcs#DescribeLabelResources']" priority="1">
        <xsl:next-match>
            <xsl:with-param name="selected-resources" select="*[rdf:type/@rdf:resource = '&gp;Item']" tunnel="yes"/>
        </xsl:next-match>
    </xsl:template>
    
    <xsl:template match="rdf:RDF">
        <xsl:param name="selected-resources" select="if (key('resources', $g:requestUri)/gp:pageOf/@rdf:resource) then key('resources-by-container', key('resources', key('resources', $g:requestUri)/gp:pageOf/@rdf:resource)/@rdf:about) else key('resources', $g:requestUri)" as="element()*" tunnel="yes"/>

        <div class="container-fluid">
	    <div class="row-fluid">
		<div class="span8">
                    <xsl:apply-templates select="." mode="bs2:BreadCrumbMode"/>

                    <xsl:if test="not(key('resources', $g:requestUri)/gc:mode/@rdf:resource = '&gc;ReadMode')">
                        <xsl:apply-templates select="." mode="bs2:HeaderMode"/>
                    </xsl:if>

                    <xsl:apply-templates select="." mode="bs2:ModeSelectMode"/>

                    <xsl:apply-templates select="." mode="bs2:PaginationMode">
                        <xsl:with-param name="count" select="count($selected-resources)" tunnel="yes"/>
                    </xsl:apply-templates>

                    <xsl:apply-templates select="." mode="gc:ModeChoiceMode">
                        <xsl:with-param name="selected-resources" select="$selected-resources" tunnel="yes"/>
                    </xsl:apply-templates>
                    
                    <xsl:apply-templates select="." mode="bs2:PaginationMode">
                        <xsl:with-param name="count" select="count($selected-resources)" tunnel="yes"/>
                    </xsl:apply-templates>
                </div>

		<div class="span4">
		    <xsl:apply-templates select="." mode="bs2:SidebarNavMode"/>
		</div>
	    </div>
	</div>
    </xsl:template>

    <xsl:template match="rdf:RDF[key('resources-by-type', '&http;Response')][not(key('resources-by-type', '&spin;ConstraintViolation'))]" mode="gc:ModeChoiceMode" priority="1">
        <xsl:apply-templates select="." mode="bs2:ReadMode"/>
    </xsl:template>
    
    <xsl:template match="rdf:RDF" mode="gc:ModeChoiceMode">
        <xsl:choose>
            <xsl:when test="key('resources', $g:requestUri)/rdf:type/@rdf:resource = '&gp;Constructor'">
                <xsl:apply-templates select="." mode="bs2:ConstructMode"/>
           </xsl:when>            
            <xsl:when test="key('resources', $g:requestUri)/gc:mode/@rdf:resource = '&gc;ListMode'">
                <xsl:apply-templates select="." mode="bs2:ListMode"/>
            </xsl:when>
            <xsl:when test="key('resources', $g:requestUri)/gc:mode/@rdf:resource = '&gc;TableMode'">
                <xsl:apply-templates select="." mode="gc:TableMode"/>
            </xsl:when>
            <xsl:when test="key('resources', $g:requestUri)/gc:mode/@rdf:resource = '&gc;GridMode'">
                <xsl:apply-templates select="." mode="bs2:GridMode"/>
            </xsl:when>
            <xsl:when test="key('resources', $g:requestUri)/gc:mode/@rdf:resource = '&gc;MapMode'">
                <xsl:apply-templates select="." mode="bs2:MapMode"/>
            </xsl:when>
            <xsl:when test="key('resources', $g:requestUri)/gc:mode/@rdf:resource = '&gc;GraphMode'">
                <xsl:apply-templates select="." mode="bs2:GraphMode"/>
            </xsl:when>
            <xsl:when test="key('resources', $g:requestUri)/gc:mode/@rdf:resource = '&gc;EditMode'">
                <xsl:apply-templates select="." mode="bs2:EditMode"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="." mode="bs2:ReadMode"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- MODE SELECT MODE -->
    
    <xsl:template match="rdf:RDF" mode="bs2:ModeSelectMode">
        <xsl:if test="key('resources-by-constructor-of', $g:requestUri) | key('resources-by-page-of', $g:requestUri) | key('resources-by-layout-of', $g:requestUri)">
            <ul class="nav nav-tabs">
                <xsl:apply-templates select="key('resources-by-constructor-of', $g:requestUri)" mode="#current"/>
                <xsl:apply-templates select="key('resources-by-page-of', $g:requestUri) | key('resources-by-layout-of', $g:requestUri)" mode="#current">
                    <xsl:sort select="gc:mode/@rdf:resource/gc:object-label(.)"/>
                </xsl:apply-templates>
            </ul>
        </xsl:if>
    </xsl:template>

    <xsl:template match="rdf:RDF[*/rdf:type/@rdf:resource = '&http;Response']" mode="bs2:ModeSelectMode" priority="1"/>
    
    <xsl:template match="*[*][@rdf:about][gc:mode/@rdf:resource]" mode="bs2:ModeSelectMode" priority="1">
	<li>
	    <xsl:if test="key('resources', $g:requestUri)/gc:mode/@rdf:resource = gc:mode/@rdf:resource">
		<xsl:attribute name="class">active</xsl:attribute>
	    </xsl:if>

            <a href="{@rdf:about}">
                <xsl:apply-templates select="gc:mode/@rdf:resource" mode="gc:ObjectLabelMode"/>
            </a>
	</li>	
    </xsl:template>

    <xsl:template match="*" mode="bs2:ModeSelectMode"/>
        
    <!-- BREADCRUMB MODE -->

    <xsl:template match="rdf:RDF" mode="bs2:BreadCrumbMode"/>
        
    <xsl:template match="rdf:RDF[if (key('resources', $g:requestUri)/gp:pageOf/@rdf:resource) then key('resources', key('resources', $g:requestUri)/gp:pageOf/@rdf:resource) else key('resources', $g:requestUri)]" mode="bs2:BreadCrumbMode" priority="1">
        <ul class="breadcrumb">
            <xsl:apply-templates select="if (key('resources', $g:requestUri)/gp:pageOf/@rdf:resource) then key('resources', key('resources', $g:requestUri)/gp:pageOf/@rdf:resource) else key('resources', $g:requestUri)" mode="#current"/>
        </ul>
    </xsl:template>

    <xsl:template match="rdf:RDF[*/rdf:type/@rdf:resource = '&http;Response']" mode="bs2:BreadCrumbMode" priority="3"/>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:BreadCrumbMode">
        <xsl:param name="leaf" select="true()" as="xs:boolean"/>
        
        <!-- walk up the parents recursively -->
        <xsl:choose>
            <xsl:when test="key('resources', sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource)">
                <xsl:apply-templates select="key('resources', sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource)" mode="#current">
                    <xsl:with-param name="leaf" select="false()"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="key('resources', gp:constructorOf/@rdf:resource)">
                <xsl:apply-templates select="key('resources', gp:constructorOf/@rdf:resource)" mode="#current">
                    <xsl:with-param name="leaf" select="false()"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource">
                <xsl:variable name="container-doc" select="document(sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource)" as="document-node()?"/>
                <xsl:apply-templates select="key('resources', sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource, $container-doc)" mode="#current">
                    <xsl:with-param name="leaf" select="false()"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="gp:constructorOf/@rdf:resource">
                <xsl:variable name="container-doc" select="document(gp:constructorOf/@rdf:resource)" as="document-node()?"/>
                <xsl:apply-templates select="key('resources', gp:constructorOf/@rdf:resource, $container-doc)" mode="#current">
                    <xsl:with-param name="leaf" select="false()"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise></xsl:otherwise>
        </xsl:choose>

        <li><!--
            <xsl:if test="@rdf:about = $g:requestUri">
                <xsl:attribute name="class">active</xsl:attribute>
            </xsl:if>
            -->
            
            <xsl:apply-templates select="@rdf:about" mode="gc:InlineMode"/>

            <xsl:if test="not($leaf)">
                <span class="divider">/</span>
            </xsl:if>
        </li>
    </xsl:template>
        
    <!-- HEADER MODE -->

    <xsl:template match="rdf:RDF[key('resources', key('resources', $g:requestUri)/gp:pageOf/@rdf:resource)]" mode="bs2:HeaderMode" priority="1">
        <xsl:apply-templates select="key('resources', key('resources', $g:requestUri)/gp:pageOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="bs2:HeaderMode">
        <xsl:apply-templates select="key('resources', $g:requestUri)" mode="#current"/>
    </xsl:template>
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:HeaderMode">
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
            
            <xsl:apply-templates select="." mode="bs2:ModeToggleMode"/>

            <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>
            
            <xsl:apply-templates select="." mode="gc:DescriptionMode"/>

            <xsl:apply-templates select="." mode="bs2:MediaTypeSelectMode"/>

	    <xsl:apply-templates select="." mode="bs2:TypeListMode"/>
        </div>
    </xsl:template>
    
    <xsl:template match="@rdf:about[. = (key('resources', $g:requestUri)/gp:pageOf/@rdf:resource, $g:requestUri)] | @rdf:about[../foaf:isPrimaryTopicOf/@rdf:resource = $g:requestUri]" mode="bs2:HeaderMode" priority="1">
        <h1 class="page-header">
	    <xsl:apply-templates select="." mode="gc:InlineMode"/>
	</h1>
    </xsl:template>

    <xsl:template match="@rdf:nodeID[../rdf:type/@rdf:resource = '&http;Response']" mode="bs2:HeaderMode" priority="1">
        <div class="alert alert-error">
            <h1>
                <xsl:apply-templates select="." mode="gc:InlineMode"/>
            </h1>
        </div>
    </xsl:template>
        
    <!-- MEDIA TYPE SELECT MODE (Export buttons) -->

    <xsl:template match="*" mode="bs2:MediaTypeSelectMode"/>
    
    <xsl:template match="*[rdf:type/@rdf:resource = '&foaf;Document']" mode="bs2:MediaTypeSelectMode" priority="1">
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

    <xsl:template match="*[key('resources', foaf:isPrimaryTopicOf/(@rdf:resource, @rdf:nodeID))]" mode="bs2:MediaTypeSelectMode" priority="2">
        <xsl:apply-templates select="key('resources', foaf:isPrimaryTopicOf/(@rdf:resource, @rdf:nodeID))" mode="#current"/>
    </xsl:template>

    <!-- MODE TOGGLE MODE (Create/Edit buttons) -->

    <xsl:template match="*" mode="bs2:ModeToggleMode"/>
    
    <xsl:template match="*[@rdf:about]" mode="bs2:ModeToggleMode" priority="1">
        <div class="pull-right">
            <form action="{gc:document-uri(@rdf:about)}?_method=DELETE" method="post">
                <button class="btn btn-primary" type="submit">
                    <xsl:apply-templates select="key('resources', 'delete', document(''))" mode="gc:LabelMode"/>
                </button>
            </form>
        </div>
        
        <xsl:apply-templates select="key('resources-by-layout-of', @rdf:about)" mode="bs2:ButtonMode"/>
        
        <xsl:apply-templates select="key('resources-by-constructor-of', @rdf:about)" mode="bs2:ButtonMode"/>
    </xsl:template>

    <xsl:template match="*[@rdf:about][gc:mode/@rdf:resource]" mode="bs2:ButtonMode" priority="1"/>

    <xsl:template match="*[@rdf:about][gc:mode/@rdf:resource = '&gc;EditMode']" mode="bs2:ButtonMode" priority="2">
        <div class="pull-right">
            <a class="btn btn-primary" href="{@rdf:about}">
                <xsl:apply-templates select="key('resources', gc:mode/@rdf:resource, document(gc:document-uri(gc:mode/@rdf:resource)))" mode="gc:LabelMode"/>
            </a>                        
        </div>
    </xsl:template>

    <xsl:template match="*[@rdf:about][gp:forClass/@rdf:resource]" mode="bs2:ButtonMode" priority="2">
        <div class="pull-right">
            <a class="btn btn-primary" href="{@rdf:about}">
                <xsl:apply-templates select="key('resources', '&gc;ConstructMode', document('&gc;'))"/>
                <xsl:text> </xsl:text>
                <xsl:apply-templates select="key('resources', gp:forClass/@rdf:resource, document(gc:document-uri(gp:forClass/@rdf:resource)))" mode="gc:LabelMode"/>
            </a>                        
        </div>
    </xsl:template>
            
    <!-- IMAGE MODE -->
        
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:ImageMode">
        <xsl:variable name="images" as="element()*">
            <xsl:apply-templates mode="gc:ImageMode"/>
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

    <xsl:template match="*[key('resources', foaf:primaryTopic/(@rdf:resource, @rdf:nodeID))]" mode="bs2:ImageMode" priority="1">
        <xsl:apply-templates select="key('resources', foaf:primaryTopic/(@rdf:resource, @rdf:nodeID))" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', foaf:primaryTopic/(@rdf:resource, @rdf:nodeID))]" mode="gc:LabelMode" priority="1">
        <xsl:apply-templates select="key('resources', foaf:primaryTopic/(@rdf:resource, @rdf:nodeID))" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[key('resources', foaf:primaryTopic/(@rdf:resource, @rdf:nodeID))]" mode="gc:DescriptionMode" priority="1">
        <xsl:apply-templates select="key('resources', foaf:primaryTopic/(@rdf:resource, @rdf:nodeID))" mode="#current"/>
    </xsl:template>

    <!-- INLINE LIST MODE -->
    
    <!-- <xsl:template match="*" mode="bs2:TypeListMode"/> -->

    <xsl:template match="*[rdf:type/@rdf:resource]" mode="bs2:TypeListMode" priority="1">
        <ul class="inline">
            <xsl:apply-templates select="rdf:type" mode="#current">
                <xsl:sort select="gc:object-label(@rdf:resource | @rdf:nodeID)" data-type="text" order="ascending" lang="{$gp:lang}"/>
            </xsl:apply-templates>
        </ul>
    </xsl:template>

    <xsl:template match="*[key('resources', foaf:primaryTopic/(@rdf:resource, @rdf:nodeID))]" mode="bs2:TypeListMode" priority="2">
        <xsl:apply-templates select="key('resources', foaf:primaryTopic/(@rdf:resource, @rdf:nodeID))" mode="#current"/>
    </xsl:template>

    <!-- PROPERTY LIST MODE -->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:PropertyListMode">
        <xsl:variable name="properties" as="element()*">
            <xsl:apply-templates mode="gc:PropertyListMode">
                <xsl:sort select="gc:property-label(.)" data-type="text" order="ascending" lang="{$gp:lang}"/>
            </xsl:apply-templates>
        </xsl:variable>

        <xsl:if test="$properties">
            <dl class="dl-horizontal">
                <xsl:copy-of select="$properties"/>
            </dl>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*[key('resources', foaf:primaryTopic/(@rdf:resource, @rdf:nodeID))]" mode="bs2:PropertyListMode" priority="1">
        <xsl:apply-templates select="key('resources', foaf:primaryTopic/(@rdf:resource, @rdf:nodeID))" mode="#current"/>
    </xsl:template>

    <!-- SIDEBAR NAV MODE -->
    
    <xsl:template match="rdf:RDF" mode="bs2:SidebarNavMode">
	<xsl:for-each-group select="*/*" group-by="concat(namespace-uri(), local-name())">
	    <xsl:sort select="gc:property-label(.)" order="ascending" lang="{$gp:lang}"/>
	    <xsl:apply-templates select="current-group()[1]" mode="#current">
                <xsl:sort select="gc:object-label(@rdf:resource)" data-type="text" order="ascending"/>
            </xsl:apply-templates>
	</xsl:for-each-group>	
    </xsl:template>

    <!-- PAGINATION MODE -->

    <xsl:template match="rdf:RDF" mode="bs2:PaginationMode">
	<xsl:param name="count" as="xs:integer" tunnel="yes"/>
        
        <xsl:apply-templates select="key('resources', $g:requestUri)" mode="#current"/>
    </xsl:template>
    
    <xsl:template match="*[key('resources', gc:layoutOf/@rdf:resource)]" mode="bs2:PaginationMode" priority="2">
        <xsl:apply-templates select="key('resources', gc:layoutOf/@rdf:resource)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*" mode="bs2:PaginationMode"/>

    <xsl:template match="*[xhv:prev/@rdf:resource] | *[xhv:next/@rdf:resource]" mode="bs2:PaginationMode">
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
                    <xsl:when test="xhv:next and $count &gt;= gp:limit">
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

    <!-- LIST MODE -->

    <xsl:template match="rdf:RDF" mode="bs2:ListMode">
        <xsl:param name="selected-resources" as="element()*" tunnel="yes"/>

        <xsl:apply-templates select="$selected-resources" mode="#current">
            <xsl:sort select="gc:label(.)" lang="{$gp:lang}"/>
        </xsl:apply-templates>
    </xsl:template>
                
    <xsl:template match="*[*][@rdf:about or @rdf:nodeID]" mode="bs2:ListMode">
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
            
            <xsl:apply-templates select="." mode="bs2:ModeToggleMode"/>

	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>
	    
	    <xsl:apply-templates select="." mode="gc:DescriptionMode"/>

	    <xsl:apply-templates select="." mode="bs2:TypeListMode"/>            

	    <xsl:if test="@rdf:nodeID">
		<xsl:apply-templates select="." mode="bs2:PropertyListMode"/>
	    </xsl:if>
	</div>
    </xsl:template>
        
    <!-- READ MODE -->
    
    <xsl:template match="rdf:RDF" mode="bs2:ReadMode">
        <xsl:param name="selected-resources" as="element()*" tunnel="yes"/>

        <xsl:apply-templates select="if (key('resources', $g:requestUri)/gp:pageOf/@rdf:resource) then key('resources', key('resources', $g:requestUri)/gp:pageOf/@rdf:resource) else key('resources', $g:requestUri) | key('resources-by-type', '&http;Response')" mode="#current"/>

        <xsl:apply-templates select="$selected-resources" mode="#current">
            <xsl:sort select="gc:label(.)" lang="{$gp:lang}"/>
        </xsl:apply-templates>
    </xsl:template>

    <!-- hide metadata -->
    <xsl:template match="*[gp:constructorOf/@rdf:resource] | *[gp:pageOf/@rdf:resource] | *[gc:layoutOf/@rdf:resource]" mode="bs2:ReadMode" priority="1"/>

    <!-- hide document if topic is present -->
    <xsl:template match="*[key('resources', foaf:primaryTopic/(@rdf:resource, @rdf:nodeID))]" mode="bs2:ReadMode" priority="1"/>
        
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:ReadMode">
        <xsl:param name="id" select="generate-id()" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        
        <xsl:apply-templates select="." mode="bs2:HeaderMode"/>

        <xsl:apply-templates select="." mode="bs2:PropertyListMode"/>
    </xsl:template>
            
    <!-- GRID MODE -->
    
    <xsl:template match="rdf:RDF" mode="bs2:GridMode">
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

    <xsl:template match="*[@rdf:about or @rdf:nodeID]" mode="bs2:GridMode">
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
		<xsl:apply-templates select="." mode="bs2:ImageMode"/>
		
		<div class="caption">
                    <xsl:apply-templates select="." mode="bs2:ModeToggleMode"/>
                                
                    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>

		    <xsl:apply-templates select="." mode="gc:DescriptionMode"/>
		</div>
	    </div>
	</li>
    </xsl:template>
    
    <!-- MAP MODE -->
    
    <xsl:template match="rdf:RDF" mode="bs2:MapMode">
        <xsl:param name="selected-resources" as="element()*" tunnel="yes"/>

        <div id="map-canvas"/>

        <!-- apply all other URI resources -->
        <xsl:apply-templates mode="#current"> <!-- select="$selected-resources" -->
            <xsl:sort select="gc:label(.)" lang="{$gp:lang}"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:MapMode"/>

    <xsl:template match="*[geo:lat castable as xs:double][geo:long castable as xs:double]" mode="bs2:MapMode" priority="1">
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
    
    <xsl:template match="rdf:RDF" mode="bs2:ConstructMode">
        <xsl:param name="method" select="'post'" as="xs:string"/>
        <xsl:param name="forClass" select="key('resources', $g:requestUri)/gp:forClass/@rdf:resource" as="xs:anyURI"/>
        <xsl:param name="action" select="$g:requestUri" as="xs:anyURI"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'form-horizontal'" as="xs:string?"/>
        <xsl:param name="button-class" select="'btn btn-primary'" as="xs:string?"/>
        <xsl:param name="accept-charset" select="'UTF-8'" as="xs:string?"/>
        <xsl:param name="enctype" as="xs:string?"/>
        <xsl:param name="template-doc" select="document(key('resources', $g:requestUri)/@rdf:about)" as="document-node()"/>
        <xsl:param name="resources" select="key('resources-by-type', $forClass)[@rdf:nodeID]" as="element()*" tunnel="yes"/>
        
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

            <xsl:apply-templates select="$resources" mode="bs2:EditMode">
                <xsl:with-param name="template-doc" select="$template-doc" tunnel="yes"/>
                <xsl:sort select="gc:label(.)"/>
            </xsl:apply-templates>

            <xsl:apply-templates select="." mode="bs2:FormActionsMode">
                <xsl:with-param name="button-class" select="$button-class"/>
            </xsl:apply-templates>
	</form>
    </xsl:template>
    
    <xsl:template match="rdf:RDF" mode="bs2:FormActionsMode">
        <xsl:param name="button-class" select="'btn btn-primary'" as="xs:string?"/>
        
        <div class="form-actions">
            <button type="submit" class="{$button-class}">Save</button>
        </div>
    </xsl:template>

    <!-- EDIT MODE -->
    
    <xsl:template match="rdf:RDF" mode="bs2:EditMode">
        <xsl:param name="method" select="'post'" as="xs:string"/>   
        <xsl:param name="action" select="xs:anyURI(concat($g:absolutePath, '?_method=PUT&amp;mode=', encode-for-uri('&gc;EditMode')))" as="xs:anyURI"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'form-horizontal'" as="xs:string?"/>
        <xsl:param name="button-class" select="'btn btn-primary'" as="xs:string?"/>
        <xsl:param name="accept-charset" select="'UTF-8'" as="xs:string?"/>
        <xsl:param name="enctype" as="xs:string?"/>
        <xsl:param name="resources" select="*[not(key('predicates-by-object', @rdf:nodeID))]" as="element()*" tunnel="yes"/>

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

            <xsl:apply-templates select="$resources" mode="#current">
                <xsl:sort select="gc:label(.)"/>
            </xsl:apply-templates>
                
            <xsl:apply-templates select="." mode="bs2:FormActionsMode">
                <xsl:with-param name="button-class" select="$button-class"/>
            </xsl:apply-templates>
        </form>
    </xsl:template>

    <!-- hide metadata -->
    <xsl:template match="*[gc:layoutOf/@rdf:resource = $g:requestUri]" mode="bs2:EditMode" priority="1"/>

    <xsl:template match="*[rdf:type/@rdf:resource = '&http;Response'] | *[rdf:type/@rdf:resource = '&spin;ConstraintViolation']" mode="bs2:EditMode" priority="1"/>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:EditMode">
        <xsl:param name="id" select="generate-id()" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="legend" select="if (@rdf:about) then true() else not(key('predicates-by-object', @rdf:nodeID))" as="xs:boolean"/>
        <xsl:param name="constraint-violations" select="key('violations-by-root', (@rdf:about, @rdf:nodeID))" as="element()*"/>
        <xsl:param name="parent-uri" select="key('resources', $g:requestUri)/(sioc:has_parent, sioc:has_container)/@rdf:resource" as="xs:anyURI?"/>
        <xsl:param name="parent-doc" select="document($parent-uri)" as="document-node()?"/>
        <xsl:param name="construct-uri" select="if ($parent-doc) then key('resources-by-constructor-of', $parent-uri, $parent-doc)[gp:forClass/@rdf:resource = key('resources', $g:requestUri)/rdf:type/@rdf:resource]/@rdf:about else ()" as="xs:anyURI*"/>
        <xsl:param name="template-doc" select="document($construct-uri)" as="document-node()?" tunnel="yes"/>
        <xsl:param name="template" select="$template-doc/rdf:RDF/*[@rdf:nodeID][every $type in rdf:type/@rdf:resource satisfies current()/rdf:type/@rdf:resource = $type]" as="element()*"/>
        <xsl:param name="traversed-ids" select="@rdf:nodeID" as="xs:string*" tunnel="yes"/>

        <fieldset>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>            
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>

            <xsl:if test="$legend">
                <legend>
                    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:InlineMode"/>
                </legend>
            </xsl:if>

            <xsl:apply-templates select="$constraint-violations" mode="bs2:ConstraintViolationMode"/>

            <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>

            <xsl:if test="not($template)">
                <xsl:message>bs2:EditMode is active but spin:constructor is not defined for resource '<xsl:value-of select="@rdf:about | @rdf:nodeID"/>'</xsl:message>
            </xsl:if>
            <xsl:apply-templates select="* | $template/*[not(concat(namespace-uri(), local-name(), @xml:lang, @rdf:datatype) = current()/*/concat(namespace-uri(), local-name(), @xml:lang, @rdf:datatype))]" mode="#current">
                <xsl:sort select="gc:property-label(.)"/>
                <xsl:with-param name="constraint-violations" select="$constraint-violations"/>
                <xsl:with-param name="traversed-ids" select="$traversed-ids" tunnel="yes"/>
            </xsl:apply-templates>
        </fieldset>
    </xsl:template>

    <!-- CONSTRAINT VIOLATION MODE -->
    
    <xsl:template match="*" mode="bs2:ConstraintViolationMode"/>

    <xsl:template match="*[rdf:type/@rdf:resource = '&spin;ConstraintViolation']" mode="bs2:ConstraintViolationMode" priority="1">
	<xsl:param name="class" select="'alert alert-error'" as="xs:string?"/>

        <div>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>            
            <xsl:apply-templates select="." mode="gc:LabelMode"/>
        </div>
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

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:InlinePropertyListMode">
        <xsl:apply-templates mode="#current">
            <xsl:sort select="gc:label(.)" lang="{$gp:lang}"/>
        </xsl:apply-templates>
    </xsl:template>
    
    <!-- object blank node (avoid infinite loop) -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID" mode="gc:InlineMode">
	<xsl:variable name="bnode" select="key('resources', .)[not(@rdf:nodeID = current()/../../@rdf:nodeID)][not(*/@rdf:nodeID = current()/../../@rdf:nodeID)]"/>

	<xsl:choose>
	    <xsl:when test="$bnode">
		<xsl:apply-templates select="$bnode" mode="bs2:ReadMode">
                    <xsl:with-param name="nested" select="true()"/>
                </xsl:apply-templates>
	    </xsl:when>
	    <xsl:otherwise>
		<span id="{.}" title="{.}">
		    <xsl:apply-templates select="." mode="gc:LabelMode"/>
		</span>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>
            
</xsl:stylesheet>