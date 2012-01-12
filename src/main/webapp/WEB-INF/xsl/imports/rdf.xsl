<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java "http://xml.apache.org/xalan/java/">
    <!ENTITY g "http://graphity.org/ontology/">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
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
exclude-result-prefixes="g url rdf rdfs">

    <!-- http://xml.apache.org/xalan-j/extensions_xsltc.html#java_ext -->
    
    <!-- object URI resource -->
    <xsl:template match="*[@rdf:about]/*/@rdf:resource | *[@rdf:nodeID]/*/@rdf:resource">
	<a href="{$base-uri}?uri={url:encode(., 'UTF-8')}">
	    <xsl:apply-templates select="." mode="g:label"/>
	</a>
    </xsl:template>

    <!-- object blank node -->
    <xsl:template match="*[@rdf:about]/*/@rdf:nodeID | *[@rdf:nodeID]/*/@rdf:nodeID">
	<!-- <xsl:apply-templates/> ??? -->
	<xsl:value-of select="."/>
    </xsl:template>

    <!-- object literal -->
    <xsl:template match="*[@rdf:about]/*/text() | *[@rdf:nodeID]/*/text()">
	<xsl:value-of select="."/>
    </xsl:template>

    <!-- subject -->
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="g:label">
	<xsl:value-of select="@rdf:about | @rdf:nodeID"/>
    </xsl:template>
    
    <!-- property -->
    <xsl:template match="*[@rdf:about]/* | *[@rdf:nodeID]/*" mode="g:label">
	<xsl:value-of select="concat(namespace-uri(.), local-name(.))"/>
    </xsl:template>

    <!-- rdf:* property -->
    <xsl:template match="*[@rdf:about]/rdf:* | *[@rdf:nodeID]/rdf:*" mode="g:label">
	<xsl:variable name="uri" select="concat(namespace-uri(.), local-name(.))"/>
	<xsl:for-each select="document('&ont-uri;')">
	    <xsl:variable name="label" select="key('resources', $uri)/rdfs:label"/>
	    <xsl:value-of select="concat(translate(substring($label, 1, 1), $lower-case, $upper-case), substring($label, 2))"/>
	</xsl:for-each>
    </xsl:template>

    <!-- object URI resource -->
    <xsl:template match="*[@rdf:about]/*/@rdf:resource[starts-with(., '&rdf;')] | *[@rdf:nodeID]/*/@rdf:resource[starts-with(., '&rdf;')]"  mode="g:label">
	<xsl:variable name="uri" select="string(.)"/>
	<xsl:for-each select="document('&ont-uri;')">
	    <xsl:variable name="label" select="key('resources', $uri)/rdfs:label"/>
	    <xsl:value-of select="concat(translate(substring($label, 1, 1), $lower-case, $upper-case), substring($label, 2))"/>
	</xsl:for-each>
    </xsl:template>

</xsl:stylesheet>