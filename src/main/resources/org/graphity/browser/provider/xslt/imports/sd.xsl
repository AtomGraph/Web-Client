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
    <!ENTITY g "http://graphity.org/ontology/">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY sd "http://www.w3.org/ns/sparql-service-description#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:sd="&sd;"
exclude-result-prefixes="g rdf rdfs sd">
    
    <xsl:template match="sd:endpoint/@rdf:resource">
	<a href="{$base-uri}sparql?endpoint-uri={encode-for-uri(.)}">
	    <xsl:value-of select="g:label(., /, $lang)"/>
	</a>
    </xsl:template>

    <xsl:template match="sd:Service[sd:endpoint/@rdf:resource]/@rdf:about | *[rdf:type/@rdf:resource = '&sd;Service'][sd:endpoint/@rdf:resource]/@rdf:about | sd:Service[sd:endpoint/@rdf:resource]/@rdf:nodeID | *[rdf:type/@rdf:resource = '&sd;Service'][sd:endpoint/@rdf:resource]/@rdf:nodeID ">
	<a href="{$base-uri}sparql?endpoint-uri={encode-for-uri(../sd:endpoint/@rdf:resource)}">
	    <xsl:value-of select="g:label(., /, $lang)"/>
	</a>
    </xsl:template>

</xsl:stylesheet>