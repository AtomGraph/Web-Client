<?xml version="1.0" encoding="UTF-8"?>
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
exclude-result-prefixes="owl rdf rdfs xsd sparql date">

	<xsl:output indent="no" omit-xml-declaration="yes" method="text" encoding="UTF-8" media-type="application/json"/>
	<xsl:strip-space elements="*"/>

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
				id: '<xsl:value-of select="generate-id()"/>', label: '<xsl:value-of select="@name"/>', type: 
				<xsl:choose>
					<xsl:when test="count(key('binding-by-name', @name)) = count(key('binding-by-name', @name)[string(number(sparql:literal)) != 'NaN'])">
					'number'
					</xsl:when>
					<xsl:when test="count(key('binding-by-name', @name)) = count(key('binding-by-name', @name)[date:date(sparql:literal) = sparql:literal])">
					'date'
					</xsl:when>
					<xsl:otherwise>'string'</xsl:otherwise>
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

	<!-- string -->
	<xsl:template match="sparql:literal" mode="sparql2wire">
		<xsl:choose>
			<xsl:when test="count(key('binding-by-name', ../@name)) = count(key('binding-by-name', ../@name)[string(number(sparql:literal)) != 'NaN'])">
				<xsl:value-of select="."/>
			</xsl:when>
			<xsl:when test="count(key('binding-by-name', ../@name)) = count(key('binding-by-name', ../@name)[date:date(sparql:literal) = sparql:literal])">
				new Date(<xsl:value-of select="date:year(.)"/>, <xsl:value-of select="date:month-in-year(.)"/>, <xsl:value-of select="date:day-in-month(.)"/>, 0, 31, 26)
			</xsl:when>
			<xsl:otherwise>
				'<xsl:value-of select="."/>'			
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="sparql:uri" mode="sparql2wire">
		'<xsl:value-of select="."/>'
	</xsl:template>

</xsl:stylesheet>