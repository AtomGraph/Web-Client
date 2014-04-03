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
    <!ENTITY gpl "http://platform.graphity.org/ontology#">
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
    <!ENTITY skos "http://www.w3.org/2004/02/skos/core#">
]>
<xsl:stylesheet
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:ixsl="http://saxonica.com/ns/interactiveXSLT"
xmlns:prop="http://saxonica.com/ns/html-property"
xmlns:style="http://saxonica.com/ns/html-style-property"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:typeahead="&typeahead;"
xmlns:gc="&gc;"
xmlns:gpl="&gpl;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:sparql="&sparql;"
xmlns:ldp="&ldp;"
xmlns:sd="&sd;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:skos="&skos;"
exclude-result-prefixes="xs prop"
extension-element-prefixes="ixsl"
version="2.0"
xpath-default-namespace="http://www.w3.org/1999/xhtml"
>

    <xsl:import href="typeahead.xsl"/>
    
    <xsl:param name="base-uri-string" as="xs:string"/>
    <xsl:param name="base-uri" select="xs:anyURI($base-uri-string)" as="xs:anyURI"/>
    <xsl:param name="absolute-path-string" as="xs:string"/>
    <xsl:param name="absolute-path" select="xs:anyURI($absolute-path-string)" as="xs:anyURI"/>
    <xsl:param name="lang" select="'en'" as="xs:string"/>
    <xsl:param name="mode-string" as="xs:string"/>
    <xsl:param name="mode" select="xs:anyURI($mode-string)" as="xs:anyURI?"/>

    <xsl:template name="main">
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:OptionMode">
        <xsl:param name="selected" as="xs:string*"/>

        <option value="{@rdf:about | @rdf:nodeID}">
            <xsl:if test="(@rdf:about, @rdf:nodeID) = $selected">
                <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="." mode="gc:LabelMode"/>
        </option>
    </xsl:template>
    
    <!-- <xsl:template match="*" mode="gc:LabelMode"> -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:LabelMode">
        <xsl:choose>
            <xsl:when test="rdfs:label">
                <xsl:value-of select="rdfs:label"/>
            </xsl:when>
            <xsl:when test="dct:title">
                <xsl:value-of select="dct:title"/>
            </xsl:when>
            <xsl:when test="foaf:name">
                <xsl:value-of select="foaf:name"/>
            </xsl:when>
            <xsl:when test="foaf:givenName and foaf:familyName">
                <xsl:value-of select="foaf:givenName"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="foaf:familyName"/>
            </xsl:when>
            <xsl:when test="foaf:nick">
                <xsl:value-of select="foaf:nick"/>
            </xsl:when>
            <xsl:when test="sioc:name">
                <xsl:value-of select="sioc:name"/>
            </xsl:when>
            <xsl:when test="skos:prefLabel">
                <xsl:value-of select="skos:prefLabel"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- EVENTS -->

    <!--
        if ($(this).find('input[name=uri]').val().indexOf('http://') == -1)
        {
            $(this).attr('action', 'resources/labelled');
            $(this).find('input[name=uri]').attr('name', 'query');
            return true;
        }
    -->
    
    <!--
    <xsl:template match="form[tokenize(@class, ' ') = 'navbar-form']" mode="ixsl:onsubmit">
        <xsl:message>form onsubmit BITCH!</xsl:message>
        <xsl:if test="not(starts-with(descendant::input[@name = 'uri']/@prop:value, 'http://'))">
            <ixsl:set-attribute name="action" select="'resources/labelled'"/>
            <xsl:for-each select="descendant::input[@name = 'uri']">
                <ixsl:set-attribute name="name" select="'query'"/>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>
    -->

    <xsl:template match="input[tokenize(@class, ' ') = 'property-typeahead']" mode="ixsl:onkeyup">
        <xsl:apply-imports>
            <xsl:with-param name="menu" select="following-sibling::ul"/>
            <xsl:with-param name="delay" select="150"/>
            <xsl:with-param name="js-function" select="'loadPropertiesXML'"/>
        </xsl:apply-imports>
    </xsl:template>

    <xsl:template match="input[tokenize(@class, ' ') = 'property-typeahead']" mode="ixsl:onblur">
        <xsl:message>ONBLUR!!</xsl:message>
        <xsl:apply-imports>
            <xsl:with-param name="menu" select="following-sibling::ul"/>
        </xsl:apply-imports>
    </xsl:template>
    
    <xsl:template match="ul[tokenize(@class, ' ') = 'dropdown-menu'][tokenize(@class, ' ') = 'property-typeahead']/li" mode="ixsl:onclick">
        <xsl:param name="property-uri" select="input[@name = 'pu']/@prop:value"/>
        <xsl:message>ONCLICK! property URI: <xsl:value-of select="$property-uri"/></xsl:message>

        <xsl:variable name="typeahead-xml" select="ixsl:get(ixsl:window(), 'propertiesXML')"/>
        <xsl:variable name="property" select="$typeahead-xml/rdf:RDF/*[@rdf:about = $property-uri]"/>
        <xsl:message>PROPERTY!?: <xsl:copy-of select="$property"/></xsl:message>

        <!-- control-group -->
        <xsl:result-document href="?select=../../.." method="ixsl:replace-content">
            <xsl:for-each select="$property">
                <label class="control-label"> <!-- for="?" -->
                    <xsl:apply-templates select="." mode="gc:LabelMode"/>
                </label>
                <input type="hidden" name="pu" value="{@rdf:about}"/>

                <div class="btn-group pull-right">
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
                            <input type="text" class="span4 resource-typeahead typeahead"/> <!-- onblur="$('#ul-{$uuid}').hide();" -->
                            <span class="help-inline span2">Resource</span>
                            <span class="help-inline span2 btn-group">
                                <button type="button" value="new" class="btn btn-small btn-toggle" title="Enter new resource URI">New</button>
                                <button type="button" value="existing" class="btn btn-small btn-toggle" disabled="disabled" title="Lookup existing resource">Exists</button>
                            </span>
                            <ul class="resource-typeahead typeahead dropdown-menu" id="ul-{$uuid}" style="display: none;"></ul>
                        </xsl:otherwise>
                    </xsl:choose>
                </div>
            </xsl:for-each>
        </xsl:result-document>
    </xsl:template>

    <xsl:template match="input[tokenize(@class, ' ') = 'resource-typeahead']" mode="ixsl:onkeyup">
        <xsl:apply-imports>
            <xsl:with-param name="menu" select="following-sibling::ul"/>
            <xsl:with-param name="delay" select="150"/>
            <xsl:with-param name="js-function" select="'loadResourcesXML'"/>
        </xsl:apply-imports>
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
                <span class="help-inline span2">Resource</span>
            </xsl:for-each>
        </xsl:result-document>
        <xsl:for-each select="../../../div[tokenize(@class, ' ') = 'pull-right']">
            <xsl:result-document href="?select=." method="ixsl:replace-content">
                <button type="button" class="btn btn-small edit-statement" title="Edit this statement">&#x270e;</button>
                <button type="button" class="btn btn-small remove-statement" title="Remove this statement">&#x2715;</button>
            </xsl:result-document>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="button[tokenize(@class, ' ') = 'btn-toggle']" mode="ixsl:onclick">
        <xsl:for-each select="../button">
            <ixsl:remove-attribute name="disabled"/>
        </xsl:for-each>
        <ixsl:set-attribute name="disabled" select="'disabled'"/>
        
        <xsl:variable name="menu" select="../../ul"/>
        <xsl:if test="@prop:value = 'new'">
            <xsl:for-each select="../../input[@prop:type = 'text']">
                <ixsl:set-attribute name="class" select="string-join(tokenize(@class, ' ')[not(. = 'resource-typeahead')][not(. = 'typeahead')], ' ')"/>
                <ixsl:set-attribute name="name" select="'ou'"/>
            </xsl:for-each>
            <xsl:call-template name="typeahead:hide">
                <xsl:with-param name="menu" select="$menu"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="@prop:value = 'existing'">
            <xsl:for-each select="../../input[@prop:type = 'text']">
                <ixsl:set-attribute name="class" select="concat(@class, ' resource-typeahead typeahead')"/> <!-- concat() all the time -->
                <ixsl:remove-attribute name="name"/>
            </xsl:for-each>
            <!--
            <xsl:apply-templates select="../../input[@prop:type = 'text']" mode="ixsl:onkeyup">
                <xsl:with-param name="menu" select="$menu"/>
                <xsl:with-param name="js-function" select="'loadResourcesXML'"/>
            </xsl:apply-templates>
            -->
        </xsl:if>
    </xsl:template>

    <xsl:template match="button[tokenize(@class, ' ') = 'add-statement']" mode="ixsl:onclick">
	<xsl:message>Add statement</xsl:message>
	<xsl:result-document href="?select=.." method="ixsl:replace-content">
            <label class="typeahead">
                <xsl:variable name="uuid" select="ixsl:call(ixsl:window(), 'generateUUID')"/>
                <input type="text" class="property-typeahead typeahead" /> <!-- onblur="$('#ul-{$uuid}').hide();" -->
                <ul class="property-typeahead typeahead dropdown-menu " id="ul-{$uuid}" style="display: none;"></ul>
            </label>
	</xsl:result-document>
	<xsl:result-document href="?select=../.." method="ixsl:append-content">
	    <xsl:copy-of select=".."/>
	</xsl:result-document>
    </xsl:template>

    <xsl:template match="button[tokenize(@class, ' ') = 'remove-statement']" mode="ixsl:onclick">
	<xsl:message>Remove statement</xsl:message>
	<xsl:for-each select="../..">
	    <ixsl:set-attribute name="style:display" select="'none'"/>
	</xsl:for-each>
    </xsl:template>

    <xsl:template match="button[tokenize(@class, ' ') = 'edit-statement']" mode="ixsl:onclick">
	<xsl:message>Edit statement</xsl:message>
	
        <xsl:for-each select="../../div[tokenize(@class, ' ') = 'controls']">
            <xsl:result-document href="?select=." method="ixsl:replace-content">
                <xsl:variable name="uuid" select="ixsl:call(ixsl:window(), 'generateUUID')"/>
                <input type="text" class="span4 resource-typeahead typeahead"/> <!-- onblur="$('#ul-{$uuid}').hide();" -->
                <span class="help-inline span2">Resource</span>
                <span class="help-inline span2 btn-group">
                    <button type="button" value="new" class="btn btn-small btn-toggle" title="Enter new resource URI">New</button>
                    <button type="button" value="existing" class="btn btn-small btn-toggle" disabled="disabled" title="Lookup existing resource">Exists</button>
                </span>
                <ul class="resource-typeahead typeahead dropdown-menu" id="ul-{$uuid}" style="display: none;"></ul>
            </xsl:result-document>
        </xsl:for-each>
    </xsl:template>

    <!-- CALLBACKS -->
    
    <xsl:template match="ixsl:window()" mode="ixsl:onresourceTypeaheadCallback">
	<xsl:variable name="event" select="ixsl:event()"/>
	<xsl:variable name="target" select="ixsl:get($event, 'target')"/>
        <xsl:variable name="menu" select="$target/following-sibling::ul"/>
        <xsl:message>CALLBACK! Typeahead &lt;select&gt; @id: <xsl:value-of select="$menu/@id"/>
	</xsl:message>

        <xsl:variable name="typeahead-xml" select="ixsl:get(ixsl:window(), 'resourcesXML')"/>
        <xsl:variable name="selected-resources" select="$typeahead-xml/rdf:RDF/*[@rdf:about][not(rdf:type/@rdf:resource = ('&ldp;Container', '&ldp;Page'))]"/>

        <xsl:call-template name="typeahead:process">
            <xsl:with-param name="menu" select="$menu"/>
            <xsl:with-param name="items" select="$selected-resources"/>
            <xsl:with-param name="element" select="$target"/>
            <xsl:with-param name="name" select="'ou'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="ixsl:window()" mode="ixsl:onpropertyTypeaheadCallback">
	<xsl:variable name="event" select="ixsl:event()"/>
	<xsl:variable name="target" select="ixsl:get($event, 'target')"/>
	<xsl:variable name="menu" select="$target/following-sibling::ul"/>	
	<xsl:message>CALLBACK! Typeahead &lt;select&gt; @id: <xsl:value-of select="$menu/@id"/>
	</xsl:message>
        
        <xsl:variable name="typeahead-xml" select="ixsl:get(ixsl:window(), 'propertiesXML')"/>
        <xsl:variable name="selected-resources" select="$typeahead-xml/rdf:RDF/*[not(rdf:type/@rdf:resource = ('&ldp;Container', '&ldp;Page'))]"/>
        
        <xsl:call-template name="typeahead:process">
            <xsl:with-param name="menu" select="$target/following-sibling::ul"/>
            <xsl:with-param name="items" select="$selected-resources"/>
            <xsl:with-param name="element" select="$target"/>
            <xsl:with-param name="name" select="'pu'"/>
        </xsl:call-template>
    </xsl:template>

</xsl:stylesheet>