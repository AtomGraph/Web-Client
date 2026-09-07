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
    <!ENTITY translations "https://w3id.org/atomgraph/client/xsl/translations.rdf#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY geo    "http://www.w3.org/2003/01/geo/wgs84_pos#">
    <!ENTITY ldt    "https://www.w3.org/ns/ldt#">
]>
<xsl:stylesheet version="3.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:ldt="&ldt;"
xmlns:geo="&geo;"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
exclude-result-prefixes="#all">
    
    <!-- LIST MODE -->

    <xsl:template match="rdf:RDF" mode="ac:List">
        <ul class="resource-list">
            <xsl:apply-templates mode="#current"/>
        </ul>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about]" mode="ac:List">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>

        <li>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>

            <xsl:apply-templates select="." mode="ac:Depiction"/>

            <xsl:apply-templates select="." mode="ac:BlockActions"/>

            <h2>
                <xsl:apply-templates select="@rdf:about" mode="xhtml:Anchor"/>
            </h2>

            <xsl:where-populated>
                <p class="description">
                    <xsl:apply-templates select="." mode="ac:description"/>
                </p>
            </xsl:where-populated>

            <xsl:apply-templates select="." mode="ac:ResourceTypes"/>

            <xsl:if test="@rdf:nodeID">
                <xsl:apply-templates select="." mode="ac:PropertyEditor"/>
            </xsl:if>
        </li>
    </xsl:template>

    <!-- GRID MODE -->

    <!-- the grid lays its items out itself (client.css), so the rows are one flat list rather than chunked rows -->
    <xsl:template match="rdf:RDF" mode="ac:Grid">
        <xsl:param name="sort-property" as="xs:anyURI?"/>

        <ul class="resource-grid">
            <xsl:apply-templates mode="#current">
                <xsl:sort select="ac:label(.)" order="ascending" lang="{ac:langs()[1]}"/>
            </xsl:apply-templates>
        </ul>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about]" mode="ac:Grid" priority="1">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>

        <li>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>

            <div class="ldhc-card">
                <xsl:apply-templates select="." mode="ac:Depiction"/>

                <div class="ldhc-card-body">
                    <xsl:apply-templates select="." mode="ac:BlockActions"/>

                    <h2>
                        <xsl:apply-templates select="@rdf:about" mode="xhtml:Anchor"/>
                    </h2>
                    <xsl:where-populated>
                        <p class="description">
                            <xsl:apply-templates select="." mode="ac:description"/>
                        </p>
                    </xsl:where-populated>
                </div>
            </div>
        </li>
    </xsl:template>

    <xsl:template match="*[*][@rdf:nodeID]" mode="ac:Grid"/>

    <!-- TABLE MODE -->

    <xsl:template match="rdf:RDF" mode="xhtml:Table">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'results-table'" as="xs:string?"/>
        <xsl:param name="predicates" as="element()*">
            <xsl:for-each-group select="*/*" group-by="concat(namespace-uri(), local-name())">
                <xsl:sort select="ac:property-label(.)" order="ascending" lang="{ac:langs()[1]}"/>

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
            <caption class="ldhc-vh">
                <xsl:apply-templates select="key('resources', '&translations;resources', ac:translations())" mode="ac:label"/>
            </caption>
            <thead>
                <tr>
                    <xsl:if test="$anchor-column">
                        <th scope="col">
                            <xsl:apply-templates select="key('resources', '&rdfs;Resource', document(ac:document-uri('&rdfs;')))" mode="ac:label" use-when="system-property('xsl:product-name') = 'SAXON'"/>
                            <xsl:value-of use-when="system-property('xsl:product-name') eq 'SaxonJS'">Resource</xsl:value-of>
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

    <!-- OpenLayers over OSM tiles: every resource in the graph with WGS84 coordinates becomes a marker,
         and the view fits the markers (world view when there are none) -->
    <xsl:template match="rdf:RDF" mode="ac:Map">
        <xsl:param name="id" select="'map-canvas'" as="xs:string"/>

        <div id="{$id}" class="map-canvas"></div>

        <script type="text/javascript" src="{resolve-uri('static/com/atomgraph/client/js/ol.js', $ac:contextUri)}"></script>
        <script type="text/javascript">
            <xsl:text>var markers = [</xsl:text>
            <xsl:for-each select="*[geo:lat[1] castable as xs:double][geo:long[1] castable as xs:double]">
                <xsl:if test="position() &gt; 1">, </xsl:if>
                <xsl:text>{ lon: </xsl:text>
                <xsl:value-of select="xs:double(geo:long[1])"/>
                <xsl:text>, lat: </xsl:text>
                <xsl:value-of select="xs:double(geo:lat[1])"/>
                <xsl:text>, title: "</xsl:text>
                <xsl:value-of select="replace(replace(string(ac:label(.)), '\\', '\\\\'), '&quot;', '\\&quot;')"/>
                <xsl:text>" }</xsl:text>
            </xsl:for-each>
            <xsl:text>];
</xsl:text>
            <![CDATA[
                var source = new ol.source.Vector({
                    features: markers.map(function(marker)
                    {
                        return new ol.Feature({
                            geometry: new ol.geom.Point(ol.proj.fromLonLat([ marker.lon, marker.lat ])),
                            name: marker.title
                        });
                    })
                });
                var map = new ol.Map({
                    target: ]]>'<xsl:value-of select="$id"/>'<![CDATA[,
                    layers: [
                        new ol.layer.Tile({ source: new ol.source.OSM() }),
                        new ol.layer.Vector({
                            source: source,
                            style: new ol.style.Style({
                                image: new ol.style.Circle({
                                    radius: 7,
                                    fill: new ol.style.Fill({ color: "#1c5bff" }),
                                    stroke: new ol.style.Stroke({ color: "#ffffff", width: 2 })
                                })
                            })
                        })
                    ],
                    view: new ol.View({ center: ol.proj.fromLonLat([ 0, 30 ]), zoom: 2 })
                });
                if (!source.isEmpty()) map.getView().fit(source.getExtent(), { padding: [ 48, 48, 48, 48 ], maxZoom: 12 });
            ]]>
        </script>
    </xsl:template>

</xsl:stylesheet>