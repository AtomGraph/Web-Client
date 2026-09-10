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
xmlns:map="http://www.w3.org/2005/xpath-functions/map"
xmlns:math="http://www.w3.org/2005/xpath-functions/math"
exclude-result-prefixes="#all">

    <!-- Paper on force directed layout in XSLT: "GraphML Transformation": http://www.mathe2.uni-bayreuth.de/axel/papers/reingold:graph_drawing_by_force_directed_placement.pdf -->
    <!-- Reference implementation: https://gist.github.com/mmisono/8972731 -->
    <!-- 1. position resource nodes (optionally also literals) randomly -->
    <!-- 2. move nodes in a loop using the force-directed algorithm -->
    <!-- 3. draw lines between the nodes, calculating the correct intersection with the node border -->
    <!-- Note: only Apache Jena's RDFXML_PLAIN form is supported: every subject is an
         rdf:Description with rdf:type inlined as an explicit property (never a typed
         element like owl:Class), properties are grouped into the description, and
         there is no blank-node nesting. -->

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

    <!-- function courtesy of Joe Wicentowski -->
    <xsl:function name="ac:trim-lines" as="xs:string*">
        <xsl:param name="string" as="xs:string*"/>
        <xsl:param name="line-length" as="xs:integer"/>
        
        <xsl:sequence select="
            let $words := tokenize($string),
                $trim-function := function ($lines, $word) {
                    let $reversed := $lines => reverse(),
                        $current-line := $reversed => head(),
                        $previous-lines := $reversed => tail() => reverse(),
                        $candidate-line := string-join(($current-line, $word), ' ')
                    return
                        if (string-length($candidate-line) gt $line-length) then
                            ($previous-lines, $current-line || ' ', $word)
                        else
                            ($previous-lines, $candidate-line)
                },
                $lines := fold-left($words, (), $trim-function)
            return $lines
        "/>
    </xsl:function>
    
    <xsl:template match="@rdf:about | @rdf:resource | @rdf:nodeID" mode="svg:Anchor">
        <xsl:param name="href" select="if (local-name() = ('about', 'resource')) then . else ()" as="xs:anyURI?"/>
        <xsl:param name="id" select="if (local-name() = 'nodeID') then . else ()" as="xs:string?"/>
        <xsl:param name="label" select="if (parent::rdf:Description) then ac:svg-label(..) else ac:svg-object-label(.)" as="xs:string"/>
        <xsl:param name="title" select="$label" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="target" as="xs:string?"/>
        <xsl:param name="font-size" select="6" as="xs:integer"/>
        <xsl:param name="dy" select=".3" as="xs:double"/>
        <xsl:param name="dy-per-line" select="-0.6" as="xs:double"/>
        <xsl:param name="line-length" select="10" as="xs:integer"/>
        <xsl:param name="max-lines" select="3" as="xs:integer"/>

        <a>
            <xsl:if test="$href">
                <xsl:attribute name="href" select="$href"/>
            </xsl:if>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title" select="$title"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            <xsl:if test="$target">
                <xsl:attribute name="target" select="$target"/>
            </xsl:if>
            
            <text x="0" y="0" text-anchor="middle" font-size="{$font-size}" dy="{$dy}">
                <xsl:variable name="lines" select="ac:trim-lines($label, $line-length)" as="xs:string*"/>
                <xsl:variable name="tspans" as="element()*">
                    <xsl:for-each select="subsequence($lines[not(. = ' ')], 1, $max-lines)">
                        <tspan x="0">
                            <xsl:if test="position() &gt; 1">
                                <xsl:attribute name="dy" select="'1.2em'"/>
                            </xsl:if>

                            <xsl:value-of select="."/>
                        </tspan>
                    </xsl:for-each>
                </xsl:variable>
                
                <xsl:attribute name="dy" select="format-number($dy + (count($tspans) - 1) * $dy-per-line, '###.00') || 'em'"/>
                <xsl:copy-of select="$tspans"/>
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
            <rect x="0" y="0" height="{$height}" width="{$width}" fill="{$fill}" stroke="{$stroke}" stroke-width="{$stroke-width}"/>
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

    <!-- positioning loop.
         Fruchterman-Reingold as the cited paper defines it (repulsion k^2/d,
         attraction d^2/k along adjacency, temperature-capped displacement with
         t - t/(step+1) cooling, frame clamping, circle seed), with the machinery
         rebuilt for performance:
         - positions live in ONE map(xs:string, map(*)) keyed by node id, so lookups
           are O(1) map:get where the former sequence-of-maps predicate scans
           ($seq[?node-id eq $id]) made every step O(n^3);
         - each step buckets the nodes into a spatial grid of 0.7-spring-length cells
           and repels only against a deterministic sample (at most 16) of the 3x3
           cell neighborhood - a ~2k cutoff radius, the customary grid approximation
           of FR - instead of all pairs;
         - the per-node displacement is a single XPath expression (per-pair XSLT
           instructions dominated the profile), and repulsion needs no sqrt in the
           delta * k^2 / d^2 form;
         - the maps are deliberately declared as="map(*)", not the precise
           map(xs:string, map(*)): SaxonJS validates a declared deep map type
           entry by entry on EVERY function call and parameter binding, which
           alone made the hot path O(n) per call (measured as the single largest
           cost). The key/value shapes are documented here instead.
         Corrected against the former implementation:
         - repulsion is counted once per node (the former edge-record regrouping
           summed it once per incident edge record, scaling it by node degree);
         - attraction acts once per adjacent pair (the former bidirectional edge
           list pulled every spring twice);
         - the canvas clamp is a rectangular frame clamp followed by a radial
           projection onto the inscribed ellipse (the former chain mixed the axes
           and took abs() of negative sqrt arguments);
         - coincident nodes separate along a deterministic direction (they used to
           contribute no force and could stick forever).
         $spring-stiffness is retained for signature compatibility but was never
         used by the force model, before or now. -->

    <xsl:template name="ac:SVGPositioningLoop">
        <xsl:param name="svg" as="document-node()"/>
        <xsl:param name="count" as="xs:integer"/>
        <xsl:param name="temperature" select="$width div 10" as="xs:double"/>

        <xsl:variable name="force-nodes" select="$svg/svg:svg//svg:g[@class = ('subject', 'object')]" as="element()*"/>
        <xsl:variable name="node-ids" select="$force-nodes/@id ! string(.)" as="xs:string*"/>
        <xsl:variable name="force-node-count" select="count($force-nodes)" as="xs:integer"/>
        <!-- initial placement on a circle, in document order -->
        <xsl:variable name="seed" as="map(*)">
            <xsl:map>
                <xsl:for-each select="$force-nodes">
                    <xsl:map-entry key="string(@id)" select="map{
                        'x': math:cos(math:pi() * (2 * position() div $force-node-count)) * ($width div 2 - 10) + ($width div 2),
                        'y': math:sin(math:pi() * (2 * position() div $force-node-count)) * ($height div 2 - 10) + ($height div 2) }"/>
                </xsl:for-each>
            </xsl:map>
        </xsl:variable>
        <!-- adjacent nodes as a union of resources and literals (in both directions),
             computed once - not per iteration -->
        <xsl:variable name="adjacency" as="map(*)">
            <xsl:map>
                <xsl:for-each select="$force-nodes">
                    <xsl:variable name="to-resources" select="key('subjects', following-sibling::svg:g[@class = 'property']/@resource)" as="element()*"/>
                    <xsl:variable name="from-resources" select="key('adjacent-resources', current()/@about)/svg:g[@about]" as="element()*"/>
                    <xsl:variable name="to-literals" select="following-sibling::svg:g[@class = 'property']/svg:g[@class = 'object'][svg:rect]" as="element()*"/>
                    <xsl:variable name="from-literals" select="self::svg:g[@class = 'object'][svg:rect]/../preceding-sibling::svg:g[@class = 'subject']" as="element()*"/>
                    <xsl:map-entry key="string(@id)"
                        select="((($to-resources | $from-resources | $to-literals | $from-literals) except .)/@id) ! string(.)"/>
                </xsl:for-each>
            </xsl:map>
        </xsl:variable>

        <xsl:variable name="positions" as="map(*)">
            <xsl:iterate select="1 to $count">
                <xsl:param name="positions" select="$seed" as="map(*)"/>
                <xsl:param name="step" select="1" as="xs:integer"/>
                <xsl:param name="temperature" select="$temperature" as="xs:double"/>

                <xsl:on-completion>
                    <xsl:sequence select="$positions"/>
                </xsl:on-completion>

                <xsl:next-iteration>
                    <xsl:with-param name="positions" select="ac:force-step($positions, $node-ids, $adjacency, $spring-length, $width, $height, $temperature)"/>
                    <xsl:with-param name="step" select="$step + 1"/>
                    <!-- cooling down temperature over time -->
                    <xsl:with-param name="temperature" select="$temperature - $temperature div ($step + 1)"/>
                </xsl:next-iteration>
            </xsl:iterate>
        </xsl:variable>

        <xsl:apply-templates select="$svg" mode="ac:SVGPositioning">
            <xsl:with-param name="positions" select="$positions" tunnel="yes"/>
        </xsl:apply-templates>
    </xsl:template>

    <!-- repulsion cutoff cell: the 3x3 neighborhood spans ~2 spring lengths -->
    <xsl:function name="ac:grid-cell" as="xs:string">
        <xsl:param name="position" as="map(*)"/>
        <xsl:param name="cell-size" as="xs:double"/>
        <xsl:sequence select="string(floor($position?x div $cell-size)) || ',' || string(floor($position?y div $cell-size))"/>
    </xsl:function>

    <!-- one force-directed step over the position map -->
    <!-- we need to re-position the whole group so that the text follows the nodes -->

    <xsl:function name="ac:force-step" as="map(*)">
        <xsl:param name="positions" as="map(*)"/>
        <xsl:param name="node-ids" as="xs:string*"/>
        <xsl:param name="adjacency" as="map(*)"/>
        <xsl:param name="spring-length" as="xs:double"/>
        <xsl:param name="width" as="xs:integer"/>
        <xsl:param name="height" as="xs:integer"/>
        <xsl:param name="temperature" as="xs:double"/>

        <xsl:variable name="cell-size" select="$spring-length * 0.7" as="xs:double"/>
        <!-- grouped, not merged with 'combine': each cell's list is built once,
             where repeated map:merge appends re-copied a packed cell's list per
             node -->
        <xsl:variable name="grid" as="map(*)">
            <xsl:map>
                <xsl:for-each-group select="$node-ids" group-by="ac:grid-cell($positions(.), $cell-size)">
                    <xsl:map-entry key="current-grouping-key()" select="current-group()"/>
                </xsl:for-each-group>
            </xsl:map>
        </xsl:variable>
        <xsl:map>
            <xsl:for-each select="$node-ids">
                <xsl:map-entry key="." select="ac:displaced(., $positions, $grid, $adjacency, $spring-length, $cell-size, $width, $height, $temperature)"/>
            </xsl:for-each>
        </xsl:map>
    </xsl:function>

    <!-- the per-node displacement, deliberately one XPath expression -->
    <xsl:function name="ac:displaced" as="map(*)">
        <xsl:param name="v" as="xs:string"/>
        <xsl:param name="positions" as="map(*)"/>
        <xsl:param name="grid" as="map(*)"/>
        <xsl:param name="adjacency" as="map(*)"/>
        <xsl:param name="spring-length" as="xs:double"/>
        <xsl:param name="cell-size" as="xs:double"/>
        <xsl:param name="width" as="xs:integer"/>
        <xsl:param name="height" as="xs:integer"/>
        <xsl:param name="temperature" as="xs:double"/>

        <xsl:sequence select="
            let $pv := $positions($v),
                $px := $pv?x, $py := $pv?y,
                $cx := floor($px div $cell-size),
                $cy := floor($py div $cell-size),
                (: repulsion sample: at most 3 nodes from each of the 3x3 cells,
                   at most 16 total - deterministic (grid lists build in node-id
                   order), spatially stratified, and bounded even when the canvas
                   packs most of the graph into one cell :)
                $neighbors := subsequence(
                    (for $dx in (-1, 0, 1), $dy in (-1, 0, 1)
                        return subsequence($grid(string($cx + $dx) || ',' || string($cy + $dy)), 1, 3))[. ne $v],
                    1, 16),
                $k2 := $spring-length * $spring-length,
                (: repulsion force coefficient (k^2/d), applied as delta * k^2 / d^2 -
                   sqrt-free; coincident nodes separate along a deterministic
                   direction derived from id order :)
                $repulsion := (for $u in $neighbors return
                    let $pu := $positions($u),
                        $dx := $px - $pu?x, $dy := $py - $pu?y,
                        $distance2 := $dx * $dx + $dy * $dy
                    return if ($distance2 ne 0)
                        then ($dx * $k2 div $distance2, $dy * $k2 div $distance2)
                        else (if ($v lt $u) then $k2 else -$k2,
                              if ($v lt $u) then $k2 else -$k2)),
                (: attraction force coefficient (d^2/k), once per adjacent pair -
                   as delta * d / k :)
                $attraction := (for $u in $adjacency($v) return
                    let $pu := $positions($u),
                        $dx := $pu?x - $px, $dy := $pu?y - $py,
                        $pull := math:sqrt($dx * $dx + $dy * $dy) div $spring-length
                    return ($dx * $pull, $dy * $pull)),
                $terms := ($repulsion, $attraction),
                $fx := sum($terms[position() mod 2 eq 1]),
                $fy := sum($terms[position() mod 2 eq 0]),
                $disp := math:sqrt($fx * $fx + $fy * $fy)
            return
                if ($disp eq 0) then $pv
                else
                    (: displacement capped at the temperature; then the frame clamp
                       and a radial projection onto the inscribed ellipse :)
                    let $d := min(($disp, $temperature)) div $disp,
                        $xc := min(($width, max((0, $px + $fx * $d)))) - $width div 2,
                        $yc := min(($height, max((0, $py + $fy * $d)))) - $height div 2,
                        $a := $width div 2,
                        $b := $height div 2,
                        $e := ($xc * $xc) div ($a * $a) + ($yc * $yc) div ($b * $b),
                        $scale := if ($e gt 1) then 1 div math:sqrt($e) else 1
                    return map{ 'x': $xc * $scale + $a, 'y': $yc * $scale + $b }"/>
    </xsl:function>

    <xsl:template match="svg:g[@class = ('subject', 'object')]" mode="ac:SVGPositioning" priority="1">
        <xsl:param name="positions" as="map(*)" tunnel="yes"/>

        <xsl:copy>
            <xsl:variable name="position" select="$positions(string(@id))" as="map(*)"/>
            <xsl:apply-templates select="@*" mode="#current">
                <xsl:with-param name="x" select="$position?x"/>
                <xsl:with-param name="y" select="$position?y"/>
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
                <!-- loop arc (resource property pointing to itself) -->
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
                    <!-- clip the line at the target circle's border by walking back
                         one radius along the unit vector (the former tangent-based
                         form divided by zero for horizontally aligned nodes) -->
                    <xsl:variable name="distance" select="math:sqrt($x-diff * $x-diff + $y-diff * $y-diff)" as="xs:double"/>
                    <xsl:variable name="x2" select="if ($distance gt 0) then $x2 - $x-diff div $distance * $r else $x2" as="xs:double"/>
                    <xsl:variable name="y2" select="if ($distance gt 0) then $y2 - $y-diff div $distance * $r else $y2" as="xs:double"/>

                    <line x1="{$x1}" y1="{$y1}" x2="{$x2}" y2="{$y2}" stroke="{$stroke}" stroke-width="{$stroke-width}" marker-end="url(#triangle)" data-id1="{preceding-sibling::svg:g/@id}" data-id2="{key('subjects', @resource)/@id}">
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
                        <xsl:attribute name="y2" select="$y2"/>
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