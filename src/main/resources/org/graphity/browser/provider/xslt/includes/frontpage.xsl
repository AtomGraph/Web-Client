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
    <!ENTITY g "http://graphity.org/ontology/">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl "http://www.w3.org/2002/07/owl#">
    <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY dct "http://purl.org/dc/terms/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY void "http://rdfs.org/ns/void#">
    <!ENTITY sd "http://www.w3.org/ns/sparql-service-description#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:void="&void;"
xmlns:sd="&sd;"
exclude-result-prefixes="xsl xhtml xs g rdf rdfs owl void sd">
    
    <!-- overrides default rendering -->
    <xsl:template match="rdf:RDF[$uri = $base-uri]">
	<div class="span2">
	</div>

	<div class="span8">
	    <h1 class="page-header">Explore Linked Data</h1>

	    <xsl:for-each-group select="*" group-ending-with="*[position() mod 2 = 0]">
		<xsl:sort select="void:triples" data-type="number" order="descending"/>
		<div class="row-fluid">
		    <xsl:apply-templates select="current-group()" mode="g:ListMode"/>
		</div>
	    </xsl:for-each-group>
	</div>
    </xsl:template>

    <xsl:template match="*[*][$uri = $base-uri]" mode="g:ListMode" priority="1">
	<div class="span6 well">
	    <xsl:if test="foaf:depiction/@rdf:resource or foaf:logo/@rdf:resource">
		<p>
		    <a href="{$base-uri}sparql?endpoint-uri={encode-for-uri(sd:endpoint/@rdf:resource)}">
			<img src="{foaf:depiction/@rdf:resource | foaf:logo/@rdf:resource}" alt="{g:label(@rdf:about | @rdf:nodeID, /, $lang)}"/>
		    </a>
		</p>
	    </xsl:if>
	    <h1>
		<xsl:apply-templates select="@rdf:about | @rdf:nodeID"/>
	    </h1>
	    <xsl:if test="rdf:type/@rdf:resource">
		<ul class="inline">
		    <xsl:for-each select="rdf:type/@rdf:resource">
			<xsl:sort select="g:label(., /, $lang)" data-type="text" order="ascending"/>
			<li>
			    <xsl:apply-templates select="."/>
			</li>
		    </xsl:for-each>
		</ul>
	    </xsl:if>
	    <xsl:if test="dct:description">
		<p>
		    <xsl:value-of select="dct:description"/>
		</p>
	    </xsl:if>
	    <p>
		<xsl:if test="void:exampleResource">
		    <a class="btn btn-primary" href="?uri={void:exampleResource[1]/@rdf:resource}">Example resource</a>
		</xsl:if>
	    </p>
	</div>
    </xsl:template>

</xsl:stylesheet>