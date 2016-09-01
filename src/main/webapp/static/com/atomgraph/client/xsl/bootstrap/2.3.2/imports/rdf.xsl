<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2012 Martynas Jusevičius <martynas@atomgraph.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
<!DOCTYPE xsl:stylesheet [
    <!ENTITY gc "http://atomgraph.com/client/ns#">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:bs2="http://graphity.org/xsl/bootstrap/2.3.2"
exclude-result-prefixes="#all">

    <xsl:template match="rdf:type" mode="bs2:PropertyList"/>

    <!--
    <xsl:template match="@rdf:resource" mode="bs2:TypeList" priority="1">
        <li>
	    <xsl:apply-templates select="." mode="xhtml:Anchor"/>
	</li>
    </xsl:template>
    -->

    <!-- necessary for hiding class description, which addClass() added to the constructor document -->
    <xsl:template match="rdf:type[key('resources', (@rdf:resource, @rdf:nodeID))]" mode="bs2:FormControl" priority="2">
        <xsl:apply-templates select="." mode="xhtml:Input">
            <xsl:with-param name="type" select="'hidden'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="#current">
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="traversed-ids" select="(., (@rdf:resource, @rdf:nodeID))" tunnel="yes"/>            
        </xsl:apply-templates>
        <xsl:apply-templates select="@xml:lang | @rdf:datatype" mode="#current">
            <xsl:with-param name="type" select="'hidden'"/>
        </xsl:apply-templates>
    </xsl:template>

</xsl:stylesheet>