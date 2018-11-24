<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2015 Martynas Jusevičius <martynas@atomgraph.com>

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
<!DOCTYPE uridef[
    <!ENTITY owl    "http://www.w3.org/2002/07/owl#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY ac     "http://atomgraph.com/ns/client#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:owl="&owl;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:ac="&ac;"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
exclude-result-prefixes="xs">

    <!-- 
    
    An XSLT stylesheet transforming Jena's RDF/XML format to JSON-LD.
    Supports @context and prefixed names.
    
    RDF/XML: http://www.w3.org/TR/REC-rdf-syntax/
    JSON-LD: http://www.w3.org/TR/json-ld/

    -->

    <xsl:output indent="no" omit-xml-declaration="yes" method="text" encoding="UTF-8" media-type="application/ld+json"/>
    <xsl:strip-space elements="*"/>

    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
        <xsl:key name="predicates-by-object" match="*[@rdf:about]/* | *[@rdf:nodeID]/*" use="@rdf:resource | @rdf:nodeID"/>
    
    <xsl:template match="/">
        <xsl:apply-templates mode="ac:JSON-LD"/>
    </xsl:template>
    
    <xsl:template match="rdf:RDF" mode="ac:JSON-LD">
        <xsl:variable name="resources" as="xs:string*">
            <!-- do not process blank nodes that are triple objects-->
            <xsl:apply-templates select="*[@rdf:about or count(key('predicates-by-object', @rdf:nodeID)) &gt; 1]" mode="#current"/>
        </xsl:variable>
        
        <xsl:sequence select="concat('[ ', string-join($resources, ', '), ' ]')"/>
    </xsl:template>

    <!-- resource description -->
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="ac:JSON-LD">
        <xsl:variable name="context" as="xs:string">
            <xsl:variable name="prefixes" as="xs:string*">
                <xsl:for-each-group select="*" group-by="substring-before(name(), ':')">
                    <xsl:sequence select="concat('&quot;', current-grouping-key(), '&quot;: &quot;', namespace-uri(), '&quot;')"/>
                </xsl:for-each-group>
            </xsl:variable>

            <xsl:variable name="context-properties" as="xs:string*">
                <!-- @context avoids shortening properties with conflicting namespace-uri()/local-name(). Those will be used as a full prefix+suffix name. -->
                <xsl:variable name="safe-properties" as="element()*">
                    <xsl:for-each-group select="*" group-by="local-name()">
                        <xsl:if test="count(distinct-values(current-group()/namespace-uri())) = 1">
                            <xsl:sequence select="."/>
                        </xsl:if>
                    </xsl:for-each-group>
                </xsl:variable>
                <xsl:apply-templates select="." mode="ac:JSON-LDContext"/>
            </xsl:variable>

            <xsl:sequence select="concat('&quot;@context&quot;: { ', string-join($prefixes, ', '),
            if (not(empty($context-properties))) then (', ') else (), string-join($context-properties, ', '), ' }')"/>
        </xsl:variable>
        
        <xsl:variable name="subject" as="xs:string">
            <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>
        </xsl:variable>
        
        <xsl:variable name="resource" select="."/>
        <xsl:variable name="properties" as="xs:string*">
            <xsl:for-each-group select="*" group-by="concat(namespace-uri(), local-name())">
                <xsl:variable name="key" as="xs:string*">
                    <xsl:choose>
                        <xsl:when test="current-grouping-key() = '&rdf;type'">
                            <xsl:sequence select="'&quot;@type&quot;'"/>
                        </xsl:when>
                        <xsl:when test="not($resource/*[local-name() = local-name(current())][not(namespace-uri() = namespace-uri(current()))])">
                            <xsl:sequence select="concat('&quot;', local-name(), '&quot;')"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <!-- conflicting namespace-uri()/local-name() - full name() is used -->
                            <xsl:sequence select="concat('&quot;', current-grouping-key(), '&quot;')"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="values" as="xs:string*">
                    <xsl:apply-templates select="current-group()" mode="#current"/>
                </xsl:variable>

                <xsl:sequence select="concat($key, ': ',
                    if (count(current-group()) &gt; 1) then '[' else (),
                    string-join($values, ', '),
                    if (count(current-group()) &gt; 1) then ']' else ())"/>
            </xsl:for-each-group>
        </xsl:variable>

        <xsl:sequence select="concat('{ ', $context, ', ', $subject, ', ', string-join($properties, ', '), ' }')"/>
    </xsl:template>
    
    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/rdf:type" mode="ac:JSON-LD" priority="1">
        <xsl:sequence select="concat('&quot;', @rdf:resource, '&quot;')"/>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="ac:JSON-LD">
        <xsl:choose>
            <xsl:when test="node() | @rdf:resource | @rdf:nodeID">
                <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="#current"/>
            </xsl:when>
            <xsl:when test=". = ''">
                <xsl:sequence select="'&quot;&quot;'"/>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="text()" mode="ac:JSON-LD">
        <xsl:sequence select="concat('&quot;', ac:escape-json(.), '&quot;')"/>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype or ../@xml:lang]" mode="ac:JSON-LD" priority="1">
        <xsl:variable name="datatype-or-lang" as="xs:string?">
            <xsl:apply-templates select="../@rdf:datatype | ../@xml:lang" mode="#current"/>
        </xsl:variable>
        <xsl:sequence select="concat('{ &quot;@value&quot;: &quot;', ac:escape-json(.), '&quot;, ', $datatype-or-lang, ' }')"/>
    </xsl:template>

    <xsl:template match="@rdf:about" mode="ac:JSON-LD">
        <xsl:sequence select="concat('&quot;@id&quot;: &quot;', ., '&quot;')"/>
    </xsl:template>

    <xsl:template match="@rdf:resource" mode="ac:JSON-LD">
        <xsl:sequence select="concat('{ &quot;@id&quot;: &quot;', ., '&quot; }')"/>
    </xsl:template>

    <xsl:template match="@rdf:nodeID" mode="ac:JSON-LD">
        <xsl:sequence select="concat('&quot;@id&quot;: &quot;_:', ., '&quot;')"/>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID" mode="ac:JSON-LD">
        <xsl:variable name="bnode" as="xs:string">
            <xsl:next-match/>
        </xsl:variable>

        <xsl:sequence select="concat('{ ', $bnode, ' }')"/>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID[count(key('predicates-by-object', .)) &lt;= 1]" mode="ac:JSON-LD" priority="1">
        <xsl:param name="traversed-ids" as="xs:string*" tunnel="yes"/>
        <xsl:variable name="bnode" select="key('resources', .)"/>
               
        <xsl:choose>
            <!-- loop if node not visited already -->
            <xsl:when test="not(. = $traversed-ids) and $bnode">
                <xsl:apply-templates select="$bnode" mode="#current">
                    <xsl:with-param name="traversed-ids" select="(., $traversed-ids)" tunnel="yes"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="@rdf:datatype" mode="ac:JSON-LD">
        <xsl:sequence select="concat('&quot;@type&quot;: &quot;', ., '&quot;')"/>
    </xsl:template>

    <xsl:template match="@xml:lang" mode="ac:JSON-LD">
        <xsl:sequence select="concat('&quot;@language&quot;: &quot;', ., '&quot;')"/>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="ac:JSON-LDContext">
        <xsl:sequence select="concat('&quot;', local-name(), '&quot; : { &quot;@id&quot;: &quot;', name(), '&quot; }')"/>
    </xsl:template>

    <xsl:template match="rdf:type[@rdf:resource]" mode="ac:JSON-LDContext" priority="1"/>

    <xsl:template match="text()" mode="ac:JSON-LDContext"/>

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

</xsl:stylesheet>