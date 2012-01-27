<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java "http://xml.apache.org/xalan/java/">
    <!ENTITY g "http://graphity.org/ontology/">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY dc "http://purl.org/dc/elements/1.1/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:url="&java;java.net.URLEncoder"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:dc="&dc;"
xmlns:foaf="&foaf;"
exclude-result-prefixes="g url rdf rdfs dc foaf">

    <!-- http://xml.apache.org/xalan-j/extensions_xsltc.html#java_ext -->

    <!-- subject/object resource -->
    <xsl:template match="@rdf:about | @rdf:resource">
	<a href="{$base-uri}?uri={url:encode(., 'UTF-8')}">
	    <xsl:value-of select="g:label(., /)"/>
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
	    <xsl:value-of select="g:label(., /)"/>
	</a>
    </xsl:template>

    <xsl:variable name="ontologies" select="(xs:anyURI('../../owl/rdf.owl'),
					    xs:anyURI('../../owl/rdfs.owl'),
					    xs:anyURI('../../owl/owl2.owl'),
					    xs:anyURI('../../owl/spin.owl'),
					    xs:anyURI('../../owl/dcterms.rdf'),
					    xs:anyURI('../../owl/dcelements.rdf'),
					    xs:anyURI('../../owl/foaf.owl'),
					    xs:anyURI('../../owl/sioc.owl'),
					    xs:anyURI('../../owl/dbpedia-owl.owl'))" as="xs:anyURI*"/>

    <xsl:function name="g:local-label" as="xs:string?">
	<xsl:param name="resource-uri" as="xs:anyURI"/>
	<xsl:param name="document" as="document-node()"/>
	<xsl:variable name="resource" select="key('resources', $resource-uri, $document)"/>
	<xsl:choose>
	    <xsl:when test="$resource/dc:title | $resource/@dc:title">
		<xsl:sequence select="$resource/dc:title | $resource/@dc:title"/>
	    </xsl:when>
	    <xsl:when test="$resource/rdfs:label | $resource/@rdfs:label">
		<xsl:sequence select="$resource/rdfs:label | $resource/@rdfs:label"/>
	    </xsl:when>
	    <xsl:when test="$resource/foaf:name | $resource/@foaf:name">
		<xsl:sequence select="$resource/foaf:name | $resource/@foaf:name"/>
	    </xsl:when>
	</xsl:choose>
    </xsl:function>
	
    <xsl:function name="g:label" as="xs:string?">
	<xsl:param name="resource-uri" as="xs:anyURI"/>
	<xsl:param name="document" as="document-node()"/>
	<xsl:variable name="local-label" select="g:local-label($resource-uri, $document)" as="xs:string?"/>
	<xsl:choose>
	    <xsl:when test="$local-label">
		<xsl:sequence select="concat(upper-case(substring($local-label, 1, 1)), lower-case(substring($local-label, 2)))"/>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:variable name="imported-label" select="(document($ontologies)/g:local-label($resource-uri, .))[1]" as="xs:string?"/>
		<xsl:choose>
		    <xsl:when test="$imported-label">
			<xsl:sequence select="concat(upper-case(substring($imported-label, 1, 1)), lower-case(substring($imported-label, 2)))"/>
		    </xsl:when>
		    <xsl:otherwise>
			<xsl:sequence select="$resource-uri"/>
		    </xsl:otherwise>
		</xsl:choose>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:function>

    <xsl:function name="rdfs:domain" as="xs:anyURI*">
	<xsl:param name="property-uri" as="xs:anyURI"/>
	<xsl:for-each select="document($ontologies)">
	    <xsl:sequence select="key('resources', $property-uri, .)/rdfs:domain/@rdf:resource"/>
	</xsl:for-each>
    </xsl:function>

</xsl:stylesheet>