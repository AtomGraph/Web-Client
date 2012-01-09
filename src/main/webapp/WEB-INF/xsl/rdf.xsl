<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
]>
<xsl:stylesheet version="1.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:rdf="&rdf;"
exclude-result-prefixes="rdf">

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

    <!-- traverses linked rdf:List -->
    <xsl:template match="*[@rdf:nodeID]" mode="rdf:List">
        <xsl:apply-templates select="key('resources', rdf:first/@rdf:resource)"/>
        <xsl:apply-templates select="key('resources', rdf:rest/@rdf:nodeID)" mode="rdf:List"/>
    </xsl:template>

</xsl:stylesheet>