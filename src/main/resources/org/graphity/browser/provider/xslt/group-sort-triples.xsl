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
<xsl:stylesheet version="1.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
xmlns:g="http://graphity.org/ontology/"
exclude-result-prefixes="xsl rdf g">

    <!-- groups and sorts triples in RDF/XML to ease further XSLT processing -->
    <xsl:output indent="yes" method="xml" encoding="UTF-8" media-type="application/rdf+xml"/>
    <xsl:strip-space elements="*"/>
  
    <!-- only match subjects (i.e. elements that have property children) -->
    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>

    <xsl:template match="rdf:RDF" mode="g:GroupTriples">
        <xsl:copy>
            <!-- URI resources -->
            <xsl:apply-templates select="*[@rdf:about][count(. | key('resources', @rdf:about)[1]) = 1]" mode="g:GroupTriples">
		<xsl:sort select="@rdf:about" data-type="text" order="ascending"/>
            </xsl:apply-templates>

            <!-- blank nodes -->
            <xsl:apply-templates select="*[@rdf:nodeID][count(. | key('resources', @rdf:nodeID)[1]) = 1]" mode="g:GroupTriples">
		<xsl:sort select="@rdf:nodeID" data-type="text" order="ascending"/> 
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>

    <!-- subject resource -->
    <xsl:template match="*[*][@rdf:about]" mode="g:GroupTriples">
	<xsl:copy>
	    <xsl:copy-of select="@*"/>
	    <xsl:for-each select="key('resources', @rdf:about)">
		<xsl:apply-templates mode="g:GroupTriples">
		    <xsl:sort select="concat(namespace-uri(.), local-name(.))" data-type="text" order="ascending"/>
		    <xsl:sort select="@rdf:resource" data-type="text" order="ascending"/>
		    <xsl:sort select="@rdf:nodeID" data-type="text" order="ascending"/>
		    <xsl:sort select="@rdf:datatype" data-type="text" order="ascending"/>
		    <xsl:sort select="@xml:lang" data-type="text" order="ascending"/>
		    <xsl:sort select="text()" data-type="text" order="ascending"/>		    
		</xsl:apply-templates>
	    </xsl:for-each>
	</xsl:copy>
    </xsl:template>

    <!-- subject blank node -->
    <xsl:template match="*[*][@rdf:nodeID]" mode="g:GroupTriples">
	<xsl:copy>
	    <xsl:copy-of select="@*"/>
	    <xsl:for-each select="key('resources', @rdf:nodeID)">
		<xsl:apply-templates mode="g:GroupTriples">
		    <xsl:sort select="concat(namespace-uri(.), local-name(.))" data-type="text" order="ascending"/>
		    <xsl:sort select="@rdf:resource" data-type="text" order="ascending"/>
		    <xsl:sort select="@rdf:nodeID" data-type="text" order="ascending"/>
		    <xsl:sort select="@rdf:datatype" data-type="text" order="ascending"/>
		    <xsl:sort select="@xml:lang" data-type="text" order="ascending"/>
		    <xsl:sort select="text()" data-type="text" order="ascending"/>
		</xsl:apply-templates>
	    </xsl:for-each>
	</xsl:copy>
    </xsl:template>

    <!-- property -->
    <xsl:template match="*[@rdf:about]/* | *[@rdf:nodeID]/*" mode="g:GroupTriples">
	<xsl:copy-of select="."/>
    </xsl:template>
    
</xsl:stylesheet>