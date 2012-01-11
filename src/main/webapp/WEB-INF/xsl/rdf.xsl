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

    <!-- property -->
    <xsl:template match="*[@rdf:about]/* | *[@rdf:nodeID]/*">
	<xsl:variable name="this" select="concat(namespace-uri(.), local-name(.))"/>
	<!-- do not repeat property name if it's the same as the previous one -->
	<xsl:if test="not(concat(namespace-uri(preceding-sibling::*[1]), local-name(preceding-sibling::*[1])) = $this)">
	    <dt>
		<a href="{$base-uri}?uri={url:encode($this, 'UTF-8')}">
		    <!-- <xsl:value-of select="namespace-uri(.)"/> -->
		    <xsl:for-each select="document('../owl/sioc.owl')">
			<xsl:choose>
			    <xsl:when test="key('resources', $this)/rdfs:label">
				<xsl:value-of select="key('resources', $this)/rdfs:label"/>
			    </xsl:when>
			    <xsl:otherwise>
				<xsl:value-of select="$this"/>
			    </xsl:otherwise>
			</xsl:choose>
		    </xsl:for-each>
		</a>
	    </dt>
	</xsl:if>
	<xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID"/>
    </xsl:template>

    <!-- object URI resource -->
    <xsl:template match="*[@rdf:about]/*/@rdf:resource | *[@rdf:nodeID]/*/@rdf:resource">
	<dd>
	    <a href="{$base-uri}?uri={url:encode(., 'UTF-8')}">
		<xsl:value-of select="."/>
	    </a>
	</dd>
    </xsl:template>

    <!-- object blank node -->
    <xsl:template match="*[@rdf:about]/*/@rdf:nodeID | *[@rdf:nodeID]/*/@rdf:nodeID">
	<dd><xsl:value-of select="."/>
	    <!-- <xsl:apply-templates select="key('resources', .)"/> -->
	    <!-- <xsl:copy-of select="key('resources', .)"/> -->
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