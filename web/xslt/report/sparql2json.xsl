<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE uridef[
	<!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:sparql="&sparql;"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
exclude-result-prefixes="#all">

    <xsl:template match="sparql:result" mode="vis-type-json">
	<xsl:text>{ 'type': '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
	<xsl:text>' }</xsl:text>
	<xsl:if test="position() != last()">,</xsl:if>
    </xsl:template>

    <xsl:template match="sparql:result" mode="binding-type-json">
	<xsl:text>{ 'type': '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
	<xsl:text>', 'visType': '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'visType']/sparql:uri"/>
	<xsl:text>'</xsl:text>
	<xsl:if test="sparql:binding[@name = 'cardinality']/sparql:literal">
	    <xsl:text>, 'cardinality': </xsl:text>
	    <xsl:value-of select="sparql:binding[@name = 'cardinality']/sparql:literal"/>
	</xsl:if>
	<xsl:if test="sparql:binding[@name = 'minCardinality']/sparql:literal">
	    <xsl:text>, 'minCardinality': </xsl:text>
	    <xsl:value-of select="sparql:binding[@name = 'minCardinality']/sparql:literal"/>
	</xsl:if>
	<xsl:if test="sparql:binding[@name = 'maxCardinality']/sparql:literal">
	    <xsl:text>, 'maxCardinality': </xsl:text>
	    <xsl:value-of select="sparql:binding[@name = 'maxCardinality']/sparql:literal"/>
	</xsl:if>
	<xsl:if test="sparql:binding[@name = 'order']/sparql:literal">
	    <xsl:text>, 'order': </xsl:text>
	    <xsl:value-of select="sparql:binding[@name = 'order']/sparql:literal"/>
	</xsl:if>
	<xsl:text>}</xsl:text>
	<xsl:if test="position() != last()">,</xsl:if>
    </xsl:template>

    <xsl:template match="sparql:result" mode="data-type-json">
	<xsl:text>{ 'type' : '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
	<xsl:text>', 'bindingType' : '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'bindingType']/sparql:uri"/>
	<xsl:text>' }</xsl:text>
	<xsl:if test="position() != last()">, </xsl:if>
    </xsl:template>

    <xsl:template match="sparql:result[sparql:binding[@name = 'option']]" mode="option-json">
	<xsl:text>{ 'option' : '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'option']/sparql:uri"/>
	<xsl:text>', 'type' : '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
	<xsl:text>', 'visType' : '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'visType']/sparql:uri"/>
	<xsl:text>', 'name' : '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'name']/sparql:literal"/>
	<xsl:text>', 'value' : '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'value']/sparql:literal"/>
	<xsl:text>', 'dataType' : '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'value']/sparql:literal/@datatype"/>
	<xsl:text>' }</xsl:text>
	<xsl:if test="position() != last()">, </xsl:if>
    </xsl:template>

    <xsl:template match="sparql:result" mode="binding-element-json">
	<xsl:text>{ 'element' :</xsl:text>
	<xsl:text>document.getElementById('</xsl:text>
	<xsl:value-of select="generate-id()"/>
	<xsl:text>-binding')</xsl:text>
	<xsl:text>, 'bindingType' : '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
	<xsl:text>' }</xsl:text>
	<xsl:if test="position() != last()">,</xsl:if>
    </xsl:template>

    <xsl:template match="sparql:result[sparql:binding[@name = 'visualization']]" mode="visualization-json">
	<xsl:text>{ 'visualization': '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'visualization']/sparql:uri"/>
	<xsl:text>', 'type': '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
	<xsl:text>', 'report' : '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'report']/sparql:uri"/>
	<xsl:text>'</xsl:text>
	<xsl:text>}</xsl:text>
	<xsl:if test="position() != last()">,</xsl:if>
    </xsl:template>

    <xsl:template match="sparql:result[sparql:binding[@name = 'binding']]" mode="binding-json">
	<xsl:text>{ 'binding': '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'binding']/sparql:uri"/>
	<xsl:text>', 'type': '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
	<xsl:text>', 'visType' : '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'visType']/sparql:uri"/>
	<xsl:text>'</xsl:text>
	<xsl:if test="sparql:binding[@name = 'order']/sparql:literal">
	    <xsl:text>, 'order': </xsl:text>
	    <xsl:value-of select="sparql:binding[@name = 'order']/sparql:literal"/>
	</xsl:if>
	<xsl:text>}</xsl:text>
	<xsl:if test="position() != last()">,</xsl:if>
    </xsl:template>

    
    <xsl:template match="sparql:result[sparql:binding[@name = 'variable']]" mode="variable-json">
	<xsl:text>{ 'variable' : </xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'variable']/sparql:literal"/>
	<xsl:text>, 'visType' : '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'visType']/sparql:uri"/>
	<xsl:text>', 'binding' : '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'binding']/sparql:uri"/>
	<xsl:text>', 'bindingType' : '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'bindingType']/sparql:uri"/>
	<xsl:text>' }</xsl:text>
	<xsl:if test="position() != last()">, </xsl:if>
    </xsl:template>

</xsl:stylesheet>