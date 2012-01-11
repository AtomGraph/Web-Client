<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java "http://xml.apache.org/xalan/java/">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
]>
<xsl:stylesheet version="1.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:url="&java;java.net.URLEncoder"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
exclude-result-prefixes="url rdf rdfs">

    <!-- http://xml.apache.org/xalan-j/extensions_xsltc.html#java_ext -->

    <!-- object URI resource -->
    <xsl:template match="*[@rdf:about]/*/@rdf:resource | *[@rdf:nodeID]/*/@rdf:resource">
	<a href="{$base-uri}?uri={url:encode(., 'UTF-8')}">
	    <xsl:value-of select="."/>
	</a>
    </xsl:template>

    <!-- object blank node -->
    <xsl:template match="*[@rdf:about]/*/@rdf:nodeID | *[@rdf:nodeID]/*/@rdf:nodeID">
	<xsl:value-of select="."/>
	<!-- <xsl:apply-templates select="key('resources', .)"/> -->
	<!-- <xsl:copy-of select="key('resources', .)"/> -->
    </xsl:template>

    <!-- object literal -->
    <xsl:template match="*[@rdf:about]/*/text() | *[@rdf:nodeID]/*/text()">
	<xsl:value-of select="."/>
    </xsl:template>

</xsl:stylesheet>