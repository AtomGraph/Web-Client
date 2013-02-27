<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright (C) 2012 Martynas Jusevičius <martynas@graphity.org>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
-->
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java "http://xml.apache.org/xalan/java/">
    <!ENTITY g "http://graphity.org/ontology#">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY dc "http://purl.org/dc/elements/1.1/">
    <!ENTITY dct "http://purl.org/dc/terms/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY skos "http://www.w3.org/2004/02/skos/core#">
    <!ENTITY gr "http://purl.org/goodrelations/v1#">
    <!ENTITY list "http://jena.hpl.hp.com/ARQ/list#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:url="&java;java.net.URLDecoder"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:sparql="&sparql;"
xmlns:dc="&dc;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:skos="&skos;"
xmlns:gr="&gr;"
xmlns:list="&list;"
exclude-result-prefixes="#all">

    <!-- http://xml.apache.org/xalan-j/extensions_xsltc.html#java_ext -->

    <xsl:key name="resources-by-subclass" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:subClassOf/@rdf:resource"/>
    <xsl:key name="resources-by-domain" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:domain/@rdf:resource"/>
    <xsl:key name="resources-by-range" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:range/@rdf:resource"/>
    <xsl:key name="resources-by-broader" match="*[@rdf:about] | *[@rdf:nodeID]" use="skos:broader/@rdf:resource"/>
    <xsl:key name="resources-by-narrower" match="*[@rdf:about] | *[@rdf:nodeID]" use="skos:narrower/@rdf:resource"/>
    
    <xsl:function name="g:local-label" as="xs:string?">
	<!-- http://www4.wiwiss.fu-berlin.de/lodcloud/state/#terms -->
	<!-- http://iswc2011.semanticweb.org/fileadmin/iswc/Papers/Research_Paper/09/70310161.pdf -->
	<xsl:param name="resource-uri" as="xs:anyURI"/>
	<xsl:param name="document" as="document-node()"/>
	<xsl:variable name="resource" select="key('resources', $resource-uri, $document)"/>

	<xsl:for-each select="$resource">
	    <xsl:choose>
		<xsl:when test="rdfs:label[not(@xml:lang)]">
		    <xsl:sequence select="rdfs:label[not(@xml:lang)][1]"/>
		</xsl:when>
		<xsl:when test="foaf:nick[not(@xml:lang)]">
		    <xsl:sequence select="foaf:nick[not(@xml:lang)][1]"/>
		</xsl:when>
		<xsl:when test="foaf:name[not(@xml:lang)]">
		    <xsl:sequence select="foaf:name[not(@xml:lang)][1]"/>
		</xsl:when>
		<xsl:when test="dc:title[not(@xml:lang)]">
		    <xsl:sequence select="dc:title[not(@xml:lang)][1]"/>
		</xsl:when>
		<xsl:when test="dct:title[not(@xml:lang)]">
		    <xsl:sequence select="dct:title[not(@xml:lang)][1]"/>
		</xsl:when>
		<xsl:when test="skos:prefLabel[not(@xml:lang)]">
		    <xsl:sequence select="skos:prefLabel[not(@xml:lang)][1]"/>
		</xsl:when>
		<xsl:when test="gr:name[not(@xml:lang)]">
		    <xsl:sequence select="gr:name[not(@xml:lang)][1]"/>
		</xsl:when>
		
		<xsl:when test="rdfs:label | @rdfs:label">
		    <xsl:sequence select="(rdfs:label | @rdfs:label)[1]"/>
		</xsl:when>
		<xsl:when test="foaf:nick | @foaf:nick">
		    <xsl:sequence select="(foaf:nick | @foaf:nick)[1]"/>
		</xsl:when>
		<xsl:when test="foaf:firstName and foaf:lastName">
		    <xsl:sequence select="concat(foaf:firstName[1], ' ', foaf:lastName[1])"/>
		</xsl:when>
		<xsl:when test="foaf:name | @foaf:name">
		    <xsl:sequence select="(foaf:name | @foaf:name)[1]"/>
		</xsl:when>
		<xsl:when test="dc:title | @dc:title">
		    <xsl:sequence select="(dc:title | @dc:title)[1]"/>
		</xsl:when>
		<xsl:when test="dct:title | @dct:title">
		    <xsl:sequence select="(dct:title | @dct:title)[1]"/>
		</xsl:when>
		<xsl:when test="skos:prefLabel | @skos:prefLabel">
		    <xsl:sequence select="(skos:prefLabel | @skos:prefLabel)[1]"/>
		</xsl:when>
		<xsl:when test="gr:name | @gr:name">
		    <xsl:sequence select="(gr:name | @gr:name)[1]"/>
		</xsl:when>
	    </xsl:choose>
	</xsl:for-each>
    </xsl:function>

    <xsl:function name="g:local-label" as="xs:string?">
	<!-- http://www4.wiwiss.fu-berlin.de/lodcloud/state/#terms -->
	<!-- http://iswc2011.semanticweb.org/fileadmin/iswc/Papers/Research_Paper/09/70310161.pdf -->
	<xsl:param name="resource-uri" as="xs:anyURI"/>
	<xsl:param name="document" as="document-node()"/>
	<xsl:param name="lang" as="xs:string"/>
	<xsl:variable name="resource" select="key('resources', $resource-uri, $document)"/>

	<xsl:for-each select="$resource">
	    <xsl:choose>
		<xsl:when test="rdfs:label[lang($lang)]">
		    <xsl:sequence select="rdfs:label[lang($lang)][1]"/>
		</xsl:when>
		<xsl:when test="foaf:nick[lang($lang)]">
		    <xsl:sequence select="foaf:nick[lang($lang)][1]"/>
		</xsl:when>
		<xsl:when test="foaf:name[lang($lang)]">
		    <xsl:sequence select="foaf:name[lang($lang)][1]"/>
		</xsl:when>
		<xsl:when test="dc:title[lang($lang)]">
		    <xsl:sequence select="dc:title[lang($lang)][1]"/>
		</xsl:when>
		<xsl:when test="dct:title[lang($lang)]">
		    <xsl:sequence select="dct:title[lang($lang)][1]"/>
		</xsl:when>
		<xsl:when test="skos:prefLabel[lang($lang)]">
		    <xsl:sequence select="skos:prefLabel[lang($lang)][1]"/>
		</xsl:when>
		<xsl:when test="gr:name[lang($lang)]">
		    <xsl:sequence select="gr:name[lang($lang)][1]"/>
		</xsl:when>
	    </xsl:choose>
	</xsl:for-each>
    </xsl:function>

    <xsl:function name="g:document-uri" as="xs:anyURI">
	<xsl:param name="uri" as="xs:anyURI"/>
	<xsl:choose>
	    <!-- strip trailing fragment identifier (#) -->
	    <xsl:when test="contains($uri, '#')">
		<xsl:sequence select="xs:anyURI(substring-before($uri, '#'))"/>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:sequence select="$uri"/>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:function>

    <xsl:function name="g:fragment-id" as="xs:string?">
	<xsl:param name="uri" as="xs:anyURI"/>
	
	<xsl:sequence select="substring-after($uri, '#')"/>
    </xsl:function>

    <xsl:function name="g:label" as="xs:string?">
	<xsl:param name="resource-uri" as="xs:anyURI"/>
	<xsl:param name="document" as="document-node()"/>
	<xsl:param name="lang" as="xs:string"/>
	<xsl:variable name="local-lang-label" select="g:local-label($resource-uri, $document, $lang)" as="xs:string?"/>
	<xsl:variable name="local-en-label" select="g:local-label($resource-uri, $document, 'en')" as="xs:string?"/>
	<xsl:variable name="local-label" select="g:local-label($resource-uri, $document)" as="xs:string?"/>

	<xsl:choose>
	    <xsl:when test="$local-lang-label"> <!-- try localized label first -->
		<xsl:sequence select="concat(upper-case(substring($local-lang-label, 1, 1)), substring($local-lang-label, 2))"/>
	    </xsl:when>
	    <xsl:when test="$local-en-label"> <!-- try english label second -->
		<xsl:sequence select="concat(upper-case(substring($local-en-label, 1, 1)), substring($local-en-label, 2))"/>
	    </xsl:when>
	    <xsl:when test="$local-label"> <!-- fallback to any label as the last resort -->
		<xsl:sequence select="concat(upper-case(substring($local-label, 1, 1)), substring($local-label, 2))"/>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:variable name="imported-lang-label" select="g:local-label($resource-uri, document(g:document-uri($resource-uri)), $lang)" as="xs:string?"/>
		<xsl:variable name="imported-en-label" select="g:local-label($resource-uri, document(g:document-uri($resource-uri)), 'en')" as="xs:string?"/>
		<xsl:variable name="imported-label" select="g:local-label($resource-uri, document(g:document-uri($resource-uri)))" as="xs:string?"/>
		<xsl:choose>
		    <xsl:when test="$imported-lang-label">
			<xsl:sequence select="concat(upper-case(substring($imported-lang-label, 1, 1)), substring($imported-lang-label, 2))"/>
		    </xsl:when>
		    <xsl:when test="$imported-en-label">
			<xsl:sequence select="concat(upper-case(substring($imported-en-label, 1, 1)), substring($imported-en-label, 2))"/>
		    </xsl:when>
		    <xsl:when test="$imported-label">
			<xsl:sequence select="concat(upper-case(substring($imported-label, 1, 1)), substring($imported-label, 2))"/>
		    </xsl:when>
		    <xsl:when test="substring-after($resource-uri, '#')">
			<xsl:sequence select="substring-after($resource-uri, '#')"/>
		    </xsl:when>
		    <xsl:when test="string-length(tokenize($resource-uri, '/')[last()]) &gt; 0">
			<xsl:sequence select="translate(url:decode(tokenize($resource-uri, '/')[last()], 'UTF-8'), '_', ' ')"/>
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
	<!-- <xsl:message>$property-uri: <xsl:value-of select="$property-uri"/></xsl:message> -->
	<xsl:for-each select="$property-uri">
	    <xsl:for-each select="document(g:document-uri($property-uri))">
		<xsl:sequence select="key('resources', $property-uri)/rdfs:domain/@rdf:resource"/>
	    </xsl:for-each>
	</xsl:for-each>
    </xsl:function>

    <xsl:function name="g:inDomainOf" as="xs:anyURI*">
	<xsl:param name="type-uri" as="xs:anyURI+"/>
	<!-- <xsl:message>$type-uri <xsl:value-of select="$type-uri"/></xsl:message> -->
	<xsl:for-each select="$type-uri">
	    <xsl:for-each select="document(g:document-uri(.))">
		<xsl:sequence select="key('resources-by-domain', $type-uri)/@rdf:about"/>
	    </xsl:for-each>
	</xsl:for-each>
    </xsl:function>

    <xsl:function name="rdfs:range" as="xs:anyURI*">
	<xsl:param name="property-uri" as="xs:anyURI+"/>
	<!-- <xsl:message>$property-uri: <xsl:value-of select="$property-uri"/></xsl:message> -->
	<xsl:for-each select="$property-uri">
	    <xsl:for-each select="document(g:document-uri($property-uri))">
		<xsl:sequence select="key('resources', $property-uri)/rdfs:range/@rdf:resource"/>
	    </xsl:for-each>
	</xsl:for-each>
    </xsl:function>

    <xsl:function name="rdfs:subClassOf" as="xs:anyURI*">
	<xsl:param name="uri" as="xs:anyURI+"/>
	<xsl:for-each select="document(g:document-uri($uri))">
	    <xsl:sequence select="key('resources', $uri)/rdfs:subClassOf/@rdf:resource"/>
	</xsl:for-each>
    </xsl:function>

    <xsl:function name="g:superClassOf" as="xs:anyURI*">
	<xsl:param name="uri" as="xs:anyURI+"/>
	<xsl:for-each select="document(g:document-uri($uri))">
	    <xsl:sequence select="key('resources-by-subclass', $uri)/@rdf:about"/>
	</xsl:for-each>
    </xsl:function>

    <xsl:function name="skos:broader" as="xs:anyURI*">
	<xsl:param name="uri" as="xs:anyURI+"/>
	<xsl:for-each select="document(g:document-uri($uri))">
	    <xsl:sequence select="key('resources', $uri)/skos:broader/@rdf:resource | key('resources-by-narrower', $uri)/@rdf:about"/>
	</xsl:for-each>
    </xsl:function>

    <xsl:function name="skos:narrower" as="xs:anyURI*">
	<xsl:param name="uri" as="xs:anyURI+"/>
	<xsl:for-each select="document(g:document-uri($uri))">
	    <xsl:sequence select="key('resources', $uri)/skos:narrower/@rdf:resource | key('resources-by-broader', $uri)/@rdf:about"/>
	</xsl:for-each>
    </xsl:function>

    <xsl:function name="list:member" as="node()*">
	<xsl:param name="list" as="node()?"/>
	<xsl:param name="document" as="document-node()"/>

	<xsl:if test="$list">
	    <xsl:sequence select="key('resources', $list/rdf:first/@rdf:resource, $document) | key('resources', $list/rdf:first/@rdf:nodeID, $document)"/>

	    <xsl:sequence select="list:member(key('resources', $list/rdf:rest/@rdf:resource, $document), $document) | list:member(key('resources', $list/rdf:rest/@rdf:nodeID, $document), $document)"/>
	</xsl:if>
    </xsl:function>
	
    <xsl:function name="g:query">
	<xsl:param name="query" as="xs:string"/>
	<xsl:param name="endpoint-uri" as="xs:anyURI"/>
	<xsl:param name="accept" as="xs:string?"/>

	<xsl:variable name="query-string" select="concat('query=', encode-for-uri($query))" as="xs:string"/>
	
	<xsl:sequence select="document(concat($endpoint-uri, '?', $query-string))"/>
    </xsl:function>

    <xsl:function name="g:query-string" as="xs:string?">
	<xsl:param name="lang" as="xs:string?"/>
	
	<xsl:sequence select="g:query-string($lang, ())"/>
    </xsl:function>

    <xsl:function name="g:query-string" as="xs:string?">
	<xsl:param name="lang" as="xs:string?"/>
	<xsl:param name="mode" as="xs:string?"/>
	
	<xsl:sequence select="g:query-string((), (), (), (), $lang, $mode)"/>
    </xsl:function>

    <xsl:function name="g:query-string" as="xs:string?">
	<xsl:param name="offset" as="xs:integer?"/>
	<xsl:param name="limit" as="xs:integer?"/>
	<xsl:param name="order-by" as="xs:string?"/>
	<xsl:param name="desc" as="xs:boolean?"/>
	<xsl:param name="lang" as="xs:string?"/>
	<xsl:param name="mode" as="xs:string?"/>
	
	<xsl:variable name="query-string">
	    <xsl:text>limit=</xsl:text><xsl:value-of select="$limit"/><xsl:text>&amp;</xsl:text>
	    <xsl:text>offset=</xsl:text><xsl:value-of select="$offset"/><xsl:text>&amp;</xsl:text>
	    <xsl:if test="$order-by">order-by=<xsl:value-of select="encode-for-uri($order-by)"/>&amp;</xsl:if>
	    <xsl:if test="$desc">desc&amp;</xsl:if>
	    <xsl:if test="$lang">lang=<xsl:value-of select="$lang"/>&amp;</xsl:if>
	    <xsl:if test="$mode">mode=<xsl:value-of select="encode-for-uri($mode)"/>&amp;</xsl:if>
	</xsl:variable>
	
	<xsl:if test="string-length($query-string) &gt; 1">
	    <xsl:sequence select="concat('?', substring($query-string, 1, string-length($query-string) - 1))"/>
	</xsl:if>
    </xsl:function>

</xsl:stylesheet>