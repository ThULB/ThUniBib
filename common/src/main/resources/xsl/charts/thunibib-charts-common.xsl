<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl">

  <xsl:param name="WebApplicationBaseURL"/>
  <xsl:param name="classification"/>
  <xsl:param name="default-height" select="450"/>
  <xsl:param name="facet"/>

  <xsl:variable name="chart-title-by-facet" select="document(concat('notnull:i18n:thunibib.statistics.chart.title.', $facet))/i18n/text()"/>

  <xsl:variable name="apos">
    <xsl:text>'</xsl:text>
  </xsl:variable>

  <xsl:template match="response" mode="generate-chart-labels">
    <xsl:param name="facet-name"/>
    <xsl:param name="classId"/>

    <xsl:text>[</xsl:text>
    <xsl:for-each select="//lst[@name = $facet-name]/int">
      <xsl:choose>
        <xsl:when test="string-length($classId) &gt; 0 and document(concat('callJava:org.mycore.common.xml.MCRXMLFunctions:isCategoryID:', $classId,':', @name)) = 'true'">
          <xsl:value-of select="concat($apos, document(concat('callJava:org.mycore.common.xml.MCRXMLFunctions:getDisplayName:', $classId,':', @name)), $apos)"/>
        </xsl:when>
        <xsl:when test="document(concat('callJava:org.mycore.common.xml.MCRXMLFunctions:isCategoryID:', $facet-name,':', @name)) = 'true'">
          <xsl:value-of select="concat($apos, document(concat('callJava:org.mycore.common.xml.MCRXMLFunctions:getDisplayName:', $facet-name,':', @name)), $apos)"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="concat($apos, @name, $apos)"/>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:if test="not(position() = last())">
        <xsl:text>,</xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text>]</xsl:text>
  </xsl:template>

  <xsl:template match="response" mode="generate-chart-colors">
    <xsl:param name="facet-name"/>
    <xsl:param name="classId"/>

    <xsl:variable name="classification-to-load">
      <xsl:choose>
        <xsl:when test="string-length($classId) &gt; 0 and document(concat('notnull:classification:metadata:-1:children:', $classId))">
          <xsl:value-of select="$classId"/>
        </xsl:when>
        <xsl:when test="document(concat('notnull:classification:metadata:-1:children:', $facet-name))">
          <xsl:value-of select="$facet-name"/>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsl:if test="$classification-to-load">
      <xsl:variable name="loaded-classification" select="document(concat('notnull:classification:metadata:-1:children:', $classification-to-load))"/>
      <xsl:text>[</xsl:text>
      <xsl:for-each select="//lst[@name='facet_fields']/lst[@name = $facet-name]/int">
        <xsl:variable name="id" select="@name"/>
        <xsl:value-of select="concat($apos, $loaded-classification//category[@ID = $id]/label[lang('x-color')]/@text, $apos)" />
        <xsl:if test="not(position() = last())">
          <xsl:text>,</xsl:text>
        </xsl:if>
      </xsl:for-each>
      <xsl:text>]</xsl:text>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
