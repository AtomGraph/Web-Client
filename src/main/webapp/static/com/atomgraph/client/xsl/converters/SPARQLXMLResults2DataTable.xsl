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
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY ac     "http://atomgraph.com/ns/client#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:sparql="&sparql;"
xmlns:ac="&ac;"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:date="http://exslt.org/dates-and-times"
exclude-result-prefixes="#all">

        <xsl:output indent="no" omit-xml-declaration="yes" method="text" encoding="UTF-8" media-type="application/json"/>
        <xsl:strip-space elements="*"/>
        
        <xsl:key name="binding-by-name" match="sparql:binding" use="@name"/> 
        <xsl:variable name="numeric-variables" select="sparql:variable[count(key('binding-by-name', @name)) = count(key('binding-by-name', @name)[string(number(sparql:literal)) != 'NaN'])]"/> 

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
        
        <xsl:template match="sparql:sparql" mode="ac:DataTable">
{
        "cols": [ <xsl:apply-templates select="sparql:head/sparql:variable" mode="#current"/> ],
        "rows": [ <xsl:apply-templates select="sparql:results/sparql:result" mode="#current"/> ]
}
        </xsl:template>

        <!--  DATA TABLE HEADER -->

        <xsl:template match="sparql:variable" mode="ac:DataTable">
                        {
                                "id": "<xsl:value-of select="generate-id()"/>",
                                "label": "<xsl:value-of select="@name"/>",
                                "type":
                                "<xsl:choose>
                                        <xsl:when test="key('binding-by-name', @name)/sparql:uri">string</xsl:when>
                                        <xsl:when test="every $datatype in key('binding-by-name', @name)/sparql:literal/@datatype satisfies $datatype = ('&xsd;integer', '&xsd;decimal', '&xsd;double', '&xsd;float')">number</xsl:when>
                                        <xsl:when test="every $datatype in key('binding-by-name', @name)/sparql:literal/@datatype satisfies $datatype = ('&xsd;dateTime', '&xsd;date')">date</xsl:when>
                                        <xsl:when test="every $datatype in key('binding-by-name', @name)/sparql:literal/@datatype satisfies $datatype = '&xsd;time'">timeofday</xsl:when>
                                        <xsl:otherwise>string</xsl:otherwise></xsl:choose>"
                        }
                <xsl:if test="position() != last()">        ,
                </xsl:if>
        </xsl:template>

        <!--  DATA TABLE ROW -->

        <xsl:template match="sparql:result" mode="ac:DataTable">
        {
                "c": [ <xsl:apply-templates mode="#current"/> ]
        }
        <xsl:if test="position() != last()">,
        </xsl:if>
        </xsl:template>

        <!--  DATA TABLE CELLS -->

        <xsl:template match="sparql:binding" mode="ac:DataTable">
                        {
                                "v": <xsl:apply-templates mode="#current"/>
                        }
                <xsl:if test="position() != last()">        ,
                </xsl:if>
        </xsl:template>

        <xsl:template match="sparql:literal[@datatype = '&xsd;boolean']" mode="ac:DataTable">
                <xsl:value-of select="."/>
        </xsl:template>

        <xsl:template match="sparql:literal[@datatype = '&xsd;integer'] | sparql:literal[@datatype = '&xsd;decimal'] | sparql:literal[@datatype = '&xsd;double'] | sparql:literal[@datatype = '&xsd;float']" mode="ac:DataTable">
                <xsl:value-of select="."/>
        </xsl:template>

        <xsl:template match="sparql:literal[@datatype = '&xsd;date']" mode="ac:DataTable">
                new Date(<xsl:value-of select="date:year(.)"/>, <xsl:value-of select="date:month-in-year(.)"/>, <xsl:value-of select="date:day-in-month(.)"/>)
        </xsl:template>

        <xsl:template match="sparql:literal[@datatype = '&xsd;dateTime']" mode="ac:DataTable">
                new Date(<xsl:value-of select="date:year(.)"/>, <xsl:value-of select="date:month-in-year(.)"/>, <xsl:value-of select="date:day-in-month(.)"/>, <xsl:value-of select="date:hour-in-day(.)"/>, <xsl:value-of select="date:minute-in-hour(.)"/>, <xsl:value-of select="date:second-in-minute(.)"/>)
        </xsl:template>

        <xsl:template match="sparql:literal[@datatype = '&xsd;time']" mode="ac:DataTable">
                [ <xsl:value-of select="substring(., 1, 2)" />, <xsl:value-of select="substring(., 4, 2)" />, <xsl:value-of select="substring(., 7, 2)" />
                <xsl:if test="contains(., '.')">
                    , <xsl:value-of select="substring(substring-after(., '.'), 1, 3)" />
                </xsl:if>
                ]
        </xsl:template>

        <xsl:template match="sparql:literal[@datatype = '&xsd;string'] | sparql:literal" mode="ac:DataTable">
                "<xsl:value-of select="replace(replace(replace(replace(replace(replace(., '\\', '\\\\'), '&quot;', '\\&quot;'), '''', '\\'''),'&#x9;', '\\t'), '&#xA;', '\\n'), '&#xD;', '\\r')"/>"</xsl:text>
        </xsl:template>

        <xsl:template match="sparql:uri | sparql:bnode" mode="ac:DataTable">
                "<xsl:value-of select="."/>"
        </xsl:template>

</xsl:stylesheet>