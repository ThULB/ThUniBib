<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:mcrstring="http://www.mycore.de/xslt/stringutils"
                xmlns:atom="http://www.w3.org/2005/Atom"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="3.0"
                exclude-result-prefixes="fn mcri18n mcrstring">

  <xsl:output method="xml" encoding="UTF-8" media-type="application/atom+xml" indent="yes"/>

  <xsl:include href="resource:xslt/functions/stringutils.xsl"/>
  <xsl:include href="resource:xslt/functions/i18n.xsl"/>

  <xsl:param name="CurrentLang"/>
  <xsl:param name="MCR.OAIDataProvider.OAI.RepositoryName"/>
  <xsl:param name="RequestURL"/>
  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match="/response">
    <atom:feed>
      <link rel="self" href="{$WebApplicationBaseURL}servlets/solr/feed?XSL.Style=atomfeed"/>
      <link rel="alternate" href="{$WebApplicationBaseURL}"/>

      <title>
        <xsl:value-of select="$MCR.OAIDataProvider.OAI.RepositoryName"/>
      </title>

      <subtitle>
        <xsl:value-of select="''"/>
      </subtitle>

      <id>
        <xsl:value-of select="$WebApplicationBaseURL"/>
      </id>

      <updated>
        <xsl:value-of select="result/doc[1]/date[@name='modified']"/>
      </updated>

      <icon>
        <xsl:value-of select="concat($WebApplicationBaseURL, 'favicon.ico')"/>
      </icon>

      <xsl:apply-templates select="result/doc"/>
    </atom:feed>
  </xsl:template>

  <xsl:template match="doc">
    <entry>
      <title>
        <xsl:value-of select="arr[@name='title']/str[1]"/>
      </title>
      <link rel="alternate" href="{$WebApplicationBaseURL}receive/{str[@name='id']}"/>

      <id>
        <xsl:value-of select="concat($WebApplicationBaseURL,'receive/', str[@name='id'])"/>
      </id>

      <category term="{str[@name='genre']}" label="{document(concat('notnull:callJava:org.mycore.common.xml.MCRXMLFunctions:getDisplayName:ubogenre:', str[@name='genre']))}"/>

      <published>
        <xsl:value-of select="date[@name='created']"/>
      </published>

      <updated>
        <xsl:value-of select="date[@name='modified']"/>
      </updated>

      <xsl:for-each select="arr[@name='person_aut']/str">
        <author>
          <name>
            <xsl:value-of select="."/>
          </name>
        </author>
      </xsl:for-each>

      <summary>
        <xsl:if test="fn:string-length(arr[@name='title']/str[1]) &gt; 0 ">
          <xsl:value-of select="mcrstring:shorten(arr[@name='title']/str[1], 256, '…')"/>
        </xsl:if>
      </summary>

      <content type="xhtml">
        <div xmlns="http://www.w3.org/1999/xhtml">
          <xsl:value-of select="mcrstring:shorten(arr[@name='title']/str[1], 256, '…')"/>
        </div>
      </content>
    </entry>
  </xsl:template>

</xsl:stylesheet>
