<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
    <!ENTITY ac     "https://w3id.org/atomgraph/client#">
]>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:ac="&ac;"
>
  
    <xsl:output method="xml" indent="yes"/>

    <xsl:param name="ac:stylesheet"/>
    <xsl:param name="ac:resolvingUncached"/>
    <xsl:param name="ac:sitemapRules"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="Context">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>

            <xsl:if test="$ac:stylesheet">
                <Parameter name="&ac;stylesheet" value="{$ac:stylesheet}" override="false"/>
            </xsl:if>
            <xsl:if test="$ac:resolvingUncached">
                <Parameter name="&ac;resolvingUncached" value="{$ac:resolvingUncached}" override="false"/>
            </xsl:if>
            <xsl:if test="$ac:sitemapRules">
                <Parameter name="&ac;sitemapRules" value="{$ac:sitemapRules}" override="false"/>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>