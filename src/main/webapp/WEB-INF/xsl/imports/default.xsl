<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java "http://xml.apache.org/xalan/java/">
    <!ENTITY g "http://graphity.org/ontology/">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY dc "http://purl.org/dc/elements/1.1/">
    <!ENTITY dct "http://purl.org/dc/terms/">
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
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
exclude-result-prefixes="g url rdf rdfs dc foaf">

    <!-- http://xml.apache.org/xalan-j/extensions_xsltc.html#java_ext -->

    <xsl:param name="g:inference" select="true()" as="xs:boolean"/>
    
    <xsl:key name="resources-by-domain" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:domain/@rdf:resource"/>
    <xsl:key name="resources-by-type" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdf:type/@rdf:resource"/>
    <xsl:key name="resources-by-subclass" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:subClassOf/@rdf:resource"/>
    
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

    <!-- http://www4.wiwiss.fu-berlin.de/lodcloud/state/#terms -->
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
	<!-- http://iswc2011.semanticweb.org/fileadmin/iswc/Papers/Research_Paper/09/70310161.pdf -->
	<xsl:param name="resource-uri" as="xs:anyURI"/>
	<xsl:param name="document" as="document-node()"/>
	<xsl:variable name="resource" select="key('resources', $resource-uri, $document)"/>
	<xsl:choose>
	    <xsl:when test="$resource/rdfs:label | $resource/@rdfs:label">
		<xsl:sequence select="$resource/rdfs:label | $resource/@rdfs:label"/>
	    </xsl:when>
	    <xsl:when test="$resource/foaf:nick | $resource/@foaf:nick">
		<xsl:sequence select="$resource/foaf:nick | $resource/@foaf:nick"/>
	    </xsl:when>
	    <xsl:when test="$resource/dc:title | $resource/@dc:title">
		<xsl:sequence select="$resource/dc:title | $resource/@dc:title"/>
	    </xsl:when>
	    <xsl:when test="$resource/foaf:name | $resource/@foaf:name">
		<xsl:sequence select="$resource/foaf:name | $resource/@foaf:name"/>
	    </xsl:when>
	    <xsl:when test="$resource/dct:title | $resource/@dct:title">
		<xsl:sequence select="$resource/dct:title | $resource/@dct:title"/>
	    </xsl:when>
	    <!-- skos:prefLabel -->
	</xsl:choose>
    </xsl:function>
	
    <xsl:function name="g:label" as="xs:string?">
	<xsl:param name="resource-uri" as="xs:anyURI"/>
	<xsl:param name="document" as="document-node()"/>
	<xsl:variable name="local-label" select="g:local-label($resource-uri, $document)" as="xs:string?"/>
	<xsl:choose>
	    <xsl:when test="$local-label">
		<xsl:sequence select="concat(upper-case(substring($local-label, 1, 1)), substring($local-label, 2))"/>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:variable name="imported-label" select="(document($ontologies)/g:local-label($resource-uri, .))[1]" as="xs:string?"/>
		<!-- <xsl:variable name="imported-label" select="(document($resource-uri)/g:local-label($resource-uri, .))[1]" as="xs:string?"/> -->
		<xsl:choose>
		    <xsl:when test="$imported-label">
			<xsl:sequence select="concat(upper-case(substring($imported-label, 1, 1)), substring($imported-label, 2))"/>
		    </xsl:when>
		    <xsl:otherwise>
			<xsl:sequence select="$resource-uri"/>
		    </xsl:otherwise>
		</xsl:choose>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:function>

    <xsl:function name="rdfs:domain" as="xs:anyURI*">
	<xsl:param name="property-uri" as="xs:anyURI+"/>
<xsl:message>$property-uri: <xsl:value-of select="$property-uri"/></xsl:message>
	<xsl:for-each select="document($ontologies)">
	    <xsl:sequence select="key('resources', $property-uri)/rdfs:domain/@rdf:resource"/>
<xsl:if test="key('resources', $property-uri)/rdfs:domain/@rdf:resource">
    <xsl:message>rdfs:domain: <xsl:value-of select="key('resources', $property-uri, .)/rdfs:domain/@rdf:resource"/></xsl:message>
</xsl:if>
	</xsl:for-each>
    </xsl:function>

    <xsl:function name="g:inDomainOf" as="xs:anyURI*">
	<xsl:param name="type-uri" as="xs:anyURI+"/>
<xsl:message>$type-uri <xsl:value-of select="$type-uri"/></xsl:message>
	<xsl:for-each select="document($ontologies)">
	    <xsl:sequence select="key('resources-by-domain', $type-uri)/@rdf:about"/>
	    <xsl:variable name="super-uris" select="rdfs:subClassOf($type-uri)" as="xs:anyURI*"/>
<!-- <xsl:message>$super-uris: <xsl:value-of select="$super-uris"/></xsl:message> -->
	    <xsl:if test="not(empty($super-uris))">
		<xsl:sequence select="g:inDomainOf($super-uris)"/>
	    </xsl:if>
<xsl:if test="key('resources-by-domain', $type-uri)/@rdf:about">
    <xsl:message>g:inDomainOf: <xsl:value-of select="key('resources-by-domain', $type-uri)/@rdf:about"/></xsl:message>
</xsl:if>
	</xsl:for-each>
    </xsl:function>

    <xsl:function name="rdfs:subClassOf" as="xs:anyURI*">
	<xsl:param name="type-uri" as="xs:anyURI+"/>
<xsl:message>$type-uri <xsl:value-of select="$type-uri"/></xsl:message>
	<!-- <xsl:for-each select="document($type-uri)"> -->
	<xsl:for-each select="document($ontologies)">
	    <xsl:sequence select="key('resources', $type-uri)/rdfs:subClassOf/@rdf:resource"/>
<xsl:if test="key('resources', $type-uri)/rdfs:subClassOf/@rdf:resource">
    <xsl:message>rdfs:subClassOf: <xsl:value-of select="key('resources', $type-uri)/rdfs:subClassOf/@rdf:resource"/></xsl:message>
</xsl:if>
	</xsl:for-each>
    </xsl:function>

    <xsl:function name="g:superClassOf" as="xs:anyURI*">
	<xsl:param name="type-uri" as="xs:anyURI+"/>
<xsl:message>$type-uri <xsl:value-of select="$type-uri"/></xsl:message>
	<!-- <xsl:for-each select="document($type-uri)"> -->
	<xsl:for-each select="document($ontologies)">
	    <xsl:sequence select="key('resources-by-subclass', $type-uri)/@rdf:about"/>
<xsl:if test="key('resources', $type-uri)/rdfs:subClassOf/@rdf:resource">
    <xsl:message>g:superClassOf: <xsl:value-of select="key('resources-by-subclass', $type-uri)/@rdf:about"/></xsl:message>
</xsl:if>
	</xsl:for-each>
    </xsl:function>

</xsl:stylesheet>