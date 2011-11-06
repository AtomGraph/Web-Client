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

        <!-- To use xsl:import-schema, you need the schema-aware version of Saxon -->
        <!-- <xsl:import-schema namespace="&sparql;" schema-location="http://www.w3.org/TR/2007/CR-rdf-sparql-XMLres-20070925/result2.xsd"/> -->

	<xsl:param name="query-result" select="()" as="xs:boolean?"/>
	<xsl:param name="visualization-result" select="()" as="xs:boolean?"/>
	<xsl:param name="query-string" select="''" as="xs:string"/>
	<xsl:param name="create-view" select="'frontend.view.report.ReportCreateView'" as="xs:string"/>
	<xsl:param name="update-view" select="'frontend.view.report.ReportUpdateView'" as="xs:string"/>

	<xsl:param name="visualizations-json" as="xs:string"/>
	<xsl:param name="bindings-json" as="xs:string"/>
	<xsl:param name="variables-json" as="xs:string"/>

	<xsl:param name="visualization-types-json" as="xs:string"/>
	<xsl:param name="binding-types-json" as="xs:string"/>
	<xsl:param name="data-types-json" as="xs:string"/>
	<xsl:param name="option-types-json" as="xs:string"/>

        <xsl:variable name="report" as="document-node()?">
            <xsl:if test="($view = $create-view and not(empty($query-result))) or $view = $update-view">
                <xsl:sequence select="document('arg://report')"/> <!-- only set after $query-result -->
            </xsl:if>
        </xsl:variable>
	<xsl:variable name="report-uri" as="xs:anyURI">
            <xsl:choose>
                <xsl:when test="$report">
                    <xsl:value-of select="$report//sparql:binding[@name = 'report']/sparql:uri"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="concat($host-uri, 'reports/', id:generate())"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
	<xsl:variable name="query-uri" as="xs:anyURI">
            <xsl:choose>
                <xsl:when test="$report">
                    <xsl:value-of select="$report//sparql:binding[@name = 'query']/sparql:uri"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="concat($host-uri, 'queries/', id:generate())"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="endpoint-uri" select="$report//sparql:binding[@name = 'endpoint']/sparql:uri" as="xs:anyURI?"/>
        <!--
            <xsl:choose>
                <xsl:when test="not(empty($query-result)) or $view = $update-view">
                        <xsl:value-of select="$report//sparql:binding[@name = 'endpoint']/sparql:uri"/>
                </xsl:when>
                <xsl:otherwise>http://dbpedia.org/sparql</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        -->
        <xsl:variable name="results" select="document('arg://results')" as="document-node()?"/>
	<xsl:variable name="endpoints" select="document('arg://endpoints')" as="document-node()"/>
        <xsl:variable name="visualizations" select="document('arg://visualizations')" as="document-node()?"/> <!-- only set after $query-result -->
	<xsl:variable name="bindings" select="document('arg://bindings')" as="document-node()?"/>
	<xsl:variable name="options" select="document('arg://options')" as="document-node()?"/>
        <xsl:variable name="variables" select="document('arg://variables')" as="document-node()?"/>
        <xsl:variable name="visualization-types" select="document('arg://visualization-types')" as="document-node()"/>
        <xsl:variable name="binding-types" select="document('arg://binding-types')" as="document-node()"/>
        <xsl:variable name="option-types" select="document('arg://option-types')" as="document-node()"/>
        <xsl:variable name="data-types" select="document('arg://data-types')" as="document-node()"/>
        <xsl:variable name="visualization-ids" as="document-node()">
            <xsl:document>
                <ids xmlns="">
                    <xsl:for-each select="$visualization-types//sparql:result">
                        <id type="{sparql:binding[@name = 'type']/sparql:uri}">
                            <xsl:value-of select="id:generate()"/>
                        </id>
                    </xsl:for-each>
                </ids>
            </xsl:document>
        </xsl:variable>
        <xsl:variable name="binding-ids" as="document-node()">
            <xsl:document>
                <ids xmlns="">
                    <xsl:for-each select="$binding-types//sparql:result">
                        <id type="{sparql:binding[@name = 'type']/sparql:uri}">
                            <xsl:value-of select="id:generate()"/>
                        </id>
                    </xsl:for-each>
                </ids>
            </xsl:document>
        </xsl:variable>
	<xsl:variable name="endpoint-list-query" select="translate(unparsed-text('../../sparql/endpoint/list/endpoints.rq'), '&#10;&#13;', '  ')" as="xs:string"/>

        <xsl:key name="result-by-vis-type" match="sparql:result" use="sparql:binding[@name = 'visType']/sparql:uri"/>
        <xsl:key name="result-by-type" match="sparql:result" use="sparql:binding[@name = 'type']/sparql:uri"/>
        <xsl:key name="id-by-type" match="id" use="@type"/>
        <xsl:key name="endpoint-by-uri" match="sparql:result" use="sparql:binding[@name = 'endpoint']/sparql:uri"/>

	<xsl:template name="title">
            <xsl:choose>
                <xsl:when test="$view = $update-view"> <!-- ReportUpdateView -->
                    Edit report
                </xsl:when>
                <xsl:otherwise> <!-- ReportCreateView -->
                    Create report
                </xsl:otherwise>
            </xsl:choose>
	</xsl:template>

	<xsl:template name="head">
            <xsl:call-template name="report-scripts"/>
        </xsl:template>

	<xsl:template name="body-onload">
            <xsl:if test="$query-result eq true() or ($view = $update-view and not($query-result eq false()))">
                <xsl:attribute name="onload">
                    <xsl:text>Report.init(</xsl:text>
                    <xsl:value-of select="$visualization-types-json"/>
		    <xsl:text>, </xsl:text>
                    <xsl:value-of select="$binding-types-json"/>
		    <xsl:text>, </xsl:text>
		    <xsl:value-of select="$data-types-json"/>
		    <xsl:text>, </xsl:text>
		    <xsl:value-of select="$option-types-json"/>
		    <xsl:text>); </xsl:text>
		    <xsl:text>report = new Report(table, </xsl:text>
		    <xsl:value-of select="$visualizations-json"/>
		    <xsl:text>, </xsl:text>
		    <xsl:value-of select="$bindings-json"/>
		    <xsl:text>, </xsl:text>
		    <xsl:value-of select="$variables-json"/>
		    <xsl:text>, [], [</xsl:text>
                    <xsl:for-each select="$visualization-types//sparql:result">
			<xsl:text>{ 'element' :</xsl:text>
			<xsl:text>document.getElementById('</xsl:text>
			<xsl:value-of select="generate-id()"/>
			<xsl:text>-visualization')</xsl:text>
			<xsl:text>, 'visType' : '</xsl:text>
			<xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
			<xsl:text>' }</xsl:text>
			<xsl:if test="position() != last()">,</xsl:if>
		    </xsl:for-each>
		    <xsl:text>]); Report.bindInstances(report); report.filterSufficientVisualizations(); Report.setToggleElements(report, [</xsl:text>
		    <xsl:apply-templates select="$visualization-types//sparql:result" mode="vis-toggle-json"/>
		    <xsl:text>]); report.setFieldsetElements([</xsl:text>
		    <xsl:apply-templates select="$visualization-types//sparql:result" mode="vis-fieldset-json"/>
		    <xsl:text>]); report.setControls([</xsl:text>
		    <xsl:apply-templates select="$binding-types//sparql:result" mode="binding-element-json"/>
		    <xsl:text>]); </xsl:text>
		    <!-- create new or use saved variables -->
                    <xsl:if test="$view = $create-view">
			<xsl:text>report.createVariables();</xsl:text>
		    </xsl:if>
		    <!--
                    <xsl:if test="$view = $update-view">
			<xsl:text>report.setVariables(</xsl:text>
			<xsl:value-of select="$variables-json"/>
			<xsl:text>);</xsl:text>
		    </xsl:if>
		    -->

		    <xsl:text>report.showWithControls(); </xsl:text>
                </xsl:attribute>
             </xsl:if>
        </xsl:template>

	<xsl:template name="content">
		<div id="main">
			<h2>
                            <xsl:call-template name="title"/>
                        </h2>

<!--
<script type="text/javascript">
    function sparql()
    {
    alert("<xsl:value-of select="$endpoint-list-query"/>");
	var query = sparqler.createQuery();
	query.query("<xsl:value-of select="$endpoint-list-query"/>",
		{ failure: alert("failure!"), success: function(json) { alert(json); } }
	);
    }
</script>
<button onclick="sparql();">sparql</button>
-->
			<!--
			<xsl:copy-of select="$bindings"/>
			<xsl:copy-of select="$options"/>
			-->

                        <!-- /reports/?view=create#visualizations -->
			<form action="{$resource//sparql:binding[@name = 'resource']/sparql:uri}" method="post" accept-charset="UTF-8">
				<p>
                                        <input type="hidden" name="view">
                                            <xsl:attribute name="value">
                                                <xsl:if test="$view = $create-view">create</xsl:if>
                                                <xsl:if test="$view = $update-view">update</xsl:if>
                                            </xsl:attribute>
                                        </input>
                                        <input type="hidden" name="report-uri" value="{$report-uri}"/>
<input type="hidden" name="rdf"/>
<input type="hidden" name="v" value="&vis;"/>
<input type="hidden" name="n" value="rdf"/>
<input type="hidden" name="v" value="&rdf;"/>
<input type="hidden" name="n" value="rep"/>
<input type="hidden" name="v" value="&rep;"/>
<input type="hidden" name="n" value="dc"/>
<input type="hidden" name="v" value="&dc;"/>
<input type="hidden" name="n" value="spin"/>
<input type="hidden" name="v" value="&spin;"/>

<input type="hidden" name="su" value="{$report-uri}"/>
<input type="hidden" name="pu" value="&rdf;type"/>
<input type="hidden" name="ou" value="&rep;Report"/>
<input type="hidden" name="pu" value="&rep;query"/>
<!-- <input type="hidden" name="ob" value="query"/> -->
<input type="hidden" name="ou" value="{$query-uri}"/>

<!-- <input type="hidden" name="sb" value="query"/> -->
<input type="hidden" name="su" value="{$query-uri}"/>
<input type="hidden" name="pu" value="&rdf;type"/>
<input type="hidden" name="ou" value="&spin;Select"/>

					<label for="query-string">Query</label>
					<br/>
<input type="hidden" name="pu" value="&spin;text"/>
<input type="hidden" name="lt" value="&xsd;string"/>

					<textarea cols="80" rows="20" id="query-string" name="ol">
						<xsl:choose>
						    <xsl:when test="not(empty($query-result)) or $view = $update-view">
							    <xsl:value-of select="$report//sparql:binding[@name = 'queryString']/sparql:literal"/>
						    </xsl:when>
						    <xsl:otherwise>
<xsl:text>PREFIX rdf: &lt;&rdf;&gt;
PREFIX rdfs: &lt;&rdfs;&gt;
PREFIX owl: &lt;&owl;&gt;
PREFIX xsd: &lt;&xsd;&gt;</xsl:text>
<xsl:text>

SELECT DISTINCT *
WHERE
{
    ?s ?p ?o
}
LIMIT 50</xsl:text>

						    </xsl:otherwise>
						</xsl:choose>
					</textarea>
					<br/>
					<em>Number of results is limited to 50</em>

<!-- <input type="hidden" name="sb" value="query"/> -->
<input type="hidden" name="su" value="{$query-uri}"/>
<input type="hidden" name="pu" value="&spin;from"/>

                                    </p>
<script>
var existingEndpointIds = ['existing-endpoint-select'];
var newEndpointIds = new Array('new-endpoint-uri', 'new-endpoint-uri-hidden', 'endpoint-rdftype', 'endpoint-class', 'endpoint-dctitle', 'endpoint-titletype', 'new-endpoint-title');
</script>

                                    <fieldset>
                                        <legend>Endpoint</legend>
                                        <xsl:variable name="existing-endpoint" select="$endpoints//sparql:result[sparql:binding[@name = 'endpoint']] and (empty($query-result) or (not(empty($query-result)) and key('endpoint-by-uri', $endpoint-uri, $endpoints)))" as="xs:boolean"/>
                                        <xsl:if test="$endpoints//sparql:result[sparql:binding[@name = 'endpoint']]">
                                            <input type="radio" id="existing-endpoint-radio" name="endpoint-exists" value="true" onclick="document.getElementById('existing-endpoint-select').disabled = false; for (var i in newEndpointIds) document.getElementById(newEndpointIds[i]).disabled = true;">
                                                <xsl:if test="$existing-endpoint">
                                                    <xsl:attribute name="checked">checked</xsl:attribute>
                                                </xsl:if>
                                            </input>
                                            <label for="existing-endpoint-radio">Existing</label>
                                            <xsl:text> </xsl:text>
                                            <select id="existing-endpoint-select" name="ou" onchange="document.getElementById('new-endpoint-uri-hidden').value = this.value">
                                                <xsl:if test="not($existing-endpoint)">
                                                    <xsl:attribute name="disabled">disabled</xsl:attribute>
                                                </xsl:if>
                                                <xsl:apply-templates select="$endpoints//sparql:result" mode="endpoint-option">
                                                    <xsl:with-param name="selected-uri" select="$endpoint-uri"/>
                                                </xsl:apply-templates>
                                            </select>
                                            <br/>
                                        </xsl:if>

                                        <input type="radio" id="new-endpoint-radio" name="endpoint-exists" value="false" onclick="document.getElementById('existing-endpoint-select').disabled = true; for (var i in newEndpointIds) document.getElementById(newEndpointIds[i]).disabled = false;">
                                            <xsl:if test="not($existing-endpoint)">
                                                <xsl:attribute name="checked">checked</xsl:attribute>
                                            </xsl:if>
                                        </input>
                                        <label for="new-endpoint-radio">New</label>
                                        <xsl:text> </xsl:text>
                                        <label for="new-endpoint-uri">URI</label>
                                        <xsl:text> </xsl:text>
                                        <input type="text" id="new-endpoint-uri" name="ou" onchange="document.getElementById('new-endpoint-uri-hidden').value = this.value">
                                            <xsl:if test="$existing-endpoint">
                                                <xsl:attribute name="disabled">disabled</xsl:attribute>
                                            </xsl:if>
                                            <xsl:if test="not($existing-endpoint)">
                                                <xsl:attribute name="value">
                                                    <xsl:value-of select="$endpoint-uri"/>
                                                </xsl:attribute>
                                            </xsl:if>
                                        </input>

<input type="hidden" name="su" value="{$endpoint-uri}" id="new-endpoint-uri-hidden">
    <xsl:if test="$existing-endpoint">
        <xsl:attribute name="disabled">disabled</xsl:attribute>
    </xsl:if>
</input>
<input type="hidden" name="pu" value="&rdf;type" id="endpoint-rdftype">
    <xsl:if test="$existing-endpoint">
        <xsl:attribute name="disabled">disabled</xsl:attribute>
    </xsl:if>
</input>
<input type="hidden" name="ou" value="&rep;Endpoint" id="endpoint-class">
    <xsl:if test="$existing-endpoint">
        <xsl:attribute name="disabled">disabled</xsl:attribute>
    </xsl:if>
</input>
<input type="hidden" name="pu" value="&dc;title" id="endpoint-dctitle">
    <xsl:if test="$existing-endpoint">
        <xsl:attribute name="disabled">disabled</xsl:attribute>
    </xsl:if>
</input>
<input type="hidden" name="lt" value="&xsd;string" id="endpoint-titletype">
    <xsl:if test="$existing-endpoint">
        <xsl:attribute name="disabled">disabled</xsl:attribute>
    </xsl:if>
</input>

                                        <xsl:text> </xsl:text>
                                        <label for="new-endpoint-title">Title</label>
                                        <xsl:text> </xsl:text>
                                        <input type="text" id="new-endpoint-title" name="ol">
                                            <xsl:if test="$existing-endpoint">
                                                <xsl:attribute name="disabled">disabled</xsl:attribute>
                                            </xsl:if>
                                            <xsl:if test="not($existing-endpoint)">
                                                <xsl:attribute name="value">
                                                    <xsl:value-of select="$report//sparql:binding[@name = 'endpointTitle']/sparql:literal"/>
                                                </xsl:attribute>
                                            </xsl:if>
                                        </input>
                                    </fieldset>

                                    <p>
					<button type="submit" name="action" value="query">Query</button>
                                    </p>

                                <xsl:if test="$query-result eq false()">
                                    <ul id="errors">
					<xsl:apply-templates select="document('arg://query-errors')//sparql:binding" mode="error"/>
                                    </ul>
                                </xsl:if>

				<xsl:choose>
				    <xsl:when test="$query-result eq true() or ($view = $update-view and not($query-result eq false()))">
					<fieldset>
					    <legend>Metadata</legend>
					    <label for="title">Title</label>
					    <xsl:text> </xsl:text>

<input type="hidden" name="su" value="{$report-uri}"/>
<input type="hidden" name="pu" value="&dc;title"/>
<input type="hidden" name="lt" value="&xsd;string"/>

					    <input type="text" id="title" name="ol">
						<xsl:attribute name="value">
						    <xsl:if test="not(empty($query-result)) or $view = $update-view">
							    <xsl:value-of select="$report//sparql:binding[@name = 'title']/sparql:literal"/>
						    </xsl:if>
						</xsl:attribute>
					    </input>
					    <br/>
					    <label for="description">Description</label>
					    <br/>

<input type="hidden" name="su" value="{$report-uri}"/>
<input type="hidden" name="pu" value="&dc;description"/>
<input type="hidden" name="lt" value="&xsd;string"/>

					    <textarea id="description" name="ol" cols="80" rows="5" >
						<xsl:if test="not(empty($query-result)) or $view = $update-view">
							<xsl:value-of select="$report//sparql:binding[@name = 'description']/sparql:literal"/>
						</xsl:if>
					    </textarea>
					</fieldset>

					<fieldset id="visualizations">
						<legend>Visualizations</legend>

						<ul id="vis-types">
							<xsl:apply-templates select="$visualization-types" mode="vis-type-item"/>
						</ul>
					</fieldset>

					<p>
					    <xsl:if test="$view = $create-view and $query-result eq true()">
						<button type="submit" name="action" value="save">Save</button>
					    </xsl:if>
					    <xsl:if test="$view = $update-view">
						<button type="submit" name="action" value="update">Save</button>
					    </xsl:if>
					</p>

					<xsl:apply-templates select="$visualization-types" mode="vis-type-fieldset"/>
				    </xsl:when>
				    <xsl:otherwise>
					<xsl:apply-templates select="$visualization-types" mode="vis-type-inputs"/>
				    </xsl:otherwise>
				</xsl:choose>
			</form>
		</div>
	</xsl:template>

	<xsl:template match="sparql:result[sparql:binding[@name = 'type']]" mode="vis-type-inputs">
                <xsl:variable name="visualization-uri" select="xs:anyURI(concat($host-uri, 'visualizations/', key('id-by-type', sparql:binding[@name = 'type']/sparql:uri, $visualization-ids)))" as="xs:anyURI"/>

<input type="hidden" name="su" value="{$report-uri}"/>
<input type="hidden" name="pu" value="&rep;visualizedBy"/>
<input type="hidden" name="ou" value="{$visualization-uri}"/>

<input type="hidden" name="su" value="{$visualization-uri}"/>
<input type="hidden" name="pu" value="&rdf;type"/>
<input type="hidden" name="ou" value="{sparql:binding[@name = 'type']/sparql:uri}"/>

		<xsl:apply-templates select="key('result-by-vis-type', sparql:binding[@name = 'type']/sparql:uri, $binding-types)"  mode="binding-type-inputs">
		    <xsl:with-param name="visualization-uri" select="$visualization-uri"/>
		</xsl:apply-templates>

		<xsl:apply-templates select="key('result-by-vis-type', sparql:binding[@name = 'type']/sparql:uri, $option-types)"  mode="option-type-inputs">
		    <xsl:with-param name="visualization-uri" select="$visualization-uri"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="sparql:result[sparql:binding[@name = 'type']]" mode="vis-type-item">
                <!-- visualization of this type (might be non-existing) -->
                <xsl:variable name="visualization" select="key('result-by-type', sparql:binding[@name = 'type']/sparql:uri, $visualizations)"/>
                <!-- reuse existing visualization URI or generate a new one -->
                <xsl:variable name="visualization-uri" as="xs:anyURI">
                    <xsl:choose>
                        <xsl:when test="$visualization">
                            <xsl:value-of select="$visualization/sparql:binding[@name = 'visualization']/sparql:uri"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="concat($host-uri, 'visualizations/', key('id-by-type', sparql:binding[@name = 'type']/sparql:uri, $visualization-ids))"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <li>

<input type="hidden" name="su" value="{$report-uri}"/>
<input type="hidden" name="pu" value="&rep;visualizedBy"/>
<input type="checkbox" name="ou" value="{$visualization-uri}" id="{generate-id()}-toggle">
    <!--
    <xsl:attribute name="onchange">
	<xsl:text>report.toggleVisualization(</xsl:text>
	<xsl:apply-templates select="." mode="vis-type-json"/>
	<xsl:text>, this.checked);</xsl:text>
    </xsl:attribute>
    -->

    <xsl:choose>
        <xsl:when test="$view = $update-view"> <!-- ReportUpdateView -->
            <xsl:if test="$visualization">
                <xsl:attribute name="checked">checked</xsl:attribute>
            </xsl:if>
        </xsl:when>
        <xsl:otherwise> <!-- ReportCreateView -->
            <!-- <xsl:attribute name="checked">checked</xsl:attribute> -->
        </xsl:otherwise>
    </xsl:choose>
</input>

                        <!-- <input type="checkbox" id="{generate-id()}-toggle" name="visualization" value="{sparql:binding[@name = 'type']/sparql:uri}" checked="checked" onchange="toggleVisualization(document.getElementById('{generate-id()}-visualization'), document.getElementById('{generate-id()}-controls'), this.checked);"/> -->
			<label for="{generate-id()}-toggle">
				<xsl:value-of select="sparql:binding[@name = 'label']/sparql:literal"/>
			</label>
		</li>
	</xsl:template>

        <xsl:template match="sparql:result[sparql:binding[@name = 'type']]" mode="vis-type-fieldset">
                <!-- visualization of this type (might be non-existing) -->
                <xsl:variable name="visualization" select="key('result-by-type', sparql:binding[@name = 'type']/sparql:uri, $visualizations)"/>
                <!-- reuse existing visualization URI or generate a new one -->
                <xsl:variable name="visualization-uri" as="xs:anyURI">
                    <xsl:choose>
                        <xsl:when test="$visualization">
                            <xsl:value-of select="$visualization/sparql:binding[@name = 'visualization']/sparql:uri"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="concat($host-uri, 'visualizations/', key('id-by-type', sparql:binding[@name = 'type']/sparql:uri, $visualization-ids))"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <fieldset id="{generate-id()}-controls">
                        <legend>
                            <xsl:value-of select="sparql:binding[@name = 'label']/sparql:literal"/>
                        </legend>
                        <p>
<input type="hidden" name="su" value="{$visualization-uri}"/>
<input type="hidden" name="pu" value="&rdf;type"/>
<input type="hidden" name="ou" value="{sparql:binding[@name = 'type']/sparql:uri}"/>

                            <xsl:apply-templates select="key('result-by-vis-type', sparql:binding[@name = 'type']/sparql:uri, $binding-types)"  mode="binding-type-select">
                                <xsl:with-param name="visualization-uri" select="$visualization-uri"/>
                                <xsl:with-param name="visualization-type" select="."/>
				<xsl:with-param name="visualization" select="$visualization"/>
                            </xsl:apply-templates>

                            <xsl:apply-templates select="key('result-by-vis-type', sparql:binding[@name = 'type']/sparql:uri, $option-types)"  mode="option-type-input">
                                <xsl:with-param name="visualization-uri" select="$visualization-uri"/>
                                <xsl:with-param name="visualization-type" select="."/>
				<xsl:with-param name="visualization" select="$visualization"/>
                            </xsl:apply-templates>
                        </p>
                </fieldset>

        <div id="{generate-id()}-visualization" class="visualization">&#160;</div>
    </xsl:template>

    <xsl:template match="sparql:result[sparql:binding[@name = 'type']]" mode="binding-type-inputs">
        <xsl:param name="visualization-uri"/>
        <xsl:variable name="binding-uri" as="xs:anyURI" select="xs:anyURI(concat($host-uri, 'bindings/', key('id-by-type', sparql:binding[@name = 'type']/sparql:uri, $binding-ids)))"/>

<input type="hidden" name="su" value="{$binding-uri}"/>
<input type="hidden" name="pu" value="&rdf;type"/>
<input type="hidden" name="ou" value="{sparql:binding[@name = 'type']/sparql:uri}"/>

<input type="hidden" name="su" value="{$visualization-uri}"/>
<input type="hidden" name="pv" value="binding"/>
<input type="hidden" name="ou" value="{$binding-uri}"/>

<input type="hidden" name="su" value="{$binding-uri}"/> <!-- TO-DO: order, dataType?? -->
<input type="hidden" name="pv" value="dataType"/>
<input type="hidden" name="lt" value="&xsd;integer"/>
<input type="hidden" name="ol" value="{sparql:binding[@name = 'order']/sparql:literal}"/>
<!-- <xsl:if test="sparql:binding[@name = 'order']/sparql:literal"> -->
<input type="hidden" name="pv" value="order"/>
<input type="hidden" name="lt" value="&xsd;integer"/>
<input type="hidden" name="ol" value="{sparql:binding[@name = 'order']/sparql:literal}"/>
<!-- </xsl:if> -->

    </xsl:template>

    <xsl:template match="sparql:result[sparql:binding[@name = 'type']]" mode="binding-type-select">
        <xsl:param name="visualization-type"/>
	<xsl:param name="visualization"/>
        <xsl:param name="visualization-uri"/>
        <!-- binding of this type (might be non-existing) -->
        <xsl:variable name="binding" select="key('result-by-type', sparql:binding[@name = 'type']/sparql:uri, $bindings)"/>
        <!-- reuse existing binding URI or generate a new one -->
        <xsl:variable name="binding-uri" as="xs:anyURI">
            <xsl:choose>
                <xsl:when test="$binding">
                    <xsl:value-of select="$binding/sparql:binding[@name = 'binding']/sparql:uri"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="concat($host-uri, 'bindings/', key('id-by-type', sparql:binding[@name = 'type']/sparql:uri, $binding-ids))"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

<input type="hidden" name="su" value="{$binding-uri}"/>
<input type="hidden" name="pu" value="&rdf;type"/>
<input type="hidden" name="ou" value="{sparql:binding[@name = 'type']/sparql:uri}"/>

<input type="hidden" name="su" value="{$visualization-uri}"/>
<input type="hidden" name="pv" value="binding"/>
<input type="hidden" name="ou" value="{$binding-uri}"/>

<input type="hidden" name="su" value="{$binding-uri}"/> <!-- TO-DO: order, dataType?? -->
<xsl:if test="sparql:binding[@name = 'order']/sparql:literal">
    <input type="hidden" name="pv" value="order"/>
    <input type="hidden" name="lt" value="&xsd;integer"/>
    <input type="hidden" name="ol" value="{sparql:binding[@name = 'order']/sparql:literal}"/>
</xsl:if>
<!--
<input type="hidden" name="pv" value="dataType"/>
<input type="hidden" name="ou" value="{sparql:binding[@name = 'order']/sparql:literal}"/>
-->

<input type="hidden" name="pv" value="variableName"/>
<input type="hidden" name="lt" value="&xsd;string"/>

        <label for="{generate-id()}-binding">
            <xsl:value-of select="sparql:binding[@name = 'label']/sparql:literal"/>
        </label>
        <select id="{generate-id()}-binding" name="ol">
	    <!--
            <xsl:attribute name="onchange">
		<xsl:text>report.setVariables(report.getVariablesFromControls()); </xsl:text>
		<xsl:text>report.draw(objectByType('</xsl:text>
		<xsl:value-of select="$visualization-type//sparql:binding[@name = 'type']/sparql:uri"/>
		<xsl:text>', report.visualizations));</xsl:text>
            </xsl:attribute>
	    -->

            <xsl:text>&#160;</xsl:text>
        </select>
    </xsl:template>

    <xsl:template match="sparql:result[sparql:binding[@name = 'type']]" mode="option-type-inputs">
        <xsl:param name="visualization-uri" as="xs:anyURI"/>
        <xsl:variable name="option-uri" select="xs:anyURI(concat($host-uri, 'options/', id:generate()))" as="xs:anyURI"/>

<input type="hidden" name="su" value="{$option-uri}"/>
<input type="hidden" name="pu" value="&rdf;type"/>
<input type="hidden" name="ou" value="{sparql:binding[@name = 'type']/sparql:uri}"/>

<input type="hidden" name="su" value="{$visualization-uri}"/>
<input type="hidden" name="pv" value="option"/>
<input type="hidden" name="ou" value="{$option-uri}"/>

<input type="hidden" name="su" value="{$option-uri}"/>
<input type="hidden" name="pv" value="name"/>
<input type="hidden" name="lt" value="&xsd;string"/>
<input type="hidden" name="ol" value="{sparql:binding[@name = 'name']/sparql:literal}"/>
<!--
<input type="hidden" name="pv" value="value"/>
<input type="hidden" name="lt" value="&xsd;string"/>
<input type="hidden" name="ol" value="xxx" id="{generate-id()}-option"/>
-->

    </xsl:template>

    <xsl:template match="sparql:result[sparql:binding[@name = 'type']]" mode="option-type-input">
        <xsl:param name="visualization-type"/>
	<xsl:param name="visualization"/>
        <xsl:param name="visualization-uri" as="xs:anyURI"/>
        <!-- option of this type (might be non-existing) -->
        <xsl:variable name="option" select="key('result-by-type', sparql:binding[@name = 'type']/sparql:uri, $options)"/>
        <!-- reuse existing option URI or generate a new one -->
        <xsl:variable name="option-uri" as="xs:anyURI">
            <xsl:choose>
                <xsl:when test="$option"> <!-- ReportUpdateView -->
                    <xsl:value-of select="$option/sparql:binding[@name = 'option']/sparql:uri"/>
                </xsl:when>
                <xsl:otherwise> <!-- ReportCreateView -->
                    <xsl:value-of select="concat($host-uri, 'options/', id:generate())"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

<input type="hidden" name="su" value="{$option-uri}"/>
<input type="hidden" name="pu" value="&rdf;type"/>
<input type="hidden" name="ou" value="{sparql:binding[@name = 'type']/sparql:uri}"/>

<input type="hidden" name="su" value="{$visualization-uri}"/>
<input type="hidden" name="pv" value="option"/>
<input type="hidden" name="ou" value="{$option-uri}"/>

<input type="hidden" name="su" value="{$option-uri}"/>
<input type="hidden" name="pv" value="name"/>
<input type="hidden" name="lt" value="&xsd;string"/>
<input type="hidden" name="ol" value="{sparql:binding[@name = 'name']/sparql:literal}"/>
<input type="hidden" name="pv" value="value"/>
<input type="hidden" name="lt" value="&xsd;string"/>
<input type="hidden" name="ol" value="xxx" id="{generate-id()}-option"/> <!-- name??? -->

    </xsl:template>

    <xsl:template match="sparql:result[sparql:binding[@name = 'endpoint']]" mode="endpoint-option">
        <xsl:param name="selected-uri"/>
        <option value="{sparql:binding[@name = 'endpoint']/sparql:uri}">
            <xsl:if test="sparql:binding[@name = 'endpoint']/sparql:uri = $selected-uri">
                <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
            <xsl:if test="sparql:binding[@name = 'title']/sparql:literal">
                <xsl:value-of select="sparql:binding[@name = 'title']/sparql:literal"/>
                <xsl:text> (</xsl:text>
            </xsl:if>
            <xsl:value-of select="sparql:binding[@name = 'endpoint']/sparql:uri"/>
            <xsl:if test="sparql:binding[@name = 'title']/sparql:literal">
                <xsl:text>)</xsl:text>
            </xsl:if>
        </option>
    </xsl:template>

    <xsl:template match="sparql:result[sparql:binding[@name = 'error']]/sparql:binding" mode="error">
	<li>
	    <xsl:choose>
		<xsl:when test="sparql:uri = 'noResults'">Query returned no results</xsl:when>
		<xsl:when test="sparql:uri = 'invalidQuery'">Invalid query</xsl:when>
		<xsl:when test="sparql:uri = 'ioError'">I/O error (endpoint down?)</xsl:when>
		<xsl:otherwise>
		    <xsl:value-of select="."/>
		</xsl:otherwise>
	    </xsl:choose>
	</li>
    </xsl:template>

</xsl:stylesheet>