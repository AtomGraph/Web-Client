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
    <!ENTITY geo "http://www.w3.org/2003/01/geo/wgs84_pos#">
    <!ENTITY dbpedia-owl "http://dbpedia.org/ontology/">
    <!ENTITY dc "http://purl.org/dc/elements/1.1/">
    <!ENTITY dct "http://purl.org/dc/terms/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY sioc "http://rdfs.org/sioc/ns#">
    <!ENTITY sp "http://spinrdf.org/sp#">
    <!ENTITY sd "http://www.w3.org/ns/sparql-service-description#">
    <!ENTITY void "http://rdfs.org/ns/void#">
    <!ENTITY list "http://jena.hpl.hp.com/ARQ/list#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
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
exclude-result-prefixes="#all">

    <xsl:import href="local-xhtml.xsl"/>

    <xsl:param name="uri" as="xs:anyURI?"/>

    <xsl:variable name="datasets" select="document('../data/datasets.rdf')" as="document-node()"/>

    <xsl:key name="resources-by-endpoint" match="*" use="void:sparqlEndpoint/@rdf:resource"/>

    <xsl:template match="/" mode="gc:HeaderMode">
	<a class="brand" href="{$base-uri}">
	    <xsl:apply-templates select="key('resources', $base-uri, $ont-model)/@rdf:about" mode="gc:LabelMode"/>
	</a>

	<div class="nav-collapse">
	    <!--
	    <ul class="nav">
		<xsl:for-each select="key('resources-by-host', $base-uri, $ont-model)/@rdf:about[not(. = $base-uri)]">
		    <xsl:sort select="gc:label(., /, $lang)" data-type="text" order="ascending" lang="{$lang}"/>
		    <li>
			<xsl:if test=". = $absolute-path">
			    <xsl:attribute name="class">active</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="."/>
		    </li>
		</xsl:for-each>
	    </ul>
	    -->
	    <form action="{$base-uri}" method="get" class="navbar-form pull-left">
		<div class="input-prepend input-append">
		    <span class="add-on">
			<xsl:variable name="dataset" select="key('resources-by-endpoint', $endpoint-uri, $datasets)"/>
			<a href="{$endpoint-uri}">
			    <xsl:choose>
				<xsl:when test="$dataset">
				    <xsl:apply-templates select="$dataset"/>
				</xsl:when>
				<xsl:otherwise>
				    <xsl:value-of select="$endpoint-uri"/>
				</xsl:otherwise>
			    </xsl:choose>
			</a>
		    </span>
		    <input type="text" name="uri" class="input-xxlarge">
			<xsl:if test="not(starts-with($uri, $base-uri))">
			    <xsl:attribute name="value">
				<xsl:value-of select="$uri"/>
			    </xsl:attribute>
			</xsl:if>
		    </input>
		    <button type="submit" class="btn btn-primary">Go</button>
		    <!--
		    <xsl:if test="$endpoint-uri">
			<input type="hidden" name="endpoint-uri" value="{$endpoint-uri}"/>
		    </xsl:if>
		    -->
		</div>
	    </form>
	</div>
	
	<xsl:for-each select="key('resources', resolve-uri('sparql', $base-uri), $ont-model)/@rdf:about">
	    <div class="nav-collapse pull-right">
		<ul class="nav">
		    <li>
			<xsl:if test=". = $absolute-path">
			    <xsl:attribute name="class">active</xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="."/>
		    </li>
		</ul>
	    </div>
	</xsl:for-each>
    </xsl:template>

    <xsl:template match="rdf:type/@rdf:resource" priority="1">
	<span title="{.}" class="btn">
	    <xsl:next-match/>
	</span>
    </xsl:template>

    <!-- subject/object resource -->
    <xsl:template match="@rdf:about | @rdf:resource | sparql:uri">
	<a href="{$base-uri}{gc:query-string(., (), (), (), (), ())}" title="{.}">
	    <xsl:apply-templates select="." mode="gc:LabelMode"/>
	</a>
    </xsl:template>

    <xsl:template match="@rdf:about[starts-with(., $base-uri)] | @rdf:resource[starts-with(., $base-uri)] | sparql:uri[starts-with(., $base-uri)]">
	<xsl:apply-imports/>
    </xsl:template>

    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(), local-name()))" as="xs:anyURI"/>
	<a href="{$base-uri}{gc:query-string($this, (), (), (), (), ())}" title="{$this}">
	    <xsl:apply-templates select="." mode="gc:LabelMode"/>
	</a>
    </xsl:template>

    <xsl:function name="gc:query-string" as="xs:string?">
	<xsl:param name="uri" as="xs:anyURI?"/>
	<!-- <xsl:param name="endpoint-uri" as="xs:anyURI?"/> -->
	<xsl:param name="offset" as="xs:integer?"/>
	<xsl:param name="limit" as="xs:integer?"/>
	<xsl:param name="order-by" as="xs:string?"/>
	<xsl:param name="desc" as="xs:boolean?"/>
	<!-- <xsl:param name="lang" as="xs:string?"/> -->
	<xsl:param name="mode" as="xs:string?"/>
	
	<xsl:variable name="query-string">
	    <xsl:if test="$uri">uri=<xsl:value-of select="encode-for-uri($uri)"/>&amp;</xsl:if>
	    <!-- <xsl:if test="$endpoint-uri">endpoint-uri=<xsl:value-of select="encode-for-uri($endpoint-uri)"/>&amp;</xsl:if> -->
	    <xsl:if test="$offset">offset=<xsl:value-of select="$offset"/>&amp;</xsl:if>
	    <xsl:if test="$limit">limit=<xsl:value-of select="$limit"/>&amp;</xsl:if>
	    <xsl:if test="$order-by">order-by=<xsl:value-of select="$order-by"/>&amp;</xsl:if>
	    <xsl:if test="$desc">desc&amp;</xsl:if>
	    <!-- <xsl:if test="$lang">lang=<xsl:value-of select="$lang"/>&amp;</xsl:if> -->
	    <xsl:if test="$mode">mode=<xsl:value-of select="encode-for-uri($mode)"/>&amp;</xsl:if>
	</xsl:variable>
	
	<xsl:sequence select="concat('?', substring($query-string, 1, string-length($query-string) - 1))"/>
    </xsl:function>

</xsl:stylesheet>