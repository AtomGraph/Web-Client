<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2012 Martynas Jusevičius <martynas@atomgraph.com>

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
    <!ENTITY a      "http://atomgraph.com/ns/core#">
    <!ENTITY ac     "http://atomgraph.com/ns/client#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY xhv    "http://www.w3.org/1999/xhtml/vocab#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY owl    "http://www.w3.org/2002/07/owl#">
    <!ENTITY http   "http://www.w3.org/2011/http#">
    <!ENTITY ldt    "https://www.w3.org/ns/ldt#">
    <!ENTITY sd     "http://www.w3.org/ns/sparql-service-description#">
    <!ENTITY dct    "http://purl.org/dc/terms/">
    <!ENTITY foaf   "http://xmlns.com/foaf/0.1/">
    <!ENTITY sp     "http://spinrdf.org/sp#">
    <!ENTITY spin   "http://spinrdf.org/spin#">
    <!ENTITY sioc   "http://rdfs.org/sioc/ns#">
]>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:a="&a;"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:http="&http;"
xmlns:ldt="&ldt;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:sp="&sp;"
xmlns:spin="&spin;"
xmlns:xhv="&xhv;"
xmlns:bs2="http://graphity.org/xsl/bootstrap/2.3.2"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
exclude-result-prefixes="#all">

    <xsl:import href="../../group-sort-triples.xsl"/>
    <xsl:import href="../../functions.xsl"/>
    <xsl:import href="../../imports/default.xsl"/>
    <xsl:import href="../../imports/dbpedia-owl.xsl"/>
    <xsl:import href="../../imports/dc.xsl"/>
    <xsl:import href="../../imports/dct.xsl"/>
    <xsl:import href="../../imports/doap.xsl"/>
    <xsl:import href="../../imports/foaf.xsl"/>
    <xsl:import href="../../imports/gr.xsl"/>
    <xsl:import href="../../imports/ldt.xsl"/>
    <xsl:import href="../../imports/rdf.xsl"/>
    <xsl:import href="../../imports/rdfs.xsl"/>
    <xsl:import href="../../imports/sd.xsl"/>
    <xsl:import href="../../imports/schema.xsl"/>
    <xsl:import href="../../imports/sioc.xsl"/>
    <xsl:import href="../../imports/skos.xsl"/>
    <xsl:import href="../../imports/sp.xsl"/>
    <xsl:import href="imports/default.xsl"/>
    <xsl:import href="imports/dct.xsl"/>
    <xsl:import href="imports/dh.xsl"/>
    <xsl:import href="imports/foaf.xsl"/>
    <xsl:import href="imports/owl.xsl"/>
    <xsl:import href="imports/rdf.xsl"/>
    <xsl:import href="imports/sioc.xsl"/>
    <xsl:import href="imports/sp.xsl"/>
    <xsl:import href="resource.xsl"/>
    <xsl:import href="container.xsl"/>

    <xsl:include href="sparql.xsl"/>

    <xsl:output method="xhtml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" media-type="application/xhtml+xml"/>
    
    <xsl:param name="ldt:base" as="xs:anyURI?"/>
    <xsl:param name="ldt:lang" select="'en'" as="xs:string"/>
    <xsl:param name="ac:contextUri" as="xs:anyURI?"/>
    <xsl:param name="ac:uri" as="xs:anyURI?"/>
    <xsl:param name="ac:endpoint" as="xs:anyURI?"/>
    <xsl:param name="ac:forClass" as="xs:anyURI?"/>
    <xsl:param name="ac:mode" select="xs:anyURI('&ac;ReadMode')" as="xs:anyURI*"/>
    <xsl:param name="ac:query" as="xs:string?"/>
    <xsl:param name="ldt:ontology" as="xs:anyURI?"/>
    <xsl:param name="rdf:type" as="xs:anyURI?"/>
    <xsl:param name="ac:sitemap" as="document-node()?"/>
    <xsl:param name="ac:googleMapsKey" select="'AIzaSyCQ4rt3EnNCmGTpBN0qoZM1Z_jXhUnrTpQ'" as="xs:string"/>
    
    <xsl:variable name="main-doc" select="/" as="document-node()"/>
    
    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
    <xsl:key name="predicates" match="*[@rdf:about]/* | *[@rdf:nodeID]/*" use="concat(namespace-uri(), local-name())"/>
    <xsl:key name="predicates-by-object" match="*[@rdf:about]/* | *[@rdf:nodeID]/*" use="@rdf:resource | @rdf:nodeID"/>
    <xsl:key name="resources-by-type" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="rdf:type/@rdf:resource"/>
    <xsl:key name="resources-by-defined-by" match="*[@rdf:about]" use="rdfs:isDefinedBy/@rdf:resource"/>
    <xsl:key name="violations-by-path" match="*" use="spin:violationPath/@rdf:resource | spin:violationPath/@rdf:nodeID"/>
    <xsl:key name="violations-by-root" match="*[@rdf:about] | *[@rdf:nodeID]" use="spin:violationRoot/@rdf:resource | spin:violationRoot/@rdf:nodeID"/>

    <rdf:Description rdf:about="">
        <foaf:maker rdf:resource="https://atomgraph.com/#company"/>
    </rdf:Description>

    <rdf:Description rdf:about="https://atomgraph.com/#company">
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
        <html lang="{$ldt:lang}">
            <xsl:variable name="grouped-rdf" as="document-node()">
                <xsl:apply-templates select="." mode="ac:GroupTriples"/>
            </xsl:variable>

            <xsl:apply-templates select="$grouped-rdf/rdf:RDF" mode="xhtml:Head"/>
            
            <xsl:apply-templates select="$grouped-rdf/rdf:RDF" mode="xhtml:Body"/>
        </html>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="xhtml:Head">
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0"/>

            <xsl:apply-templates select="." mode="xhtml:Title"/>
            
            <xsl:apply-templates select="." mode="xhtml:Style"/>

            <xsl:apply-templates select="." mode="xhtml:Script"/>
        </head>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="xhtml:Body">
        <body>
            <xsl:apply-templates select="." mode="bs2:NavBar"/>

            <div class="container-fluid">
                <div class="row-fluid">
                    <xsl:apply-templates select="." mode="bs2:Main"/>

                    <xsl:apply-templates select="." mode="bs2:Right"/>
                </div>
            </div>
    
            <xsl:apply-templates select="." mode="bs2:Footer"/>
        </body>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="bs2:NavBar">
        <div class="navbar navbar-fixed-top">
            <div class="navbar-inner">
                <div class="container-fluid">
                    <button class="btn btn-navbar" onclick="if ($('#collapsing-top-navbar').hasClass('in')) $('#collapsing-top-navbar').removeClass('collapse in').height(0); else $('#collapsing-top-navbar').addClass('collapse in').height('auto');">
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>

                    <xsl:if test="$ldt:base and doc-available($ldt:base)">
                        <a class="brand" href="{$ldt:base}">
                            <xsl:for-each select="key('resources', $ldt:base, document($ldt:base))">
                                <img src="{foaf:logo/@rdf:resource}">
                                    <xsl:attribute name="alt"><xsl:apply-templates select="." mode="ac:label"/></xsl:attribute>
                                </img>
                            </xsl:for-each>
                        </a>
                    </xsl:if>

                    <div id="collapsing-top-navbar" class="nav-collapse collapse">
                        <form action="" method="get" class="navbar-form pull-left" accept-charset="UTF-8">
                            <div class="input-append">
                                <input type="text" name="uri" class="input-xxlarge">
                                    <xsl:if test="$ac:uri">
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="$ac:uri"/>
                                        </xsl:attribute>
                                    </xsl:if>
                                </input>
                                <button type="submit" class="btn btn-primary">Go</button>
                            </div>
                        </form>

                        <ul class="nav pull-right">
                            <li>
                                <a href="?uri={encode-for-uri($ac:uri)}&amp;mode={encode-for-uri('&ac;QueryEditorMode')}">Query editor</a>
                            </li>
                        </ul>
                    </div>
                </div>
            </div>

            <xsl:apply-templates select="." mode="bs2:ActionBar"/>
        </div>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="bs2:ActionBar">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'navbar-inner action-bar'" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>

            <div class="container-fluid">
                <div class="row-fluid">
                    <xsl:apply-templates select="." mode="bs2:ActionBarLeft"/>

                    <xsl:apply-templates select="." mode="bs2:ActionBarMain"/>
                    
                    <xsl:apply-templates select="." mode="bs2:ActionBarRight"/>
                </div>
            </div>
        </div>
    </xsl:template>
    
    <xsl:template match="rdf:RDF" mode="bs2:ActionBarLeft">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'span2'" as="xs:string?"/>
        
        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            
            <xsl:apply-templates select="." mode="bs2:Create"/>
        </div>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="bs2:ActionBarMain">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'span10'" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            
            <xsl:apply-templates select="." mode="bs2:MediaTypeList"/>

            <xsl:apply-templates select="." mode="bs2:NavBarActions"/>

            <xsl:apply-templates select="." mode="bs2:ModeList"/>

            <xsl:apply-templates select="." mode="bs2:BreadCrumbList"/>
        </div>
    </xsl:template>
    
    <xsl:template match="rdf:RDF" mode="bs2:ActionBarRight"/>

    <xsl:template match="*[@rdf:about]" mode="bs2:NavBarListItem">
        <xsl:param name="space" as="xs:anyURI*"/>
        <li>
            <xsl:if test="@rdf:about = $space">
                <xsl:attribute name="class">active</xsl:attribute>
            </xsl:if>
            
            <xsl:apply-templates select="." mode="xhtml:Anchor"/>
        </li>
    </xsl:template>
    
    <xsl:template match="rdf:RDF" mode="bs2:Footer">
        <div class="footer text-center">
            <p>
                <hr/>
                <xsl:value-of select="format-date(current-date(), '[Y]', $ldt:lang, (), ())"/>.
                Developed by <xsl:apply-templates select="key('resources', key('resources', '', document(''))/foaf:maker/@rdf:resource, document(''))" mode="xhtml:Anchor"/>.
                <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache License</a>.
            </p>
        </div>
    </xsl:template>

    <!-- TITLE -->
    
    <xsl:template match="rdf:RDF[key('resources-by-type', '&http;Response')]" mode="xhtml:Title" priority="2">
        <title>
            <xsl:apply-templates select="key('resources-by-type', '&http;Response')" mode="#current"/>
        </title>
    </xsl:template>
    
    <xsl:template match="rdf:RDF[$ac:uri]" mode="xhtml:Title" priority="1">
        <title>
            <xsl:apply-templates select="key('resources', ac:document-uri($ac:uri))" mode="ac:label"/>
        </title>
    </xsl:template>

    <xsl:template match="*" mode="xhtml:Title"/>
    
    <!-- STYLE MODE -->
    
    <xsl:template match="rdf:RDF" mode="xhtml:Style">
        <link href="{resolve-uri('static/css/bootstrap.css', $ac:contextUri)}" rel="stylesheet" type="text/css"/>
        <link href="{resolve-uri('static/css/bootstrap-responsive.css', $ac:contextUri)}" rel="stylesheet" type="text/css"/>
        <link href="{resolve-uri('static/com/atomgraph/client/css/bootstrap.css', $ac:contextUri)}" rel="stylesheet" type="text/css"/>
    </xsl:template>
    
    <!-- SCRIPT MODE -->

    <xsl:template match="rdf:RDF" mode="xhtml:Script">
        <script type="text/javascript" src="{resolve-uri('static/js/jquery.min.js', $ac:contextUri)}"></script>
        <script type="text/javascript" src="{resolve-uri('static/js/bootstrap.js', $ac:contextUri)}"></script>
        <script type="text/javascript" src="{resolve-uri('static/com/atomgraph/client/js/UUID.js', $ac:contextUri)}"></script>
        <script type="text/javascript" src="{resolve-uri('static/com/atomgraph/client/js/jquery.js', $ac:contextUri)}"></script>
        <xsl:if test="$ac:mode = '&ac;MapMode'">
            <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key={$ac:googleMapsKey}"/>
            <script type="text/javascript" src="{resolve-uri('static/com/atomgraph/client/js/google-maps.js', $ac:contextUri)}"></script>
        </xsl:if>
    </xsl:template>

    <!-- MAIN MODE -->

    <!-- always show errors in block mode -->
    <xsl:template match="rdf:RDF[key('resources-by-type', '&http;Response')][not(key('resources-by-type', '&spin;ConstraintViolation'))]" mode="bs2:Main" priority="1">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'span12'" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
        
            <xsl:apply-templates mode="bs2:Block"/>
        </div>
    </xsl:template>
    
    <xsl:template match="rdf:RDF" mode="bs2:Main">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'span8'" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>

            <xsl:apply-templates select="." mode="ac:ModeChoice"/>
        </div>
    </xsl:template>
            
    <xsl:template match="rdf:RDF" mode="ac:ModeChoice">
        <xsl:choose>
            <xsl:when test="$ac:mode = '&ac;EditMode' or $ac:forClass">
                <xsl:apply-templates select="." mode="bs2:Form"/>
            </xsl:when>
            <xsl:when test="$ac:mode = '&ac;MapMode'">
                <xsl:apply-templates select="." mode="bs2:Map"/>
            </xsl:when>            
            <xsl:otherwise>
                <xsl:apply-templates select="." mode="bs2:Block"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- LIST -->
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:List">
        <xsl:param name="active" as="xs:boolean?"/>
        
        <li>
            <xsl:if test="$active">
                <xsl:attribute name="class">active</xsl:attribute>
            </xsl:if>

            <xsl:apply-templates select="." mode="xhtml:Anchor"/>
        </li>
    </xsl:template>
    
    <!-- NAVBAR ACTIONS -->
    
    <xsl:template match="rdf:RDF" mode="bs2:NavBarActions">
        <div class="pull-right">
            <form action="{@rdf:about}?_method=DELETE" method="post">
                <button class="btn btn-delete" type="submit">
                    <xsl:apply-templates select="key('resources', '&ac;Delete', document('&ac;'))" mode="ac:label"/>
                </button>
            </form>
        </div>

        <xsl:if test="not($ac:mode = '&ac;EditMode')">
            <div class="pull-right">
                <a class="btn" href="{@rdf:about}{ac:query-string((), xs:anyURI('&ac;EditMode'))}">
                    <xsl:apply-templates select="key('resources', '&ac;EditMode', document('&ac;'))" mode="ac:label"/>
                </a>
            </div>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:NavBarActions"/>
    
    <!-- CREATE -->
    
    <xsl:template match="rdf:RDF" mode="bs2:Create"/>

    <!-- MODE LIST -->

    <xsl:template match="rdf:RDF[key('resources-by-type', '&http;Response')][not(key('resources-by-type', '&spin;ConstraintViolation'))]" mode="bs2:ModeList" priority="1"/>

    <xsl:template match="rdf:RDF" mode="bs2:ModeList">
        <xsl:param name="modes" select="key('resources-by-type', ('&ac;Mode'), document('&ac;'))" as="element()*"/>
        
        <div class="btn-group pull-right">
            <button type="button" class="btn dropdown-toggle" title="{ac:label(key('resources', '&ac;Mode', document('&ac;')))}">
                <xsl:apply-templates select="key('resources', '&ac;Mode', document('&ac;'))" mode="ac:label"/>
                <xsl:text> </xsl:text>
                <span class="caret"></span>
            </button>

            <ul class="dropdown-menu">
                <xsl:variable name="active" select="$ac:mode[1]" as="xs:anyURI?"/> <!-- multiple modes can be present; selecting first as a workaround -->
                <xsl:for-each select="$modes">
                    <xsl:sort select="ac:label(.)"/>
                    <xsl:apply-templates select="." mode="bs2:ModeListItem">
                        <xsl:with-param name="active" select="$active"/>
                    </xsl:apply-templates>
                </xsl:for-each>
            </ul>
        </div>
    </xsl:template>
                
    <xsl:template match="*[@rdf:about]" mode="bs2:ModeListItem">
        <xsl:param name="active" as="xs:anyURI*"/>
        
        <li>
            <xsl:if test="@rdf:about = $active">
                <xsl:attribute name="class">active</xsl:attribute>
            </xsl:if>

            <a href="?uri={$ac:uri}&amp;mode={encode-for-uri(@rdf:about)}" title="{ac:label(.)}">
                <xsl:apply-templates select="." mode="ac:label"/>
            </a>
        </li>
    </xsl:template>
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:ModeList"/>

    <!-- BREADCRUMB MODE -->

    <xsl:template match="rdf:RDF[$ac:uri]" mode="bs2:BreadCrumbList" priority="1">
        <ul class="breadcrumb">
            <xsl:apply-templates select="key('resources', ac:document-uri($ac:uri))" mode="bs2:BreadCrumbListItem"/>
        </ul>
    </xsl:template>

    <xsl:template match="*" mode="bs2:BreadCrumbList"/>
        
    <xsl:template match="*[@rdf:about]" mode="bs2:BreadCrumbListItem">
        <xsl:param name="leaf" select="true()" as="xs:boolean" tunnel="yes"/>

        <xsl:choose>
            <xsl:when test="key('resources', sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource)">
                <xsl:apply-templates select="key('resources', sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource)" mode="#current">
                    <xsl:with-param name="leaf" select="false()" tunnel="yes"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource">
                <xsl:if test="doc-available((sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource)[1])">
                    <xsl:variable name="parent-doc" select="document(sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource)" as="document-node()?"/>
                    <xsl:apply-templates select="key('resources', sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource, $parent-doc)" mode="#current">
                        <xsl:with-param name="leaf" select="false()" tunnel="yes"/>
                    </xsl:apply-templates>
                </xsl:if>
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

    <xsl:template match="*[rdf:type/@rdf:resource = '&http;Response']" mode="bs2:Header" priority="1">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'alert alert-error well'" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>

            <h2>
                <xsl:apply-templates select="." mode="ac:label"/>
            </h2>
        </div>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:Header">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'well header'" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>

            <xsl:apply-templates select="." mode="bs2:Image"/>
            
            <xsl:apply-templates select="." mode="bs2:Actions"/>

            <h2>
                <xsl:apply-templates select="." mode="xhtml:Anchor"/>
            </h2>
            
            <p>
                <xsl:apply-templates select="." mode="ac:description"/>
            </p>

            <xsl:apply-templates select="." mode="bs2:TypeList"/>
        </div>
    </xsl:template>
    
    <!-- MEDIA TYPE SELECT MODE (Export buttons) -->
        
    <xsl:template match="rdf:RDF" mode="bs2:MediaTypeList">
        <div class="btn-group pull-right">
            <div class="btn dropdown-toggle">Export <span class="caret"></span></div>
            <ul class="dropdown-menu">
                <li>
                    <a href="?uri={encode-for-uri($ac:uri)}&amp;accept={encode-for-uri('application/rdf+xml')}">RDF/XML</a>
                </li>
                <li>
                    <a href="?uri={encode-for-uri($ac:uri)}&amp;accept={encode-for-uri('text/turtle')}">Turtle</a>
                </li>
                <!--
                <xsl:if test="@rdf:about = $a:requestUri and $query-res/sp:text">
                    <li>
                        <a href="{resolve-uri('sparql', $ldt:base)}?query={encode-for-uri($query-res/sp:text)}">SPARQL</a>
                    </li>
                </xsl:if>
                -->
            </ul>
        </div>
    </xsl:template>

    <!-- RIGHT NAV MODE -->
    
    <xsl:template match="rdf:RDF[key('resources-by-type', '&http;Response')][not(key('resources-by-type', '&spin;ConstraintViolation'))]" mode="bs2:Right" priority="1"/>
    
    <xsl:template match="rdf:RDF" mode="bs2:Right">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'span4'" as="xs:string?"/>
        
        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>

            <xsl:apply-templates mode="#current"/>
        </div>
    </xsl:template>
        
    <xsl:template match="*[*][@rdf:about or @rdf:nodeID]" mode="bs2:Right"/>

    <!-- BLOCK MODE -->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:Block">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>

            <xsl:apply-templates select="." mode="bs2:Header"/>

            <xsl:apply-templates select="." mode="bs2:PropertyList"/>
        </div>
    </xsl:template>

    <!-- inline blank node resource if there is only one property except foaf:primaryTopic having it as object -->
    <xsl:template match="@rdf:nodeID[key('resources', .)][count(key('predicates-by-object', .)[not(self::foaf:primaryTopic)]) = 1]" priority="2">
        <xsl:param name="inline" select="true()" as="xs:boolean" tunnel="yes"/>

        <xsl:choose>
            <xsl:when test="$inline">
                <xsl:apply-templates select="key('resources', .)" mode="bs2:Block">
                    <xsl:with-param name="display" select="$inline" tunnel="yes"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    
    <!-- hide inlined blank node resources from the main block flow -->
    <xsl:template match="*[*][key('resources', @rdf:nodeID)][count(key('predicates-by-object', @rdf:nodeID)[not(self::foaf:primaryTopic)]) = 1]" mode="bs2:Block" priority="1">
        <xsl:param name="display" select="false()" as="xs:boolean" tunnel="yes"/>
        
        <xsl:if test="$display">
            <xsl:next-match/>
        </xsl:if>
    </xsl:template>

    <!-- FORM MODE -->

    <xsl:template match="rdf:RDF" mode="bs2:Form" priority="3">
        <xsl:param name="method" select="'post'" as="xs:string"/>
        <xsl:param name="action" select="xs:anyURI('?_method=PUT')" as="xs:anyURI"/>
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

            <xsl:apply-templates select="." mode="bs2:Legend"/>

            <xsl:choose>
                <xsl:when test="$ac:forClass and not(key('resources-by-type', '&spin;ConstraintViolation'))">
                    <xsl:apply-templates select="ac:construct-doc($ldt:ontology, $ac:forClass, $ldt:base)/rdf:RDF/*" mode="#current"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates mode="#current"/>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:apply-templates select="." mode="bs2:FormActions">
                <xsl:with-param name="button-class" select="$button-class"/>
            </xsl:apply-templates>
        </form>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:Form">
        <xsl:apply-templates select="." mode="bs2:FormControl">
            <xsl:sort select="ac:label(.)"/>
        </xsl:apply-templates>
    </xsl:template>
    
    <!-- LEGEND -->

    <xsl:template match="rdf:RDF" mode="bs2:Legend" priority="2">
        <xsl:apply-templates mode="#current"/>
    </xsl:template>

    <xsl:template match="*[rdf:type/@rdf:resource = $ac:forClass]" mode="bs2:Legend" priority="1">
        <xsl:param name="forClass" select="$ac:forClass" as="xs:anyURI"/>

        <xsl:for-each select="key('resources', $forClass, $ac:sitemap)">
            <legend>
                <xsl:apply-templates select="key('resources', '&ac;ConstructMode', document('&ac;'))" mode="ac:label"/>
                <xsl:text> </xsl:text>
                <xsl:apply-templates select="." mode="ac:label"/>
            </legend>
            <xsl:if test="ac:description(.)">
                <p class="text-info">
                    <xsl:apply-templates select="." mode="ac:description"/>
                </p>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <!--
    <xsl:template match="*[@rdf:about = $ac:uri]" mode="bs2:Legend" priority="1">
        <legend>
            <xsl:apply-templates select="." mode="xhtml:Anchor"/>
        </legend>
    </xsl:template>
    -->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:Legend"/>

    <!-- FORM ACTIONS -->
    
    <xsl:template match="rdf:RDF" mode="bs2:FormActions">
        <xsl:param name="button-class" select="'btn btn-primary'" as="xs:string?"/>
        
        <div class="form-actions">
            <button type="submit" class="{$button-class}">Save</button>
        </div>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:FormControl">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="legend" select="if (@rdf:about) then true() else not(key('predicates-by-object', @rdf:nodeID))" as="xs:boolean"/>
        <xsl:param name="violations" select="key('violations-by-root', (@rdf:about, @rdf:nodeID))" as="element()*"/>
        <xsl:param name="template-doc" select="if ($ldt:ontology) then ac:construct-doc($ldt:ontology, $ac:forClass, $ldt:base) else ()" as="document-node()?"/>
        <xsl:param name="template" select="$template-doc/rdf:RDF/*[@rdf:nodeID][every $type in rdf:type/@rdf:resource satisfies current()/rdf:type/@rdf:resource = $type]" as="element()*"/>
        <xsl:param name="traversed-ids" select="@rdf:*" as="xs:string*" tunnel="yes"/>

        <fieldset>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>

            <xsl:apply-templates select="$violations" mode="bs2:Violation"/>

            <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>

            <xsl:if test="not($template)">
                <xsl:message>Template is not defined for resource '<xsl:value-of select="@rdf:about | @rdf:nodeID"/>' with types '<xsl:value-of select="rdf:type/@rdf:resource"/>'</xsl:message>
            </xsl:if>
            <xsl:apply-templates select="* | $template/*[not(concat(namespace-uri(), local-name(), @xml:lang, @rdf:datatype) = current()/*/concat(namespace-uri(), local-name(), @xml:lang, @rdf:datatype))]" mode="#current">
                <xsl:sort select="ac:property-label(.)"/>
                <xsl:with-param name="violations" select="$violations"/>
                <xsl:with-param name="traversed-ids" select="$traversed-ids" tunnel="yes"/>
            </xsl:apply-templates>
        </fieldset>
    </xsl:template>

    <!-- CONSTRAINT VIOLATION MODE -->
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:Violation"/>

    <xsl:template match="*[rdf:type/@rdf:resource = '&spin;ConstraintViolation']" mode="bs2:Violation" priority="1">
        <xsl:param name="class" select="'alert alert-error'" as="xs:string?"/>

        <div>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="." mode="ac:label"/>
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
                    <xsl:apply-templates select="." mode="ac:label"/>
                </span>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
            
</xsl:stylesheet>