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
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java "http://xml.apache.org/xalan/java/">
    <!ENTITY gc "http://client.graphity.org/ontology#">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl "http://www.w3.org/2002/07/owl#">
    <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY geo "http://www.w3.org/2003/01/geo/wgs84_pos#">
    <!ENTITY dbpedia-owl "http://dbpedia.org/ontology/">
    <!ENTITY dc "http://purl.org/dc/elements/1.1/">
    <!ENTITY dct "http://purl.org/dc/terms/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY sioc "http://rdfs.org/sioc/ns#">
    <!ENTITY sp "http://spinrdf.org/sp#">
    <!ENTITY sd "http://www.w3.org/ns/sparql-service-description#">
    <!ENTITY void "http://rdfs.org/ns/void#">
    <!ENTITY list "http://jena.hpl.hp.com/ARQ/list#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:sparql="&sparql;"
xmlns:geo="&geo;"
xmlns:dbpedia-owl="&dbpedia-owl;"
xmlns:dc="&dc;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:sp="&sp;"
xmlns:sd="&sd;"
xmlns:void="&void;"
xmlns:list="&list;"
xmlns:javaee="http://java.sun.com/xml/ns/javaee"
exclude-result-prefixes="#all">

    <xsl:import href="imports/local.xsl"/>
    <xsl:import href="imports/external.xsl"/>
    <xsl:import href="imports/dbpedia-owl.xsl"/>
    <xsl:import href="imports/dc.xsl"/>
    <xsl:import href="imports/dct.xsl"/>
    <xsl:import href="imports/doap.xsl"/>
    <xsl:import href="imports/foaf.xsl"/>
    <xsl:import href="imports/gr.xsl"/>
    <xsl:import href="imports/owl.xsl"/>
    <xsl:import href="imports/rdf.xsl"/>
    <xsl:import href="imports/rdfs.xsl"/>
    <xsl:import href="imports/sd.xsl"/>
    <xsl:import href="imports/sioc.xsl"/>
    <xsl:import href="imports/skos.xsl"/>
    <xsl:import href="imports/void.xsl"/>
    <xsl:import href="layout.xsl"/>

    <xsl:param name="uri" as="xs:anyURI?"/>

    <xsl:key name="resources-by-endpoint" match="*" use="void:sparqlEndpoint/@rdf:resource"/>

    <xsl:template match="rdf:RDF" mode="gc:TitleMode">
	<xsl:choose>
	    <xsl:when test="$uri">
		<xsl:apply-templates select="key('resources', $base-uri, $ont-model)" mode="gc:LabelMode"/>
		<xsl:text> - </xsl:text>
		<xsl:apply-templates select="key('resources', $uri)" mode="gc:LabelMode"/>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:apply-imports/>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <xsl:template match="/" mode="gc:HeaderMode">
	<button class="btn btn-navbar" onclick="if ($('#collapsing-navbar').hasClass('in')) $('#collapsing-navbar').removeClass('collapse in').height(0); else $('#collapsing-navbar').addClass('collapse in').height('auto');">
	    <span class="icon-bar"></span>
	    <span class="icon-bar"></span>
	    <span class="icon-bar"></span>
	</button>

	<a class="brand" href="{$base-uri}">
	    <xsl:for-each select="key('resources', $base-uri, $ont-model)">
		<img src="{foaf:logo/@rdf:resource}">
		    <xsl:attribute name="alt"><xsl:apply-templates select="." mode="gc:LabelMode"/></xsl:attribute>
		</img>
	    </xsl:for-each>
	</a>

	<div id="collapsing-navbar" class="nav-collapse collapse">
	    <form action="{$base-uri}" method="get" class="navbar-form pull-left" accept-charset="UTF-8"
		  onsubmit="if ($(this).find('input[name=uri]').val().indexOf('http://') == -1) {{ $(this).attr('action', 'resources/labelled'); $(this).find('input[name=uri]').attr('name', 'query'); return true; }}">
		<div class="input-append">
		    <xsl:choose>
			<xsl:when test="key('resources-by-type', '&void;Dataset', $ont-model)[void:uriSpace[starts-with($uri, .)]]">
			    <xsl:attribute name="class">input-prepend input-append</xsl:attribute>
			    <span class="add-on">
				<xsl:for-each select="key('resources-by-type', '&void;Dataset', $ont-model)[void:uriSpace[starts-with($uri, .)]]">
				    <xsl:choose>
					<xsl:when test="foaf:homepage/@rdf:resource">
                                            <xsl:apply-templates select="foaf:homepage/@rdf:resource" mode="gc:InlineMode"/>
					</xsl:when>
					<xsl:otherwise>
					    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:InlineMode"/>
					</xsl:otherwise>
				    </xsl:choose>
				</xsl:for-each>
			    </span>
			</xsl:when>
			<xsl:when test="key('resources', $absolute-path)/void:inDataset/@rdf:resource">
			    <xsl:attribute name="class">input-prepend input-append</xsl:attribute>
			    <span class="add-on">
				<xsl:for-each select="key('resources', key('resources', $absolute-path)/void:inDataset/@rdf:resource, $ont-model)">
				    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:InlineMode"/>
				</xsl:for-each>
			    </span>
			</xsl:when>
		    </xsl:choose>

		    <input type="text" name="uri" class="input-xxlarge">
			<xsl:if test="not(starts-with($uri, $base-uri))">
			    <xsl:attribute name="value">
				<xsl:value-of select="$uri"/>
			    </xsl:attribute>
			</xsl:if>
			<xsl:if test="$query">
			    <xsl:attribute name="value">
				<xsl:value-of select="$query"/>
			    </xsl:attribute>
			</xsl:if>
		    </input>
		    <button type="submit" class="btn btn-primary">Go</button>
		</div>
	    </form>

	    <xsl:if test="key('resources', $base-uri, $ont-model)/rdfs:isDefinedBy/@rdf:resource | key('resources', key('resources', $base-uri, $ont-model)/void:inDataset/@rdf:resource, $ont-model)/void:sparqlEndpoint/@rdf:resource">
		<ul class="nav pull-right">
		    <xsl:for-each select="key('resources', $base-uri, $ont-model)/rdfs:isDefinedBy/@rdf:resource | key('resources', key('resources', $base-uri, $ont-model)/void:inDataset/@rdf:resource, $ont-model)/void:sparqlEndpoint/@rdf:resource">
			<!-- <xsl:sort select="gc:label(.)" data-type="text" order="ascending" lang="{$lang}"/> -->
			<li>
			    <xsl:if test="gc:document-uri(.) = $absolute-path">
				<xsl:attribute name="class">active</xsl:attribute>
			    </xsl:if>
			    <xsl:apply-templates select="." mode="gc:InlineMode"/>
			</li>
		    </xsl:for-each>
		</ul>
	    </xsl:if>
	</div>
    </xsl:template>

    <xsl:template match="rdf:RDF">
	<xsl:choose>
	    <xsl:when test="$uri">
		<xsl:apply-templates select="key('resources', $uri)"/>

		<xsl:variable name="secondary-resources" select="*[not(@rdf:about = $uri)][not(key('predicates-by-object', @rdf:nodeID))]"/>
		<xsl:if test="$secondary-resources">
		    <xsl:apply-templates select="." mode="gc:ModeSelectMode">
			<xsl:with-param name="default-mode" select="xs:anyURI('&gc;PropertyListMode')" tunnel="yes"/>
		    </xsl:apply-templates>

		    <!-- apply all other URI resources -->
		    <xsl:apply-templates select="$secondary-resources">
			<xsl:with-param name="default-mode" select="xs:anyURI('&gc;PropertyListMode')" tunnel="yes"/>
		    </xsl:apply-templates>
		</xsl:if>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:apply-imports/>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <xsl:template match="@rdf:about" mode="gc:ModeSelectMode">
	<xsl:param name="default-mode" as="xs:anyURI" tunnel="yes"/>

	<xsl:choose>
	    <xsl:when test="$uri">
		<xsl:choose>
		    <xsl:when test=". = $default-mode">
			<a href="{$absolute-path}{gc:query-string($uri, ())}">
			    <xsl:apply-templates select=".." mode="gc:LabelMode"/>
			</a>		
		    </xsl:when>
		    <xsl:otherwise>
			<a href="{$absolute-path}{gc:query-string($uri, .)}">
			    <xsl:apply-templates select=".." mode="gc:LabelMode"/>
			</a>		
		    </xsl:otherwise>
		</xsl:choose>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:apply-imports/>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <xsl:template match="*[@rdf:about = resolve-uri('sparql', $base-uri)]" mode="gc:QueryFormMode">
	<p class="form-inline">
	    <label for="endpoint-select">SPARQL endpoint</label>
	    <xsl:text> </xsl:text>
	    <select id="endpoint-select" name="endpoint-uri" class="span6">
		<xsl:apply-templates select="key('resources-by-type', '&void;Dataset', $ont-model)[void:sparqlEndpoint/@rdf:resource]" mode="gc:QueryFormMode">
		    <xsl:sort select="gc:label(.)" order="ascending"/>
		</xsl:apply-templates>
	    </select>
	</p>
	
	<xsl:apply-imports/>
    </xsl:template>

    <xsl:template match="*[@rdf:about | @rdf:nodeID][void:sparqlEndpoint/@rdf:resource]" mode="gc:QueryFormMode">
	<option value="{void:sparqlEndpoint/@rdf:resource}">
	    <xsl:if test="$endpoint-uri = void:sparqlEndpoint/@rdf:resource">
		<xsl:attribute name="selected">selected</xsl:attribute>
	    </xsl:if>

	    <xsl:apply-templates select="." mode="gc:LabelMode"/>
	    [<xsl:value-of select="void:sparqlEndpoint/@rdf:resource"/>]
	</option>	
    </xsl:template>

    <xsl:template match="*[@rdf:about | @rdf:nodeID][void:sparqlEndpoint/@rdf:resource = resolve-uri('sparql', $base-uri)]" mode="gc:QueryFormMode" priority="1">
	<option value="{void:sparqlEndpoint/@rdf:resource}">
	    <xsl:if test="not($endpoint-uri) or $endpoint-uri = void:sparqlEndpoint/@rdf:resource">
		<xsl:attribute name="selected">selected</xsl:attribute>
	    </xsl:if>

	    <xsl:apply-templates select="." mode="gc:LabelMode"/>
	    [<xsl:value-of select="void:sparqlEndpoint/@rdf:resource"/>]
	</option>
    </xsl:template>

</xsl:stylesheet>