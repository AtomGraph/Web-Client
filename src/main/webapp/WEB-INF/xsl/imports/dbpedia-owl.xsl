<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java "http://xml.apache.org/xalan/java/">
    <!ENTITY g "http://graphity.org/ontology/">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY dbpedia-owl "http://dbpedia.org/ontology/">
]>
<xsl:stylesheet version="1.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:url="&java;java.net.URLEncoder"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:dbpedia-owl="&dbpedia-owl;"
exclude-result-prefixes="url rdf dbpedia-owl">

    <xsl:param name="ontology-uri" select="'../../owl/dbpedia-owl.owl'"/>

    <xsl:template match="dbpedia-owl:thumbnail/@rdf:resource">
	<a href="{$base-uri}?uri={url:encode(., 'UTF-8')}">
	    <img src="{.}" alt=""/>
	</a>
    </xsl:template>

    <!-- dbpedia-owl:* property -->
    <xsl:template match="*[@rdf:about]/dbpedia-owl:* | *[@rdf:nodeID]/dbpedia-owl:*" mode="g:label">
	<xsl:variable name="uri" select="concat(namespace-uri(.), local-name(.))"/>
	<xsl:for-each select="document($ontology-uri)">
	    <xsl:variable name="label" select="key('resources', $uri)/rdfs:label"/>
	    <xsl:value-of select="concat(translate(substring($label, 1, 1), $lower-case, $upper-case), substring($label, 2))"/>
	</xsl:for-each>
    </xsl:template>

    <!-- object URI resource -->
    <xsl:template match="*[@rdf:about]/*/@rdf:resource[starts-with(., '&dbpedia-owl;')] | *[@rdf:nodeID]/*/@rdf:resource[starts-with(., '&dbpedia-owl;')]"  mode="g:label">
	<xsl:variable name="uri" select="."/>
	<xsl:for-each select="document($ontology-uri)">
	    <xsl:variable name="label" select="key('resources', $uri)/rdfs:label"/>
	    <xsl:value-of select="concat(translate(substring($label, 1, 1), $lower-case, $upper-case), substring($label, 2))"/>
	</xsl:for-each>
    </xsl:template>

</xsl:stylesheet>