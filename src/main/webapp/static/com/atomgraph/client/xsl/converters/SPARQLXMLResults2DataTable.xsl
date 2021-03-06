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
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY srx    "http://www.w3.org/2005/sparql-results#">
    <!ENTITY ac     "https://w3id.org/atomgraph/client#">
]>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:srx="&srx;"
xmlns:ac="&ac;"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
exclude-result-prefixes="#all">

        <xsl:output indent="no" omit-xml-declaration="yes" method="text" encoding="UTF-8" media-type="application/json"/>
        <xsl:strip-space elements="*"/>
        
        <xsl:key name="binding-by-name" match="srx:binding" use="@name"/> 
        <xsl:variable name="numeric-variables" select="srx:variable[count(key('binding-by-name', @name)) = count(key('binding-by-name', @name)[string(number(srx:literal)) != 'NaN'])]"/> 

        <!-- 
        http://code.google.com/apis/visualization/documentation/reference.html#dataparam
        http://code.google.com/apis/visualization/documentation/dev/implementing_data_source.html#responseformat

        {
          "cols": [{id: 'A', label: 'NEW A', type: 'string'},
                         {id: 'B', label: 'B-label', type: 'number'},
                         {id: 'C', label: 'C-label', type: 'date'}
                        ],
          "rows": [{c:[{v: 'a'}, {v: 1.0, f: 'One'}, {v: "Date(2008, 1, 28, 0, 31, 26)", f: '2/28/08 12:31 AM'}]},
                         {c:[{v: 'b'}, {v: 2.0, f: 'Two'}, {v: "Date(2008, 2, 30, 0, 31, 26)", f: '3/30/08 12:31 AM'}]},
                         {c:[{v: 'c'}, {v: 3.0, f: 'Three'}, {v: "Date(2008, 3, 30, 0, 31, 26)", f: '4/30/08 12:31 AM'}]}
                        ]
        }

        -->

        <xsl:template match="/" mode="ac:DataTable">
                <xsl:apply-templates mode="#current"/>
        </xsl:template>
        
        <xsl:template match="srx:sparql" mode="ac:DataTable">
            <xsl:param name="var-names" as="xs:string*" tunnel="yes"/>
            <xsl:param name="variables" as="element()*">
                <xsl:choose>
                    <xsl:when test="not(empty($var-names))">
                        <xsl:variable name="current" select="."/>
                        <xsl:for-each select="$var-names">
                            <xsl:sequence select="$current/srx:head/srx:variable[@name = current()]"/>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:sequence select="srx:head/srx:variable"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:param>
{
        "cols": [ <xsl:apply-templates select="$variables" mode="#current"/> ],
        "rows": [
            <xsl:apply-templates select="srx:results/srx:result" mode="#current">
                <xsl:with-param name="variables" select="$variables"/>
            </xsl:apply-templates>
        ]
}
        </xsl:template>

        <!--  DATA TABLE HEADER -->

        <xsl:template match="srx:variable" mode="ac:DataTable">
                        {
                                "id": "<xsl:value-of select="generate-id()"/>",
                                "label": "<xsl:value-of select="@name"/>",
                                "type":
                                "<xsl:choose>
                                        <xsl:when test="count(key('binding-by-name', @name)) = count(key('binding-by-name', @name)/srx:uri)">string</xsl:when>
                                        <xsl:when test="count(key('binding-by-name', @name)) = count(key('binding-by-name', @name)/srx:bnode)">string</xsl:when>
                                        <xsl:when test="count(key('binding-by-name', @name)) = count(key('binding-by-name', @name)/srx:literal[@datatype = ('&xsd;integer', '&xsd;decimal', '&xsd;double', '&xsd;float')])">number</xsl:when>
                                        <xsl:when test="count(key('binding-by-name', @name)) = count(key('binding-by-name', @name)/srx:literal[@datatype = ('&xsd;dateTime', '&xsd;date')])">date</xsl:when>
                                        <xsl:when test="count(key('binding-by-name', @name)) = count(key('binding-by-name', @name)/srx:literal[@datatype = ('&xsd;time')])">timeofday</xsl:when>
                                        <xsl:otherwise>string</xsl:otherwise></xsl:choose>"
                        }
                <xsl:if test="position() != last()">        ,
                </xsl:if>
        </xsl:template>

        <!--  DATA TABLE ROW -->

        <xsl:template match="srx:result" mode="ac:DataTable">
            <xsl:param name="variables" as="element()*"/>

        {
               "c": [ 
 
            <xsl:variable name="result" select="."/>
            <xsl:for-each select="$variables">
                <xsl:choose>
                    <xsl:when test="$result/srx:binding[@name = current()/@name]">
                        <xsl:apply-templates select="$result/srx:binding[@name = current()/@name]" mode="#current"/>
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

        <xsl:template match="srx:binding" mode="ac:DataTable">
                        {
                                "v": <xsl:apply-templates select="*" mode="#current"/>
                        }
                <xsl:if test="position() != last()">        ,
                </xsl:if>
        </xsl:template>

        <xsl:template match="srx:literal[@datatype = '&xsd;boolean']" mode="ac:DataTable">
                <xsl:value-of select="."/>
        </xsl:template>

        <xsl:template match="srx:literal[@datatype = '&xsd;integer'] | srx:literal[@datatype = '&xsd;decimal'] | srx:literal[@datatype = '&xsd;double'] | srx:literal[@datatype = '&xsd;float']" mode="ac:DataTable">
                <xsl:value-of select="."/>
        </xsl:template>

        <xsl:template match="srx:literal[@datatype = '&xsd;date']" mode="ac:DataTable">
                "Date(<xsl:value-of select="year-from-date(.)"/>, <xsl:value-of select="month-from-date(.) - 1"/>, <xsl:value-of select="day-from-date(.)"/>)"
        </xsl:template>

        <xsl:template match="srx:literal[@datatype = '&xsd;dateTime']" mode="ac:DataTable">
                "Date(<xsl:value-of select="year-from-dateTime(.)"/>, <xsl:value-of select="month-from-dateTime(.) - 1"/>, <xsl:value-of select="day-from-dateTime(.)"/>, <xsl:value-of select="hours-from-dateTime(.)"/>, <xsl:value-of select="minutes-from-dateTime(.)"/>, <xsl:value-of select="seconds-from-dateTime(.)"/>)"
        </xsl:template>

        <xsl:template match="srx:literal[@datatype = '&xsd;time']" mode="ac:DataTable">
                [ <xsl:value-of select="substring(., 1, 2)" />, <xsl:value-of select="substring(., 4, 2)" />, <xsl:value-of select="substring(., 7, 2)" />
                <xsl:if test="contains(., '.')">
                    , <xsl:value-of select="substring(substring-after(., '.'), 1, 3)" />
                </xsl:if>
                ]
        </xsl:template>

        <xsl:template match="srx:literal[@datatype = '&xsd;string'] | srx:literal" mode="ac:DataTable">
                "<xsl:value-of select="replace(replace(replace(replace(replace(replace(., '\\', '\\\\'), '&quot;', '\\&quot;'), '/', '\\/'), '&#xA;', '\\n'), '&#xD;', '\\r'), '&#x9;', '\\t')"/>"
        </xsl:template>

        <xsl:template match="srx:uri | srx:bnode" mode="ac:DataTable">
                "<xsl:value-of select="."/>"
        </xsl:template>

</xsl:stylesheet>