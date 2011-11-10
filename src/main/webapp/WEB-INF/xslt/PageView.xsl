<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:h="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:include href="FrontEndView.xsl"/>

        <xsl:variable name="page-uri" select="resolve-uri(concat('pages/', $relative-uri, '.xml'))"/>
        <xsl:variable name="page" select="document($page-uri)"/>

	<xsl:template name="title">
            <xsl:value-of select="$page//h:h2"/>
	</xsl:template>

	<xsl:template name="head">
            <title>
                <xsl:call-template name="title"/>
            </title>
        </xsl:template>

	<xsl:template name="body-onload"/>

	<xsl:template name="content">
            <xsl:copy-of select="$page"/>
        </xsl:template>

</xsl:stylesheet>