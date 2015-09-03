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
    <!ENTITY java           "http://xml.apache.org/xalan/java/">
    <!ENTITY gc             "http://graphity.org/gc#">
    <!ENTITY gp             "http://graphity.org/gp#">
    <!ENTITY g              "http://graphity.org/g#">
    <!ENTITY rdf            "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs           "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd            "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY owl            "http://www.w3.org/2002/07/owl#">
    <!ENTITY geo            "http://www.w3.org/2003/01/geo/wgs84_pos#">
    <!ENTITY sparql         "http://www.w3.org/2005/sparql-results#">
    <!ENTITY http           "http://www.w3.org/2011/http#">
    <!ENTITY sd             "http://www.w3.org/ns/sparql-service-description#">
    <!ENTITY dbpedia-owl    "http://dbpedia.org/ontology/">
    <!ENTITY dc             "http://purl.org/dc/elements/1.1/">
    <!ENTITY dct            "http://purl.org/dc/terms/">
    <!ENTITY foaf           "http://xmlns.com/foaf/0.1/">
    <!ENTITY sp             "http://spinrdf.org/sp#">
    <!ENTITY void           "http://rdfs.org/ns/void#">
    <!ENTITY sioc           "http://rdfs.org/sioc/ns#">
    <!ENTITY list           "http://jena.hpl.hp.com/ARQ/list#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:gp="&gp;"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:sparql="&sparql;"
