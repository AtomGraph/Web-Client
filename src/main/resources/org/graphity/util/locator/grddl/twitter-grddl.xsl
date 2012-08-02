<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright (C) 2012 Martynas Jusevičius <martynas@graphity.org>

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
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY dct "http://purl.org/dc/terms/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY sioc "http://rdfs.org/sioc/ns#">
    <!ENTITY dbpedia "http://dbpedia.org/resource/">
    <!ENTITY dbpedia-owl "http://dbpedia.org/ontology/">
]>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:grddl="http://www.w3.org/2003/g/data-view#"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:dbpedia="&dbpedia;"
xmlns:dbpedia-owl="&dbpedia-owl;"
>

    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

    <xsl:template match="/*[@type = 'array']">
	<rdf:RDF>
<xsl:message>   
    <xsl:copy-of select="."/>
</xsl:message>

	    <xsl:apply-templates/>
	</rdf:RDF>
    </xsl:template>
    
    <xsl:template match="/*[not(@type = 'array')]" priority="1">
<xsl:message>    
    <xsl:copy-of select="."/>
</xsl:message>
	<rdf:RDF>
	    <xsl:next-match/>
	</rdf:RDF>
    </xsl:template>

    <xsl:template match="status">
	<sioc:Post rdf:about="http://api.twitter.com/1/statuses/show/{id}.xml">
	    <rdf:type rdf:resource="http://rdfs.org/sioc/types#MicroblogPost"/>
	    <sioc:link rdf:resource="http://twitter.com/{user/screen_name}/status/{id}"/>

	    <xsl:apply-templates/>
	</sioc:Post>
    </xsl:template>

    <xsl:template match="entities | user_mentions | urls | url[expanded_url] | hashtags">
	<xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="status/user">
	<sioc:has_creator>
	    <xsl:next-match/>
	</sioc:has_creator>
    </xsl:template>
    
    <xsl:template match="user">
	<sioc:UserAccount rdf:about="https://api.twitter.com/1/users/show.xml?screen_name={screen_name}">
	    <sioc:link rdf:resource="http://twitter.com/{screen_name}"/>

	    <xsl:apply-templates/>
	</sioc:UserAccount>
    </xsl:template>

    <xsl:template match="user_mention">
	<sioc:addressed_to>
	    <sioc:UserAccount rdf:about="https://api.twitter.com/1/users/show.xml?screen_name={screen_name}">
		<sioc:link rdf:resource="http://twitter.com/{screen_name}"/>

		<xsl:apply-templates/>
	    </sioc:UserAccount>
	</sioc:addressed_to>
    </xsl:template>

    <xsl:template match="expanded_url">
	<sioc:links_to rdf:resource="{.}"/>
    </xsl:template>

    <xsl:template match="id">
	<dct:identifier>
	    <xsl:value-of select="."/>
	</dct:identifier>
    </xsl:template>
	
    <xsl:template match="created_at">
	<!-- parse Twitter's dateTime format -->
	<!--
	<dct:created rdf:datatype="&xsd;dateTime">
	    <xsl:value-of select="xs:dateTime(.)"/>
	</dct:created>
	-->
    </xsl:template>

    <xsl:template match="text">
	<sioc:content>
	    <xsl:value-of select="."/>
	</sioc:content>
    </xsl:template>

    <xsl:template match="name">
	<sioc:name>
	    <xsl:value-of select="."/>
	</sioc:name>
    </xsl:template>
    
    <xsl:template match="description">
	<dct:description>
	    <xsl:value-of select="."/>
	</dct:description>
    </xsl:template>

    <xsl:template match="profile_image_url">
	<sioc:avatar rdf:resource="{.}"/>
    </xsl:template>

    <xsl:template match="user/url">
	<foaf:homepage rdf:resource="{.}"/>
    </xsl:template>

    <xsl:template match="hashtag">
	<sioc:topic rdf:resource="http://twitter.com/search/{encode-for-uri(text)}"/>
    </xsl:template>

    <!-- ignore other elements, otherwise they will produce unwanted text nodes -->
    <xsl:template match="*"/>

</xsl:stylesheet>