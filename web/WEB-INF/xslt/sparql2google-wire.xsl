<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of Graphity SemanticReports package.
Copyright (C) 2009-2011  Martynas Jusevičius

SemanticReports is free software: you can redistribute it and/or modify
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
	<!ENTITY dom "http://www.itu.dk/people/martynas/Thesis/whatsup.owl#">
	<!ENTITY sys "http://www.xml.lt/system-ont.owl#">
	<!ENTITY geo "http://www.w3.org/2003/01/geo/wgs84_pos#">
	<!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
]>
<xsl:stylesheet version="1.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:owl="&owl;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:sparql="&sparql;"
xmlns:date="http://exslt.org/dates-and-times"
exclude-result-prefixes="#all">

	<xsl:import href="markup.xsl" />

	<xsl:output indent="no" omit-xml-declaration="yes" method="text" encoding="UTF-8" media-type="application/json"/>
	<xsl:strip-space elements="*"/>
	
	<xsl:param name="special-json-chars" select="document('special-json-chars.xml')//character"/>

	<xsl:key name="binding-by-name" match="sparql:binding" use="@name"/> 
	<xsl:variable name="numeric-variables" select="sparql:variable[count(key('binding-by-name', @name)) = count(key('binding-by-name', @name)[string(number(sparql:literal)) != 'NaN'])]"/> 

	<!-- 
	http://dbpedia.org/sparql/?query=PREFIX+rdf%3A+<http%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23>%0D%0APREFIX+rdfs%3A+<http%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23>%0D%0APREFIX+owl%3A+<http%3A%2F%2Fwww.w3.org%2F2002%2F07%2Fowl%23>%0D%0APREFIX+xsd%3A+<http%3A%2F%2Fwww.w3.org%2F2001%2FXMLSchema%23>%0D%0A%0D%0ASELECT+DISTINCT+*%0D%0AWHERE%0D%0A{%0D%0A%09%3Fcompany+rdfs%3Alabel+%3Flabel+.%0D%0A%09%3Fcompany+<http%3A%2F%2Fdbpedia.org%2Fontology%2FnumberOfEmployees>+%3Femployees+.%0D%0A%09%3Fcompany+<http%3A%2F%2Fdbpedia.org%2Fontology%2Frevenue>+%3Frevenue%0D%0A%09FILTER+(DATATYPE(%3Frevenue)+%3D+<http%3A%2F%2Fdbpedia.org%2Fontology%2Feuro>)%0D%0A%09FILTER+(xsd%3Ainteger(%3Frevenue)+>+10000000000)%0D%0A%09FILTER+(xsd%3Ainteger(%3Femployees)+>+0)%0D%0A%09FILTER+(LANG(%3Flabel)+%3D+'en')%0D%0A}%0D%0A%23+ORDER+BY+DESC(xsd%3Ainteger(%3Frevenue))+DESC(xsd%3Ainteger(%3Femployees))%0D%0A&format=application/sparql-results+xml

	http://code.google.com/apis/visualization/documentation/reference.html#dataparam
	http://code.google.com/apis/visualization/documentation/dev/implementing_data_source.html#responseformat

	{
	  cols: [{id: 'A', label: 'NEW A', type: 'string'},
			 {id: 'B', label: 'B-label', type: 'number'},
			 {id: 'C', label: 'C-label', type: 'date'}
			],
	  rows: [{c:[{v: 'a'}, {v: 1.0, f: 'One'}, {v: new Date(2008, 1, 28, 0, 31, 26), f: '2/28/08 12:31 AM'}]},
			 {c:[{v: 'b'}, {v: 2.0, f: 'Two'}, {v: new Date(2008, 2, 30, 0, 31, 26), f: '3/30/08 12:31 AM'}]},
			 {c:[{v: 'c'}, {v: 3.0, f: 'Three'}, {v: new Date(2008, 3, 30, 0, 31, 26), f: '4/30/08 12:31 AM'}]}
			]
	}

	-->

	<xsl:template match="character" mode="markup">
		<xsl:param name="word" />
		<xsl:text>\</xsl:text>
		<xsl:value-of select="$word"/>
	</xsl:template>

	<xsl:template match="/" mode="sparql2wire">
		<xsl:apply-templates mode="sparql2wire"/>
	</xsl:template>
	
	<xsl:template match="sparql:sparql" mode="sparql2wire">
{
	cols: [ <xsl:apply-templates select="sparql:head/sparql:variable" mode="sparql2wire"/> ],
	rows: [ <xsl:apply-templates select="sparql:results/sparql:result" mode="sparql2wire"/> ]
}
	</xsl:template>

	<!--  DATA TABLE HEADER -->

	<!-- string -->
	<xsl:template match="sparql:variable" mode="sparql2wire">
			{
				id: "<xsl:value-of select="generate-id()"/>", label: "<xsl:value-of select="@name"/>", type: 
				<xsl:choose>
					<xsl:when test="count(key('binding-by-name', @name)) = count(key('binding-by-name', @name)[string(number(sparql:literal)) != 'NaN'])">
					"number"
					</xsl:when>
					<xsl:when test="count(key('binding-by-name', @name)) = count(key('binding-by-name', @name)[date:date(sparql:literal) = sparql:literal])">
					"date"
					</xsl:when>
					<xsl:otherwise>"string"</xsl:otherwise>
				</xsl:choose>
			}
		<xsl:if test="position() != last()">	,
		</xsl:if>
	</xsl:template>

	<!--  DATA TABLE ROW -->

	<xsl:template match="sparql:result" mode="sparql2wire">
	{
		c: [ <xsl:apply-templates mode="sparql2wire"/> ]
	}
	<xsl:if test="position() != last()">,
	</xsl:if>
	</xsl:template>

	<!--  DATA TABLE CELLS -->

	<xsl:template match="sparql:binding" mode="sparql2wire">
			{
				v: <xsl:apply-templates mode="sparql2wire"/>
			}
		<xsl:if test="position() != last()">	,
		</xsl:if>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;boolean']" mode="sparql2wire">
		<xsl:value-of select="."/>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;integer'] | sparql:literal[@datatype = '&xsd;decimal'] | sparql:literal[@datatype = '&xsd;double'] | sparql:literal[@datatype = '&xsd;float']" mode="sparql2wire">
		<xsl:value-of select="."/>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;date']" mode="sparql2wire">
		new Date(<xsl:value-of select="date:year(.)"/>, <xsl:value-of select="date:month-in-year(.)"/>, <xsl:value-of select="date:day-in-month(.)"/>)
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;dateTime']" mode="sparql2wire">
		new Date(<xsl:value-of select="date:year(.)"/>, <xsl:value-of select="date:month-in-year(.)"/>, <xsl:value-of select="date:day-in-month(.)"/>, <xsl:value-of select="date:hour-in-day(.)"/>, <xsl:value-of select="date:minute-in-hour(.)"/>, <xsl:value-of select="date:second-in-minute(.)"/>)
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;time']" mode="sparql2wire">
		???
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;string'] | sparql:literal" mode="sparql2wire">
		<xsl:text>"</xsl:text>
		<xsl:call-template name="escape-bs-string">
			<xsl:with-param name="s" select="."/>
		</xsl:call-template>
		<xsl:text>"</xsl:text>
	</xsl:template>

	<xsl:template match="sparql:literal" mode="sparql2wire" priority="0.5">
		<xsl:choose>
			<xsl:when test="count(key('binding-by-name', ../@name)) = count(key('binding-by-name', ../@name)[string(number(sparql:literal)) != 'NaN'])">
				<xsl:value-of select="."/>
			</xsl:when>
			<xsl:when test="count(key('binding-by-name', ../@name)) = count(key('binding-by-name', ../@name)[date:date(sparql:literal) = sparql:literal])">
				new Date(<xsl:value-of select="date:year(.)"/>, <xsl:value-of select="date:month-in-year(.)"/>, <xsl:value-of select="date:day-in-month(.)"/>, 0, 31, 26)
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>"</xsl:text>
				<xsl:call-template name="escape-bs-string">
					<xsl:with-param name="s" select="."/>
				</xsl:call-template>
				<xsl:text>"</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!--
				<xsl:call-template name="markup">
					<xsl:with-param name="text" select="." />
					<xsl:with-param name="phrases" select="$special-json-chars" />
					<xsl:with-param name="first-only" select="false()" />
					<xsl:with-param name="words-only" select="false()" />
				</xsl:call-template>
	-->

	<!--
	<xsl:template match="sparql:uri" mode="sparql2wire">
		"<xsl:value-of select="."/>"
	</xsl:template>
	-->

	<xsl:template match="sparql:uri" mode="sparql2wire">
		'<a href="{.}"><xsl:value-of select="."/></a>'
	</xsl:template>

	<!-- Escape the backslash (\) before everything else. -->
	<xsl:template name="escape-bs-string">
		<xsl:param name="s"/>
		<xsl:choose>
			<xsl:when test="contains($s,'\')">
				<xsl:call-template name="escape-quot-string">
					<xsl:with-param name="s" select="concat(substring-before($s,'\'),'\\')"/>
				</xsl:call-template>
				<xsl:call-template name="escape-bs-string">
					<xsl:with-param name="s" select="substring-after($s,'\')"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="escape-quot-string">
					<xsl:with-param name="s" select="$s"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Escape the double quote ("). -->
	<xsl:template name="escape-quot-string">
		<xsl:param name="s"/>
		<xsl:choose>
			<xsl:when test="contains($s,'&quot;')">
				<xsl:call-template name="encode-string">
					<xsl:with-param name="s" select="concat(substring-before($s,'&quot;'),'\&quot;')"/>
				</xsl:call-template>
				<xsl:call-template name="escape-quot-string">
					<xsl:with-param name="s" select="substring-after($s,'&quot;')"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="encode-string">
					<xsl:with-param name="s" select="$s"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- Replace tab, line feed and/or carriage return by its matching escape code. Can't escape backslash
	or double quote here, because they don't replace characters (&#x0; becomes \t), but they prefix 
	characters (\ becomes \\). Besides, backslash should be seperate anyway, because it should be 
	processed first. This function can't do that. -->
	<xsl:template name="encode-string">
		<xsl:param name="s"/>
		<xsl:choose>
			<!-- tab -->
			<xsl:when test="contains($s,'&#x9;')">
				<xsl:call-template name="encode-string">
					<xsl:with-param name="s" select="concat(substring-before($s,'&#x9;'),'\t',substring-after($s,'&#x9;'))"/>
				</xsl:call-template>
			</xsl:when>
			<!-- line feed -->
			<xsl:when test="contains($s,'&#xA;')">
				<xsl:call-template name="encode-string">
					<xsl:with-param name="s" select="concat(substring-before($s,'&#xA;'),'\n',substring-after($s,'&#xA;'))"/>
				</xsl:call-template>
			</xsl:when>
			<!-- carriage return -->
			<xsl:when test="contains($s,'&#xD;')">
				<xsl:call-template name="encode-string">
					<xsl:with-param name="s" select="concat(substring-before($s,'&#xD;'),'\r',substring-after($s,'&#xD;'))"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise><xsl:value-of select="$s"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>