xmlns:geo="&geo;"
xmlns:dbpedia-owl="&dbpedia-owl;"
xmlns:dc="&dc;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:sp="&sp;"
xmlns:sd="&sd;"
xmlns:void="&void;"
xmlns:list="&list;"
xmlns:uuid="java:java.util.UUID"
exclude-result-prefixes="#all">

    <xsl:import href="imports/local.xsl"/>
    <xsl:import href="imports/external.xsl"/>
    <xsl:import href="imports/dbpedia-owl.xsl"/>
    <xsl:import href="imports/dc.xsl"/>
    <xsl:import href="imports/dct.xsl"/>
    <xsl:import href="imports/doap.xsl"/>
    <xsl:import href="imports/foaf.xsl"/>
    <xsl:import href="imports/gc.xsl"/>
    <xsl:import href="imports/gp.xsl"/>
    <xsl:import href="imports/gr.xsl"/>
    <xsl:import href="imports/owl.xsl"/>
    <xsl:import href="imports/rdf.xsl"/>
    <xsl:import href="imports/rdfs.xsl"/>
    <xsl:import href="imports/xhv.xsl"/>    
    <xsl:import href="imports/sd.xsl"/>
    <xsl:import href="imports/sioc.xsl"/>
    <xsl:import href="imports/skos.xsl"/>
    <xsl:import href="imports/sp.xsl"/>
    <xsl:import href="imports/spin.xsl"/>
    <xsl:import href="imports/void.xsl"/>
    <xsl:import href="layout.xsl"/>

    <xsl:param name="label" as="xs:string?"/>

    <xsl:key name="resources-by-endpoint" match="*" use="void:sparqlEndpoint/@rdf:resource"/>

    <rdf:Description rdf:nodeID="save-as">
	<rdfs:label xml:lang="en">Save as...</rdfs:label>
    </rdf:Description>
    
    <xsl:template match="/" mode="gc:NavBarMode">
	<div class="navbar navbar-fixed-top">
	    <div class="navbar-inner">
		<div class="container-fluid">
                    <button class="btn btn-navbar" onclick="if ($('#collapsing-top-navbar').hasClass('in')) $('#collapsing-top-navbar').removeClass('collapse in').height(0); else $('#collapsing-top-navbar').addClass('collapse in').height('auto');">
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>

                    <a class="brand" href="{$g:baseUri}">
                        <xsl:for-each select="key('resources', $g:baseUri, document($g:baseUri))">
                            <img src="{foaf:logo/@rdf:resource}">
                                <xsl:attribute name="alt"><xsl:apply-templates select="." mode="gc:LabelMode"/></xsl:attribute>
                            </img>
                        </xsl:for-each>
                    </a>

                    <div id="collapsing-top-navbar" class="nav-collapse collapse">
                        <form action="resources/labelled/" method="get" class="navbar-form pull-left" accept-charset="UTF-8">
                            <div class="input-append">
                                <input type="text" name="uri" class="input-xxlarge">
                                    <xsl:if test="not(starts-with($gc:uri, $g:baseUri))">
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="$gc:uri"/>
                                        </xsl:attribute>
                                    </xsl:if>
                                    <xsl:if test="$label">
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="$label"/>
                                        </xsl:attribute>
                                    </xsl:if>
                                </input>
                                <button type="submit" class="btn btn-primary">Go</button>
                            </div>
                        </form>

                        <xsl:variable name="space" select="($gc:uri, key('resources', $gc:uri)/sioc:has_container/@rdf:resource)" as="xs:anyURI*"/>
                        <xsl:if test="key('resources-by-type', '&gp;SPARQLEndpoint', document($g:baseUri))">
                            <ul class="nav pull-right">
                                <xsl:apply-templates select="key('resources-by-type', '&gp;SPARQLEndpoint', document($g:baseUri))" mode="gc:NavBarMode">
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
    
    <!--    
    <xsl:template match="*[@rdf:about = $gc:uri]" mode="gc:ModeToggleMode" priority="1">
        <div class="pull-right">
            <a class="btn btn-primary" href="{$g:absolutePath}{gc:query-string(@rdf:about, xs:anyURI('&gp;CreateItemMode'))}">
                <xsl:apply-templates select="key('resources', 'save-as', document(''))" mode="gc:LabelMode"/>
            </a>
        </div>
    </xsl:template>
    -->

    <xsl:template match="rdf:RDF" mode="gc:ReadMode">
        <xsl:param name="selected-resources" select="*[not(@rdf:about = $gc:uri)][not(key('predicates-by-object', @rdf:nodeID))]" as="element()*" tunnel="yes"/>

        <xsl:apply-imports>
            <xsl:with-param name="selected-resources" select="$selected-resources" tunnel="yes"/>
        </xsl:apply-imports>
    </xsl:template>
      
    <xsl:template match="rdf:RDF[key('resources', $gc:uri)/rdf:type/@rdf:resource = '&gp;SPARQLEndpoint'][$gc:endpointUri]" mode="gc:QueryResultMode">
        <div class="btn-group pull-right">
            <a href="{$gc:endpointUri}?query={encode-for-uri($query)}" class="btn">Source</a>
        </div>

        <xsl:apply-imports>
            <xsl:with-param name="result-doc" select="document(concat($g:absolutePath, gc:query-string($gc:endpointUri, $query, $gp:mode, ())))"/>
        </xsl:apply-imports>
    </xsl:template>
        
    <!-- DOCUMENT -->

    <!-- only show edit mode for the main resource -->
    <!-- <xsl:template match="*[@rdf:about][$gc:uri][not(@rdf:about = $gc:uri)]" mode="gc:EditMode"/> -->
    
    <!--
    <xsl:template match="@rdf:about[. = $gc:uri][../rdf:type/@rdf:resource = '&foaf;Document']" mode="gc:EditMode" priority="1">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>
        
	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'sb'"/>
	    <xsl:with-param name="type" select="'hidden'"/>
	    <xsl:with-param name="id" select="generate-id()"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
	    <xsl:with-param name="value" select="'this'"/>
	</xsl:call-template>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about = $gc:uri][rdf:type/@rdf:resource = '&foaf;Document']" mode="gc:EditMode" priority="1">
        <xsl:param name="container" select="resolve-uri('saved', $g:baseUri)" as="xs:anyURI"/>
        
        <xsl:apply-imports/>

        <xsl:apply-templates select="@rdf:about" mode="#current"/>

        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'pu'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&rdf;type'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'ou'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&gp;Item'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'pu'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&gp;slug'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'ol'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="xs:string(uuid:randomUUID())"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'pu'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&sioc;has_container'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'ou'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="$container"/>
        </xsl:call-template>
    </xsl:template>
    -->
    
    <!-- THING -->
        
    <!--
    <xsl:template match="@rdf:about[. = $gc:uri]" mode="gc:EditMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>
        
	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'sb'"/>
	    <xsl:with-param name="type" select="'hidden'"/>
	    <xsl:with-param name="id" select="generate-id()"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
	    <xsl:with-param name="value" select="'thing'"/>
	</xsl:call-template>
    </xsl:template>
    
    <xsl:template match="*[*][@rdf:about = $gc:uri]" mode="gc:EditMode">
        <xsl:param name="container" select="resolve-uri('saved', $g:baseUri)" as="xs:anyURI"/>

        <xsl:apply-imports/>
        
        <xsl:apply-templates select="@rdf:about" mode="#current"/>
        
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'pu'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&foaf;isPrimaryTopicOf'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'ob'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'this'"/>
        </xsl:call-template>

        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'sb'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'this'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'pu'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&rdf;type'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'ou'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&foaf;Document'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'ou'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&gp;Item'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'pu'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&gp;slug'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'ol'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="xs:string(uuid:randomUUID())"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'pu'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&foaf;primaryTopic'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'ob'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'thing'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'pu'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="'&sioc;has_container'"/>
        </xsl:call-template>
        <xsl:call-template name="gc:InputTemplate">
            <xsl:with-param name="name" select="'ou'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="$container"/>
        </xsl:call-template>
    </xsl:template>
    -->
    
</xsl:stylesheet>