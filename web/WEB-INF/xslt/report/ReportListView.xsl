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
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:sparql="&sparql;"
exclude-result-prefixes="#all">

        <xsl:include href="../FrontEndView.xsl"/>
        <xsl:include href="../query-string.xsl"/>
        <xsl:include href="../page-numbers.xsl"/>

        <xsl:param name="total-item-count" as="xs:integer"/>
        <xsl:param name="offset" select="0" as="xs:integer"/>
        <xsl:param name="limit" select="20" as="xs:integer"/>
        <xsl:param name="order-by" as="xs:string"/>
        <xsl:param name="desc-default" select="true()" as="xs:boolean"/>
        <xsl:param name="desc" select="$desc-default" as="xs:boolean"/>

	<xsl:variable name="reports" select="document('arg://reports')" as="document-node()"/>
        <xsl:variable name="endpoints" select="document('arg://endpoints')" as="document-node()"/>
        <!-- <xsl:variable name="query-objects" select="document('arg://query-objects')" as="document-node()"/> -->
	<xsl:variable name="query-uris" select="document('arg://query-uris')" as="document-node()"/>

	<xsl:template name="title">
		Semantic Reports: Reports
	</xsl:template>

	<xsl:template name="head">
            <title>
                <xsl:call-template name="title"/>
            </title>
        </xsl:template>

	<xsl:template name="body-onload">
        </xsl:template>

	<xsl:template name="content">
		<div id="main">
			<h2>
                            <xsl:call-template name="title"/>
                        </h2>

			<form action="{$resource//sparql:binding[@name = 'resource']/sparql:uri}" method="get" accept-charset="UTF-8">
				<p>
					<button type="submit" name="view" value="create">Create</button>
				</p>
			</form>

                        <xsl:call-template name="sort-paging-controls">
                            <xsl:with-param name="uri" select="'reports'"/>
                            <xsl:with-param name="item-count-param" select="$total-item-count"/>
                            <xsl:with-param name="offset-param" select="$offset"/>
                            <xsl:with-param name="limit-param" select="$limit"/>
                            <xsl:with-param name="order-by-param" select="$order-by"/>
                            <xsl:with-param name="desc-param" select="$desc"/>
                            <xsl:with-param name="desc-default-param" select="$desc-default"/>
                        </xsl:call-template>

			<table>
				<thead>
					<td>Title</td>
					<td>Description</td>
					<td>Used URIs</td>
					<td>Endpoint</td>
					<td>Creator</td>
					<td>Created</td>
					<td>Modified</td>
                                </thead>
				<tbody>
					<xsl:apply-templates select="$reports" mode="report-table"/>
				</tbody>
			</table>

                        <xsl:call-template name="sort-paging-controls">
                            <xsl:with-param name="uri" select="'reports'"/>
                            <xsl:with-param name="item-count-param" select="$total-item-count"/>
                            <xsl:with-param name="offset-param" select="$offset"/>
                            <xsl:with-param name="limit-param" select="$limit"/>
                            <xsl:with-param name="order-by-param" select="$order-by"/>
                            <xsl:with-param name="desc-param" select="$desc"/>
                            <xsl:with-param name="desc-default-param" select="$desc-default"/>
                        </xsl:call-template>
		</div>
	</xsl:template>

	<xsl:template match="sparql:result" mode="report-table">
		<tr>
			<td>
				<a href="{sparql:binding[@name = 'report']/sparql:uri}">
					<xsl:value-of select="sparql:binding[@name = 'title']/sparql:literal"/>
				</a>
			</td>
			<td>
				<xsl:value-of select="sparql:binding[@name = 'description']/sparql:literal"/>
			</td>
			<td>
                            <xsl:variable name="current-uris" select="$query-uris//sparql:result[sparql:binding[@name = 'report']/sparql:uri = current()/sparql:binding[@name = 'report']/sparql:uri]"/>
                            <xsl:if test="$current-uris">
                                <ul>
                                    <xsl:apply-templates select="$current-uris" mode="uri-list-item"/>
                                </ul>
                             </xsl:if>
			</td>
			<td>
				<a href="{sparql:binding[@name = 'endpoint']/sparql:uri}?query={encode-for-uri(sparql:binding[@name = 'queryString']/sparql:literal)}">
                                    <xsl:variable name="endpoint" select="$endpoints//sparql:result[sparql:binding[@name = 'endpoint']/sparql:uri = current()/sparql:binding[@name = 'endpoint']/sparql:uri]"/>
                                    <xsl:choose>
                                        <xsl:when test="$endpoint">
                                            <xsl:value-of select="$endpoint/sparql:binding[@name = 'title']/sparql:literal"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="sparql:binding[@name = 'endpoint']/sparql:uri"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
				</a>
			</td>
			<td>
                                <a href="{sparql:binding[@name = 'creator']/sparql:uri}">
                                    <xsl:value-of select="sparql:binding[@name = 'creatorName']/sparql:literal"/>
                                </a>
			</td>
			<td>
				<xsl:value-of select="sparql:binding[@name = 'dateCreated']/sparql:literal"/>
			</td>
			<td>
				<xsl:value-of select="sparql:binding[@name = 'dateModified']/sparql:literal"/>
			</td>
		</tr>
	</xsl:template>

        <xsl:template match="sparql:result[sparql:binding[@name = 'uri']]" mode="uri-list-item">
            <xsl:variable name="uri" select="sparql:binding[@name = 'uri']/sparql:uri" as="xs:anyURI"/>
            <xsl:variable name="uri-label" as="xs:string">
                <xsl:choose>
                    <xsl:when test="starts-with($uri, '&rdf;')">
                        <xsl:value-of select="concat('rdf:', substring-after($uri, '&rdf;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&rdfs;')">
                        <xsl:value-of select="concat('rdfs:', substring-after($uri, '&rdfs;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&owl;')">
                        <xsl:value-of select="concat('owl:', substring-after($uri, '&owl;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&dc;')">
                        <xsl:value-of select="concat('dc:', substring-after($uri, '&dc;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&foaf;')">
                        <xsl:value-of select="concat('foaf:', substring-after($uri, '&foaf;'))"/>
                    </xsl:when>
		    <xsl:when test="starts-with($uri, '&skos;')">
                        <xsl:value-of select="concat('skos:', substring-after($uri, '&skos;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&sioc;')">
                        <xsl:value-of select="concat('sioc:', substring-after($uri, '&sioc;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&geo;')">
                        <xsl:value-of select="concat('geo:', substring-after($uri, '&geo;'))"/>
                    </xsl:when>
		    <xsl:when test="starts-with($uri, '&category;')">
                        <xsl:value-of select="concat('category:', substring-after($uri, '&category;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&dbpedia;')">
                        <xsl:value-of select="concat('dbpedia:', substring-after($uri, '&dbpedia;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&dbpedia-owl;')">
                        <xsl:value-of select="concat('dbpedia-owl:', substring-after($uri, '&dbpedia-owl;'))"/>
                    </xsl:when>
                    <xsl:when test="starts-with($uri, '&dbpprop;')">
                        <xsl:value-of select="concat('dbpprop:', substring-after($uri, '&dbpprop;'))"/>
                    </xsl:when>
		    <xsl:otherwise>
                        <xsl:value-of select="$uri"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <li>
                <a href="{$uri}">
                    <xsl:value-of select="$uri-label"/>
                </a>
            </li>
        </xsl:template>

</xsl:stylesheet>