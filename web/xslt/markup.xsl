<?xml version="1.0" encoding="ISO-8859-1" ?>
<!--
    markup.xsl - an XSLT stylesheet to markup words and phrases in text
    Copyright (C) 2000  Dr Jeni Tennison

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
    
    Dr Jeni Tennison - http://www.jenitennison.com - mail@jenitennison.com
-->
<!DOCTYPE xsl:stylesheet [
<!ENTITY tab "&#x9;">
<!ENTITY lf "&#xA;">
<!ENTITY cr "&#xD;">
<!ENTITY nbsp "&#160;">
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">


<xsl:variable name="punctuation">
	<xsl:text>.,:;!?&tab;&cr;&lf;&nbsp; &quot;'()[]&lt;>{}</xsl:text>
</xsl:variable>
<xsl:variable name="lowercase" select="'abcdefghijklmnopqrstuvwxyz'" />
<xsl:variable name="uppercase" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

<xsl:template name="markup">
  <xsl:param name="text" />
  <xsl:param name="phrases" />
  <xsl:param name="words-only" select="true()" />
  <xsl:param name="first-only" select="false()" />
  <xsl:param name="match-case" select="false()" />
  <xsl:variable name="lcase-text" select="translate($text, $uppercase, $lowercase)" />
  <xsl:variable name="included-phrases"
                select="$phrases[($match-case and contains($text, .)) or
                                 (not($match-case) and contains($lcase-text,
                                                                translate(., $uppercase, $lowercase)))]" />
  <xsl:choose>
  	<xsl:when test="$included-phrases">
		  <xsl:for-each select="$included-phrases">
		  	<xsl:sort select="string-length(.)" data-type="number" order="descending" />
		  	<xsl:if test="position() = 1">
				  <xsl:variable name="phrase" select="." />
				  <xsl:variable name="word" select="string($phrase)" />
				  <xsl:variable name="remaining" select="$included-phrases[. != $word]" />
				  <xsl:variable name="match">
				  	<xsl:choose>
				  		<xsl:when test="$words-only">
						  	<xsl:call-template name="get-first-word">
						  		<xsl:with-param name="text" select="$text" />
						  		<xsl:with-param name="word" select="$word" />
						  		<xsl:with-param name="match-case" select="$match-case" />
						  	</xsl:call-template>
						  </xsl:when>
						  <xsl:otherwise><xsl:value-of select="$word" /></xsl:otherwise>
						</xsl:choose>
				  </xsl:variable>
				  <xsl:choose>
				  	<xsl:when test="string($match)">
			  			<xsl:variable name="first">
			  				<xsl:if test="contains($punctuation, substring($match, 1, 1))"><xsl:value-of select="substring($match, 1, 1)" /></xsl:if>
			  			</xsl:variable>
			  			<xsl:variable name="last">
			  				<xsl:if test="contains($punctuation, substring($match, string-length($match)))"><xsl:value-of select="substring($match, string-length($match))" /></xsl:if>			  				
			  			</xsl:variable>
				  		<xsl:variable name="replace" select="substring($match, string-length($first) + 1,
				  		                                                       string-length($match) - (string-length($first) + string-length($last)))" />
						  <xsl:choose>
						    <xsl:when test="$remaining">
					        <xsl:call-template name="markup">
					          <xsl:with-param name="text" select="concat(substring-before($text, $match), $first)" />
					          <xsl:with-param name="phrases" select="$remaining" />
									  <xsl:with-param name="words-only" select="$words-only" />
									  <xsl:with-param name="first-only" select="$first-only" />
									  <xsl:with-param name="match-case" select="$match-case" />
					        </xsl:call-template>
					        <xsl:apply-templates select="$phrase" mode="markup">
					        	<xsl:with-param name="word" select="$replace" />
					        </xsl:apply-templates>
						      <xsl:choose>
						      	<xsl:when test="$first-only">
							        <xsl:call-template name="markup">
							          <xsl:with-param name="text" select="concat($last, substring-after($text, $match))" />
							          <xsl:with-param name="phrases" select="$remaining" />
											  <xsl:with-param name="words-only" select="$words-only" />
											  <xsl:with-param name="first-only" select="$first-only" />
											  <xsl:with-param name="match-case" select="$match-case" />
							        </xsl:call-template>
							      </xsl:when>
							      <xsl:otherwise>
							        <xsl:call-template name="markup">
							          <xsl:with-param name="text" select="concat($last, substring-after($text, $match))" />
							          <xsl:with-param name="phrases" select="$included-phrases" />
											  <xsl:with-param name="words-only" select="$words-only" />
											  <xsl:with-param name="first-only" select="$first-only" />
											  <xsl:with-param name="match-case" select="$match-case" />
							        </xsl:call-template>
							      </xsl:otherwise>
							    </xsl:choose>
						    </xsl:when>
						    <xsl:otherwise>
						      <xsl:value-of select="concat(substring-before($text, $match), $first)" />
					        <xsl:apply-templates select="$phrase" mode="markup">
					        	<xsl:with-param name="word" select="$replace" />
					        </xsl:apply-templates>
						      <xsl:value-of select="concat($last, substring-after($text, $match))" />
						    </xsl:otherwise>
						  </xsl:choose>
						</xsl:when>
						<xsl:otherwise>
							<xsl:choose>
								<xsl:when test="$remaining">
									<xsl:call-template name="markup">
										<xsl:with-param name="text" select="$text" />
										<xsl:with-param name="phrases" select="$remaining" />
									  <xsl:with-param name="words-only" select="$words-only" />
									  <xsl:with-param name="first-only" select="$first-only" />
									  <xsl:with-param name="match-case" select="$match-case" />
									</xsl:call-template>
								</xsl:when>
								<xsl:otherwise><xsl:value-of select="$text" /></xsl:otherwise>
							</xsl:choose>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
			</xsl:for-each>
	  	</xsl:when>
  	<xsl:otherwise><xsl:value-of select="$text" /></xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="get-first-word">
	<xsl:param name="text" />
	<xsl:param name="word" />
	<xsl:param name="match-case" select="false()" />
	<xsl:choose>
		<xsl:when test="$match-case">
			<xsl:call-template name="get-first-word-matching-case">
				<xsl:with-param name="text" select="$text" />
				<xsl:with-param name="word" select="$word" />
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
			<xsl:call-template name="get-first-word-non-matching-case">
				<xsl:with-param name="text" select="$text" />
				<xsl:with-param name="word" select="$word" />
			</xsl:call-template>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="get-first-word-matching-case">
	<xsl:param name="text" />
	<xsl:param name="word" />
	<xsl:variable name="before" select="substring-before($text, $word)" />
	<xsl:variable name="after" select="substring-after($text, $word)" />
	<xsl:variable name="punc-before" select="contains($punctuation, substring($before, string-length($before), 1))" />
	<xsl:variable name="punc-after" select="contains($punctuation, substring($after, 1, 1))" />
	<xsl:choose>
		<xsl:when test="not(contains($text, $word))" />
		<xsl:when test="$punc-before and $punc-after">
			<xsl:value-of select="substring($text, string-length($before), string-length($word) + 2)" />
		</xsl:when>
		<xsl:when test="$text = $word">
			<xsl:value-of select="$word" />
		</xsl:when>
		<xsl:when test="$punc-after and starts-with($text, $word)">
			<xsl:value-of select="substring($text, 1, string-length($word) + 1)" />
		</xsl:when>
		<xsl:when test="$punc-before and not(substring-after($text, $word))">
			<xsl:value-of select="substring($text, string-length($text) - string-length($word))" />
		</xsl:when>
		<xsl:when test="contains($after, $word)">
			<xsl:call-template name="get-first-word-matching-case">
				<xsl:with-param name="text" select="$after" />
				<xsl:with-param name="word" select="$word" />
			</xsl:call-template>
		</xsl:when>
	</xsl:choose>	
