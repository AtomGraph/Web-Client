<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2019 Martynas Jusevičius <martynas@atomgraph.com>

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
    <!ENTITY ac     "http://atomgraph.com/ns/client#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY geo    "http://www.w3.org/2003/01/geo/wgs84_pos#">
    <!ENTITY ldt    "https://www.w3.org/ns/ldt#">
]>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:ldt="&ldt;"
xmlns:geo="&geo;"
xmlns:bs2="http://graphity.org/xsl/bootstrap/2.3.2"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
exclude-result-prefixes="#all">

    <!-- ACTIONS MODE (Create/Edit buttons) -->

    <!-- <xsl:template match="rdf:RDF" mode="bs2:Actions">
        <xsl:apply-templates mode="#current"/>
    </xsl:template> -->
    
    <xsl:template match="*[@rdf:about]" mode="bs2:Actions" priority="1">
<!--        <div class="pull-right">
            <form action="{ac:document-uri(@rdf:about)}?_method=DELETE" method="post">
                <button class="btn btn-primary btn-delete" type="submit">
                    <xsl:apply-templates select="key('resources', '&ac;Delete', document('&ac;'))" mode="ac:label" use-when="system-property('xsl:product-name') = 'SAXON'"/>
                    <xsl:text use-when="system-property('xsl:product-name') = 'Saxon-CE'">Delete</xsl:text>  TO-DO: cache ontologies in localStorage 
                </button>
            </form>
        </div>

        <div class="pull-right">
            <a class="btn btn-primary" href="?uri={encode-for-uri(ac:document-uri(@rdf:about))}&amp;mode={encode-for-uri('&ac;EditMode')}">
                <xsl:apply-templates select="key('resources', '&ac;EditMode', document('&ac;'))" mode="ac:label" use-when="system-property('xsl:product-name') = 'SAXON'"/>
                <xsl:text use-when="system-property('xsl:product-name') = 'Saxon-CE'">Edit</xsl:text>  TO-DO: cache ontologies in localStorage 
            </a>
        </div>-->
    </xsl:template>
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:Actions"/>
    
    <!-- IMAGE MODE -->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:Image">
        <!--
        <xsl:variable name="images" as="element()*">
            <xsl:apply-templates mode="ac:image"/>
        </xsl:variable>
        <xsl:if test="$images">
            <div class="carousel slide">
                <div class="carousel-inner">
                    <xsl:for-each select="$images">
                        <div class="item">
                            <xsl:if test="position() = 1">
                                <xsl:attribute name="class">active item</xsl:attribute>
                            </xsl:if>
                            <xsl:copy-of select="."/>
                        </div>
                    </xsl:for-each>
                    <a class="carousel-control left" onclick="$(this).parents('.carousel').carousel('prev');">&#8249;</a>
                    <a class="carousel-control right" onclick="$(this).parents('.carousel').carousel('next');">&#8250;</a>
                </div>
            </div>
        </xsl:if>
        -->
    </xsl:template>
    
    <!-- TYPE MODE -->
        
    <xsl:template match="*[@rdf:about or @rdf:nodeID][rdf:type/@rdf:resource]" mode="bs2:TypeList" priority="1">
        <ul class="inline">
            <xsl:for-each select="rdf:type/@rdf:resource">
                <xsl:sort select="ac:object-label(.)" order="ascending"/> <!--  lang="{$ldt:lang}" -->
                <xsl:choose use-when="system-property('xsl:product-name') = 'SAXON'">
                    <xsl:when test="doc-available(ac:document-uri(.))">
                        <xsl:apply-templates select="key('resources', ., document(ac:document-uri(.)))" mode="bs2:TypeListItem"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="."/>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:value-of select="." use-when="system-property('xsl:product-name') = 'Saxon-CE'"/>
            </xsl:for-each>
        </ul>
    </xsl:template>

    <xsl:template match="*" mode="bs2:TypeList"/>

    <xsl:template match="*[@rdf:about]" mode="bs2:TypeListItem">
<!--        <li>
            <span title="{@rdf:about}" class="btn btn-type">
                <xsl:apply-templates select="." mode="xhtml:Anchor"/>
            </span>
        </li>-->
    </xsl:template>

    <!-- PROPERTY LIST MODE -->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:PropertyList">
<!--        <xsl:variable name="properties" as="element()*">
            <xsl:apply-templates mode="#current">
                <xsl:sort select="ac:property-label(.)" data-type="text" order="ascending" lang="{$ldt:lang}"/>
            </xsl:apply-templates>
        </xsl:variable>

        <xsl:if test="$properties">
            <dl class="dl-horizontal">
                <xsl:copy-of select="$properties"/>
            </dl>
        </xsl:if>-->
    </xsl:template>
    
</xsl:stylesheet>