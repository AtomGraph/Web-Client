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
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
xmlns:ac="https://w3id.org/atomgraph/client#"
exclude-result-prefixes="xsl rdf ac">

    <!-- groups and sorts triples in RDF/XML to ease further XSLT processing -->
    <xsl:output indent="yes" method="xml" encoding="UTF-8" media-type="application/rdf+xml"/>
    <xsl:strip-space elements="*"/>
  
    <!-- only match subjects (i.e. elements that have property children) -->
    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>

    <xsl:template match="/" mode="ac:GroupTriples">
        <xsl:copy>
            <xsl:apply-templates mode="ac:GroupTriples"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="rdf:RDF" mode="ac:GroupTriples">
        <xsl:copy>
            <!-- URI resources -->
            <xsl:apply-templates select="*[@rdf:about][count(. | key('resources', @rdf:about)[1]) = 1]" mode="ac:GroupTriples">
                <xsl:sort select="@rdf:about" data-type="text" order="ascending"/>
            </xsl:apply-templates>

            <!-- blank nodes -->
            <xsl:apply-templates select="*[@rdf:nodeID][count(. | key('resources', @rdf:nodeID)[1]) = 1]" mode="ac:GroupTriples">
                <xsl:sort select="@rdf:nodeID" data-type="text" order="ascending"/> 
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>

    <!-- subject resource -->
    <xsl:template match="*[*][@rdf:about]" mode="ac:GroupTriples">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:for-each select="key('resources', @rdf:about)">
                <xsl:apply-templates mode="ac:GroupTriples">
                    <xsl:sort select="concat(namespace-uri(), local-name())" data-type="text" order="ascending"/>
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
    <xsl:template match="*[*][@rdf:nodeID]" mode="ac:GroupTriples">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:for-each select="key('resources', @rdf:nodeID)">
                <xsl:apply-templates mode="ac:GroupTriples">
                    <xsl:sort select="concat(namespace-uri(), local-name())" data-type="text" order="ascending"/>
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
    <xsl:template match="*[@rdf:about]/* | *[@rdf:nodeID]/*" mode="ac:GroupTriples">
        <xsl:copy-of select="."/>
    </xsl:template>
    
</xsl:stylesheet>