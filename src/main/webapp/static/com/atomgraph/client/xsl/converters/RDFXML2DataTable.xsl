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
<!DOCTYPE uridef[
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY ac     "http://atomgraph.com/ns/client#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:ac="&ac;"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:date="http://exslt.org/dates-and-times"
exclude-result-prefixes="xs">

        <xsl:output indent="no" omit-xml-declaration="yes" method="text" encoding="UTF-8" media-type="application/json"/>
        <xsl:strip-space elements="*"/>

        <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
        <xsl:key name="properties" match="*[@rdf:about or @rdf:nodeID]/*" use="concat(namespace-uri(), local-name())"/>

        <!-- 
        http://code.google.com/apis/visualization/documentation/reference.html#dataparam
        http://code.google.com/apis/visualization/documentation/dev/implementing_data_source.html#responseformat

        {
          "cols": [{id: 'A', label: 'NEW A', type: 'string'},
                         {id: 'B', label: 'B-label', type: 'number'},
                         {id: 'C', label: 'C-label', type: 'date'}
                        ],
          "rows": [{c:[{v: 'a'}, {v: 1.0, f: 'One'}, {v: new Date(2008, 1, 28, 0, 31, 26), f: '2/28/08 12:31 AM'}]},
                         {c:[{v: 'b'}, {v: 2.0, f: 'Two'}, {v: new Date(2008, 2, 30, 0, 31, 26), f: '3/30/08 12:31 AM'}]},
                         {c:[{v: 'c'}, {v: 3.0, f: 'Three'}, {v: new Date(2008, 3, 30, 0, 31, 26), f: '4/30/08 12:31 AM'}]}
                        ]
        }

        -->

        <xsl:template match="/" mode="ac:DataTable">
                <xsl:apply-templates mode="#current"/>
        </xsl:template>
        
        <xsl:template match="rdf:RDF" mode="ac:DataTable">
            <xsl:param name="x-property" as="xs:anyURI?" tunnel="yes"/>
            <xsl:param name="y-property" as="xs:anyURI?" tunnel="yes"/>
            <xsl:param name="properties" as="element()*">
                <xsl:choose>
                    <xsl:when test="$x-property and $y-property">
                        <xsl:for-each-group select="*/*[concat(namespace-uri(), local-name()) = $x-property][1]" group-by="concat(namespace-uri(), local-name())">
                            <xsl:sort select="xs:anyURI(concat(namespace-uri(), local-name()))"/>
                            <xsl:sequence select="current-group()[1]"/>
                        </xsl:for-each-group>
                        <xsl:for-each-group select="*/*[concat(namespace-uri(), local-name()) = $y-property][1]" group-by="concat(namespace-uri(), local-name())">
                            <xsl:sort select="xs:anyURI(concat(namespace-uri(), local-name()))"/>
                            <xsl:sequence select="current-group()[1]"/>
                        </xsl:for-each-group>
                    </xsl:when>
                    <xsl:when test="$y-property">
                        <xsl:for-each-group select="*/*[concat(namespace-uri(), local-name()) = $y-property][1]" group-by="concat(namespace-uri(), local-name())">
                            <xsl:sort select="xs:anyURI(concat(namespace-uri(), local-name()))"/>
                            <xsl:sequence select="current-group()[1]"/>
                        </xsl:for-each-group>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:for-each-group select="*/*" group-by="concat(namespace-uri(), local-name())">
                            <xsl:sort select="xs:anyURI(concat(namespace-uri(), local-name()))"/>
                            <xsl:sequence select="current-group()[1]"/>
                        </xsl:for-each-group>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:param>

{
        "cols": [
                <!-- resource URI/bnode becomes the first column if none is provided explicitly -->
                <xsl:if test="not($x-property)">
                    { 
                            "id": "<xsl:value-of select="generate-id()"/>",
                            "type": "string"
                    },
                </xsl:if>

                <xsl:for-each select="$properties">
                        <xsl:apply-templates select="." mode="ac:DataTableColumns"/>
                        
                        <xsl:if test="position() != last()">        ,
                        </xsl:if>
                </xsl:for-each>
        ],
        "rows": [
                <xsl:apply-templates mode="#current">
                    <xsl:with-param name="properties" select="$properties"/>
                </xsl:apply-templates>
                ]
}
        </xsl:template>

        <!--  DATA TABLE HEADER -->
        <!-- properties -->

        <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="ac:DataTableColumns">

                        {
                                "id": "<xsl:value-of select="generate-id()"/>",
                                "label": "<xsl:value-of select="concat(namespace-uri(), local-name())"/>",
                                "type":
                                "<xsl:choose>
                                        <xsl:when test="count(../../*/*[concat(namespace-uri(), local-name()) = current()/concat(namespace-uri(), local-name())][@rdf:datatype = ('&xsd;integer', '&xsd;decimal', '&xsd;double', '&xsd;float')]) = count(../../*/*[concat(namespace-uri(), local-name()) = current()/concat(namespace-uri(), local-name())])">number</xsl:when>
                                        <xsl:when test="count(../../*/*[concat(namespace-uri(), local-name()) = current()/concat(namespace-uri(), local-name())][@rdf:datatype = ('&xsd;dateTime', '&xsd;date')]) = count(../../*/*[concat(namespace-uri(), local-name()) = current()/concat(namespace-uri(), local-name())])">date</xsl:when>
                                        <xsl:when test="count(../../*/*[concat(namespace-uri(), local-name()) = current()/concat(namespace-uri(), local-name())][@rdf:datatype = ('&xsd;time')]) = count(../../*/*[concat(namespace-uri(), local-name()) = current()/concat(namespace-uri(), local-name())])">timeofday</xsl:when>
                                        <xsl:otherwise>string</xsl:otherwise></xsl:choose>"
                        }
                <xsl:if test="position() != last()">        ,
                </xsl:if>
        </xsl:template>

        <!--  DATA TABLE ROW -->
        <!-- subject -->

        <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="ac:DataTable">
            <xsl:param name="x-property" as="xs:anyURI?" tunnel="yes"/>
            <xsl:param name="y-property" as="xs:anyURI?" tunnel="yes"/>
            <xsl:param name="properties" as="element()*"/>

        {
                "c": [
                <!-- resource URI/bnode becomes the first column if none is provided explicitly -->
                <xsl:if test="not($x-property)">
                    {
                        "v": <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>
                    },
                </xsl:if>

                <xsl:variable name="subject" select="."/>
                <xsl:for-each select="$properties">
                    <xsl:choose>
                        <xsl:when test="$subject/*[concat(namespace-uri(), local-name()) = current()/concat(namespace-uri(), local-name())]">
                            <xsl:apply-templates select="$subject/*[concat(namespace-uri(), local-name()) = current()/concat(namespace-uri(), local-name())]" mode="#current"/>
                        </xsl:when>
                        <xsl:otherwise>
                            { "v": null }    
                        </xsl:otherwise>
                    </xsl:choose>
                    
                    <xsl:if test="position() != last()">        ,
                    </xsl:if>
                </xsl:for-each>
                ]
        }
            <xsl:if test="position() != last()">,
            </xsl:if>
        </xsl:template>

        <!--  DATA TABLE CELLS -->
        <!-- properties -->

        <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="ac:DataTable">
                        {
                                "v": <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="#current"/>
                        }
                <xsl:if test="position() != last()">        ,
                </xsl:if>
        </xsl:template>
        
        <xsl:template match="text()[../@rdf:datatype = '&xsd;boolean']" mode="ac:DataTable">
                <xsl:value-of select="."/>
        </xsl:template>

        <xsl:template match="text()[../@rdf:datatype = '&xsd;integer'] | text()[../@rdf:datatype = '&xsd;decimal'] | text()[../@rdf:datatype = '&xsd;double'] | text()[../@rdf:datatype = '&xsd;float']" mode="ac:DataTable">
                <xsl:value-of select="."/>
        </xsl:template>

        <xsl:template match="text()[../@rdf:datatype = '&xsd;date']" mode="ac:DataTable">
                new Date(<xsl:value-of select="date:year(.)"/>, <xsl:value-of select="date:month-in-year(.) - 1"/>, <xsl:value-of select="date:day-in-month(.)"/>)
        </xsl:template>

        <xsl:template match="text()[../@rdf:datatype = '&xsd;dateTime']" mode="ac:DataTable">
                new Date(<xsl:value-of select="date:year(.)"/>, <xsl:value-of select="date:month-in-year(.) - 1"/>, <xsl:value-of select="date:day-in-month(.)"/>, <xsl:value-of select="date:hour-in-day(.)"/>, <xsl:value-of select="date:minute-in-hour(.)"/>, <xsl:value-of select="date:second-in-minute(.)"/>)
        </xsl:template>

        <xsl:template match="text()[../@rdf:datatype = '&xsd;time']" mode="ac:DataTable">
                [ <xsl:value-of select="substring(., 1, 2)" />, <xsl:value-of select="substring(., 4, 2)" />, <xsl:value-of select="substring(., 7, 2)" />
                <xsl:if test="contains(., '.')">
                    , <xsl:value-of select="substring(substring-after(., '.'), 1, 3)" />
                </xsl:if>
                ]
        </xsl:template>

        <xsl:template match="text()[../@rdf:datatype = '&xsd;string'] | text()" mode="ac:DataTable">
                "<xsl:value-of select="replace(replace(replace(replace(replace(replace(., '\\', '\\\\'), '&quot;', '\\&quot;'), '''', '\\'''), '&#x9;', '\\t'), '&#xA;', '\\n'), '&#xD;', '\\r')"/>"
        </xsl:template>

        <xsl:template match="node()[../@rdf:parseType = 'Literal']" mode="ac:DataTable">
                <xsl:if test="position() = 1">
                    <xsl:text>"</xsl:text>
                </xsl:if>

                <xsl:value-of select="replace(replace(replace(replace(replace(replace(., '\\', '\\\\'), '&quot;', '\\&quot;'), '''', '\\'''), '&#x9;', '\\t'), '&#xA;', '\\n'), '&#xD;', '\\r')"/>
                
                <xsl:if test="position() = last()">
                    <xsl:text>"</xsl:text>
                </xsl:if>
        </xsl:template>

        <xsl:template match="@rdf:about | @rdf:resource | @rdf:nodeID" mode="ac:DataTable">
                "<xsl:value-of select="."/>"
        </xsl:template>

</xsl:stylesheet>