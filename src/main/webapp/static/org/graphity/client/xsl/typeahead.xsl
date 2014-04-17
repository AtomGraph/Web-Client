<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright (C) 2013 Martynas Jusevičius <martynas@graphity.org>

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
    <!ENTITY typeahead "http://platform.graphity.org/ontologies/typeahead#">
    <!ENTITY gc "http://client.graphity.org/ontology#">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl "http://www.w3.org/2002/07/owl#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY ldp "http://www.w3.org/ns/ldp#">
    <!ENTITY sd "http://www.w3.org/ns/sparql-service-description#">
    <!ENTITY dct "http://purl.org/dc/terms/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY sioc "http://rdfs.org/sioc/ns#">
]>
<xsl:stylesheet
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:ixsl="http://saxonica.com/ns/interactiveXSLT"
xmlns:prop="http://saxonica.com/ns/html-property"
xmlns:style="http://saxonica.com/ns/html-style-property"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:typeahead="&typeahead;"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:sparql="&sparql;"
xmlns:ldp="&ldp;"
xmlns:sd="&sd;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
exclude-result-prefixes="xs prop"
extension-element-prefixes="ixsl"
version="2.0"
xpath-default-namespace="http://www.w3.org/1999/xhtml"
>

    <!-- MATCH TEMPLATES -->
    
    <xsl:template match="input[tokenize(@class, ' ') = 'typeahead']" mode="ixsl:onkeyup">
        <xsl:param name="menu" select="following-sibling::ul" as="element()"/>
        <xsl:param name="delay" select="0" as="xs:integer"/>
        <xsl:param name="js-function" as="xs:string"/>
        
	<xsl:choose>
	    <xsl:when test="@prop:value">
                <ixsl:schedule-action wait="$delay">
                    <xsl:call-template name="typeahead:load-xml">
                        <xsl:with-param name="element" select="."/>
                        <xsl:with-param name="query" select="@prop:value"/>
                        <xsl:with-param name="js-function" select="$js-function"/>
                    </xsl:call-template>
                </ixsl:schedule-action>
	    </xsl:when>
	    <xsl:otherwise>
                <xsl:call-template name="typeahead:hide">
                    <xsl:with-param name="menu" select="$menu"/>
                </xsl:call-template>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <xsl:template match="input[tokenize(@class, ' ') = 'typeahead']" mode="ixsl:onblur">
        <xsl:param name="menu" as="element()"/>
        
        <xsl:call-template name="typeahead:hide">
            <xsl:with-param name="menu" select="$menu"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:TypeaheadOptionMode">
        <xsl:param name="query" as="xs:string"/>
        <xsl:param name="name" as="xs:string"/>
        <xsl:variable name="label">
            <xsl:apply-templates select="." mode="gc:LabelMode"/>
        </xsl:variable>

        <li>
            <input type="hidden" name="{$name}" value="{@rdf:about}"/>
            
            <a title="{@rdf:about}">
                <xsl:variable name="query-start-pos" select="string-length(substring-before(upper-case($label), upper-case($query))) + 1"/>
                <xsl:variable name="query-end-pos" select="string-length($label) - string-length(substring-after(upper-case($label), upper-case($query))) + 1"/>
                
                <xsl:if test="$query-start-pos &gt; 0">
                    <xsl:value-of select="substring($label, 1, $query-start-pos - 1)"/>
                </xsl:if>
                <strong>
                    <xsl:value-of select="substring($label, $query-start-pos, string-length($query))"/>
                </strong>
                <xsl:value-of select="substring($label, $query-end-pos)"/>
                <xsl:text> </xsl:text>                                
                <span class="pull-right" style="font-size: smaller;">
                    <xsl:apply-templates select="rdf:type/@rdf:resource" mode="gc:ObjectLabelMode"/>
                    <!--
                    <xsl:for-each select="rdf:type/@rdf:resource">
                        <xsl:value-of select="substring-after(., '#')"/>
                        <xsl:if test="position() != last()">
                            <xsl:text> </xsl:text>
                        </xsl:if>
                    </xsl:for-each>
                    -->
                </span>
            </a>
        </li>
    </xsl:template>

