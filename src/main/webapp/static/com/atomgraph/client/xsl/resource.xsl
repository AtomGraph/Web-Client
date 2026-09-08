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
    <!ENTITY foaf   "http://xmlns.com/foaf/0.1/">
    <!ENTITY spin   "http://spinrdf.org/spin#">
    <!ENTITY sioc   "http://rdfs.org/sioc/ns#">
]>
<xsl:stylesheet version="3.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:ldt="&ldt;"
xmlns:geo="&geo;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
exclude-result-prefixes="#all">

    <!-- BREADCRUMB  -->

    <xsl:template match="*[@rdf:about]" mode="ac:BreadcrumbItem">
        <xsl:param name="leaf" select="true()" as="xs:boolean" tunnel="yes"/>

        <xsl:choose>
            <xsl:when test="key('resources', sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource)">
                <xsl:apply-templates select="key('resources', sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource)" mode="#current">
                    <xsl:with-param name="leaf" select="false()" tunnel="yes"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource">
                <xsl:if test="doc-available((sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource)[1])">
                    <xsl:variable name="parent-doc" select="document(sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource)" as="document-node()?"/>
                    <xsl:apply-templates select="key('resources', sioc:has_container/@rdf:resource | sioc:has_parent/@rdf:resource, $parent-doc)" mode="#current">
                        <xsl:with-param name="leaf" select="false()" tunnel="yes"/>
                    </xsl:apply-templates>
                </xsl:if>
            </xsl:when>
        </xsl:choose>
        
        <li>
            <xsl:apply-templates select="@rdf:about" mode="xhtml:Anchor"/>

            <xsl:if test="not($leaf)">
                <span class="msi sm" aria-hidden="true">chevron_right</span>
            </xsl:if>
        </li>
    </xsl:template>

    <!-- DEFAULT MODE -->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'block'" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>

            <xsl:apply-templates select="." mode="ac:BlockHeader"/>

            <xsl:apply-templates select="." mode="ac:PropertyEditor"/>
        </div>
    </xsl:template>

    <!-- inline blank node resource if there is only one property except foaf:primaryTopic having it as object -->
    <xsl:template match="@rdf:nodeID[key('resources', .)][count(key('predicates-by-object', .)[not(self::foaf:primaryTopic)]) = 1]" priority="2">
        <xsl:param name="inline" select="true()" as="xs:boolean" tunnel="yes"/>

        <xsl:choose>
            <xsl:when test="$inline">
                <xsl:apply-templates select="key('resources', .)" mode="#current">
                    <xsl:with-param name="display" select="$inline" tunnel="yes"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- hide inlined blank node resources from the main block flow -->
    <xsl:template match="*[*][key('resources', @rdf:nodeID)][count(key('predicates-by-object', @rdf:nodeID)[not(self::foaf:primaryTopic)]) = 1]" priority="1">
        <xsl:param name="display" select="false()" as="xs:boolean" tunnel="yes"/>
        
        <xsl:if test="$display">
            <xsl:next-match/>
        </xsl:if>
    </xsl:template>
    
    <!-- HEADER MODE -->
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="ac:BlockHeader">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'block-header'" as="xs:string?"/>

        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>

            <xsl:apply-templates select="." mode="ac:Depiction"/>

            <xsl:apply-templates select="." mode="ac:BlockActions"/>

            <h2>
                <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="xhtml:Anchor"/>
            </h2>

            <xsl:where-populated>
                <p class="description">
                    <xsl:apply-templates select="." mode="ac:description"/>
                </p>
            </xsl:where-populated>

            <xsl:apply-templates select="." mode="ac:ResourceTypes"/>
        </div>
    </xsl:template>
    
    <!-- ACTIONS MODE -->

    <!-- an empty extension point: Edit and Delete act on documents, so they live in the action bar
         (ac:HeaderActions), and importing layers add their own per-block actions here -->
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="ac:BlockActions"/>
    
    <!-- IMAGE MODE -->

    <xsl:template match="*[*][@rdf:about]" mode="ac:Depiction">
        <xsl:variable name="image-uris" as="attribute()*">
            <xsl:apply-templates select="." mode="ac:image"/>
        </xsl:variable>
        <xsl:variable name="this" select="." as="element()"/>
        <xsl:variable name="link" as="element()">
            <xsl:apply-templates select="@rdf:about" mode="xhtml:Anchor"/>
        </xsl:variable>
            
        <xsl:for-each select="$image-uris[1]">
            <a class="depiction" href="{$link/@href}" title="{ac:label($this)}">
                <img src="{.}" alt="{ac:label($this)}"/>
            </a>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="*[*][@rdf:nodeID]" mode="ac:Depiction">
        <xsl:variable name="image-uris" as="attribute()*">
            <xsl:apply-templates select="." mode="ac:image"/>
        </xsl:variable>
        <xsl:variable name="this" select="." as="element()"/>

        <xsl:for-each select="$image-uris[1]">
            <span class="depiction">
                <img src="{.}" alt="{ac:label($this)}"/>
            </span>
        </xsl:for-each>
    </xsl:template>
    
    <!-- TYPE MODE -->
        
    <xsl:template match="*[@rdf:about or @rdf:nodeID][rdf:type/@rdf:resource]" mode="ac:ResourceTypes" priority="1">
        <ul class="types">
            <xsl:for-each select="rdf:type/@rdf:resource">
                <xsl:sort select="ac:object-label(.)" order="ascending" lang="{ac:langs()[1]}"/>

                <li>
                    <xsl:apply-templates select="."/>
                </li>
            </xsl:for-each>
        </ul>
    </xsl:template>

    <xsl:template match="*" mode="ac:ResourceTypes"/>

    <!-- PROPERTY LIST MODE -->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="ac:PropertyEditor">
        <xsl:variable name="definitions" as="document-node()">
            <xsl:document>
                <dl class="properties">
                    <xsl:apply-templates select="*" mode="#current">
                        <xsl:sort select="ac:property-label(.)" order="ascending" lang="{ac:langs()[1]}"/>
                        <xsl:sort select="if (exists((text(), @rdf:resource, @rdf:nodeID))) then ac:object-label((text(), @rdf:resource, @rdf:nodeID)[1]) else()" order="ascending" lang="{ac:langs()[1]}"/>
                    </xsl:apply-templates>
                </dl>
            </xsl:document>
        </xsl:variable>

        <xsl:apply-templates select="$definitions" mode="ac:PropertyGroups"/>
    </xsl:template>
    
    <xsl:template match="@* | node()" mode="ac:PropertyGroups">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="xhtml:dt[xhtml:span/@title = preceding-sibling::xhtml:dt[1]/xhtml:span/@title]" mode="ac:PropertyGroups" priority="1"/>

    <!-- FORM MODE -->
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="ac:ResourceForm">
        <xsl:apply-templates select="." mode="ac:FormControl">
            <xsl:sort select="ac:label(.)"/>
        </xsl:apply-templates>
    </xsl:template>
    
    <!-- FORM CONTROL MODE -->
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="ac:FormControl" use-when="system-property('xsl:product-name') = 'SAXON'">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="legend" select="if (@rdf:about) then true() else not(key('predicates-by-object', @rdf:nodeID))" as="xs:boolean"/>
        <xsl:param name="violations" select="key('violations-by-root', (@rdf:about, @rdf:nodeID))" as="element()*"/>
        <xsl:param name="constructor" select="if ($ldt:ontology) then ac:construct($ldt:ontology, $ac:forClass, $ldt:base) else ()" as="document-node()?"/>
        <xsl:param name="template" select="$constructor/rdf:RDF/*[@rdf:nodeID][every $type in rdf:type/@rdf:resource satisfies current()/rdf:type/@rdf:resource = $type]" as="element()*"/>
        <xsl:param name="traversed-ids" select="@rdf:*" as="xs:string*" tunnel="yes"/>

        <fieldset>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>

            <!-- name the resource whose statements follow, so stacked resources in one form stay apart -->
            <xsl:if test="$legend">
                <legend>
                    <xsl:value-of select="ac:label(.)"/>
                </legend>
            </xsl:if>

            <xsl:apply-templates select="$violations" mode="ac:Violation"/>

            <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>

            <xsl:if test="not($template)">
                <xsl:message>Template is not defined for resource '<xsl:value-of select="@rdf:about | @rdf:nodeID"/>' with types '<xsl:value-of select="rdf:type/@rdf:resource"/>'</xsl:message>
            </xsl:if>
            <xsl:apply-templates select="* | $template/*[not(concat(namespace-uri(), local-name(), @xml:lang, @rdf:datatype) = current()/*/concat(namespace-uri(), local-name(), @xml:lang, @rdf:datatype))]" mode="#current">
                <xsl:sort select="ac:property-label(.)"/>
                <xsl:with-param name="violations" select="$violations"/>
                <xsl:with-param name="traversed-ids" select="$traversed-ids" tunnel="yes"/>
            </xsl:apply-templates>
        </fieldset>
    </xsl:template>
    
    <!-- LEGEND -->

    <xsl:template match="*[rdf:type/@rdf:resource = $ac:forClass]" mode="ac:Legend" priority="1" use-when="system-property('xsl:product-name') = 'SAXON'">
        <xsl:param name="forClass" select="$ac:forClass" as="xs:anyURI"/>

        <xsl:for-each select="key('resources', $forClass, document(ac:document-uri($forClass)))">
            <legend>
                <xsl:value-of>
                    <xsl:apply-templates select="key('resources', '&ac;ConstructMode', document(ac:document-uri('&ac;')))" mode="ac:label"/>
                </xsl:value-of>
                <xsl:text> </xsl:text>
                <xsl:value-of select="ac:label(.)"/>
            </legend>
            <xsl:if test="ac:description(.)">
                <p class="description">
                    <xsl:apply-templates select="." mode="ac:description"/>
                </p>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="ac:Legend"/>

    <!-- CONSTRAINT VIOLATION  -->
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="ac:Violation"/>

    <xsl:template match="*[rdf:type/@rdf:resource = '&spin;ConstraintViolation']" mode="ac:Violation" priority="1">
        <xsl:param name="class" select="'ldhc-alert va-negative'" as="xs:string?"/>

        <xsl:apply-templates select="." mode="ac:Alert">
            <xsl:with-param name="class" select="$class"/>
            <xsl:with-param name="text" as="item()*">
                <xsl:value-of>
                    <xsl:apply-templates select="." mode="ac:label"/>
                </xsl:value-of>
            </xsl:with-param>
        </xsl:apply-templates>
    </xsl:template>
    
</xsl:stylesheet>