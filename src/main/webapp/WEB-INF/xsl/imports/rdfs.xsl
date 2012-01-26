<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java "http://xml.apache.org/xalan/java/">
    <!ENTITY g "http://graphity.org/ontology/">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY ont-uri "../../owl/rdfs.owl">
]>
<xsl:stylesheet version="1.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:url="&java;java.net.URLEncoder"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
exclude-result-prefixes="url g rdf rdfs">
    
    <!-- subject/object URI resource -->
    <xsl:template match="@rdf:about[starts-with(., '&rdfs;')] | @rdf:resource[starts-with(., '&rdfs;')]"  mode="g:label">
	<xsl:variable name="this" select="string(.)"/>
	<xsl:for-each select="document('&ont-uri;')">
	    <!-- refactor!!! -->
	    <xsl:variable name="label" select="key('resources', $this)/rdfs:label | key('resources', $this)/@rdfs:label"/>
	    <xsl:value-of select="concat(translate(substring($label, 1, 1), $lower-case, $upper-case), substring($label, 2))"/>
	</xsl:for-each>
    </xsl:template>

    <!-- rdfs:* property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/rdfs:*" mode="g:label">
	<xsl:variable name="this" select="concat(namespace-uri(.), local-name(.))"/>
	<xsl:for-each select="document('&ont-uri;')">
	    <!-- refactor!!! -->
	    <xsl:variable name="label" select="key('resources', $this)/rdfs:label | key('resources', $this)/@rdfs:label"/>
	    <xsl:value-of select="concat(translate(substring($label, 1, 1), $lower-case, $upper-case), substring($label, 2))"/>
	</xsl:for-each>
    </xsl:template>

</xsl:stylesheet>