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
    <!ENTITY time "http://www.w3.org/2006/time#">
    <!ENTITY tzont "http://www.w3.org/2006/timezone#">
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
xmlns:time="&time;"
xmlns:tzont="&tzont;"
>

    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
    
    <xsl:param name="base-uri" as="xs:anyURI"/>
    <xsl:param name="uri" as="xs:anyURI"/>

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

    <xsl:template match="@type[. = 'date']">
	<xsl:attribute name="rdf:datatype">&xsd;date</xsl:attribute>
    </xsl:template>

    <xsl:template match="@type[. = 'datetime']">
	<xsl:attribute name="rdf:datatype">&xsd;dateTime</xsl:attribute>
    </xsl:template>

    <!-- PEOPLE -->

    <xsl:template match="person[id]">
	<!--
	<sioc:UserAccount rdf:nodeID="person{id}">
	    <sioc:account_of>
	    	<foaf:Person rdf:about="{resolve-uri(concat('/people/', id, '#person'), $base-uri)}">
		    <xsl:apply-templates/>
		</foaf:Person>
	    </sioc:account_of>

	    <xsl:apply-templates select="user-name"/>
	</sioc:UserAccount>
	-->
	<foaf:Person rdf:about="{resolve-uri(concat('/people/', id, '#person'), $base-uri)}">
	    <xsl:apply-templates/>
	    
	    <!-- these links are part of HATEOS, but missing in Basecamp API (implicit in URI pattern) -->
	    <xsl:if test="matches($uri, concat($base-uri, '/projects/(\d+)/people'))">
		<foaf:currentProject rdf:resource="{substring-before($uri, '/people')}"/>
	    </xsl:if>
	</foaf:Person>
    </xsl:template>

    <xsl:template match="first-name">
	<foaf:firstName>
	    <xsl:value-of select="."/>
	</foaf:firstName>
    </xsl:template>

    <xsl:template match="last-name">
	<foaf:lastName>
	    <xsl:value-of select="."/>
	</foaf:lastName>
    </xsl:template>

    <xsl:template match="id">
	<dct:identifier>
	    <xsl:value-of select="."/>
	</dct:identifier>
    </xsl:template>

    <xsl:template match="user-name[text()]">
	<sioc:name>
	    <xsl:value-of select="."/>
	</sioc:name>
    </xsl:template>

    <xsl:template match="email-address">
	<foaf:mbox rdf:resource="mailto:{.}"/>
    </xsl:template>

    <xsl:template match="avatar-url">
	<foaf:depiction rdf:resource="{.}"/>
    </xsl:template>

    <xsl:template match="created-at">
	<dct:created>
	    <xsl:apply-templates select="@type"/>
	    <xsl:value-of select="."/>
	</dct:created>
    </xsl:template>

    <xsl:template match="created-on">
	<dct:created>
	    <xsl:apply-templates select="@type"/>
	    <xsl:value-of select="."/>
	    <xsl:text>Z</xsl:text>
	</dct:created>
    </xsl:template>

    <xsl:template match="updated-at | last-changed-on">
	<dct:modified>
	    <xsl:apply-templates select="@type"/>
	    <xsl:value-of select="."/>
	</dct:modified>
    </xsl:template>

    <xsl:template match="posted-on">
	<dct:issued>
	    <xsl:apply-templates select="@type"/>
	    <xsl:value-of select="."/>
	</dct:issued>
    </xsl:template>

    <xsl:template match="person/company-id">
	<foaf:member rdf:resource="{resolve-uri(concat('/companies/', ., '#company'), $base-uri)}"/>
    </xsl:template>

    <!-- MESSAGES -->
    
    <xsl:template match="post[id]">
	<sioc:Post rdf:about="{resolve-uri(concat('/posts/', id), $base-uri)}">
	    <xsl:apply-templates/>
	</sioc:Post>
    </xsl:template>

    <xsl:template match="post/author-id">
	<sioc:has_creator rdf:nodeID="person{.}"/>
    </xsl:template>

    <xsl:template match="post/project-id">
	<sioc:has_container rdf:resource="{resolve-uri(concat('/projects/', ., '/posts'), $base-uri)}"/>
    </xsl:template>

    <xsl:template match="comments-count[. &gt; 0]">
	<sioc:num_items>
	    <xsl:value-of select="."/>
	</sioc:num_items>
    </xsl:template>

    <xsl:template match="title[text()]">
	<dct:title>
	    <xsl:value-of select="."/>
	</dct:title>
    </xsl:template>

    <xsl:template match="body">
	<sioc:content>
	    <xsl:value-of select="."/>
	</sioc:content>
    </xsl:template>

    <!-- COMPANIES -->

    <xsl:template match="company[id]">
	<foaf:Organization rdf:about="{resolve-uri(concat('/companies/', id, '#company'), $base-uri)}">
	    <rdf:type rdf:resource="&dbpedia-owl;Company"/>
	    <xsl:apply-templates/>
	    
	    <rdfs:seeAlso rdf:resource="{resolve-uri(concat('/companies/', id, '/people'), $base-uri)}"/>
	</foaf:Organization>
    </xsl:template>

    <xsl:template match="name">
	<foaf:name>
	    <xsl:value-of select="."/>
	</foaf:name>
    </xsl:template>

    <xsl:template match="web-address[text()]">
	<foaf:homepage rdf:resource="{.}"/>
    </xsl:template>

    <xsl:template match="address-one">
    </xsl:template>

    <xsl:template match="zip">
	<dbpedia-owl:postalCode>
	    <xsl:value-of select="."/>
	</dbpedia-owl:postalCode>
    </xsl:template>

    <xsl:template match="city[text()]">
	<dbpedia-owl:city rdf:resource="&dbpedia;{encode-for-uri(.)}"/>
	<foaf:based_near rdf:resource="&dbpedia;{encode-for-uri(.)}"/>
    </xsl:template>

    <xsl:template match="country[text()]">
	<dbpedia-owl:country rdf:resource="&dbpedia;{encode-for-uri(.)}"/>
    </xsl:template>

    <xsl:template match="phone-number-office[text()]">
	<foaf:phone rdf:resource="tel:{.}"/>
    </xsl:template>

    <!-- PROJECTS -->

    <xsl:template match="project[id]">
	<foaf:Project rdf:about="{resolve-uri(concat('/projects/', id, '#project'), $base-uri)}">
	    <xsl:apply-templates/>
	    
	    <rdfs:seeAlso rdf:resource="{resolve-uri(concat('/projects/', id, '/posts'), $base-uri)}"/>	    
	    <rdfs:seeAlso rdf:resource="{resolve-uri(concat('/projects/', id, '/people'), $base-uri)}"/>
	    <rdfs:seeAlso rdf:resource="{resolve-uri(concat('/projects/', id, '/todo_lists'), $base-uri)}"/>
	    <rdfs:seeAlso rdf:resource="{resolve-uri(concat('/projects/', id, '/companies'), $base-uri)}"/>
	</foaf:Project>
    </xsl:template>

    <xsl:template match="project/company">
	
    </xsl:template>

    <xsl:template match="account">
	<foaf:OnlineAccount rdf:about="{$base-uri}"> <!-- {resolve-uri('/account', $base-uri)} -->
	    <xsl:apply-templates/>
	</foaf:OnlineAccount>
    </xsl:template>
	
    <!--
    <xsl:template match="category">
	<skos:Concept rdf:resource="{.}"/>
    </xsl:template>
    -->

    <!-- CALENDAR -->
    
    <xsl:template match="calendar-entry[id][project-id]">
	<time:Instant rdf:about="{resolve-uri(concat('/projects/', id, '/calendar_entries/', id, '#time'), $base-uri)}">
	    <xsl:apply-templates/>
	</time:Instant>
    </xsl:template>
    
    <xsl:template match="deadline">
	<time:inXSDDateTime>
	    <xsl:apply-templates select="@type"/>
	    <xsl:value-of select="."/>
	</time:inXSDDateTime>
    </xsl:template>
	
    <!-- ignore other elements, otherwise they will produce unwanted text nodes -->
    <xsl:template match="*"/>

</xsl:stylesheet>