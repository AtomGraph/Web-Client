<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="node()" mode="process-line-breaks">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates mode="process-line-breaks"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="text()" mode="process-line-breaks">
		<xsl:call-template name="process-line-breaks">
			<xsl:with-param name="text" select="."/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="process-line-breaks">
		<xsl:param name="text"/>
		<xsl:variable name="break" select="'&#10;'"/>
		<xsl:variable name="return" select="'&#13;'"/>
		<xsl:choose>
			<xsl:when test="contains($text, $break)">
				<!-- <xsl:value-of select="substring-before($text, $return)"/> -->
				<xsl:value-of select="substring-before($text, $break)"/>
				<br/>
				<xsl:call-template name="process-line-breaks">
					<xsl:with-param name="text" select="substring-after($text, $break)"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$text"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>