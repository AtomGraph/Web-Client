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
    <!ENTITY rdf            "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs           "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl            "http://www.w3.org/2002/07/owl#">
    <!ENTITY xsd            "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql         "http://www.w3.org/2005/sparql-results#">
    <!ENTITY ldp            "http://www.w3.org/ns/ldp#">
    <!ENTITY geo            "http://www.w3.org/2003/01/geo/wgs84_pos#">
    <!ENTITY dbpedia-owl    "http://dbpedia.org/ontology/">
    <!ENTITY dc             "http://purl.org/dc/elements/1.1/">
    <!ENTITY dct            "http://purl.org/dc/terms/">
    <!ENTITY foaf           "http://xmlns.com/foaf/0.1/">
    <!ENTITY sioc           "http://rdfs.org/sioc/ns#">
    <!ENTITY skos           "http://www.w3.org/2004/02/skos/core#">
    <!ENTITY sp             "http://spinrdf.org/sp#">
    <!ENTITY spin           "http://spinrdf.org/spin#">
    <!ENTITY sd             "http://www.w3.org/ns/sparql-service-description#">
    <!ENTITY list           "http://jena.hpl.hp.com/ARQ/list#">
    <!ENTITY xhv            "http://www.w3.org/1999/xhtml/vocab#">
    <!ENTITY void           "http://rdfs.org/ns/void#">
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
xmlns:geo="&geo;"
xmlns:dbpedia-owl="&dbpedia-owl;"
xmlns:dc="&dc;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:skos="&skos;"
xmlns:sp="&sp;"
xmlns:spin="&spin;"
xmlns:sd="&sd;"
xmlns:list="&list;"
xmlns:xhv="&xhv;"
xmlns:void="&void;"
xmlns:url="&java;java.net.URLDecoder"
exclude-result-prefixes="#all">

    <xsl:import href="imports/local.xsl"/>
    <xsl:import href="imports/dbpedia-owl.xsl"/>
    <xsl:import href="imports/dc.xsl"/>
    <xsl:import href="imports/dct.xsl"/>
    <xsl:import href="imports/doap.xsl"/>
    <xsl:import href="imports/foaf.xsl"/>
    <xsl:import href="imports/gp.xsl"/>
    <xsl:import href="imports/gr.xsl"/>
    <xsl:import href="imports/owl.xsl"/>
    <xsl:import href="imports/rdf.xsl"/>
    <xsl:import href="imports/rdfs.xsl"/>
    <xsl:import href="imports/sd.xsl"/>
    <xsl:import href="imports/sp.xsl"/>
    <xsl:import href="imports/sioc.xsl"/>
    <xsl:import href="imports/skos.xsl"/>
    <xsl:import href="imports/void.xsl"/>
    <xsl:import href="layout.xsl"/>

    <xsl:template match="*[*][@rdf:about = $absolute-path]" mode="gc:PageHeaderMode" priority="1">
	<div>
            <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>
        </div>
    </xsl:template>

    <xsl:template match="rdf:type/@rdf:resource" priority="1" mode="gc:InlineMode">
	<h2 title="{.}">
            <xsl:next-match/>
        </h2>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:PropertyListMode">
	<xsl:variable name="type-containers" as="element()*">
	    <xsl:for-each-group select="*" group-by="if (not(empty(rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name())))))) then rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name()))) else key('resources', '&rdfs;Resource', document('&rdfs;'))/@rdf:about">
		<xsl:sort select="if (rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name())))) then gc:object-label(rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name())))[1]) else ()"/>

		<xsl:variable name="properties" as="element()*">
                    <xsl:for-each-group select="current-group()" group-by="concat(namespace-uri(), local-name())">
			<xsl:sort select="gc:property-label(.)" order="ascending" lang="{$lang}"/>
                        
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
                                <xsl:apply-templates select="rdfs:domain(xs:anyURI(concat(namespace-uri(), local-name())))" mode="gc:InlineMode"/>
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

</xsl:stylesheet>