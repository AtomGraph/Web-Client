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
<!DOCTYPE uridef[
	<!ENTITY owl "http://www.w3.org/2002/07/owl#">
	<!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
	<!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
	<!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
	<!ENTITY geo "http://www.w3.org/2003/01/geo/wgs84_pos#">
	<!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:owl="&owl;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:sparql="&sparql;"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:date="http://exslt.org/dates-and-times"
exclude-result-prefixes="xs">

	<xsl:output indent="no" omit-xml-declaration="yes" method="text" encoding="UTF-8" media-type="application/json"/>
	<xsl:strip-space elements="*"/>

	<xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
	<xsl:key name="properties" match="*[@rdf:about or @rdf:nodeID]/*" use="concat(namespace-uri(.), local-name(.))"/>

	<!-- 
	http://dbpedia.org/sparql/?query=PREFIX+rdf%3A+<http%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23>%0D%0APREFIX+rdfs%3A+<http%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23>%0D%0APREFIX+owl%3A+<http%3A%2F%2Fwww.w3.org%2F2002%2F07%2Fowl%23>%0D%0APREFIX+xsd%3A+<http%3A%2F%2Fwww.w3.org%2F2001%2FXMLSchema%23>%0D%0A%0D%0ASELECT+DISTINCT+*%0D%0AWHERE%0D%0A{%0D%0A%09%3Fcompany+rdfs%3Alabel+%3Flabel+.%0D%0A%09%3Fcompany+<http%3A%2F%2Fdbpedia.org%2Fontology%2FnumberOfEmployees>+%3Femployees+.%0D%0A%09%3Fcompany+<http%3A%2F%2Fdbpedia.org%2Fontology%2Frevenue>+%3Frevenue%0D%0A%09FILTER+(DATATYPE(%3Frevenue)+%3D+<http%3A%2F%2Fdbpedia.org%2Fontology%2Feuro>)%0D%0A%09FILTER+(xsd%3Ainteger(%3Frevenue)+>+10000000000)%0D%0A%09FILTER+(xsd%3Ainteger(%3Femployees)+>+0)%0D%0A%09FILTER+(LANG(%3Flabel)+%3D+'en')%0D%0A}%0D%0A%23+ORDER+BY+DESC(xsd%3Ainteger(%3Frevenue))+DESC(xsd%3Ainteger(%3Femployees))%0D%0A&format=application/sparql-results+xml

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

<!--
	<xsl:template match="/">
		<xsl:apply-templates mode="sparql2wire"/>
	</xsl:template>
-->

	<xsl:template match="/" mode="sparql2wire">
		<xsl:apply-templates mode="sparql2wire"/>
	</xsl:template>
	
	<xsl:template match="rdf:RDF" mode="sparql2wire">
{
	"cols": [
		{ 
			"id": "<xsl:value-of select="generate-id()"/>",
			"label": "Resource",
			"type": "string"
		},
		<xsl:for-each-group select="*/*" group-by="concat(namespace-uri(.), local-name(.))">
			<xsl:sort select="concat(namespace-uri(.), local-name(.))" data-type="text"/>

			<xsl:apply-templates select="current-group()[1]" mode="sparql2wire-cols"/>
		<xsl:if test="position() != last()">	,
		</xsl:if>

		</xsl:for-each-group>
	],
	"rows": [ <xsl:apply-templates mode="sparql2wire"/> ]
}
	</xsl:template>

	<!--  DATA TABLE HEADER -->
	<!-- properties -->

	<xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="sparql2wire-cols">
			{
				"id": "<xsl:value-of select="generate-id()"/>",
				"label": "<xsl:value-of select="concat(namespace-uri(.), local-name(.))"/>",
				"type": "string"
				<!--"<xsl:choose>
					<xsl:when test="every $literal in key('binding-by-name', @name)/sparql:literal satisfies number($literal)">number</xsl:when>
					<xsl:when test="every $literal in key('binding-by-name', @name)/sparql:literal satisfies ($literal castable as xs:dateTime)">date</xsl:when>
					<xsl:when test="every $literal in key('binding-by-name', @name)/sparql:literal satisfies ($literal castable as xs:time)">timeofday</xsl:when>
					<xsl:otherwise>string</xsl:otherwise></xsl:choose>" -->
			}
		<xsl:if test="position() != last()">	,
		</xsl:if>
	</xsl:template>

	<!--  DATA TABLE ROW -->
	<!-- subject -->

	<xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="sparql2wire">
	{
		"c": [
		{
		    "v": <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="sparql2wire"/>
		},
		<xsl:variable name="subject" select="."/>
		<xsl:for-each-group select="/rdf:RDF/*/*" group-by="concat(namespace-uri(.), local-name(.))">
		    <xsl:sort select="concat(namespace-uri(.), local-name(.))" data-type="text"/>
		    
		    <xsl:choose>
			    <xsl:when test="$subject/*[concat(namespace-uri(.), local-name(.)) = current-grouping-key()]">
				<xsl:apply-templates select="$subject/*[concat(namespace-uri(.), local-name(.)) = current-grouping-key()][1]" mode="sparql2wire"/>
			    </xsl:when>
			    <xsl:otherwise>
			    { "v": null }    
			    </xsl:otherwise>
		    </xsl:choose>

		    <xsl:if test="position() != last()">	,
		    </xsl:if>
		</xsl:for-each-group> ]

		<!-- "c": [ <xsl:apply-templates mode="sparql2wire"/> ] -->
	}
	<xsl:if test="position() != last()">,
	</xsl:if>
	</xsl:template>

	<!--  DATA TABLE CELLS -->
	<!-- properties -->

	<xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="sparql2wire">
			{
				"v": <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="sparql2wire"/>
			}
		<xsl:if test="position() != last()">	,
		</xsl:if>
	</xsl:template>
	
	<!--
	<xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="sparql2wire">
		<xsl:param name="subject"/>
		<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>

		<xsl:choose>
			<xsl:when test="$subject/*[concat(namespace-uri(.), local-name(.)) = $this]">
			{
				"v": <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="sparql2wire"/>
			}
			</xsl:when>
			<xsl:otherwise>
			{ "v": null }    
			</xsl:otherwise>
		</xsl:choose>
		
		<xsl:if test="position() != count($subject/*)">	,
		</xsl:if>
	</xsl:template>
	-->
	
	<xsl:template match="text()[../@rdf:datatype = '&xsd;boolean']" mode="sparql2wire">
		<xsl:value-of select="."/>
	</xsl:template>

	<xsl:template match="text()[../@rdf:datatype = '&xsd;integer'] | text()[../@rdf:datatype = '&xsd;decimal'] | text()[../@rdf:datatype = '&xsd;double'] | text()[../@rdf:datatype = '&xsd;float']" mode="sparql2wire">
		<xsl:value-of select="."/>
	</xsl:template>

	<xsl:template match="text()[../@rdf:datatype = '&xsd;date']" mode="sparql2wire">
		new Date(<xsl:value-of select="date:year(.)"/>, <xsl:value-of select="date:month-in-year(.)"/>, <xsl:value-of select="date:day-in-month(.)"/>)
	</xsl:template>

	<xsl:template match="text()[../@rdf:datatype = '&xsd;dateTime']" mode="sparql2wire">
		new Date(<xsl:value-of select="date:year(.)"/>, <xsl:value-of select="date:month-in-year(.)"/>, <xsl:value-of select="date:day-in-month(.)"/>, <xsl:value-of select="date:hour-in-day(.)"/>, <xsl:value-of select="date:minute-in-hour(.)"/>, <xsl:value-of select="date:second-in-minute(.)"/>)
	</xsl:template>

	<xsl:template match="text()[../@rdf:datatype = '&xsd;time']" mode="sparql2wire">
		[ <xsl:value-of select="substring(., 1, 2)" />, <xsl:value-of select="substring(., 4, 2)" />, <xsl:value-of select="substring(., 7, 2)" />
		<xsl:if test="contains(., '.')">
		    , <xsl:value-of select="substring(substring-after(., '.'), 1, 3)" />
		</xsl:if>
		]
	</xsl:template>

	<xsl:template match="text()[../@rdf:datatype = '&xsd;string'] | text()" mode="sparql2wire">
		<xsl:text>"</xsl:text>
	    <!-- <xsl:value-of select='replace(., "'", "\\'" )' /> -->
		<!-- <xsl:value-of select="replace(., '&quot;', '\\&quot;')"/> -->
		<xsl:value-of select="replace(replace(replace(replace(replace(replace(., '\\', '\\\\'), '&quot;', '\\&quot;'), '''', '\\'''), '&#x9;', '\\t'), '&#xA;', '\\n'), '&#xD;', '\\r')"/>
		
		<xsl:text>"</xsl:text>
	</xsl:template>

	<xsl:template match="node()[../@rdf:parseType = 'Literal']" mode="sparql2wire">
		<xsl:if test="position() = 1">
		    <xsl:text>"</xsl:text>
		</xsl:if>
	    <!-- <xsl:value-of select='replace(., "'", "\\'" )' /> -->

		<!-- <xsl:value-of select="replace(., '&quot;', '\\&quot;')"/> -->
		<xsl:value-of select="replace(replace(replace(replace(replace(replace(., '\\', '\\\\'), '&quot;', '\\&quot;'), '''', '\\'''), '&#x9;', '\\t'), '&#xA;', '\\n'), '&#xD;', '\\r')"/>
		
		<xsl:if test="position() = last()">
		    <xsl:text>"</xsl:text>
		</xsl:if>
	</xsl:template>

	<xsl:template match="@rdf:about | @rdf:resource" mode="sparql2wire">
		'&lt;a href=&quot;<xsl:value-of select="."/>&quot;&gt;<xsl:value-of select="."/>&lt;/a&gt;'
	</xsl:template>

	<xsl:template match="@rdf:nodeID" mode="sparql2wire">
		"<xsl:value-of select="."/>"
	</xsl:template>

</xsl:stylesheet>