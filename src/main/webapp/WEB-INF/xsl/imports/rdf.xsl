<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java "http://xml.apache.org/xalan/java/">
    <!ENTITY g "http://graphity.org/ontology/">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY dc "http://purl.org/dc/elements/1.1/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY ont-uri "../../owl/rdf.owl">
]>
<xsl:stylesheet version="1.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:url="&java;java.net.URLEncoder"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:dc="&dc;"
xmlns:foaf="&foaf;"
exclude-result-prefixes="g url rdf rdfs">

    <!-- http://xml.apache.org/xalan-j/extensions_xsltc.html#java_ext -->

    <!-- subject/object resource -->
    <xsl:template match="@rdf:about | @rdf:resource">
	<a href="{$base-uri}?uri={url:encode(., 'UTF-8')}">
	    <xsl:apply-templates select="." mode="g:label"/>
	</a>
    </xsl:template>

    <!-- object blank node -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID">
	<xsl:apply-templates select="key('resources', .)"/>
	<!-- <xsl:value-of select="."/> -->
	<!-- <xsl:copy-of select=".."/> -->
    </xsl:template>

    <!-- object literal -->
    <xsl:template match="text()">
	<xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="@rdf:about | @rdf:resource" mode="g:type">
	<a href="{$base-uri}?uri={url:encode(., 'UTF-8')}">
	    <xsl:apply-templates select="." mode="g:label"/>
	</a>
    </xsl:template>
	
    <!-- subject -->
    <xsl:template match="@rdf:about | @rdf:resource" mode="g:label">
	<xsl:choose>
	    <xsl:when test="../dc:title">
		<xsl:value-of select="../dc:title"/>
	    </xsl:when>
	    <xsl:when test="../rdfs:label">
		<xsl:value-of select="../rdfs:label"/>
	    </xsl:when>
	    <xsl:when test="../foaf:name">
		<xsl:value-of select="../foaf:name"/>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:value-of select="."/>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>
    
    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="g:label">
	<xsl:value-of select="concat(namespace-uri(.), local-name(.))"/>
    </xsl:template>

    <!-- subject/object URI resource -->
    <xsl:template match="@rdf:about[starts-with(., '&rdf;')] | @rdf:resource[starts-with(., '&rdf;')]"  mode="g:label">
	<xsl:variable name="this" select="string(.)"/>
	<xsl:for-each select="document('&ont-uri;')">
	    <xsl:variable name="label" select="key('resources', $this)/rdfs:label"/>
	    <xsl:value-of select="concat(translate(substring($label, 1, 1), $lower-case, $upper-case), substring($label, 2))"/>
	</xsl:for-each>
    </xsl:template>

    <!-- rdf:* property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/rdf:*" mode="g:label">
	<xsl:variable name="this" select="concat(namespace-uri(.), local-name(.))"/>
	<xsl:for-each select="document('&ont-uri;')">
	    <xsl:variable name="label" select="key('resources', $this)/rdfs:label"/>
	    <xsl:value-of select="concat(translate(substring($label, 1, 1), $lower-case, $upper-case), substring($label, 2))"/>
	</xsl:for-each>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="rdfs:domain"/>
	
</xsl:stylesheet>