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
    <!ENTITY ac     "https://w3id.org/atomgraph/client#">
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
    
    <!-- LIST MODE -->

    <xsl:template match="rdf:RDF" mode="bs2:BlockList">
        <xsl:apply-templates mode="#current"/>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about]" mode="bs2:BlockList">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'well'" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>

            <xsl:apply-templates select="." mode="bs2:Image"/>
            
            <xsl:apply-templates select="." mode="bs2:Actions"/>

            <h2>
                <xsl:apply-templates select="@rdf:about" mode="xhtml:Anchor"/>
            </h2>

            <p>
                <xsl:apply-templates select="." mode="ac:description"/>
            </p>

            <xsl:apply-templates select="." mode="bs2:TypeList"/>

            <xsl:if test="@rdf:nodeID">
                <xsl:apply-templates select="." mode="bs2:PropertyList"/>
            </xsl:if>
        </div>
    </xsl:template>
        
    <!-- GRID MODE -->

    <xsl:template match="rdf:RDF" mode="bs2:Grid">
        <xsl:param name="thumbnails-per-row" select="2" as="xs:integer"/>
        <xsl:param name="sort-property" as="xs:anyURI?"/>

        <xsl:variable name="prelim-items" as="item()*">
            <xsl:apply-templates mode="#current">
                <xsl:sort select="ac:label(.)" order="ascending" lang="{$ldt:lang}" use-when="system-property('xsl:product-name') = 'SAXON'"/>
                <xsl:sort select="ac:label(.)" order="ascending" use-when="system-property('xsl:product-name') eq 'Saxon-JS'"/>
                <xsl:with-param name="thumbnails-per-row" select="$thumbnails-per-row" tunnel="yes"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:variable name="items" select="$prelim-items/self::*" as="element()*"/>
        
        <xsl:for-each-group select="$items" group-adjacent="(position() - 1) idiv $thumbnails-per-row">
            <div class="row-fluid">
                <ul class="thumbnails">
                    <xsl:copy-of select="current-group()"/>
                </ul>
            </div>
        </xsl:for-each-group>
    </xsl:template>
    
    <xsl:template match="*[*][@rdf:about]" mode="bs2:Grid" priority="1">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="thumbnails-per-row" as="xs:integer" tunnel="yes"/>
        <xsl:param name="class" select="concat('span', 12 div $thumbnails-per-row)" as="xs:string?"/>

        <li>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>

            <div class="thumbnail">
                <xsl:apply-templates select="." mode="bs2:Image"/>

                <div class="caption">
                    <xsl:apply-templates select="." mode="bs2:Actions"/>

                    <h2>
                        <xsl:apply-templates select="@rdf:about" mode="xhtml:Anchor"/>
                    </h2>
                    <p>
                        <xsl:apply-templates select="." mode="ac:description"/>
                    </p>
                </div>
            </div>
        </li>
    </xsl:template>

    <xsl:template match="*[*][@rdf:nodeID]" mode="bs2:Grid"/>

    <!-- TABLE MODE -->

    <xsl:template match="rdf:RDF" mode="xhtml:Table">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'table table-bordered table-striped'" as="xs:string?"/>
        <xsl:param name="predicates" as="element()*">
            <xsl:for-each-group select="*/*" group-by="concat(namespace-uri(), local-name())">
                <xsl:sort select="ac:property-label(.)" order="ascending" lang="{$ldt:lang}" use-when="system-property('xsl:product-name') = 'SAXON'"/>
                <xsl:sort select="ac:property-label(.)" order="ascending" use-when="system-property('xsl:product-name') eq 'Saxon-JS'"/>

                <xsl:sequence select="current-group()[1]"/>
            </xsl:for-each-group>
        </xsl:param>
        <xsl:param name="anchor-column" as="xs:boolean" select="true()" tunnel="yes"/>

        <table>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            <thead>
                <tr>
                    <xsl:if test="$anchor-column">
                        <th>
                            <xsl:apply-templates select="key('resources', '&rdfs;Resource', document(ac:document-uri('&rdfs;')))" mode="ac:label" use-when="system-property('xsl:product-name') = 'SAXON'"/>
                            <xsl:value-of use-when="system-property('xsl:product-name') eq 'Saxon-JS'">Resource</xsl:value-of>
                        </th>
                    </xsl:if>
                    
                    <xsl:apply-templates select="$predicates" mode="xhtml:TableHeaderCell"/>
                </tr>
            </thead>
            <tbody>
                <xsl:apply-templates mode="#current">
                    <xsl:with-param name="predicates" select="$predicates" tunnel="yes"/>
                </xsl:apply-templates>
            </tbody>
        </table>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about]" mode="xhtml:Table" priority="1">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="predicates" as="element()*" tunnel="yes"/>
        <xsl:param name="anchor-column" as="xs:boolean" select="true()" tunnel="yes"/>

        <tr>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>

            <xsl:if test="$anchor-column">
                <td>
                    <xsl:apply-templates select="@rdf:about" mode="xhtml:Anchor"/>
                </td>
            </xsl:if>
            
            <xsl:variable name="resource" select="." as="element()"/>
            <xsl:for-each select="$predicates">
                <xsl:choose>
                    <xsl:when test="$resource/*[concat(namespace-uri(), local-name()) = current()/concat(namespace-uri(), local-name())]">
                        <xsl:apply-templates select="$resource/*[concat(namespace-uri(), local-name()) = current()/concat(namespace-uri(), local-name())]" mode="xhtml:TableDataCell"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <td></td>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </tr>
    </xsl:template>

    <!-- <xsl:template match="*[*][@rdf:nodeID]" mode="xhtml:Table"/> -->

    <!-- MAP MODE -->

    <xsl:template match="rdf:RDF" mode="bs2:Map">
        <xsl:param name="canvas-id" select="'map-canvas'" as="xs:string"/>

        <div id="{$canvas-id}">
            <xsl:apply-templates mode="#current"/>
        </div>
        
        <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?key={$ac:googleMapsKey}&amp;callback=initMap" async="async"/>
        <xsl:for-each select="key('resources', ac:uri())">
            <script type="text/javascript">
                <![CDATA[
                    function initMap()
                    {
                        var latLng = new google.maps.LatLng(]]><xsl:value-of select="geo:lat[1]"/>, <xsl:value-of select="geo:long[1]"/><![CDATA[);
                        var map = new google.maps.Map(document.getElementById(']]><xsl:value-of select="$canvas-id"/><![CDATA['), { center: latLng, zoom: 8 });
                        var marker = new google.maps.Marker({
                            position: latLng,
                            map: map,
                            title: "]]><xsl:value-of><xsl:apply-templates select="." mode="ac:label"/></xsl:value-of><![CDATA["
                        });
                    }
                ]]>
            </script>
        </xsl:for-each>
    </xsl:template>

<!--    <xsl:template match="*[@rdf:about or @rdf:nodeID][geo:lat castable as xs:double][geo:long castable as xs:double]" mode="bs2:Map" priority="1">
        <xsl:param name="nested" as="xs:boolean?"/>

        <script type="text/javascript">
            <![CDATA[
                function initialize]]><xsl:sequence select="generate-id()"/><![CDATA[()
                {
                    var latLng = new google.maps.LatLng(]]><xsl:value-of select="geo:lat[1]"/>, <xsl:value-of select="geo:long[1]"/><![CDATA[);
                    var marker = new google.maps.Marker({
                        position: latLng,
                        map: map,
                        title: "]]><xsl:value-of><xsl:apply-templates select="." mode="ac:label"/></xsl:value-of><![CDATA["
                    });
                }

                google.maps.event.addDomListener(window, 'load', initialize]]><xsl:sequence select="generate-id()"/><![CDATA[);
            ]]>
        </script>
    </xsl:template>-->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:Map"/>

</xsl:stylesheet>