<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2012 Martynas Jusevičius <martynas@atomgraph.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java   "http://xml.apache.org/xalan/java/">
    <!ENTITY ac     "http://atomgraph.com/ns/client#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY ldt    "https://www.w3.org/ns/ldt#">
    <!ENTITY dc     "http://purl.org/dc/elements/1.1/">
    <!ENTITY dct    "http://purl.org/dc/terms/">
    <!ENTITY foaf   "http://xmlns.com/foaf/0.1/">
    <!ENTITY skos   "http://www.w3.org/2004/02/skos/core#">
    <!ENTITY sp     "http://spinrdf.org/sp#">
    <!ENTITY list   "http://jena.hpl.hp.com/ARQ/list#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:url="&java;java.net.URLDecoder"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:sparql="&sparql;"
xmlns:ldt="&ldt;"
xmlns:dc="&dc;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:skos="&skos;"
xmlns:sp="&sp;"
xmlns:list="&list;"
exclude-result-prefixes="#all">

    <!-- http://xml.apache.org/xalan-j/extensions_xsltc.html#java_ext -->

    <xsl:key name="resources-by-subclass" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:subClassOf/@rdf:resource | rdfs:subClassOf/@rdf:nodeID"/>
    <xsl:key name="resources-by-domain" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:domain/@rdf:resource"/>
    <xsl:key name="resources-by-range" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:range/@rdf:resource"/>
    <xsl:key name="resources-by-broader" match="*[@rdf:about] | *[@rdf:nodeID]" use="skos:broader/@rdf:resource"/>
    <xsl:key name="resources-by-narrower" match="*[@rdf:about] | *[@rdf:nodeID]" use="skos:narrower/@rdf:resource"/>

    <!-- INSTANCE CONSTRUCTOR -->
    
    <xsl:function use-when="system-property('xsl:product-name') = 'SAXON'" name="ac:construct-doc" as="document-node()?">
        <xsl:param name="ontology" as="xs:anyURI"/>
        <xsl:param name="classes" as="xs:anyURI*"/>
        <xsl:param name="base" as="xs:anyURI"/>

        <xsl:sequence select="mxw:getConstructedSource($ontology, $classes, $base)" xmlns:mxw="com.atomgraph.client.writer.DatasetXSLTWriter"/>
    </xsl:function>
    
    <!-- LABEL MODE -->

    <xsl:template match="node()" mode="ac:label"/>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="ac:label">
        <xsl:variable name="labels" as="xs:string*">
            <xsl:variable name="lang-labels" as="xs:string*">
                <xsl:apply-templates select="*[lang($ldt:lang)]" mode="#current"/>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="not(empty($lang-labels))">
                    <xsl:sequence select="$lang-labels"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates mode="#current"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="not(empty($labels))">
                <xsl:value-of select="concat(upper-case(substring($labels[1], 1, 1)), substring($labels[1], 2))"/>
            </xsl:when>
            <xsl:when test="contains(@rdf:about, '#') and not(ends-with(@rdf:about, '#'))">
                <xsl:variable name="label" select="substring-after(@rdf:about, '#')"/>
                <xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
            </xsl:when>
            <xsl:when test="string-length(tokenize(@rdf:about, '/')[last()]) &gt; 0">
                <xsl:variable name="label" use-when="function-available('url:decode')" select="translate(url:decode(tokenize(@rdf:about, '/')[last()], 'UTF-8'), '_', ' ')"/>
                <xsl:variable name="label" use-when="not(function-available('url:decode'))" select="translate(tokenize(@rdf:about, '/')[last()], '_', ' ')"/>
                <xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@rdf:about | @rdf:nodeID"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- DESCRIPTION MODE -->

    <xsl:template match="node()" mode="ac:description"/>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="ac:description">
        <xsl:variable name="descriptions" as="xs:string*">
            <xsl:variable name="lang-descriptions" as="xs:string*">
                <xsl:apply-templates select="*[lang($ldt:lang)]" mode="#current"/>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="not(empty($lang-descriptions))">
                    <xsl:sequence select="$lang-descriptions"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates mode="#current"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:if test="not(empty($descriptions))">
            <xsl:copy-of select="$descriptions[1]"/>
        </xsl:if>
    </xsl:template>

    <!-- PROPERTY LABEL MODE -->
        
    <xsl:template match="node()" mode="ac:property-label"/>
        
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="ac:property-label">
        <xsl:variable name="this" select="concat(namespace-uri(), local-name())"/>
        
        <xsl:choose>
            <xsl:when test="key('resources', $this)">
                <xsl:apply-templates select="key('resources', $this)" mode="ac:label"/>
            </xsl:when>
            <xsl:when test="doc-available(namespace-uri()) and key('resources', $this, document(namespace-uri()))" use-when="system-property('xsl:product-name') = 'SAXON'" >
                <xsl:apply-templates select="key('resources', $this, document(namespace-uri()))" mode="ac:label"/>
            </xsl:when>
            <xsl:when test="contains(concat(namespace-uri(), local-name()), '#') and not(ends-with(concat(namespace-uri(), local-name()), '#'))">
                <xsl:value-of select="substring-after(concat(namespace-uri(), local-name()), '#')"/>
            </xsl:when>
            <xsl:when test="string-length(tokenize($this, '/')[last()]) &gt; 0">
                <xsl:value-of use-when="function-available('url:decode')" select="translate(url:decode(tokenize($this, '/')[last()], 'UTF-8'), '_', ' ')"/>
                <xsl:value-of use-when="not(function-available('url:decode'))" select="translate(tokenize($this, '/')[last()], '_', ' ')"/>                    
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$this"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- OBJECT LABEL NODE -->
    
    <xsl:template match="node()" mode="ac:object-label"/>
        
    <xsl:template match="@rdf:resource | @rdf:nodeID | sparql:uri" mode="ac:object-label">
        <xsl:choose>
            <xsl:when test="key('resources', .)">
                <xsl:apply-templates select="key('resources', .)" mode="ac:label"/>
            </xsl:when>
            <xsl:when test="doc-available(ac:document-uri(.)) and key('resources', ., document(ac:document-uri(.)))" use-when="system-property('xsl:product-name') = 'SAXON'" >
                <xsl:apply-templates select="key('resources', ., document(ac:document-uri(.)))" mode="ac:label"/>
            </xsl:when>
            <xsl:when test="contains(., '#') and not(ends-with(., '#'))">
                <xsl:value-of select="substring-after(., '#')"/>
            </xsl:when>
            <xsl:when test="string-length(tokenize(., '/')[last()]) &gt; 0">
                <xsl:value-of use-when="function-available('url:decode')" select="translate(url:decode(tokenize(., '/')[last()], 'UTF-8'), '_', ' ')"/>
                <xsl:value-of use-when="not(function-available('url:decode'))" select="translate(tokenize(., '/')[last()], '_', ' ')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- IMAGE MODE -->

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="ac:image"/>

    <!-- FUNCTIONS -->
    
    <xsl:function name="ac:label" as="xs:string?">
        <xsl:param name="resource" as="element()"/>

        <xsl:variable name="labels" as="xs:string*">
            <xsl:apply-templates select="$resource" mode="ac:label"/>
        </xsl:variable>
        <xsl:sequence select="$labels[1]"/>
    </xsl:function>

    <xsl:function name="ac:description" as="xs:string?">
        <xsl:param name="resource" as="element()"/>

        <xsl:variable name="descriptions" as="xs:string*">
            <xsl:apply-templates select="$resource" mode="ac:description"/>
        </xsl:variable>
        <xsl:sequence select="$descriptions[1]"/>
    </xsl:function>

    <xsl:function name="ac:property-label" as="xs:string?">
        <xsl:param name="property" as="element()"/>
        
        <xsl:apply-templates select="$property" mode="ac:property-label"/>
    </xsl:function>

    <xsl:function name="ac:object-label" as="xs:string?">
        <xsl:param name="object" as="attribute()"/>
        
        <xsl:apply-templates select="$object" mode="ac:object-label"/>
    </xsl:function>

    <xsl:function name="ac:document-uri" as="xs:anyURI">
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

    <xsl:function name="ac:fragment-id" as="xs:string?">
        <xsl:param name="uri" as="xs:anyURI"/>
        
        <xsl:sequence select="substring-after($uri, '#')"/>
    </xsl:function>

    <xsl:function name="rdfs:domain" as="attribute()*">
        <xsl:param name="property-uri" as="xs:anyURI*"/>
        <xsl:for-each select="$property-uri">
            <xsl:for-each select="document(ac:document-uri($property-uri))">
                <xsl:sequence select="key('resources', $property-uri)/rdfs:domain/@rdf:resource"/>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:function>

    <xsl:function name="ac:inDomainOf" as="attribute()*">
        <xsl:param name="type-uri" as="xs:anyURI*"/>
        <xsl:for-each select="$type-uri">
            <xsl:for-each select="document(ac:document-uri(.))">
                <xsl:sequence select="key('resources-by-domain', $type-uri)/@rdf:about"/>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:function>

    <xsl:function name="rdfs:range" as="attribute()*">
        <xsl:param name="property-uri" as="xs:anyURI*"/>
        <xsl:for-each select="$property-uri">
            <xsl:for-each select="document(ac:document-uri($property-uri))">
                <xsl:sequence select="key('resources', $property-uri)/rdfs:range/@rdf:resource"/>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:function>

    <xsl:function name="rdfs:subClassOf" as="attribute()*">
        <xsl:param name="uri" as="xs:anyURI*"/>
        <xsl:sequence select="rdfs:subClassOf($uri, document(ac:document-uri($uri)))"/>
    </xsl:function>

    <xsl:function name="rdfs:subClassOf" as="attribute()*">
        <xsl:param name="uri" as="xs:anyURI*"/>
        <xsl:param name="document" as="document-node()"/>
        <xsl:for-each select="$document">
            <xsl:sequence select="key('resources', $uri)/rdfs:subClassOf/@rdf:resource"/>
        </xsl:for-each>
    </xsl:function>

    <xsl:function name="ac:superClassOf" as="attribute()*">
        <xsl:param name="uri" as="xs:anyURI*"/>
        <xsl:sequence select="ac:superClassOf($uri, document(ac:document-uri($uri)))"/>
    </xsl:function>

    <xsl:function name="ac:superClassOf" as="attribute()*">
        <xsl:param name="uri" as="xs:anyURI*"/>
        <xsl:param name="document" as="document-node()"/>
        <xsl:for-each select="$document">
            <xsl:sequence select="key('resources-by-subclass', $uri)/@rdf:about"/>
        </xsl:for-each>
    </xsl:function>

    <xsl:function name="skos:broader" as="attribute()*">
        <xsl:param name="uri" as="xs:anyURI*"/>
        <xsl:sequence select="skos:broader($uri, document(ac:document-uri($uri)))"/>
    </xsl:function>

    <xsl:function name="skos:broader" as="attribute()*">
        <xsl:param name="uri" as="xs:anyURI*"/>
        <xsl:param name="document" as="document-node()"/>
        <xsl:for-each select="$document">
            <xsl:sequence select="key('resources', $uri)/skos:broader/@rdf:resource | key('resources-by-narrower', $uri)/@rdf:about"/>
        </xsl:for-each>
    </xsl:function>

    <xsl:function name="skos:narrower" as="attribute()*">
        <xsl:param name="uri" as="xs:anyURI*"/>
        <xsl:sequence select="skos:narrower($uri, document(ac:document-uri($uri)))"/>
    </xsl:function>

    <xsl:function name="skos:narrower" as="attribute()*">
        <xsl:param name="uri" as="xs:anyURI*"/>
        <xsl:param name="document" as="document-node()"/>
        <xsl:for-each select="$document">
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

    <xsl:function name="ac:query-string" as="xs:string?">
        <xsl:param name="offset" as="xs:integer?"/>
        <xsl:param name="limit" as="xs:integer?"/>
        <xsl:param name="order-by" as="xs:string?"/>
        <xsl:param name="desc" as="xs:boolean?"/>
        <xsl:param name="mode" as="xs:anyURI?"/>
        
        <xsl:variable name="query-string">
            <xsl:if test="not(empty($offset))">offset=<xsl:value-of select="$offset"/>&amp;</xsl:if>
            <xsl:if test="not(empty($limit))">limit=<xsl:value-of select="$limit"/>&amp;</xsl:if>
            <xsl:if test="not(empty($order-by))">orderBy=<xsl:value-of select="encode-for-uri($order-by)"/>&amp;</xsl:if>
            <xsl:if test="$desc">desc=true&amp;</xsl:if>
            <xsl:if test="not(empty($mode))">mode=<xsl:value-of select="encode-for-uri($mode)"/>&amp;</xsl:if>
        </xsl:variable>
        
        <xsl:if test="string-length($query-string) &gt; 1">
            <xsl:sequence select="concat('?', substring($query-string, 1, string-length($query-string) - 1))"/>
        </xsl:if>
    </xsl:function>

    <xsl:function name="ac:query-string" as="xs:string?">
        <xsl:param name="uri" as="xs:anyURI?"/>
        <xsl:param name="mode" as="xs:anyURI?"/>

        <xsl:variable name="query-string">
            <xsl:if test="not(empty($uri))">uri=<xsl:value-of select="encode-for-uri($uri)"/>&amp;</xsl:if>
            <xsl:if test="not(empty($mode))">mode=<xsl:value-of select="encode-for-uri($mode)"/>&amp;</xsl:if>
        </xsl:variable>
        
        <xsl:if test="string-length($query-string) &gt; 1">
            <xsl:sequence select="concat('?', substring($query-string, 1, string-length($query-string) - 1))"/>
        </xsl:if>
    </xsl:function>

    <xsl:function name="ac:query-string" as="xs:string?">
        <xsl:param name="endpoint" as="xs:anyURI?"/>
        <xsl:param name="query" as="xs:string?"/>
        <xsl:param name="mode" as="xs:anyURI?"/>
        <xsl:param name="accept" as="xs:string?"/>

        <xsl:variable name="query-string">
            <xsl:if test="not(empty($endpoint))">endpointUri=<xsl:value-of select="encode-for-uri($endpoint)"/>&amp;</xsl:if>
            <xsl:if test="not(empty($query))">query=<xsl:value-of select="encode-for-uri($query)"/>&amp;</xsl:if>
            <xsl:if test="not(empty($mode))">mode=<xsl:value-of select="encode-for-uri($mode)"/>&amp;</xsl:if>
            <xsl:if test="not(empty($accept))">accept=<xsl:value-of select="encode-for-uri($accept)"/>&amp;</xsl:if>
        </xsl:variable>
        
        <xsl:if test="string-length($query-string) &gt; 1">
            <xsl:sequence select="concat('?', substring($query-string, 1, string-length($query-string) - 1))"/>
        </xsl:if>
    </xsl:function>
    
    <xsl:function name="ac:visit-elements" as="element()*">
        <xsl:param name="element" as="element()"/>
        <xsl:param name="type" as="xs:string?"/>
        
        <xsl:choose>
            <xsl:when test="$element/rdf:type/@rdf:resource = $type">
                <xsl:sequence select="key('resources', $element/sp:query/(@rdf:resource, @rdf:nodeID), root($element))"/>
            </xsl:when>
            <xsl:when test="list:member($element, root($element))">
                <xsl:sequence select="list:member($element, root($element))/ac:visit-elements(., $type)"/>
            </xsl:when>
            <xsl:when test="$element/sp:elements/@rdf:nodeID">
                <xsl:sequence select="key('resources', $element/sp:elements/@rdf:nodeID, root($element))/ac:visit-elements(., $type)"/>
            </xsl:when>
        </xsl:choose>
    </xsl:function>
    
    <xsl:function name="ac:escape-json" as="xs:string?">
        <xsl:param name="string" as="xs:string?"/>

        <xsl:variable name="string" select="replace($string, '\\', '\\\\')"/>
        <xsl:variable name="string" select="replace($string, '&quot;', '\\&quot;')"/>
        <xsl:variable name="string" select="replace($string, '''', '\\''')"/>
        <xsl:variable name="string" select="replace($string, '&#09;', '\\t')"/>
        <xsl:variable name="string" select="replace($string, '&#10;', '\\n')"/>
        <xsl:variable name="string" select="replace($string, '&#13;', '\\r')"/>

        <xsl:sequence select="$string"/>
    </xsl:function>

    <xsl:function name="ac:escape-regex" as="xs:string?">
        <xsl:param name="string" as="xs:string?"/>
        
        <xsl:variable name="string" select="replace($string, '\.', '\\\\.')"/>
        <xsl:variable name="string" select="replace($string, '\*', '\\\\*')"/>
        <xsl:variable name="string" select="replace($string, '\+', '\\\\+')"/>
        <xsl:variable name="string" select="replace($string, '\?', '\\\\?')"/>
        <xsl:variable name="string" select="replace($string, '\{', '\\\\{')"/>
        <xsl:variable name="string" select="replace($string, '\[', '\\\\[')"/>
        <xsl:variable name="string" select="replace($string, '\(', '\\\\(')"/>
        <xsl:variable name="string" select="replace($string, '\)', '\\\\)')"/>
        <xsl:variable name="string" select="replace($string, '\|', '\\\\|')"/>
        <xsl:variable name="string" select="replace($string, '\\', '\\\\\\')"/>
        <xsl:variable name="string" select="replace($string, '\^', '\\\\^')"/>
        <xsl:variable name="string" select="replace($string, '\$', '\\\\\$')"/>

        <xsl:sequence select="$string"/>
    </xsl:function>
    
</xsl:stylesheet>