<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
    <!ENTITY owl "http://www.w3.org/2002/07/owl#">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY hn "http://semantic-web.dk/ontologies/heltnormalt#">
    <!ENTITY dc "http://purl.org/dc/elements/1.1/">
    <!ENTITY dct "http://purl.org/dc/terms/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY sioc "http://rdfs.org/sioc/ns#">
    <!ENTITY zodiac "http://data.totl.net/zodiac/ontology/">
    <!ENTITY list "http://jena.hpl.hp.com/ARQ/list#">
    <!ENTITY awol "http://bblfish.net/work/atom-owl/2006-06-06/AtomOwl.html#">
    <!ENTITY og "http://ogp.me/ns#">
    <!ENTITY fb "http://ogp.me/ns/fb#">
]>
<xsl:stylesheet version="1.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:php="http://php.net/xsl"
xmlns:date="http://exslt.org/dates-and-times"
xmlns:math="http://exslt.org/math"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:sparql="&sparql;"
xmlns:dc="&dc;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:hn="&hn;"
xmlns:zodiac="&zodiac;"
xmlns:list="&list;"
xmlns:awol="&awol;"
xmlns:og="&og;"
xmlns:fb="&fb;"
xmlns:exslt="http://exslt.org/common"
exclude-result-prefixes="xsl xhtml php date math rdf rdfs sparql dc dct foaf sioc hn zodiac list awol exslt">

    <xsl:import href="foaf.xsl"/>

    <xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes" media-type="application/xhtml+xml" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" />

    <xsl:preserve-space elements="pre"/>

    <xsl:param name="uri"/>
    <xsl:param name="base-uri"/>
    <xsl:param name="view"/>
    <xsl:param name="action" select="false()"/>
    <xsl:param name="php-os"/>
    <xsl:param name="fb-app-id" select="'264143360289485'"/>

    <xsl:variable name="resource" select="/"/>
    <xsl:variable name="lower-case" select="'abcdefghijklmnopqrstuvwxyz'" />
    <xsl:variable name="upper-case" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

    <xsl:key name="resources" match="*[@rdf:about] | *[@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
    <xsl:key name="resources-by-domain" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:domain/@rdf:resource"/>
    <xsl:key name="resources-by-type" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdf:type/@rdf:resource"/>
    <xsl:key name="resources-by-is-part-of" match="*[@rdf:about] | *[@rdf:nodeID]" use="dct:isPartOf/@rdf:resource"/>
    <xsl:key name="resources-by-issued-date" match="*[@rdf:about] | *[@rdf:nodeID]" use="dct:issued"/>
    <xsl:key name="resources-by-container" match="*[@rdf:about] | *[@rdf:nodeID]" use="sioc:has_container/@rdf:resource"/>
    <xsl:key name="resources-by-subject" match="*[@rdf:about] | *[@rdf:nodeID]" use="dc:subject/@rdf:resource"/>
    <xsl:key name="uri-resources" match="*[@rdf:about]" use="@rdf:about"/>
    <xsl:key name="bnodes" match="*[@rdf:nodeID]" use="@rdf:nodeID"/>
    <xsl:key name="properties" match="*" use="concat(namespace-uri(.), local-name(.))"/>
    <xsl:key name="object-properties" match="*[@rdf:resource or */@rdf:about]" use="concat(namespace-uri(.), local-name(.))"/>
    <xsl:key name="datatype-properties" match="*[*]" use="concat(namespace-uri(.), local-name(.))"/>

    <xsl:template match="rdf:RDF">
	<html>
	    <head>
		<title>Graphity</title>
	    </head>
	    <body>
		<xsl:apply-templates/>
	    </body>
	</html>
    </xsl:template>

    <!-- subject URI resource -->
    <xsl:template match="*[*][@rdf:about]">
	<h1>
	    <xsl:value-of select="dc:title"/>
	</h1>
	<dl>
	    <xsl:apply-templates>
		<xsl:sort select="concat(namespace-uri(.), local-name(.))" data-type="text" order="ascending"/>
	    </xsl:apply-templates>
	</dl>
    </xsl:template>

    <!-- subject blank node -->
    <xsl:template match="*[*][@rdf:nodeID]" mode="rdf:List">
	Blank node
    </xsl:template>
    
    <!-- property -->
    <xsl:template match="*[@rdf:about]/* | *[@rdf:nodeID]/*">
	<dt>
	    <a href="{concat(namespace-uri(.), local-name(.))}">
		<xsl:value-of select="concat(namespace-uri(.), local-name(.))"/>
	    </a>
	</dt>
	<xsl:apply-templates select="node() | @rdf:resource"/>
    </xsl:template>

    <!-- object resource -->
    <xsl:template match="*[@rdf:about]/*/@rdf:resource | *[@rdf:nodeID]/*/@rdf:resource">
	<dd>
	    <a href="{.}">
		<xsl:value-of select="."/>
	    </a>
	</dd>
    </xsl:template>

    <!-- object literal -->
    <xsl:template match="*[@rdf:about]/*/text() | *[@rdf:nodeID]/*/text()">
	<dd>
	    <xsl:value-of select="."/>
	</dd>	
    </xsl:template>
	
    <xsl:template match="foaf:img/@rdf:resource | foaf:depiction/@rdf:resource | foaf:thumbnail/@rdf:resource | foaf:logo/@rdf:resource" priority="1">
	<dd>
	    <img src="{.}" alt=""/>
	</dd>
    </xsl:template>

    <!-- traverses linked rdf:List -->
    <xsl:template match="*[@rdf:nodeID]" mode="rdf:List">
        <xsl:apply-templates select="key('resources', rdf:first/@rdf:resource)"/>
        <xsl:apply-templates select="key('resources', rdf:rest/@rdf:nodeID)" mode="rdf:List"/>
    </xsl:template>

</xsl:stylesheet>