<!--
    <xsl:template match="ul[tokenize(@class, ' ') = 'dropdown-menu'][tokenize(@class, ' ') = 'property-typeahead']/li" mode="ixsl:onclick">
        <xsl:param name="property-uri" select="input[@name = 'pu']/@prop:value"/>
        <xsl:message>ONCLICK! property URI: <xsl:value-of select="$property-uri"/></xsl:message>

        <xsl:variable name="typeahead-xml" select="ixsl:get(ixsl:window(), 'propertiesXML')"/>
        <xsl:variable name="property" select="$typeahead-xml/rdf:RDF/*[@rdf:about = $property-uri]"/>
        <xsl:message>PROPERTY!?: <xsl:copy-of select="$property"/></xsl:message>

        <xsl:result-document href="?select=../../.." method="ixsl:replace-content">
            <xsl:for-each select="$property">
                <label class="control-label">
                    <xsl:apply-templates select="." mode="gc:LabelMode"/>
                </label>
                <input type="hidden" name="pu" value="{@rdf:about}"/>

                <div class="btn-group pull-right">
                    <button type="button" class="btn btn-small edit-statement" title="Edit this statement">&#x270e;</button>                
                    <button type="button" class="btn btn-small remove-statement" title="Remove this statement">&#x2715;</button>   
                </div>
                
                <div class="controls controls-row">
                    <xsl:message>PROPERTY TYPE: <xsl:value-of select="rdf:type/@rdf:resource"/> RANGE: <xsl:value-of select="rdfs:range/@rdf:resource"/></xsl:message>
                    <xsl:choose>
                        <xsl:when test="rdf:type/@rdf:resource = '&owl;DatatypeProperty' or rdfs:range/@rdf:resource[. = '&rdfs;Literal' or starts-with(., '&xsd;')]">
                            <input type="text" name="ol"/>
                            
                            <span class="help-inline span2">
                                <xsl:if test="rdfs:range/@rdf:resource[starts-with(., '&xsd;')]">
                                    <select name="lt">
                                        <xsl:for-each select="rdfs:range/@rdf:resource[starts-with(., '&xsd;')]">
                                            <option value="{.}">
                                                <xsl:value-of select="."/>
                                            </option>
                                        </xsl:for-each>
                                    </select>
                                </xsl:if>
                            </span>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:variable name="uuid" select="ixsl:call(ixsl:window(), 'generateUUID')"/>
                            <input type="text" class="resource-typeahead typeahead"/>
                            <ul class="resource-typeahead typeahead dropdown-menu" id="ul-{$uuid}" style="display: none;"></ul>
                        </xsl:otherwise>
                    </xsl:choose>
                </div>
            </xsl:for-each>
        </xsl:result-document>
    </xsl:template>

    <xsl:template match="ul[tokenize(@class, ' ') = 'dropdown-menu'][tokenize(@class, ' ') = 'resource-typeahead']/li" mode="ixsl:onclick">
        <xsl:param name="resource-uri" select="input[@name = 'ou']/@prop:value"/>
        <xsl:message>ONCLICK! resource URI: <xsl:value-of select="$resource-uri"/></xsl:message>

        <xsl:variable name="typeahead-xml" select="ixsl:get(ixsl:window(), 'resourcesXML')"/>
        <xsl:variable name="resource" select="$typeahead-xml/rdf:RDF/*[@rdf:about = $resource-uri]"/>
        <xsl:message>RESOURCE!?: <xsl:copy-of select="$resource"/></xsl:message>

        <xsl:result-document href="?select=../.." method="ixsl:replace-content">
            <xsl:for-each select="$resource">
                <a href="{@rdf:about}" class="btn span4">
                    <xsl:apply-templates select="." mode="gc:LabelMode"/>
                    <input type="hidden" name="ou" value="{@rdf:about}"/>
                </a>
            </xsl:for-each>
        </xsl:result-document>
    </xsl:template>

    -->
    
    <!-- NAMED TEMPLATES -->

    <xsl:template name="typeahead:load-xml">
	<xsl:param name="element" as="element()"/>
        <xsl:param name="query" as="xs:string"/>
        <xsl:param name="js-function" as="xs:string"/>
	<xsl:variable name="event" select="ixsl:event()"/>
        <!-- if the value hasn't changed during the delay -->
        <xsl:if test="$query = $element/@prop:value">
            <xsl:value-of select="ixsl:call(ixsl:window(), $js-function, $event, $query)"/>
        </xsl:if>
    </xsl:template>

    <xsl:template name="typeahead:process">
        <xsl:param name="menu" as="element()"/>
        <xsl:param name="items" as="element()*"/>
        <xsl:param name="element" as="element()"/>
        <xsl:param name="name" as="xs:string"/>

        <xsl:choose>
            <xsl:when test="$items">
                <xsl:call-template name="typeahead:render">
                    <xsl:with-param name="menu" select="$menu"/>
                    <xsl:with-param name="items" select="$items"/>
                    <xsl:with-param name="element" select="$element"/>
                    <xsl:with-param name="name" select="$name"/>
                </xsl:call-template>
                
                <xsl:call-template name="typeahead:show">
                    <xsl:with-param name="element" select="$element"/>
                    <xsl:with-param name="menu" select="$menu"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="typeahead:hide">
                    <xsl:with-param name="menu" select="$menu"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="typeahead:render">
        <xsl:param name="menu" as="element()"/>
        <xsl:param name="items" as="element()*"/>
        <xsl:param name="element" as="element()"/>
        <xsl:param name="name" as="xs:string"/>
        
        <xsl:result-document href="#{$menu/@id}" method="ixsl:replace-content">
            <xsl:apply-templates select="$items" mode="gc:TypeaheadOptionMode">
                <xsl:with-param name="query" select="$element/@prop:value"/>
                <xsl:with-param name="name" select="$name"/>
                <xsl:sort select="rdfs:label[1]"/>
                <xsl:sort select="dct:title[1]"/>
                <xsl:sort select="foaf:name[1]"/>
                <xsl:sort select="foaf:nick[1]"/>
                <xsl:sort select="sioc:name[1]"/>
                <xsl:sort select="@rdf:about"/>
            </xsl:apply-templates>
        </xsl:result-document>
    </xsl:template>
    
    <xsl:template name="typeahead:show">
        <xsl:param name="element" as="element()"/>
        <xsl:param name="menu" as="element()"/>
        
        <xsl:for-each select="$menu">
            <ixsl:set-attribute name="style:display" select="'block'"/>
            <ixsl:set-attribute name="style:top" select="concat($element/@prop:offsetTop + $element/@prop:offsetHeight, 'px')"/>
            <ixsl:set-attribute name="style:left" select="concat($element/@prop:offsetLeft, 'px')"/>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="typeahead:hide">
        <xsl:param name="menu" as="element()"/>

        <xsl:for-each select="$menu">
            <ixsl:set-attribute name="style:display" select="'none'"/>
        </xsl:for-each>
    </xsl:template>
    
</xsl:stylesheet>