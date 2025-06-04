<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:mcri18n="http://www.mycore.de/xslt/i18n"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:mcrstring="http://www.mycore.de/xslt/stringutils"
                version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="fn mcri18n mcrstring">

  <xsl:output method="xml" encoding="UTF-8" media-type="application/rss+xml" indent="yes"/>

  <xsl:include href="resource:xslt/functions/stringutils.xsl"/>
  <xsl:include href="resource:xslt/functions/i18n.xsl"/>

  <xsl:param name="CurrentLang"/>
  <xsl:param name="RequestURL"/>
  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match="/response">
    <rss version="2.0">
      <channel>
        <title>
          <xsl:value-of select="concat(mcri18n:translate('thunibib.university.full.name'), ', ', mcri18n:translate('ubo.publications'))"/>
        </title>
        <link>
          <xsl:value-of select="$WebApplicationBaseURL"/>
        </link>
        <description>
          <xsl:value-of select="concat(mcri18n:translate('thunibib.university.full.name'), ', ', mcri18n:translate('ubo.publications'))"/>
        </description>
        <language>
          <xsl:value-of select="$CurrentLang"/>
        </language>
        <xsl:apply-templates select="result/doc"/>
      </channel>
    </rss>
  </xsl:template>

  <xsl:template match="doc">
    <item>
      <title>
        <xsl:value-of select="arr[@name='title']/str[1]"/>
      </title>

      <description>
        <xsl:for-each select="arr[@name='person_aut']/str">
          <xsl:value-of select="."/>
          <xsl:if test="not(position()=fn:last())">
            <xsl:value-of select="'; '"/>
          </xsl:if>
        </xsl:for-each>

        <xsl:if test="int[@name='year']">
          <xsl:value-of select="concat(', ', int[@name='year'])"/>
        </xsl:if>
        <![CDATA[<br/>]]>
        <![CDATA[<strong>]]>
        <xsl:value-of select="arr[@name='title']/str[1]"/>
        <![CDATA[</strong>]]>
      </description>

      <xsl:variable name="url" select="concat($WebApplicationBaseURL, 'receive/', str[@name='id'])"/>
      <link>
        <xsl:value-of select="$url"/>
      </link>
      <guid>
        <xsl:value-of select="$url"/>
      </guid>
    </item>
  </xsl:template>

</xsl:stylesheet>
