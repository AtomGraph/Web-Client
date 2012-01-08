<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE uridef[
        <!ENTITY rep "http://www.semantic-web.dk/ontologies/semantic-reports/">
	<!ENTITY vis "http://code.google.com/apis/visualization/">
        <!ENTITY spin "http://spinrdf.org/sp#">
	<!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
	<!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
	<!ENTITY owl "http://www.w3.org/2002/07/owl#">
	<!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
	<!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
	<!ENTITY geo "http://www.w3.org/2003/01/geo/wgs84_pos#">
        <!ENTITY dc "http://purl.org/dc/elements/1.1/">
	<!ENTITY foaf "http://xmlns.com/foaf/0.1/">
	<!ENTITY sioc "http://rdfs.org/sioc/ns#">
        <!ENTITY skos "http://www.w3.org/2004/02/skos/core#">
        <!ENTITY dbpedia "http://dbpedia.org/resource/">
        <!ENTITY dbpedia-owl "http://dbpedia.org/ontology/">
        <!ENTITY dbpprop "http://dbpedia.org/property/">
        <!ENTITY category "http://dbpedia.org/resource/Category:">
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
xmlns:id="java:util.IDGenerator"
exclude-result-prefixes="#all">

	<xsl:import href="../sparql2google-wire.xsl"/>
	<xsl:import href="sparql2json.xsl"/>

	<xsl:include href="../FrontEndView.xsl"/>

	<xsl:variable name="bindings" select="document('arg://bindings')" as="document-node()"/>
	<xsl:variable name="variables" select="document('arg://variables')" as="document-node()"/>
	<xsl:variable name="binding-types" select="document('arg://binding-types')" as="document-node()"/>

        <xsl:key name="binding-type-by-vis-type" match="sparql:result" use="sparql:binding[@name = 'visType']/sparql:uri"/>
        <xsl:key name="result-by-visualization" match="sparql:result" use="sparql:binding[@name = 'visualization']/sparql:uri"/>

	<xsl:template name="title">
		<xsl:value-of select="$report//sparql:binding[@name = 'title']/sparql:literal"/>
	</xsl:template>

	<xsl:template name="head">
            <title>
                Semantic Reports: <xsl:call-template name="title"/>
            </title>

            <xsl:call-template name="report-scripts"/>
        </xsl:template>


	<xsl:template name="body-onload">
            <xsl:attribute name="onload">
		    <!--
                    <xsl:text>Report.init([ </xsl:text>
                    <xsl:apply-templates select="$visualization-types//sparql:result" mode="vis-type-json"/>
		    <xsl:text>], [</xsl:text>
                    <xsl:apply-templates select="$binding-types//sparql:result" mode="binding-type-json"/>
		    <xsl:text>], [</xsl:text>
		    <xsl:apply-templates select="$data-types//sparql:result" mode="data-type-json"/>
		    <xsl:text>]); </xsl:text>
		    -->
		    <xsl:text>report = new Report(table, [</xsl:text>
		    <xsl:apply-templates select="$visualizations//sparql:result" mode="visualization-json"/>
		    <xsl:text>], [</xsl:text>
		    <xsl:apply-templates select="$bindings//sparql:result" mode="binding-json"/>
		    <xsl:text>], [], [</xsl:text>
                    <xsl:for-each select="$visualizations//sparql:result">
			<xsl:text>{ 'element' :</xsl:text>
			<xsl:text>document.getElementById('</xsl:text>
			<xsl:value-of select="generate-id()"/>
			<xsl:text>-visualization')</xsl:text>
			<xsl:text>, 'visType' : '</xsl:text>
			<xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
			<xsl:text>' }</xsl:text>
			<xsl:if test="position() != last()">,</xsl:if>
		    </xsl:for-each>
		    <xsl:text>]); report.setVariables(</xsl:text>
		    <xsl:text>[</xsl:text>
		    <xsl:apply-templates select="$variables//sparql:result" mode="variable-json"/>
		    <xsl:text>]</xsl:text>

		    <xsl:text>); report.show();</xsl:text>
            </xsl:attribute>
        </xsl:template>
        
	<xsl:template name="content">
		<div id="main">
			<h2><xsl:call-template name="title"/></h2>

			<!-- <xsl:copy-of select="$report"/> -->
                        <!-- <xsl:copy-of select="$results"/> -->
			<!-- <xsl:copy-of select="$visualizations"/> -->
                        <!--
			<xsl:copy-of select="$query-objects"/>
			<xsl:copy-of select="$bindings"/>
                        <xsl:copy-of select="$variables"/>
                        -->

			<xsl:apply-templates select="$visualizations//sparql:result" mode="vis-container"/>
                </div>
	</xsl:template>

        <xsl:template match="sparql:result[sparql:binding[@name = 'visualization']]" mode="vis-container">
            <div id="{generate-id()}-visualization" class="visualization">&#160;</div>
        </xsl:template>

</xsl:stylesheet>