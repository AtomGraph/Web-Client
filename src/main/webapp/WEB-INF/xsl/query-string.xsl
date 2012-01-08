<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of Graphity Analytics package.
Copyright (C) 2009-2011  Martynas Jusevičius

Graphity Analytics is free software: you can redistribute it and/or modify
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