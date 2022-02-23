<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright (C) 2019 Martynas Jusevičius <martynas@atomgraph.com>

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
    <!ENTITY ac     "https://w3id.org/atomgraph/client#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY owl    "http://www.w3.org/2002/07/owl#">
]>
<xsl:stylesheet version="3.0"
xmlns="http://www.w3.org/2000/svg"
xmlns:svg="http://www.w3.org/2000/svg"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:owl="&owl;"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:math="http://www.w3.org/2005/xpath-functions/math"
exclude-result-prefixes="#all">

    <!-- Paper on force directed layout in XSLT: "GraphML Transformation": http://www.mathe2.uni-bayreuth.de/axel/papers/reingold:graph_drawing_by_force_directed_placement.pdf -->
    <!-- Reference implementation: https://gist.github.com/mmisono/8972731 -->
    <!-- 1. position resource nodes (optionally also literals) randomly -->
    <!-- 2. move nodes in a loop using the force-directed algorithm -->
    <!-- 3. draw lines between the nodes, calculating the correct intersection with the node border -->
    <!-- Note: only "flat" RDF/XML (properties grouped into descriptions; no nesting) is supported. It's called RDFXML_PLAIN in Apache Jena. -->
    
    <xsl:output method="xml" indent="yes" encoding="UTF-8" media-type="image/svg+xml"/>
    <xsl:strip-space elements="*"/>

    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
    <xsl:key name="properties" match="*[@rdf:about or @rdf:nodeID]/*" use="namespace-uri() || local-name()"/>
    <xsl:key name="nodes" match="svg:g[svg:circle] | svg:g[svg:rect]" use="svg:circle/local-name() || svg:rect/local-name()"/>
    <xsl:key name="subjects" match="svg:g[@class = 'subject']" use="@about"/>
    <xsl:key name="adjacent-resources" match="svg:g" use="svg:g[@class = 'property']/@resource"/>
    
    <xsl:param name="show-literals" select="false()" as="xs:boolean"/>
    <xsl:param name="show-object-resources" select="false()" as="xs:boolean"/>
    <xsl:param name="step-count" select="30" as="xs:integer"/> <!-- number of iteration steps -->
    <xsl:param name="preserveAspectRatio" as="xs:string?"/>
    <xsl:param name="spring-stiffness" select="0.01" as="xs:double"/>
    <xsl:param name="spring-length" select="50" as="xs:double"/> <!-- ideal spring length -->
    <xsl:param name="padding" select="$spring-length div 2" as="xs:double"/>
    <xsl:param name="width" select="1000" as="xs:integer"/> <!-- drawing width -->
    <xsl:param name="height" select="800" as="xs:integer"/> <!-- drawing height -->

    <xsl:mode name="ac:SVGPositioning" on-no-match="shallow-copy"/>

    <xsl:function name="ac:svg-label" as="xs:string?">
        <xsl:param name="resource" as="element()"/>

        <xsl:choose>
            <xsl:when test="ends-with($resource/@rdf:about, '/')">
                <xsl:sequence select="tokenize($resource/@rdf:about, '/')[last() - 1] || '/'"/>
            </xsl:when>
            <xsl:when test="$resource/@rdf:about">
                <xsl:sequence select="tokenize($resource/@rdf:about, '/')[last()]"/>
            </xsl:when>
            <xsl:when test="$resource/@rdf:nodeID">
                <xsl:sequence select="$resource/@rdf:nodeID"/>
            </xsl:when>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="ac:svg-object-label" as="xs:string?">
        <xsl:param name="object" as="attribute()"/>

        <xsl:choose>
            <xsl:when test="$object/local-name() = 'resource' and ends-with($object, '/')">
                <xsl:sequence select="tokenize($object, '/')[last() - 1] || '/'"/>
            </xsl:when>
            <xsl:when test="$object/local-name() = 'resource'">
                <xsl:sequence select="tokenize($object, '/')[last()]"/>
            </xsl:when>
            <xsl:when test="$object/local-name() = 'nodeID'">
                <xsl:sequence select="$object"/>
            </xsl:when>
        </xsl:choose>
    </xsl:function>

    <xsl:template match="rdf:RDF">
        <xsl:apply-templates select="." mode="ac:SVG">
            <xsl:with-param name="step-count" select="$step-count"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="rdf:RDF" mode="ac:SVG">
        <xsl:param name="step-count" as="xs:integer"/>
        <xsl:param name="spring-stiffness" select="$spring-stiffness" as="xs:double" tunnel="yes"/>
        <xsl:param name="spring-length" select="$spring-length" as="xs:double" tunnel="yes"/>
        <xsl:param name="viewBox" as="xs:string?"/>
        <xsl:param name="preserveAspectRatio" select="$preserveAspectRatio" as="xs:string?"/>
        <xsl:param name="height" as="xs:string?"/>
        <xsl:param name="width" as="xs:string?"/>

        <xsl:variable name="svg" as="document-node()">
            <xsl:document>
                <svg version="1.1">
                    <defs>
                        <marker id="triangle" viewBox="0 0 10 10" refX="10" refY="5" markerUnits="strokeWidth" markerWidth="8" markerHeight="6" orient="auto">
                            <path d="M 0 0 L 10 5 L 0 10 z" fill="gray"/>
                        </marker>
                    </defs>

                    <!-- draw nodes -->
                    <xsl:apply-templates mode="#current"/>

                    <xsl:if test="$show-object-resources">
                        <!-- select and group objects which are not already present as subjects -->
                        <xsl:for-each-group select="*/*/@rdf:resource[not(key('resources', .))] | */*/@rdf:nodeID[not(key('resources', .))]" group-by=".">
                            <xsl:apply-templates select="current-group()[1]" mode="#current"/>
                        </xsl:for-each-group>
                    </xsl:if>
                </svg>
            </xsl:document>
        </xsl:variable>
        <!-- move nodes in a loop -->
        <xsl:variable name="svg" as="document-node()">
            <xsl:call-template name="ac:SVGPositioningLoop">
                <xsl:with-param name="svg" select="$svg"/>

                <xsl:with-param name="count" select="$step-count"/>
                <xsl:with-param name="spring-stiffness" select="$spring-stiffness" tunnel="yes"/>
                <xsl:with-param name="spring-length" select="$spring-length" tunnel="yes"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:for-each select="$svg/svg:svg">
            <xsl:copy>
                <xsl:copy-of select="@*"/>

                <xsl:choose>
                    <xsl:when test="$viewBox">
                        <xsl:attribute name="viewBox" select="$viewBox"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- calculate the size of the viewport by finding nodes with min/max translated coordinates -->
                        <xsl:variable name="min-x" select="min((key('nodes', 'circle')/(svg:circle/@cx + (if (@transform) then xs:double(substring-before(substring-after(@transform, 'translate('), ' ')) else 0)), key('nodes', 'rect')/(svg:rect/@x + (if (@transform) then xs:double(substring-before(substring-after(@transform, 'translate('), ' ')) else 0)))) - $padding" as="xs:double?"/>
                        <xsl:variable name="min-y" select="min((key('nodes', 'circle')/(svg:circle/@cy + (if (@transform) then xs:double(substring-before(substring-after(@transform, ' '), ')')) else 0)), key('nodes', 'rect')/(svg:rect/@y + (if (@transform) then xs:double(substring-before(substring-after(@transform, ' '), ')')) else 0)))) - $padding" as="xs:double?"/>
                        
                        <!-- $min-x/$min-y are empty sequences if there are no actual <svg:circle> elements in <svg:svg> -->
                        <xsl:if test="$min-x and $min-y">
                            <xsl:variable name="width" select="max((key('nodes', 'circle')/(svg:circle/@cx + (if (@transform) then xs:double(substring-before(substring-after(@transform, 'translate('), ' ')) else 0)), key('nodes', 'rect')/(svg:rect/@x + (if (@transform) then xs:double(substring-before(substring-after(@transform, 'translate('), ' ')) else 0)))) - $min-x + $padding" as="xs:double"/>
                            <xsl:variable name="height" select="max((key('nodes', 'circle')/(svg:circle/@cy + (if (@transform) then xs:double(substring-before(substring-after(@transform, ' '), ')')) else 0)), key('nodes', 'rect')/(svg:rect/@y + (if (@transform) then xs:double(substring-before(substring-after(@transform, ' '), ')')) else 0)))) - $min-y + $padding" as="xs:double"/>
                            
                            <xsl:attribute name="viewBox" select="$min-x || ' ' || $min-y || ' ' || $width || ' ' || $height"/>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="$preserveAspectRatio">
                    <xsl:attribute name="preserveAspectRatio" select="$preserveAspectRatio"/>
                </xsl:if>
                <xsl:if test="$height">
                    <xsl:attribute name="height" select="$height"/>
                </xsl:if>
                <xsl:if test="$width">
                    <xsl:attribute name="width" select="$width"/>
                </xsl:if>

                <!-- draw lines between nodes -->
                <xsl:apply-templates select="." mode="ac:SVGLines"/>

                <xsl:copy-of select="node()"/>
            </xsl:copy>
        </xsl:for-each>
    </xsl:template>

    <!-- NODE DRAWING -->

    <!-- resources description -->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="ac:SVG">
        <g>
            <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>

            <xsl:apply-templates mode="#current"/>
        </g>
    </xsl:template>

    <!-- subject/object node -->

    <xsl:template match="@rdf:about | @rdf:resource | @rdf:nodeID" mode="ac:SVG">
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="r" select="15" as="xs:double"/>
        <xsl:param name="random-seed" select="if (../rdf:type/@rdf:*) then random-number-generator(../rdf:type[1]/@rdf:*)?number else ()" as="xs:double?"/>
        <xsl:param name="hsl" select="if ($random-seed) then 'hsl(' || $random-seed * 360 || ', 50%, 70%)' else ()" as="xs:string?"/>
        <xsl:param name="fill" select="if ($hsl) then $hsl else '#acf'" as="xs:string"/>
        <xsl:param name="stroke" select="'gray'" as="xs:string"/>
        <xsl:param name="stroke-width" select="1" as="xs:integer"/>

        <!-- @x and @y will be set by the ac:SVGPositioningLoop -->
        <g id="{$id}" class="subject" about="{.}" transform="translate(0 0)"> <!-- need an initial @transform for ac:SVGPositioning template to match -->
            <circle r="{$r}" cx="0" cy="0" fill="{$fill}" stroke="{$stroke}" stroke-width="{$stroke-width}">
                <title><xsl:value-of select="."/></title>
            </circle>

            <xsl:apply-templates select="." mode="svg:Anchor"/>
        </g>
    </xsl:template>

    <xsl:template match="@rdf:about | @rdf:resource | @rdf:nodeID" mode="svg:Anchor">
        <xsl:param name="href" select="if (local-name() = ('about', 'resource')) then . else ()" as="xs:anyURI?"/>
        <xsl:param name="id" select="if (local-name() = 'nodeID') then . else ()" as="xs:string?"/>
        <xsl:param name="title" select="if (parent::rdf:Description) then ac:svg-label(..) else ac:svg-object-label(.)" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="target" as="xs:string?"/>
        <xsl:param name="font-size" select="6" as="xs:integer"/>
        <xsl:param name="dy" select="'.3em'" as="xs:string"/>
        
        <a>
            <xsl:if test="$href">
                <xsl:attribute name="href"><xsl:sequence select="$href"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:sequence select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:sequence select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:sequence select="$class"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$target">
                <xsl:attribute name="target"><xsl:sequence select="$target"/></xsl:attribute>
            </xsl:if>
            
            <text x="0" y="0" text-anchor="middle" font-size="{$font-size}" dy="{$dy}">
                <xsl:choose>
                    <!-- subject -->
                    <xsl:when test="parent::rdf:Description">
                        <xsl:value-of select="ac:svg-label(..)"/>
                    </xsl:when>
                    <!-- object -->
                    <xsl:otherwise>
                        <xsl:value-of select="ac:svg-object-label(.)"/>
                    </xsl:otherwise>
                </xsl:choose>
            </text>
        </a>
    </xsl:template>

    <!-- property -->

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[@rdf:resource | @rdf:nodeID]" mode="ac:SVG">
        <g class="property" property="{namespace-uri() || local-name()}" resource="{@rdf:resource | @rdf:nodeID}"/>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[node()][$show-literals]" mode="ac:SVG" priority="1">
        <g class="property" property="{namespace-uri() || local-name()}" content="{node()}">
            <xsl:apply-templates mode="#current"/>
        </g>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[node()]" mode="ac:SVG"/>

    <!-- literal node -->

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/text() | *[@rdf:about or @rdf:nodeID]/*[@rdf:parseType = 'Literal']/*" mode="ac:SVG">
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="height" select="25" as="xs:double"/>
        <xsl:param name="width" select="50" as="xs:double"/>
        <xsl:param name="fill" select="'#fc3'" as="xs:string"/>
        <xsl:param name="stroke" select="'gray'" as="xs:string"/>
        <xsl:param name="max-literal-length" select="15" as="xs:integer"/>
        <xsl:param name="stroke-width" select="1" as="xs:integer"/>
        <xsl:param name="font-size" select="6" as="xs:integer"/>
        <xsl:param name="dy" select="'.3em'" as="xs:string"/>

        <!-- @x and @y will be set by the ac:SVGPositioningLoop -->
        <g id="{$id}" class="object" transform="translate(0 0)"> <!-- need an initial @transform for ac:SVGPositioning template to match -->
            <rect x="0" y="0" height="{$height}" width="{$width}" fill="{$fill}" stroke="{$stroke}" stroke-width="{$stroke-width}" />
            <text x="0" y="0" text-anchor="middle" font-size="{$font-size}" dy="{$dy}">
                <xsl:choose>
                    <xsl:when test="string-length(.) &gt; $max-literal-length">
                        <xsl:value-of select="substring(., 0, $max-literal-length)"/>
                        <xsl:text>...</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="."/>
                    </xsl:otherwise>
                </xsl:choose>
            </text>
        </g>
    </xsl:template>

    <xsl:template match="@rdf:datatype | @xml:lang" mode="ac:SVG"/>

    <!-- POSITIONING -->

    <!-- positioning loop -->

    <xsl:template name="ac:SVGPositioningLoop">
        <xsl:param name="svg" as="document-node()"/>
        <xsl:param name="count" as="xs:integer"/>
        <xsl:param name="temperature" select="$width div 10" as="xs:double"/>
        <xsl:variable name="node-adjacency" as="map(xs:string, item()*)*">
            <xsl:variable name="force-nodes" select="$svg/svg:svg//svg:g[@class = ('subject', 'object')]" as="element()*"/>
            <xsl:iterate select="1 to $count">
                <xsl:param name="node-adjacency" as="map(xs:string, item()*)*">
                    <xsl:variable name="force-node-count" select="count($force-nodes)" as="xs:integer"/>
                    <xsl:for-each select="$force-nodes">
                        <!-- adjacent nodes as a union of resources and literals (in both directions) -->
                        <xsl:variable name="to-resources" select="key('subjects', following-sibling::svg:g[@class = 'property']/@resource)" as="element()*"/>
                        <xsl:variable name="from-resources" select="key('adjacent-resources', current()/@about)/svg:g[@about]" as="element()*"/>
                        <xsl:variable name="to-literals" select="following-sibling::svg:g[@class = 'property']/svg:g[@class = 'object'][svg:rect]" as="element()*"/>
                        <xsl:variable name="from-literals" select="self::svg:g[@class = 'object'][svg:rect]/../preceding-sibling::svg:g[@class = 'subject']" as="element()*"/>
                        <xsl:variable name="adjacent-nodes" select="$to-resources | $from-resources | $to-literals | $from-literals" as="element()*"/>
                        <xsl:map>
                            <xsl:map-entry key="'node-id'" select="@id"/>
                            <xsl:map-entry key="'x'" select="math:cos(math:pi() * (2 * position() div $force-node-count)) * ($width div 2 - 10) + ($width div 2)"/>
                            <xsl:map-entry key="'y'" select="math:sin(math:pi() * (2 * position() div $force-node-count)) * ($height div 2 - 10) + ($height div 2)"/>
                            <xsl:map-entry key="'adjacent-ids'" select="($adjacent-nodes except current())/@id"/>
                            <xsl:map-entry key="'non-adjacent-ids'" select="($force-nodes except $adjacent-nodes except current())/@id"/>
                        </xsl:map>
                    </xsl:for-each>
                </xsl:param>
                <xsl:param name="edges" select="fold-left($node-adjacency, (), function($a, $b) { ($a, for $adjacent-id in $b?adjacent-ids
                    return map{ 'v-id': $b?node-id, 'u-id': $adjacent-id }) })" as="map(xs:string, item())*"/>
                <xsl:param name="step" select="1" as="xs:integer"/>
                <xsl:param name="temperature" select="$temperature" as="xs:double"/>

                <xsl:on-completion>
                    <xsl:sequence select="$node-adjacency"/>
                </xsl:on-completion>
                
                <xsl:next-iteration>
                    <!-- cooling down temperature over time -->
                    <xsl:with-param name="node-adjacency" select="ac:force-step($node-adjacency, $edges, $spring-stiffness, $spring-length, $width, $height, $temperature)"/>
                    <xsl:with-param name="step" select="$step + 1"/>
                    <xsl:with-param name="temperature" select="$temperature - $temperature div ($step + 1)"/>
                </xsl:next-iteration>
            </xsl:iterate>
        </xsl:variable>

        <xsl:apply-templates select="$svg" mode="ac:SVGPositioning">
            <xsl:with-param name="node-adjacency" select="$node-adjacency" tunnel="yes"/>
        </xsl:apply-templates>
    </xsl:template>

    <!-- force directed position of nodes -->
    <!-- we need to re-position the whole group so that the text follows the nodes -->

    <xsl:function name="ac:force-step" as="map(xs:string, item()*)*">
        <xsl:param name="node-adjacency" as="map(xs:string, item()*)*"/>
        <xsl:param name="edges" as="map(xs:string, item())*"/>
        <xsl:param name="spring-stiffness" as="xs:double"/>
        <xsl:param name="spring-length" as="xs:double"/>
        <xsl:param name="width" as="xs:integer"/>
        <xsl:param name="height" as="xs:integer"/>
        <xsl:param name="temperature" as="xs:double"/>

        <!-- calculate repulsive forces against all other nodes -->
        <xsl:variable name="node-adjacency" as="map(xs:string, item()*)*">
            <xsl:for-each select="$node-adjacency">
                <xsl:variable name="v" select="." as="map(xs:string, item()*)"/>

                <xsl:iterate select="(?non-adjacent-ids, ?adjacent-ids)">
                    <xsl:param name="v-dx" select="0.00" as="xs:double"/>
                    <xsl:param name="v-dy" select="0.00" as="xs:double"/>

                    <xsl:on-completion>
                        <xsl:map>
                            <xsl:for-each select="$v">
                                <xsl:map-entry key="'node-id'" select="?node-id"/>
                                <xsl:map-entry key="'x'" select="?x"/>
                                <xsl:map-entry key="'y'" select="?y"/>
                                <xsl:map-entry key="'adjacent-ids'" select="?adjacent-ids"/>
                                <xsl:map-entry key="'non-adjacent-ids'" select="?non-adjacent-ids"/>
                                <xsl:map-entry key="'dx'" select="$v-dx"/>
                                <xsl:map-entry key="'dy'" select="$v-dy"/>
                            </xsl:for-each>
                        </xsl:map>
                    </xsl:on-completion>

                    <xsl:variable name="u" select="$node-adjacency[?node-id eq current()]" as="map(xs:string, item()*)"/>
                    <xsl:variable name="dx" select="$v?x - $u?x" as="xs:double"/>
                    <xsl:variable name="dy" select="$v?y - $u?y" as="xs:double"/>
                    <!-- square of euclidean distance -->
                    <xsl:variable name="distance2" select="$dx * $dx + $dy * $dy" as="xs:double"/>
                    <xsl:choose>
                        <xsl:when test="$distance2 ne 0">
                            <!-- euclidean distance -->
                            <xsl:variable name="distance" select="math:sqrt($distance2)" as="xs:double"/>
                            <!-- repulsion force coefficient (k^2/d) -->
                            <xsl:variable name="force" select="($spring-length * $spring-length) div $distance" as="xs:double"/>
                            <xsl:variable name="d" select="$force div $distance" as="xs:double"/>

                            <xsl:next-iteration>
                                <!-- displacement -->
                                <xsl:with-param name="v-dx" select="$v-dx + $dx * $d"/>
                                <xsl:with-param name="v-dy" select="$v-dy + $dy * $d"/>
                            </xsl:next-iteration>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:next-iteration>
                                <!-- displacement -->
                                <xsl:with-param name="v-dx" select="$v-dx"/>
                                <xsl:with-param name="v-dy" select="$v-dy"/>
                            </xsl:next-iteration>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:iterate>
            </xsl:for-each>
        </xsl:variable>

        <!-- calculate attractive forces between adjacent nodes -->
        <xsl:variable name="edge-nodes" as="map(xs:string, item()*)*">
            <xsl:for-each select="$edges">
                <xsl:variable name="v" select="$node-adjacency[?node-id = current()?v-id]" as="map(xs:string, item()*)"/>
                <xsl:variable name="u" select="$node-adjacency[?node-id = current()?u-id]" as="map(xs:string, item()*)"/>
                <xsl:variable name="dx" select="$v?x - $u?x" as="xs:double"/>  
                <xsl:variable name="dy" select="$v?y - $u?y" as="xs:double"/>
                <!-- square of euclidean distance -->
                <xsl:variable name="distance2" select="$dx * $dx + $dy * $dy" as="xs:double"/>
                <xsl:choose>
                    <xsl:when test="$distance2 ne 0">
                        <!-- euclidean distance -->
                        <xsl:variable name="distance" select="math:sqrt($distance2)" as="xs:double"/>
                        <!-- attraction force coeficient (d^2/k)-->
                        <xsl:variable name="force" select="$distance2 div $spring-length" as="xs:double"/>
                        <xsl:variable name="d" select="$force div $distance" as="xs:double"/>
                        <!-- displacement -->
                        <xsl:variable name="ddx" select="$dx * $d" as="xs:double"/>
                        <xsl:variable name="ddy" select="$dy * $d" as="xs:double"/>

                        <xsl:map>
                            <xsl:for-each select="$v">
                                <xsl:map-entry key="'node-id'" select="?node-id"/>
                                <xsl:map-entry key="'x'" select="?x"/>
                                <xsl:map-entry key="'y'" select="?y"/>
                                <xsl:map-entry key="'adjacent-ids'" select="?adjacent-ids"/>
                                <xsl:map-entry key="'non-adjacent-ids'" select="?non-adjacent-ids"/>
                                <xsl:map-entry key="'dx'" select="?dx - $ddx"/>
                                <xsl:map-entry key="'dy'" select="?dy - $ddy"/>
                            </xsl:for-each>
                        </xsl:map>
                        <xsl:map>
                            <xsl:for-each select="$u">
                                <xsl:map-entry key="'node-id'" select="?node-id"/>
                                <xsl:map-entry key="'x'" select="?x"/>
                                <xsl:map-entry key="'y'" select="?y"/>
                                <xsl:map-entry key="'adjacent-ids'" select="?adjacent-ids"/>
                                <xsl:map-entry key="'non-adjacent-ids'" select="?non-adjacent-ids"/>
                                <xsl:map-entry key="'dx'" select="?dx + $ddx"/>
                                <xsl:map-entry key="'dy'" select="?dy + $ddy"/>
                            </xsl:for-each>
                        </xsl:map>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:sequence select="."/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="edge-nodes" as="map(xs:string, item()*)*">
            <xsl:for-each-group select="$edge-nodes" group-by="?node-id">
                <xsl:map>
                    <xsl:map-entry key="'node-id'" select="current-grouping-key()"/>
                    <xsl:map-entry key="'x'" select="?x"/>
                    <xsl:map-entry key="'y'" select="?y"/>
                    <xsl:map-entry key="'adjacent-ids'" select="?adjacent-ids"/>
                    <xsl:map-entry key="'non-adjacent-ids'" select="?non-adjacent-ids"/>
                    <xsl:map-entry key="'dx'" select="sum(current-group()?dx)"/>
                    <xsl:map-entry key="'dy'" select="sum(current-group()?dy)"/>
                </xsl:map>
            </xsl:for-each-group>
        </xsl:variable>
        <!-- combine edge nodes with standalone nodes (nodes that are not part of any edge) -->
        <xsl:variable name="node-adjacency" select="($edge-nodes, $node-adjacency[not(?node-id = $edge-nodes?node-id)])" as="map(xs:string, item()*)*"/>

        <xsl:for-each select="$node-adjacency">
            <xsl:variable name="v" select="." as="map(xs:string, item()*)"/>
            <xsl:variable name="disp" select="math:sqrt($v?dx * $v?dx + $v?dy * $v?dy)" as="xs:double"/>
            <xsl:variable name="d" select="min(($disp, $temperature)) div $disp" as="xs:double"/>
            <xsl:variable name="x" select="$v?x + $v?dx * $d" as="xs:double"/>
            <xsl:variable name="y" select="$v?y + $v?dy * $d" as="xs:double"/>
            <xsl:variable name="x" select="min(($width, max((0, $x)))) - $width div 2" as="xs:double"/>
            <xsl:variable name="y" select="min(($height, max((0, $y)))) - $height div 2" as="xs:double"/>
            <xsl:variable name="x" select="min((math:sqrt(abs($width * $width div 4 - $y * $y)), max((-1 * math:sqrt(abs($width * $width div 4 - $y * $y)), $x)))) + $width div 2" as="xs:double"/>
            <xsl:variable name="y" select="min((math:sqrt(abs($height * $height div 4 - $x * $x)), max((-1 * math:sqrt(abs($height * $height div 4 - $x * $x)), $y)))) + $height div 2" as="xs:double"/>

            <xsl:map>
                <xsl:map-entry key="'node-id'" select="?node-id"/>
                <xsl:map-entry key="'x'" select="$x"/>
                <xsl:map-entry key="'y'" select="$y"/>
                <xsl:map-entry key="'adjacent-ids'" select="?adjacent-ids"/>
                <xsl:map-entry key="'non-adjacent-ids'" select="?non-adjacent-ids"/>
                <xsl:map-entry key="'dx'" select="?dx"/>
                <xsl:map-entry key="'dy'" select="?dy"/>
            </xsl:map>
        </xsl:for-each>
    </xsl:function>
    
    <xsl:template match="svg:g[@class = ('subject', 'object')]" mode="ac:SVGPositioning" priority="1">
        <xsl:param name="node-adjacency" as="map(xs:string, item()*)*" tunnel="yes"/>
        <xsl:variable name="id" select="@id" as="xs:string"/>
        
        <xsl:copy>
            <xsl:variable name="map" select="$node-adjacency[?node-id eq $id]" as="map(xs:string, item()*)"/>
            <xsl:apply-templates select="@*" mode="#current">
                <xsl:with-param name="x" select="$map?x"/>
                <xsl:with-param name="y" select="$map?y"/>
            </xsl:apply-templates>

            <xsl:apply-templates mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="svg:g[@class = ('subject', 'object')]/@transform" mode="ac:SVGPositioning">
        <xsl:param name="x" as="xs:double"/>
        <xsl:param name="y" as="xs:double"/>

        <xsl:attribute name="{local-name()}" select="'translate(' || $x || ' ' || $y || ')'"/>
    </xsl:template>

    <!-- center the rectangle around the force point -->
    <xsl:template match="svg:g[@class = 'object']/svg:rect/@x" mode="ac:SVGPositioning">
        <xsl:attribute name="{local-name()}" select="-1 * ../@width div 2"/>
    </xsl:template>

    <!-- center the rectangle around the force point -->
    <xsl:template match="svg:g[@class = 'object']/svg:rect/@y" mode="ac:SVGPositioning">
        <xsl:attribute name="{local-name()}" select="-1 * ../@height div 2"/>
    </xsl:template>

    <!-- LINE DRAWING -->

    <!-- TO-DO: optimize using $node-adjacency -->
    <xsl:template match="svg:svg" mode="ac:SVGLines">
        <xsl:param name="stroke" select="'gray'" as="xs:string"/>
        <xsl:param name="stroke-width" select="1" as="xs:integer"/>

        <!-- arcs to object resources - place before/under the resource nodes -->
        <xsl:for-each select="*/*[@property][@resource]">
            <!-- node coordinates need to be translated to get the effective position -->
            <xsl:variable name="x1-offset" select="if (preceding-sibling::svg:g/@transform) then xs:double(substring-before(substring-after(preceding-sibling::svg:g/@transform, 'translate('), ' ')) else 0" as="xs:double"/>
            <xsl:variable name="y1-offset" select="if (preceding-sibling::svg:g/@transform) then xs:double(substring-before(substring-after(preceding-sibling::svg:g/@transform, ' '), ')')) else 0" as="xs:double"/>

            <xsl:choose>
                <!-- loop arc (resource property pointing to itself -->
                <xsl:when test="preceding-sibling::svg:g[@class = 'subject'] is key('subjects', @resource)">
                    <xsl:variable name="property" select="@property" as="xs:string"/>

                    <!-- loop path always pointing down for now -->
                    <xsl:for-each select="preceding-sibling::svg:g/svg:circle">
                        <xsl:variable name="width" select="@r div 2" as="xs:double"/>
                        <xsl:variable name="height" select="100" as="xs:double"/>
                        <!-- find the distance from the circle to the tangent line: https://math.stackexchange.com/questions/1391470/find-distance-between-point-on-tangent-line-and-circle -->
                        <xsl:variable name="y-delta" select="@r - math:sqrt(@r * @r - $width * $width)" as="xs:double"/>

                        <path d="M {$x1-offset + @cx - $width},{$y1-offset + @cy + @r - $y-delta} C {$x1-offset + @cx - @r div 2},{$y1-offset + @cy + @r + $height} {$x1-offset + @cx + @r div 2},{$y1-offset + @cy + @r + $height} {$x1-offset + @cx + $width},{$y1-offset + @cy + @r - $y-delta}" stroke="{$stroke}" stroke-width="{$stroke-width}" fill="none" marker-end="url(#triangle)">
                            <title><xsl:value-of select="$property"/></title>
                        </path>
                    </xsl:for-each>
                </xsl:when>
                <!-- arc -->
                <xsl:when test="key('subjects', @resource)">
                    <!-- node coordinates need to be translated to get the effective position -->
                    <xsl:variable name="x1" select="$x1-offset + preceding-sibling::svg:g/svg:circle/@cx" as="xs:double"/>
                    <xsl:variable name="y1" select="$y1-offset + preceding-sibling::svg:g/svg:circle/@cy" as="xs:double"/>
                    <xsl:variable name="x2-offset" select="if (key('subjects', @resource)/@transform) then xs:double(substring-before(substring-after(key('subjects', @resource)/@transform, 'translate('), ' ')) else 0" as="xs:double"/>
                    <xsl:variable name="y2-offset" select="if (key('subjects', @resource)/@transform) then xs:double(substring-before(substring-after(key('subjects', @resource)/@transform, ' '), ')')) else 0" as="xs:double"/>
                    <xsl:variable name="x2" select="$x2-offset + key('subjects', @resource)/svg:circle/@cx" as="xs:double"/>
                    <xsl:variable name="y2" select="$y2-offset + key('subjects', @resource)/svg:circle/@cy" as="xs:double"/>
                    <xsl:variable name="r" select="key('subjects', @resource)/svg:circle/@r" as="xs:double"/>
                    <xsl:variable name="x-diff" select="$x2 - $x1" as="xs:double"/>
                    <xsl:variable name="y-diff" select="$y2 - $y1" as="xs:double"/>
                    <!-- TO-DO: $x-diff = 0 and $y-diff = 0 -->
                    <!-- find the point where the line intersect the circle -->
                    <xsl:variable name="tan" select="$x-diff div $y-diff" as="xs:double"/>
                    <xsl:variable name="yc" select="abs($r div math:sqrt($tan * $tan + 1))" as="xs:double"/>
                    <xsl:variable name="xc" select="abs($r * $tan * math:sqrt(1 div ($tan * $tan + 1)))" as="xs:double"/>
                    <xsl:variable name="x2" select="if ($x1 &gt; $x2) then ($x2 + $xc) else ($x2 - $xc)" as="xs:double"/>
                    <xsl:variable name="y2" select="if ($y1 &gt; $y2) then ($y2 + $yc) else ($y2 - $yc)" as="xs:double"/>

                    <line x1="{$x1}" y1="{$y1}" x2="{$x2}" y2="{$y2}" stroke="{$stroke}" stroke-width="{$stroke-width}" marker-end="url(#triangle)">
                        <title><xsl:value-of select="@property"/></title>
                    </line>
                </xsl:when>
            </xsl:choose>
        </xsl:for-each>
        <!-- arcs to object literals -->
        <xsl:for-each select="*/*[@property][svg:g[@class = 'object']]">
            <!-- node coordinates need to be translated to get the effective position -->
            <xsl:variable name="x1-offset" select="if (preceding-sibling::svg:g/@transform) then xs:double(substring-before(substring-after(preceding-sibling::svg:g/@transform, 'translate('), ' ')) else 0" as="xs:double"/>
            <xsl:variable name="y1-offset" select="if (preceding-sibling::svg:g/@transform) then xs:double(substring-before(substring-after(preceding-sibling::svg:g/@transform, ' '), ')')) else 0" as="xs:double"/>
            <xsl:variable name="x1" select="$x1-offset + preceding-sibling::svg:g/svg:circle/@cx" as="xs:double"/>
            <xsl:variable name="y1" select="$y1-offset + preceding-sibling::svg:g/svg:circle/@cy" as="xs:double"/>
            <xsl:variable name="x2-offset" select="if (svg:g/@transform) then xs:double(substring-before(substring-after(svg:g/@transform, 'translate('), ' ')) else 0" as="xs:double"/>
            <xsl:variable name="y2-offset" select="if (svg:g/@transform) then xs:double(substring-before(substring-after(svg:g/@transform, ' '), ')')) else 0" as="xs:double"/>
            <xsl:variable name="x2" select="$x2-offset + svg:g/svg:rect/@x + svg:g/svg:rect/@width div 2"/>
            <xsl:variable name="y2" select="$y2-offset + svg:g/svg:rect/@y + svg:g/svg:rect/@height div 2" as="xs:double"/>
            <xsl:variable name="x-diff" select="$x2 - $x1" as="xs:double"/>
            <xsl:variable name="y-diff" select="$y2 - $y1" as="xs:double"/>

            <!-- handle the cases of the line approaching the rectangle at different angles -->
            <line x1="{$x1}" y1="{$y1}" stroke="{$stroke}" stroke-width="{$stroke-width}" marker-end="url(#triangle)">
                <xsl:choose>
                    <xsl:when test="$x-diff = 0 and $y-diff = 0">
                        <xsl:attribute name="x2" select="$x2"/>
                        <xsl:attribute name="y2" select="$x2"/>
                    </xsl:when>
                    <xsl:when test="$x-diff = 0 and $y-diff &lt; 0">
                        <xsl:attribute name="x2" select="$x2"/>
                        <xsl:attribute name="y2" select="svg:g/svg:rect/@y + svg:g/svg:rect/@height"/>
                    </xsl:when>
                    <xsl:when test="$x-diff = 0 and $y-diff &gt; 0">
                        <xsl:attribute name="x2" select="$x2"/>
                        <xsl:attribute name="y2" select="svg:g/svg:rect/@y"/>
                    </xsl:when>
                    <xsl:when test="$y-diff = 0 and $x-diff &lt; 0">
                        <xsl:attribute name="x2" select="svg:g/svg:rect/@x + svg:g/svg:rect/@width"/>
                        <xsl:attribute name="y2" select="$y2"/>
                    </xsl:when>
                    <xsl:when test="$y-diff = 0 and $x-diff &gt; 0">
                        <xsl:attribute name="x2" select="svg:g/svg:rect/@x"/>
                        <xsl:attribute name="y2" select="$y2"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- find the point where the line intersects the rectangle -->
                        <xsl:variable name="rect-tan" select="svg:g/svg:rect/@width div svg:g/svg:rect/@height" as="xs:double"/>
                        <xsl:variable name="diff-tan" select="$x-diff div $y-diff" as="xs:double"/>

                        <xsl:if test="abs($rect-tan) &gt; abs($diff-tan)">
                            <xsl:variable name="y-delta" select="svg:g/svg:rect/@height div 2" as="xs:double"/>
                            <xsl:variable name="x-delta" select="$y-delta * $x-diff div $y-diff" as="xs:double"/>

                            <xsl:if test="$y-diff &lt; 0">
                                <xsl:attribute name="x2" select="$x2 + $x-delta"/>
                                <xsl:attribute name="y2" select="$y2 + $y-delta"/>
                            </xsl:if>
                            <xsl:if test="$y-diff &gt; 0">
                                <xsl:attribute name="x2" select="$x2 - $x-delta"/>
                                <xsl:attribute name="y2" select="$y2 - $y-delta"/>
                            </xsl:if>
                        </xsl:if>
                        <xsl:if test="abs($rect-tan) &lt; abs($diff-tan)">
                            <xsl:variable name="x-delta" select="svg:g/svg:rect/@width div 2" as="xs:double"/>
                            <xsl:variable name="y-delta" select="$x-delta * $y-diff div $x-diff" as="xs:double"/>

                            <xsl:if test="($y-diff &lt; 0 and $x-diff &lt; 0) or ($y-diff &gt; 0 and $x-diff &lt; 0)">
                                <xsl:attribute name="x2" select="$x2 + $x-delta"/>
                                <xsl:attribute name="y2" select="$y2 + $y-delta"/>
                            </xsl:if>
                            <xsl:if test="($y-diff &lt; 0 and $x-diff &gt; 0) or ($y-diff &gt; 0 and $x-diff &gt; 0)">
                                <xsl:attribute name="x2" select="$x2 - $x-delta"/>
                                <xsl:attribute name="y2" select="$y2 - $y-delta"/>
                            </xsl:if>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>

                <title><xsl:value-of select="@property"/></title>
            </line>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>