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
    <!ENTITY a      "https://w3id.org/atomgraph/core#">
    <!ENTITY ac     "https://w3id.org/atomgraph/client#">
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
<xsl:stylesheet version="3.0"
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

    <xsl:function name="ac:uri" as="xs:anyURI?">
        <xsl:sequence select="$ac:uri"/>
    </xsl:function>
    
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
            <xsl:apply-templates select="." mode="xhtml:Meta"/>
    
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
                                    <xsl:attribute name="alt">
                                        <xsl:value-of>
                                            <xsl:apply-templates select="." mode="ac:label"/>
                                        </xsl:value-of>
                                    </xsl:attribute>
                                </img>
                            </xsl:for-each>
                        </a>
                    </xsl:if>

                    <div id="collapsing-top-navbar" class="nav-collapse collapse">
                        <form action="" method="get" class="navbar-form pull-left" accept-charset="UTF-8">
                            <div class="input-append">
                                <input type="text" name="uri" class="input-xxlarge">
                                    <xsl:if test="ac:uri()">
                                        <xsl:attribute name="value">
                                            <xsl:sequence select="ac:uri()"/>
                                        </xsl:attribute>
                                    </xsl:if>
                                </input>
                                <button type="submit" class="btn btn-primary">Go</button>
                            </div>
                        </form>

                        <ul class="nav pull-right">
                            <li>
                                <a href="{ac:build-uri((), map{ 'mode': '&ac;QueryEditorMode' })}">Query editor</a>
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
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
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
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            
            <xsl:apply-templates select="." mode="bs2:Create"/>
        </div>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="bs2:ActionBarMain">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'span10'" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            
            <xsl:apply-templates select="." mode="bs2:MediaTypeList"/>

            <xsl:apply-templates select="." mode="bs2:NavBarActions"/>

            <xsl:apply-templates select="." mode="bs2:ModeList"/>

            <xsl:apply-templates select="." mode="bs2:BreadCrumbList"/>
        </div>
    </xsl:template>
    
    <xsl:template match="rdf:RDF" mode="bs2:ActionBarRight"/>
    
    <xsl:template match="rdf:RDF" mode="bs2:Footer">
        <div class="footer text-center">
            <p>
                <hr/>
                <xsl:sequence select="format-date(current-date(), '[Y]', $ldt:lang, (), ())"/>.
                Developed by <xsl:apply-templates select="key('resources', key('resources', '', document(''))/foaf:maker/@rdf:resource, document(''))/@rdf:about" mode="xhtml:Anchor"/>.
                <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache License</a>.
            </p>
        </div>
    </xsl:template>
    
    <!-- META -->
    
    <xsl:template match="rdf:RDF" mode="xhtml:Meta">
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    </xsl:template>
    
    <!-- TITLE -->
    
    <xsl:template match="rdf:RDF[key('resources-by-type', '&http;Response')]" mode="xhtml:Title" priority="2">
        <title>
            <xsl:apply-templates select="key('resources-by-type', '&http;Response')" mode="#current"/>
        </title>
    </xsl:template>
    
    <xsl:template match="rdf:RDF[ac:uri()]" mode="xhtml:Title" priority="1">
        <title>
            <xsl:value-of>
                <xsl:apply-templates select="key('resources', ac:document-uri(ac:uri()))" mode="ac:label"/>
            </xsl:value-of>
        </title>
    </xsl:template>

    <xsl:template match="*" mode="xhtml:Title"/>
    
    <!-- STYLE  -->
    
    <xsl:template match="rdf:RDF" mode="xhtml:Style">
        <link href="{resolve-uri('static/css/bootstrap.css', $ac:contextUri)}" rel="stylesheet" type="text/css"/>
        <link href="{resolve-uri('static/css/bootstrap-responsive.css', $ac:contextUri)}" rel="stylesheet" type="text/css"/>
        <link href="{resolve-uri('static/com/atomgraph/client/css/bootstrap.css', $ac:contextUri)}" rel="stylesheet" type="text/css"/>
    </xsl:template>
    
    <!-- SCRIPT  -->

    <xsl:template match="rdf:RDF" mode="xhtml:Script">
        <script type="text/javascript" src="{resolve-uri('static/js/jquery.min.js', $ac:contextUri)}" defer="defer"></script>
        <script type="text/javascript" src="{resolve-uri('static/js/bootstrap.js', $ac:contextUri)}" defer="defer"></script>
        <script type="text/javascript" src="{resolve-uri('static/com/atomgraph/client/js/UUID.js', $ac:contextUri)}" defer="defer"></script>
        <script type="text/javascript" src="{resolve-uri('static/com/atomgraph/client/js/jquery.js', $ac:contextUri)}" defer="defer"></script>
    </xsl:template>

    <!-- MAIN  -->

    <!-- always show errors in block  -->
    <xsl:template match="rdf:RDF[key('resources-by-type', '&http;Response')][not(key('resources-by-type', '&spin;ConstraintViolation'))]" mode="bs2:Main" priority="1">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'span12'" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
        
            <xsl:apply-templates mode="bs2:Block"/>
        </div>
    </xsl:template>
    
    <xsl:template match="rdf:RDF" mode="bs2:Main">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'span8'" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
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
            <xsl:when test="$ac:mode = '&ac;GraphMode'">
                <xsl:apply-templates select="." mode="bs2:Graph"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="." mode="bs2:Block"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- NAVBAR ACTIONS -->
    
    <xsl:template match="rdf:RDF[ac:uri()]" mode="bs2:NavBarActions" priority="1">
        <div class="pull-right">
            <form action="{ac:document-uri(ac:uri())}?_method=DELETE" method="post">
                <button class="btn btn-delete" type="submit">
                    <xsl:value-of>
                        <xsl:apply-templates select="key('resources', '&ac;Delete', document(ac:document-uri('&ac;')))" mode="ac:label"/>
                    </xsl:value-of>
                </button>
            </form>
        </div>

        <xsl:if test="not($ac:mode = '&ac;EditMode')">
            <div class="pull-right">
                <a class="btn" href="{ac:build-uri(xs:anyURI(''), map{ 'uri': string(ac:document-uri(ac:uri())), 'mode': '&ac;EditMode' })}">
                    <xsl:value-of>
                        <xsl:apply-templates select="key('resources', '&ac;EditMode', document(ac:document-uri('&ac;')))" mode="ac:label"/>
                    </xsl:value-of>
                </a>
            </div>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="rdf:RDF" mode="bs2:NavBarActions"/>
    
    <!-- CREATE -->
    
    <xsl:template match="rdf:RDF" mode="bs2:Create"/>

    <!-- MODE LIST -->

    <xsl:template match="rdf:RDF[key('resources-by-type', '&http;Response')][not(key('resources-by-type', '&spin;ConstraintViolation'))]" mode="bs2:ModeList" priority="2"/>

    <xsl:template match="rdf:RDF[ac:uri()]" mode="bs2:ModeList" priority="1">
        <xsl:param name="modes" select="key('resources-by-type', ('&ac;DocumentMode'), document(ac:document-uri('&ac;')))" as="element()*"/>
        
        <div class="btn-group pull-right">
            <button type="button" class="btn dropdown-toggle" title="{ac:label(key('resources', '&ac;Mode', document(ac:document-uri('&ac;'))))}">
                <xsl:value-of>
                    <xsl:apply-templates select="key('resources', '&ac;Mode', document(ac:document-uri('&ac;')))" mode="ac:label"/>
                </xsl:value-of>
                <xsl:text> </xsl:text>
                <span class="caret"></span>
            </button>

            <ul class="dropdown-menu">
                <xsl:for-each select="$modes">
                    <xsl:sort select="ac:label(.)"/>
                    <xsl:apply-templates select="." mode="bs2:ModeListItem">
                        <xsl:with-param name="active" select="@rdf:about = $ac:mode"/>
                    </xsl:apply-templates>
                </xsl:for-each>
            </ul>
        </div>
    </xsl:template>
                
    <xsl:template match="*[@rdf:about]" mode="bs2:ModeListItem">
        <xsl:param name="active" as="xs:boolean"/>
        <xsl:param name="class" select="if ($active) then 'active' else ()" as="xs:string?"/>

        <li>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>

            <a href="{ac:build-uri((), map{ 'uri': string(ac:document-uri(ac:uri())), 'mode': string(@rdf:about) })}" title="{ac:label(.)}">
                <xsl:value-of>
                    <xsl:apply-templates select="." mode="ac:label"/>
                </xsl:value-of>
            </a>
        </li>
    </xsl:template>
    
    <xsl:template match="*" mode="bs2:ModeList"/>

    <!-- HEADER -->

    <xsl:template match="*[rdf:type/@rdf:resource = '&http;Response']" mode="bs2:Header" priority="1">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'alert alert-error well'" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>

            <h2>
                <xsl:value-of>
                    <xsl:apply-templates select="." mode="ac:label"/>
                </xsl:value-of>
            </h2>
        </div>
    </xsl:template>

    <!-- MEDIA TYPE SELECT MODE (Export buttons) -->
        
    <xsl:template match="rdf:RDF[ac:uri()]" mode="bs2:MediaTypeList" priority="1">
        <div class="btn-group pull-right">
            <div class="btn dropdown-toggle">Export <span class="caret"></span></div>
            <ul class="dropdown-menu">
                <li>
                    <a href="{ac:build-uri((), map{ 'uri': string(ac:document-uri(ac:uri())), 'accept': 'application/rdf+xml' })}">RDF/XML</a>
                </li>
                <li>
                    <a href="{ac:build-uri((), map{ 'uri': string(ac:document-uri(ac:uri())), 'accept': 'text/turtle' })}">Turtle</a>
                </li>
                <!--
                <xsl:if test="@rdf:about = $a:requestUri and $query-res/sp:text">
                    <li>
                        <a href="{ac:build-uri(resolve-uri('sparql', $ldt:base), map{ 'query': string($query-res/sp:text) })}">SPARQL</a>
                    </li>
                </xsl:if>
                -->
            </ul>
        </div>
    </xsl:template>

    <xsl:template match="*" mode="bs2:MediaTypeList"/>
    
    <!-- RIGHT NAV  -->
    
    <xsl:template match="rdf:RDF[key('resources-by-type', '&http;Response')][not(key('resources-by-type', '&spin;ConstraintViolation'))]" mode="bs2:Right" priority="1"/>
    
    <xsl:template match="rdf:RDF" mode="bs2:Right">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'span4'" as="xs:string?"/>
        
        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>

            <xsl:apply-templates mode="#current"/>
        </div>
    </xsl:template>
        
    <xsl:template match="*[*][@rdf:about or @rdf:nodeID]" mode="bs2:Right"/>

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
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID">
        <xsl:variable name="bnode" select="key('resources', .)[not(@rdf:nodeID = current()/../../@rdf:nodeID)][not(*/@rdf:nodeID = current()/../../@rdf:nodeID)]" as="element()?"/>

        <xsl:choose>
            <xsl:when test="$bnode">
                <xsl:apply-templates select="$bnode" mode="bs2:Block">
                    <xsl:with-param name="display" select="true()" tunnel="yes"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
            
</xsl:stylesheet>