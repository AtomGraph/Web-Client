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

    <xsl:template match="sparql:result" mode="vis-toggle-json">
	<xsl:text>{ 'element' : </xsl:text>
	<xsl:text>document.getElementById('</xsl:text>
	<xsl:value-of select="generate-id()"/>
	<xsl:text>-toggle')</xsl:text>
	<xsl:text>, 'visType' : '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
	<xsl:text>' }</xsl:text>
	<xsl:if test="position() != last()">,</xsl:if>
    </xsl:template>

    <xsl:template match="sparql:result" mode="vis-fieldset-json">
	<xsl:text>{ 'element' : </xsl:text>
	<xsl:text>document.getElementById('</xsl:text>
	<xsl:value-of select="generate-id()"/>
	<xsl:text>-controls')</xsl:text>
	<xsl:text>, 'visType' : '</xsl:text>
	<xsl:value-of select="sparql:binding[@name = 'type']/sparql:uri"/>
	<xsl:text>' }</xsl:text>
	<xsl:if test="position() != last()">,</xsl:if>
    </xsl:template>

</xsl:stylesheet>