<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template name="sort-page-numbers">
		<xsl:param name="uri"/>
		<xsl:param name="lang-param"/>
		<xsl:param name="view-param"/>
		<xsl:param name="item-count-param"/>
		<xsl:param name="offset-param"/>
		<xsl:param name="limit-param"/>
		<xsl:param name="order-by-param"/>
		<xsl:param name="desc-param"/>
		<xsl:param name="desc-default-param"/>
		<xsl:param name="page-number" select="0"/>
		<xsl:param name="max-numbers" select="6"/>

		<xsl:variable name="page-count" select="ceiling($item-count-param div $limit-param)"/>

                <li>
                    <xsl:choose>
                            <xsl:when test="$page-number mod ceiling($page-count div $max-numbers) = 0 or $offset-param = $page-number * $limit-param or $offset-param = ($page-number + 1) * $limit-param or $offset-param = ($page-number - 1) * $limit-param">
                                    <xsl:choose>
                                            <xsl:when test="$offset-param = $page-number * $limit-param">
                                                    <xsl:value-of select="$page-number * $limit-param + 1"/>&#8211;<xsl:value-of select="($page-number  + 1) * $limit-param"/>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                    <xsl:variable name="number-query-string">
                                                            <xsl:call-template name="query-string">
                                                                    <xsl:with-param name="lang-param" select="$lang-param"/>
                                                                    <xsl:with-param name="view-param" select="$view-param"/>
                                                                    <xsl:with-param name="offset-param" select="$page-number * $limit-param"/>
                                                                    <xsl:with-param name="order-by-param" select="$order-by-param"/>
                                                                    <xsl:with-param name="limit-param" select="$limit-param"/>
                                                                    <xsl:with-param name="desc-param" select="$desc-param"/>
                                                                    <xsl:with-param name="desc-default-param" select="$desc-default-param"/>
                                                            </xsl:call-template>
                                                    </xsl:variable>

                                                    <a class="grey" href="{$uri}{$number-query-string}">
                                                            <xsl:value-of select="$page-number * $limit-param + 1"/>&#8211;<xsl:value-of select="($page-number  + 1) * $limit-param"/>
                                                    </a>
                                            </xsl:otherwise>
                                    </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise>
                                    <xsl:if test="($page-number - 1) mod round($page-count div $max-numbers) = 0">
                                    ...
                                    </xsl:if>
                            </xsl:otherwise>
                    </xsl:choose>
                </li>
		<xsl:if test="$page-number + 1 &lt; $page-count">
			<xsl:call-template name="sort-page-numbers">
                                <xsl:with-param name="uri" select="$uri"/>
				<xsl:with-param name="page-number" select="$page-number + 1"/>
				<xsl:with-param name="lang-param" select="$lang-param"/>
				<xsl:with-param name="view-param" select="$view-param"/>
				<xsl:with-param name="item-count-param" select="$item-count-param"/>
				<xsl:with-param name="offset-param" select="$offset-param"/>
				<xsl:with-param name="order-by-param" select="$order-by-param"/>
				<xsl:with-param name="limit-param" select="$limit-param"/>
				<xsl:with-param name="desc-param" select="$desc-param"/>
				<xsl:with-param name="desc-default-param" select="$desc-default-param"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

	<xsl:template name="sort-paging-controls">
		<xsl:param name="uri"/>
                <xsl:param name="lang-param"/>
		<xsl:param name="view-param"/>
		<xsl:param name="item-count-param"/>
		<xsl:param name="offset-param"/>
		<xsl:param name="limit-param"/>
		<xsl:param name="order-by-param"/>
		<xsl:param name="desc-param"/>
		<xsl:param name="desc-default-param"/>

		<xsl:if test="$item-count-param &gt; 1">
                        <xsl:if test="$item-count-param &gt; $limit-param">
                                <p>
                                    <xsl:choose>
                                            <xsl:when test="$offset-param - $limit-param &gt;= 0">
                                                    <xsl:variable name="previous-query-string">
                                                            <xsl:call-template name="query-string">
                                                                    <xsl:with-param name="lang-param" select="$lang-param"/>
                                                                    <xsl:with-param name="view-param" select="$view-param"/>
                                                                    <xsl:with-param name="offset-param" select="$offset-param - $limit-param"/>
                                                                    <xsl:with-param name="order-by-param" select="$order-by-param"/>
                                                                    <xsl:with-param name="limit-param" select="$limit-param"/>
                                                                    <xsl:with-param name="desc-param" select="$desc-param"/>
                                                                    <xsl:with-param name="desc-default-param" select="$desc-default-param"/>
                                                            </xsl:call-template>
                                                    </xsl:variable>

                                                    <a class="grey" href="{$uri}{$previous-query-string}">&lt; Previous</a>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                    <xsl:text>&lt; Previous</xsl:text>
                                            </xsl:otherwise>
                                    </xsl:choose>
                                </p>

                                <ol class="pagination">
                                    <xsl:call-template name="sort-page-numbers">
                                            <xsl:with-param name="uri" select="$uri"/>
                                            <xsl:with-param name="lang-param" select="$lang-param"/>
                                            <xsl:with-param name="view-param" select="$view-param"/>
                                            <xsl:with-param name="item-count-param" select="$item-count-param"/>
                                            <xsl:with-param name="offset-param" select="$offset-param"/>
                                            <xsl:with-param name="order-by-param" select="$order-by-param"/>
                                            <xsl:with-param name="limit-param" select="$limit-param"/>
                                            <xsl:with-param name="desc-param" select="$desc-param"/>
                                            <xsl:with-param name="desc-default-param" select="$desc-default-param"/>
                                    </xsl:call-template>
                                </ol>

                                <p>
                                    <xsl:choose>
                                            <xsl:when test="$offset-param + $limit-param &lt;= $item-count-param">
                                                    <xsl:variable name="next-query-string">
                                                            <xsl:call-template name="query-string">
                                                                    <xsl:with-param name="lang-param" select="$lang-param"/>
                                                                    <xsl:with-param name="view-param" select="$view-param"/>
                                                                    <xsl:with-param name="offset-param" select="$offset-param + $limit-param"/>
                                                                    <xsl:with-param name="order-by-param" select="$order-by-param"/>
                                                                    <xsl:with-param name="limit-param" select="$limit-param"/>
                                                                    <xsl:with-param name="desc-param" select="$desc-param"/>
                                                                    <xsl:with-param name="desc-default-param" select="$desc-default-param"/>
                                                            </xsl:call-template>
                                                    </xsl:variable>

                                                    <a class="grey" href="{$uri}{$next-query-string}">Next &gt;</a>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                   <xsl:text>Next &gt;</xsl:text>
                                            </xsl:otherwise>
                                    </xsl:choose>
                                </p>
                        </xsl:if>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>