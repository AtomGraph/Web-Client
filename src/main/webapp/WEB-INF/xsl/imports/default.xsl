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
exclude-result-prefixes="g url rdf rdfs dc dct foaf">

    <!-- http://xml.apache.org/xalan-j/extensions_xsltc.html#java_ext -->

    <xsl:param name="g:inference" select="true()" as="xs:boolean"/>
    
    <xsl:key name="resources-by-domain" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:domain/@rdf:resource"/>
    <xsl:key name="resources-by-type" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdf:type/@rdf:resource"/>
    <xsl:key name="resources-by-subclass" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:subClassOf/@rdf:resource"/>

    <!-- subject/object resource -->
    <xsl:template match="@rdf:about | @rdf:resource">
	<a href="{$base-uri}?uri={url:encode(., 'UTF-8')}">
	    <xsl:value-of select="g:label(., /, $lang)"/>
	</a>
    </xsl:template>

    <!-- subject -->
    <xsl:template match="@rdf:about" mode="g:EditMode">
	<label for="{generate-id()}">????
	    <xsl:value-of select="g:label(., /, $lang)"/>
	</label>
	<input type="text" name="su" id="{generate-id()}" value="{.}"/>
    </xsl:template>
	
    <!-- object -->
    <xsl:template match="@rdf:resource" mode="g:EditMode">
	<option value="{.}">
	    <xsl:value-of select="g:label(., /, $lang)"/>
	</option>
    </xsl:template>

    <!-- property -->
    <xsl:template match="*[node() or @rdf:resource or @rdf:nodeID]">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>
	<a href="{$base-uri}?uri={url:encode($this, 'UTF-8')}">
	    <xsl:value-of select="g:label($this, /, $lang)"/>
	</a>
    </xsl:template>

    <xsl:template match="*[node() or @rdf:resource or @rdf:nodeID]" mode="g:EditMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>
	<label for="{generate-id()}">
	    <xsl:value-of select="g:label($this, /, $lang)"/>
	</label>
	<input type="hidden" name="pu" value="{$this}"/>
    </xsl:template>

    <!-- object blank node -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID">
	<xsl:apply-templates select="key('resources', .)"/>
    </xsl:template>

    <!-- object literal -->
    <xsl:template match="text()">
	<xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="text()" mode="g:EditMode">
	<xsl:choose>
	    <xsl:when test="string-length(.) &lt; 20">
		<input type="text" name="ol" id="{generate-id(..)}" value="{.}"/>
	    </xsl:when>
	    <xsl:otherwise>
		<textarea name="ol" id="{generate-id(..)}" cols="{80}" rows="{string-length(.) div 80}">
		    <xsl:value-of select="."/>
		</textarea>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <xsl:template match="@rdf:about | @rdf:resource" mode="g:TypeMode">
	<a href="{$base-uri}?uri={url:encode(., 'UTF-8')}">
	    <xsl:value-of select="g:label(., /, $lang)"/>
	</a>
    </xsl:template>

    <xsl:function name="g:local-label" as="xs:string?">
	<!-- http://www4.wiwiss.fu-berlin.de/lodcloud/state/#terms -->
	<!-- http://iswc2011.semanticweb.org/fileadmin/iswc/Papers/Research_Paper/09/70310161.pdf -->
	<xsl:param name="resource-uri" as="xs:anyURI"/>
	<xsl:param name="document" as="document-node()"/>
	<xsl:param name="lang" as="xs:string"/>
	<xsl:variable name="resource" select="key('resources', $resource-uri, $document)"/>
	<xsl:choose>
	    <xsl:when test="$resource/rdfs:label[count(../dc:title) = 1 or lang($lang) or not(@xml:lang)] | $resource/@rdfs:label">
		<xsl:sequence select="($resource/rdfs:label[count(../dc:title) = 1 or lang($lang) or not(@xml:lang)] | $resource/@rdfs:label[lang($lang)])[1]"/>
	    </xsl:when>
	    <xsl:when test="$resource/foaf:nick[count(../dc:title) = 1 or lang($lang) or not(@xml:lang)] | $resource/@foaf:nick">
		<xsl:sequence select="$resource/foaf:nick[count(../dc:title) = 1 or lang($lang) or not(@xml:lang)] | $resource/@foaf:nick"/>
	    </xsl:when>
	    <xsl:when test="($resource/dc:title[count(../dc:title) = 1 or lang($lang) or not(@xml:lang)] | $resource/@dc:title)[1]">
		<xsl:sequence select="$resource/dc:title[count(../dc:title) = 1 or lang($lang) or not(@xml:lang)] | $resource/@dc:title"/>
	    </xsl:when>
	    <xsl:when test="$resource/foaf:name[count(../dc:title) = 1 or lang($lang) or not(@xml:lang)] | $resource/@foaf:name">
		<xsl:sequence select="$resource/foaf:name[count(../dc:title) = 1 or lang($lang) or not(@xml:lang)] | $resource/@foaf:name"/>
	    </xsl:when>
	    <xsl:when test="$resource/dct:title[count(../dc:title) = 1 or lang($lang) or not(@xml:lang)] | $resource/@dct:title">
		<xsl:sequence select="$resource/dct:title[count(../dc:title) = 1 or lang($lang) or not(@xml:lang)] | $resource/@dct:title"/>
	    </xsl:when>
	    <!-- skos:prefLabel -->
	</xsl:choose>
    </xsl:function>
	
    <xsl:function name="g:label" as="xs:string?">
	<xsl:param name="resource-uri" as="xs:anyURI"/>
	<xsl:param name="document" as="document-node()"/>
	<xsl:param name="lang" as="xs:string"/>
	<xsl:variable name="local-label" select="g:local-label($resource-uri, $document, $lang)" as="xs:string?"/>

	<xsl:choose>
	    <xsl:when test="$local-label">
		<xsl:sequence select="concat(upper-case(substring($local-label, 1, 1)), substring($local-label, 2))"/>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:variable name="document-uri" as="xs:anyURI">
		    <xsl:choose>
			<!-- strip trailing fragment identifier (#) -->
			<xsl:when test="contains($resource-uri, '#')">
			    <xsl:value-of select="substring-before($resource-uri, '#')"/>
			</xsl:when>
			<xsl:otherwise>
			    <xsl:value-of select="$resource-uri"/>
			</xsl:otherwise>
		    </xsl:choose>
		</xsl:variable>
		<!-- <xsl:variable name="imported-label" select="(document($ontologies)/g:local-label($resource-uri, ., $lang))[1]" as="xs:string?"/> -->
		<xsl:variable name="imported-label" select="g:local-label($resource-uri, document($document-uri), $lang)[1]" as="xs:string?"/>
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
	<!-- <xsl:for-each select="document($ontologies)"> -->
	<xsl:for-each select="document($property-uri)">
	    <xsl:sequence select="key('resources', $property-uri)/rdfs:domain/@rdf:resource"/>
<xsl:if test="key('resources', $property-uri)/rdfs:domain/@rdf:resource">
    <xsl:message>rdfs:domain: <xsl:value-of select="key('resources', $property-uri, .)/rdfs:domain/@rdf:resource"/></xsl:message>
</xsl:if>
	</xsl:for-each>
    </xsl:function>

    <xsl:function name="g:inDomainOf" as="xs:anyURI*">
	<xsl:param name="type-uri" as="xs:anyURI+"/>
<xsl:message>$type-uri <xsl:value-of select="$type-uri"/></xsl:message>
	<!-- <xsl:for-each select="document($ontologies)"> -->
	<xsl:for-each select="document($type-uri)">
	    <xsl:sequence select="key('resources-by-domain', $type-uri)/@rdf:about"/>
	    <xsl:variable name="super-uris" select="rdfs:subClassOf($type-uri)" as="xs:anyURI*"/>
<!-- <xsl:message>$super-uris: <xsl:value-of select="$super-uris"/></xsl:message> -->
	    <!-- 
	    <xsl:if test="not(empty($super-uris))">
		<xsl:sequence select="g:inDomainOf($super-uris)"/>
	    </xsl:if>
	    -->
<xsl:if test="key('resources-by-domain', $type-uri)/@rdf:about">
    <xsl:message>g:inDomainOf: <xsl:value-of select="key('resources-by-domain', $type-uri)/@rdf:about"/></xsl:message>
</xsl:if>
	</xsl:for-each>
    </xsl:function>

    <xsl:function name="rdfs:subClassOf" as="xs:anyURI*">
	<xsl:param name="type-uri" as="xs:anyURI+"/>
<xsl:message>$type-uri <xsl:value-of select="$type-uri"/></xsl:message>
	<xsl:for-each select="document($type-uri)">
	<!-- <xsl:for-each select="document($ontologies)"> -->
	    <xsl:sequence select="key('resources', $type-uri)/rdfs:subClassOf/@rdf:resource"/>
<xsl:if test="key('resources', $type-uri)/rdfs:subClassOf/@rdf:resource">
    <xsl:message>rdfs:subClassOf: <xsl:value-of select="key('resources', $type-uri)/rdfs:subClassOf/@rdf:resource"/></xsl:message>
</xsl:if>
	</xsl:for-each>
    </xsl:function>

    <xsl:function name="g:superClassOf" as="xs:anyURI*">
	<xsl:param name="type-uri" as="xs:anyURI+"/>
<xsl:message>$type-uri <xsl:value-of select="$type-uri"/></xsl:message>
	<xsl:for-each select="document($type-uri)">
	<!-- <xsl:for-each select="document($ontologies)"> -->
	    <xsl:sequence select="key('resources-by-subclass', $type-uri)/@rdf:about"/>
<xsl:if test="key('resources', $type-uri)/rdfs:subClassOf/@rdf:resource">
    <xsl:message>g:superClassOf: <xsl:value-of select="key('resources-by-subclass', $type-uri)/@rdf:about"/></xsl:message>
</xsl:if>
	</xsl:for-each>
    </xsl:function>

</xsl:stylesheet>