</xsl:template>

<xsl:template name="get-first-word-non-matching-case">
	<xsl:param name="text" />
	<xsl:param name="word" />
	<xsl:variable name="lcase-text" select="translate($text, $uppercase, $lowercase)" />
	<xsl:variable name="lcase-word" select="translate($word, $uppercase, $lowercase)" />
	<xsl:variable name="before" select="substring($text, 1, string-length(substring-before($lcase-text, $lcase-word)))" />
	<xsl:variable name="after" select="substring($text, string-length($before) + string-length($word) + 1)" />
	<xsl:variable name="punc-before" select="contains($punctuation, substring($before, string-length($before), 1))" />
	<xsl:variable name="punc-after" select="contains($punctuation, substring($after, 1, 1))" />
	<xsl:choose>
		<xsl:when test="not(contains($lcase-text, $lcase-word))" />
		<xsl:when test="$punc-before and $punc-after">
			<xsl:value-of select="substring($text, string-length($before), string-length($word) + 2)" />
		</xsl:when>
		<xsl:when test="$lcase-text = $lcase-word">
			<xsl:value-of select="$text" />
		</xsl:when>
		<xsl:when test="$punc-after and starts-with($lcase-text, $lcase-word)">
			<xsl:value-of select="substring($text, 1, string-length($word) + 1)" />
		</xsl:when>
		<xsl:when test="$punc-before and not(substring-after($lcase-text, $lcase-word))">
			<xsl:value-of select="substring($text, string-length($text) - string-length($word))" />
		</xsl:when>
		<xsl:when test="contains(translate($after, $uppercase, $lowercase), $lcase-word)">
			<xsl:call-template name="get-first-word-non-matching-case">
				<xsl:with-param name="text" select="$after" />
				<xsl:with-param name="word" select="$word" />
			</xsl:call-template>
		</xsl:when>
	</xsl:choose>
</xsl:template>

<xsl:template match="*" mode="markup">
	<xsl:param name="word" />
	<a href="{@id}.html">
		<xsl:value-of select="$word" />
	</a>
</xsl:template>

</xsl:stylesheet>
