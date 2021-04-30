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
    <!ENTITY ac     "https://w3id.org/atomgraph/client#">
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
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:ixsl="http://saxonica.com/ns/interactiveXSLT"
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
xmlns:map="http://www.w3.org/2005/xpath-functions/map"
exclude-result-prefixes="#all"
extension-element-prefixes="ixsl"
>

    <!-- http://xml.apache.org/xalan-j/extensions_xsltc.html#java_ext -->

    <xsl:key name="resources-by-subclass" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:subClassOf/@rdf:resource | rdfs:subClassOf/@rdf:nodeID"/>
    <xsl:key name="resources-by-domain" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:domain/@rdf:resource"/>
    <xsl:key name="resources-by-range" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:range/@rdf:resource"/>
    <xsl:key name="resources-by-broader" match="*[@rdf:about] | *[@rdf:nodeID]" use="skos:broader/@rdf:resource"/>
    <xsl:key name="resources-by-narrower" match="*[@rdf:about] | *[@rdf:nodeID]" use="skos:narrower/@rdf:resource"/>
    
    <!-- function stub so that Saxon-EE doesn't complain when compiling SEF -->
    <xsl:function name="ac:uuid" as="xs:string" override-extension-function="no">
        <xsl:value-of use-when="system-property('xsl:product-name') eq 'Saxon-JS'" select="ixsl:call(ixsl:window(), 'generateUUID', [])"/>
        <xsl:message use-when="system-property('xsl:product-name') = 'SAXON'" terminate="yes">
            Not implemented -- com.atomgraph.client.writer.function.UUID needs to be registered as an extension function
        </xsl:message>
    </xsl:function>
    
    <!-- function stub so that Saxon-EE doesn't complain when compiling SEF -->
    <xsl:function name="ac:construct-doc" as="document-node()*" override-extension-function="no">
        <xsl:param name="ontology" as="xs:anyURI"/>
        <xsl:param name="classes" as="xs:anyURI*"/>
        <xsl:param name="base" as="xs:anyURI"/>
            
        <xsl:message use-when="system-property('xsl:product-name') = 'SAXON'" terminate="yes">
            Not implemented -- com.atomgraph.client.writer.function.ConstructDocument needs to be registered as an extension function
        </xsl:message>
    </xsl:function>
    
    <xsl:function name="ac:label" as="xs:string?">
        <xsl:param name="resource" as="element()"/>

        <xsl:variable name="labels" as="xs:string*">
            <xsl:apply-templates select="$resource" mode="ac:label"/>
        </xsl:variable>
        <xsl:sequence select="upper-case(substring($labels[1], 1, 1)) || substring($labels[1], 2)"/>
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

        <xsl:variable name="labels" as="xs:string*">
            <xsl:apply-templates select="$property" mode="ac:property-label"/>
        </xsl:variable>
        <xsl:sequence select="upper-case(substring($labels[1], 1, 1)) || substring($labels[1], 2)"/>
    </xsl:function>

    <xsl:function name="ac:object-label" as="xs:string?">
        <xsl:param name="object" as="node()"/>
        
        <xsl:variable name="labels" as="xs:string*">
            <xsl:apply-templates select="$object" mode="ac:object-label"/>
        </xsl:variable>
        <xsl:sequence select="$labels[1]"/>
    </xsl:function>

    <xsl:function name="ac:svg-label" as="xs:string?">
        <xsl:param name="resource" as="element()"/>

        <xsl:sequence select="ac:label($resource)"/>
    </xsl:function>
    
    <xsl:function name="ac:svg-object-label" as="xs:string?">
        <xsl:param name="object" as="attribute()"/>

        <xsl:sequence select="ac:object-label($object)"/>
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

    <!-- builds URL query string out of a parameter map and appends it to the given URI, if any -->
    <xsl:function name="ac:build-uri" as="xs:anyURI?">
        <xsl:param name="absolute-path" as="xs:anyURI?"/>
        <xsl:param name="query-params" as="map(xs:string, xs:string)"/>
        
        <xsl:sequence select="xs:anyURI($absolute-path || string-join(map:keys($query-params)[. ne ''] ! (encode-for-uri(.) || '=' || encode-for-uri($query-params?(.))), '&amp;')[string-length(.) gt 0] ! ('?' || .))"/>
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