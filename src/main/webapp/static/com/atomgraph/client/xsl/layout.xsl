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
    <!ENTITY translations "https://w3id.org/atomgraph/client/xsl/translations.rdf#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY xhv    "http://www.w3.org/1999/xhtml/vocab#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY owl    "http://www.w3.org/2002/07/owl#">
    <!ENTITY srx    "http://www.w3.org/2005/sparql-results#">
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
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:a="&a;"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:srx="&srx;"
xmlns:http="&http;"
xmlns:ldt="&ldt;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:sp="&sp;"
xmlns:spin="&spin;"
xmlns:xhv="&xhv;"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
exclude-result-prefixes="#all">

    <xsl:include href="sparql.xsl"/>

    <xsl:output method="xhtml" html-version="5" encoding="UTF-8" indent="yes" omit-xml-declaration="yes" media-type="application/xhtml+xml"/>
    
    <xsl:param name="ldt:base" as="xs:anyURI?"/>
    <xsl:param name="ac:contextUri" as="xs:anyURI?"/>
    <xsl:param name="ac:endpoint" as="xs:anyURI?"/>
    <xsl:param name="ac:forClass" as="xs:anyURI?"/>
    <xsl:param name="ac:mode" select="xs:anyURI('&ac;ReadMode')" as="xs:anyURI*"/>
    <xsl:param name="ac:query" as="xs:string?"/>
    <xsl:param name="ldt:ontology" as="xs:anyURI?"/>
    <!-- ordered language preference list; the writer passes Accept-Language, client-side stylesheets override with the browser's list -->
    <xsl:param name="ac:langs" select="'en'" as="xs:string*"/>
    <!-- the language the representation is composed in, as opposed to the one the reader asked for: the writer supplies the
         highest-ranked accepted language the UI actually has. Distinct from what the reader accepts because asking for a
         language does not make the page that language - reporting the request as the content's language is what put
         lang="de" on a page written entirely in English.

         Defaults to en rather than to the reader's top preference. The rule is "the first accepted language the UI provides, else en", and
         Web-Client's own strings are English only, so for a client that supplies no value the negotiation would always answer
         en whatever the reader asked for. Defaulting to their preference would return a value the negotiation cannot produce - and
         would keep the defect as the out-of-the-box behaviour -->
    <xsl:param name="ac:contentLang" select="'en'" as="xs:string"/>

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
        <html lang="{$ac:contentLang}">
            <xsl:apply-templates/>
        </html>
    </xsl:template>

    <xsl:template match="rdf:RDF">
        <xsl:variable name="grouped-rdf" as="document-node()">
            <xsl:apply-templates select="root(.)" mode="ac:GroupTriples"/>
        </xsl:variable>

        <xsl:for-each select="$grouped-rdf/rdf:RDF">
            <xsl:apply-templates select="." mode="xhtml:Head"/>

            <xsl:apply-templates select="." mode="xhtml:Body"/>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="srx:sparql">
        <xsl:apply-templates select="." mode="xhtml:Head"/>

        <xsl:apply-templates select="." mode="xhtml:Body"/>
    </xsl:template>
    
    <xsl:template match="rdf:RDF | srx:sparql" mode="xhtml:Head">
        <head>
            <xsl:apply-templates select="." mode="xhtml:Meta"/>

            <xsl:apply-templates select="." mode="xhtml:Title"/>

            <xsl:apply-templates select="." mode="xhtml:Style"/>

            <xsl:apply-templates select="." mode="xhtml:Script"/>
        </head>
    </xsl:template>

    <!-- the map is a full-bleed canvas: it takes every pixel below the bars (client.css keys on the class) -->
    <xsl:template match="rdf:RDF[$ac:mode = '&ac;MapMode']" mode="xhtml:Body" priority="1">
        <xsl:next-match>
            <xsl:with-param name="class" select="'map-view'"/>
        </xsl:next-match>
    </xsl:template>

    <xsl:template match="rdf:RDF | srx:sparql" mode="xhtml:Body">
        <xsl:param name="class" as="xs:string?"/>

        <body>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>

            <xsl:apply-templates select="." mode="ac:Header"/>

            <div class="content">
                <xsl:apply-templates select="." mode="ac:Main"/>

                <xsl:apply-templates select="." mode="ac:Aside"/>
            </div>

            <xsl:apply-templates select="." mode="ac:Footer"/>
        </body>
    </xsl:template>

    <xsl:template match="rdf:RDF | srx:sparql" mode="ac:Header">
        <div class="header">
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

            <form action="" method="get" class="uri-form" accept-charset="UTF-8">
                <div class="ldhc-field">
                    <div class="ldhc-field-box sz-md">
                        <span class="ldhc-adorn"><span class="msi outline sm" aria-hidden="true">public</span></span>
                        <input type="text" name="uri">
                            <xsl:if test="base-uri()">
                                <xsl:attribute name="value" select="base-uri()"/>
                            </xsl:if>
                        </input>
                    </div>
                </div>
                <button type="submit" class="ldhc-btn in-primary ap-solid sz-md">
                    <xsl:apply-templates select="key('resources', '&translations;go', ac:translations())" mode="ac:label"/>
                </button>
            </form>

            <ul class="nav">
                <li>
                    <a href="{ac:build-uri((), map{ 'mode': '&ac;QueryEditorMode' })}">
                        <xsl:apply-templates select="key('resources', '&translations;query-editor', ac:translations())" mode="ac:label"/>
                    </a>
                </li>
            </ul>
        </div>

        <xsl:apply-templates select="." mode="ac:ActionBar"/>
    </xsl:template>

    <xsl:template match="rdf:RDF | srx:sparql" mode="ac:ActionBar">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'action-bar'" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>

            <xsl:apply-templates select="." mode="ac:ActionBarLeft"/>

            <xsl:apply-templates select="." mode="ac:ActionBarMain"/>

            <xsl:apply-templates select="." mode="ac:ActionBarRight"/>
        </div>
    </xsl:template>

    <xsl:template match="rdf:RDF | srx:sparql" mode="ac:ActionBarLeft">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'ab-left'" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>

            <xsl:apply-templates select="." mode="ac:Create"/>
        </div>
    </xsl:template>

    <xsl:template match="rdf:RDF | srx:sparql" mode="ac:ActionBarMain">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'ab-main'" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>

            <xsl:apply-templates select="." mode="ac:Breadcrumb"/>

            <xsl:apply-templates select="." mode="ac:ModeList"/>

            <xsl:apply-templates select="." mode="ac:HeaderActions"/>

            <xsl:apply-templates select="." mode="ac:MediaTypeList"/>
        </div>
    </xsl:template>

    <xsl:template match="srx:sparql" mode="ac:Breadcrumb"/>

    <xsl:template match="rdf:RDF | srx:sparql" mode="ac:ActionBarRight"/>

    <xsl:template match="rdf:RDF | srx:sparql" mode="ac:Footer">
        <div class="footer">
            <p>
                <xsl:sequence select="format-date(current-date(), '[Y]', ac:langs()[1], (), ())"/>.
                Developed by <xsl:apply-templates select="key('resources', key('resources', '', document(''))/foaf:maker/@rdf:resource, document(''))/@rdf:about" mode="xhtml:Anchor"/>.
                <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache License</a>.
            </p>
        </div>
    </xsl:template>
    
    <!-- META -->
    
    <xsl:template match="rdf:RDF | srx:sparql" mode="xhtml:Meta">
        <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    </xsl:template>
    
    <!-- TITLE -->
    
    <xsl:template match="rdf:RDF[key('resources-by-type', '&http;Response')]" mode="xhtml:Title" priority="2">
        <title>
            <xsl:apply-templates select="key('resources-by-type', '&http;Response')" mode="#current"/>
        </title>
    </xsl:template>
    
    <xsl:template match="rdf:RDF[base-uri()] | srx:sparql[base-uri()]" mode="xhtml:Title" priority="1">
        <title>
            <xsl:value-of>
                <xsl:apply-templates select="key('resources', ac:absolute-path(base-uri()))" mode="ac:label"/>
            </xsl:value-of>
        </title>
    </xsl:template>

    <xsl:template match="*" mode="xhtml:Title"/>
    
    <!-- STYLE  -->

    <!-- the map's own stylesheet rides only the mode that renders a map. Lives beside the base template
         (not in container.xsl) because layout.xsl is included at entry level: an imported override would
         lose on import precedence however high its priority -->
    <xsl:template match="rdf:RDF[$ac:mode = '&ac;MapMode']" mode="xhtml:Style" priority="1">
        <xsl:next-match/>

        <link href="{resolve-uri('static/com/atomgraph/client/css/ol.css', $ac:contextUri)}" rel="stylesheet" type="text/css"/>
    </xsl:template>

    <!-- design-system tokens and core kit (core.css imports controls/overlays/surfaces), then Web-Client's own browser chrome -->
    <xsl:template match="rdf:RDF | srx:sparql" mode="xhtml:Style">
        <link href="{resolve-uri('static/com/atomgraph/client/css/colors_and_type.css', $ac:contextUri)}" rel="stylesheet" type="text/css"/>
        <link href="{resolve-uri('static/com/atomgraph/client/css/core.css', $ac:contextUri)}" rel="stylesheet" type="text/css"/>
        <link href="{resolve-uri('static/com/atomgraph/client/css/client.css', $ac:contextUri)}" rel="stylesheet" type="text/css"/>
    </xsl:template>

    <!-- SCRIPT  -->

    <xsl:template match="rdf:RDF | srx:sparql" mode="xhtml:Script">
        <script type="text/javascript" src="{resolve-uri('static/com/atomgraph/client/js/client.js', $ac:contextUri)}" defer="defer"></script>
    </xsl:template>

    <!-- MAIN  -->

    <!-- always show errors in block  -->
    <xsl:template match="rdf:RDF[key('resources-by-type', '&http;Response')][not(key('resources-by-type', '&spin;ConstraintViolation'))]" mode="ac:Main" priority="1">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'main'" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
        
            <xsl:apply-templates/>
        </div>
    </xsl:template>
    
    <xsl:template match="rdf:RDF | srx:sparql" mode="ac:Main">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'main'" as="xs:string?"/>

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
                <xsl:apply-templates select="." mode="ac:ResourceForm"/>
            </xsl:when>
            <xsl:when test="$ac:mode = '&ac;MapMode'">
                <xsl:apply-templates select="." mode="ac:Map"/>
            </xsl:when>
            <xsl:when test="$ac:mode = '&ac;GraphMode'">
                <xsl:apply-templates select="." mode="ac:Graph"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="srx:sparql" mode="ac:ModeChoice">
        <xsl:apply-templates select="." mode="xhtml:Table"/>
    </xsl:template>
    
    <!-- NAVBAR ACTIONS -->
    
    <xsl:template match="rdf:RDF[base-uri()]" mode="ac:HeaderActions" priority="1">
        <div class="actions">
            <xsl:if test="not($ac:mode = '&ac;EditMode')">
                <a class="ldhc-btn in-neutral ap-outline sz-sm" href="{ac:build-uri(xs:anyURI(''), map{ 'uri': string(ac:absolute-path(base-uri())), 'mode': '&ac;EditMode' })}">
                    <span class="msi sm" aria-hidden="true">edit</span>
                    <xsl:value-of>
                        <xsl:apply-templates select="key('resources', '&translations;edit', ac:translations())" mode="ac:label"/>
                    </xsl:value-of>
                </a>
            </xsl:if>

            <form action="{ac:build-uri(xs:anyURI(''), map{ 'uri': string(ac:absolute-path(base-uri()))})}?_method=DELETE" method="post">
                <button class="ldhc-btn in-destructive ap-outline sz-sm btn-delete" type="submit">
                    <xsl:attribute name="data-confirm">
                        <xsl:apply-templates select="key('resources', '&translations;confirm-delete', ac:translations())" mode="ac:label"/>
                    </xsl:attribute>
                    <span class="msi sm" aria-hidden="true">delete</span>
                    <xsl:value-of>
                        <xsl:apply-templates select="key('resources', '&translations;delete', ac:translations())" mode="ac:label"/>
                    </xsl:value-of>
                </button>
            </form>
        </div>
    </xsl:template>
    
    <xsl:template match="rdf:RDF | srx:sparql" mode="ac:HeaderActions"/>
    
    <!-- CREATE -->
    
    <xsl:template match="rdf:RDF | srx:sparql" mode="ac:Create"/>

    <!-- MODE LIST -->

    <xsl:template match="rdf:RDF[key('resources-by-type', '&http;Response')][not(key('resources-by-type', '&spin;ConstraintViolation'))]" mode="ac:ModeList" priority="2"/>

    <xsl:template match="rdf:RDF[base-uri()]" mode="ac:ModeList" priority="1">
        <xsl:param name="base-uri" select="base-uri()" as="xs:anyURI"/>
        <xsl:param name="modes" select="key('resources-by-type', ('&ac;DocumentMode'), document(ac:document-uri('&ac;')))" as="element()*"/>
        
        <details class="menu">
            <summary class="ldhc-btn in-neutral ap-outline sz-sm" title="{ac:label(key('resources', '&ac;Mode', document(ac:document-uri('&ac;'))))}">
                <xsl:value-of>
                    <xsl:apply-templates select="key('resources', '&ac;Mode', document(ac:document-uri('&ac;')))" mode="ac:label"/>
                </xsl:value-of>
                <span class="msi sm" aria-hidden="true">expand_more</span>
            </summary>

            <ul class="menu-list">
                <xsl:for-each select="$modes">
                    <xsl:sort select="ac:label(.)"/>
                    <xsl:apply-templates select="." mode="ac:ModeListItem">
                        <xsl:with-param name="base-uri" select="$base-uri" tunnel="yes"/>
                        <xsl:with-param name="active" select="@rdf:about = $ac:mode"/>
                    </xsl:apply-templates>
                </xsl:for-each>
            </ul>
        </details>
    </xsl:template>
    
    <xsl:template match="srx:sparql" mode="ac:ModeList"/>
    
    <xsl:template match="*[@rdf:about]" mode="ac:ModeListItem">
        <xsl:param name="base-uri" select="base-uri()" as="xs:anyURI" tunnel="yes"/>
        <xsl:param name="active" as="xs:boolean"/>
        <xsl:param name="class" select="if ($active) then 'is-active' else ()" as="xs:string?"/>

        <li>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            <xsl:if test="$active">
                <xsl:attribute name="aria-current" select="'true'"/>
            </xsl:if>

            <a href="{ac:build-uri((), map{ 'uri': string(ac:absolute-path($base-uri)), 'mode': string(@rdf:about) })}" title="{ac:label(.)}">
                <xsl:value-of>
                    <xsl:apply-templates select="." mode="ac:label"/>
                </xsl:value-of>
            </a>
        </li>
    </xsl:template>
    
    <xsl:template match="*" mode="ac:ModeList"/>

    <!-- HEADER -->

    <xsl:template match="*[rdf:type/@rdf:resource = '&http;Response']" mode="ac:BlockHeader" priority="1">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'ldhc-alert va-negative'" as="xs:string?"/>

        <div role="alert">
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>

            <span class="ldhc-alert-ic"><span class="msi outline" aria-hidden="true">error</span></span>
            <div class="ldhc-alert-body">
                <h2 class="ldhc-alert-text">
                    <xsl:value-of>
                        <xsl:apply-templates select="." mode="ac:label"/>
                    </xsl:value-of>
                </h2>
            </div>
        </div>
    </xsl:template>

    <!-- MEDIA TYPE SELECT MODE (Export buttons) -->
        
    <xsl:template match="rdf:RDF[base-uri()]" mode="ac:MediaTypeList" priority="1">
        <details class="menu">
            <summary class="ldhc-btn in-neutral ap-outline sz-sm">
                <span class="msi sm" aria-hidden="true">download</span>
                <xsl:apply-templates select="key('resources', '&translations;export', ac:translations())" mode="ac:label"/>
                <span class="msi sm" aria-hidden="true">expand_more</span>
            </summary>
            <ul class="menu-list">
                <li>
                    <a href="{ac:build-uri((), map{ 'uri': string(ac:absolute-path(base-uri())), 'accept': 'application/rdf+xml' })}">RDF/XML</a>
                </li>
                <li>
                    <a href="{ac:build-uri((), map{ 'uri': string(ac:absolute-path(base-uri())), 'accept': 'text/turtle' })}">Turtle</a>
                </li>
            </ul>
        </details>
    </xsl:template>

    <xsl:template match="*" mode="ac:MediaTypeList"/>
    
    <!-- RIGHT NAV  -->
    
    <xsl:template match="rdf:RDF[key('resources-by-type', '&http;Response')][not(key('resources-by-type', '&spin;ConstraintViolation'))]" mode="ac:Aside" priority="1"/>
    
    <xsl:template match="rdf:RDF" mode="ac:Aside">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'aside'" as="xs:string?"/>
        
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
    
    <xsl:template match="srx:sparql" mode="ac:Aside"/>

    <xsl:template match="*[*][@rdf:about or @rdf:nodeID]" mode="ac:Aside"/>

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
                <xsl:apply-templates select="$bnode">
                    <xsl:with-param name="display" select="true()" tunnel="yes"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
            
</xsl:stylesheet>