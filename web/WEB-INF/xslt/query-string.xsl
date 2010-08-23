<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template name="query-string">
		<xsl:param name="view-param"/>
		<xsl:param name="offset-param"/>
		<xsl:param name="limit-param"/>
		<xsl:param name="order-by-param"/>
		<xsl:param name="desc-param"/>
		<xsl:param name="desc-default-param"/>
		<xsl:param name="search-param"/>

		<xsl:variable name="temp-qs">
			<xsl:if test="$view-param">view=<xsl:value-of select="$view-param"/>&amp;</xsl:if>
			<xsl:if test="$offset-param">offset=<xsl:value-of select="$offset-param"/>&amp;</xsl:if>
			<xsl:if test="$limit-param">limit=<xsl:value-of select="$limit-param"/>&amp;</xsl:if>
			<xsl:if test="$order-by-param">order-by=<xsl:value-of select="$order-by-param"/>&amp;</xsl:if>
			<xsl:if test="$desc-param != $desc-default-param and $desc-param = false()">asc&amp;</xsl:if>
			<xsl:if test="$desc-param != $desc-default-param and $desc-param = true()">desc&amp;</xsl:if>
			<xsl:if test="$search-param">search=<xsl:value-of select="$search-param"/>&amp;</xsl:if>
		</xsl:variable>
		<xsl:if test="string-length($temp-qs) &gt; 1">?<xsl:value-of select="substring($temp-qs, 1, string-length($temp-qs) - 1)"/></xsl:if>
	</xsl:template>

</xsl:stylesheet>