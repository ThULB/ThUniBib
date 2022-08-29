<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:mods="http://www.loc.gov/mods/v3" exclude-result-prefixes="xlink xsl" version="1.0">

  <xsl:variable name="search" select="'https://bibliographie.ub.uni-due.de/'"/>
  <xsl:variable name="replace" select="'https://bibliographie.uni-jena.de/'"/>

  <xsl:template match='@*|node()'>
    <xsl:copy>
      <xsl:apply-templates select='@*|node()'/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:classification[contains(@valueURI, $search) and contains(@authorityURI, $search)]">
    <mods:classification>
      <xsl:attribute name="valueURI">
        <xsl:call-template name="replace-all">
          <xsl:with-param name="text" select="@valueURI"/>
          <xsl:with-param name="find" select="$search"/>
          <xsl:with-param name="replace" select="$replace"/>
        </xsl:call-template>
      </xsl:attribute>

      <xsl:attribute name="authorityURI">
        <xsl:call-template name="replace-all">
          <xsl:with-param name="text" select="@authorityURI"/>
          <xsl:with-param name="find" select="$search"/>
          <xsl:with-param name="replace" select="$replace"/>
        </xsl:call-template>
      </xsl:attribute>
    </mods:classification>

  </xsl:template>

  <xsl:template name="replace-all">
    <xsl:param name="text"/>
    <xsl:param name="find"/>
    <xsl:param name="replace"/>

    <xsl:choose>

      <xsl:when test="$text = '' or $find = ''or not($find)">
        <xsl:value-of select="$text"/>
      </xsl:when>

      <xsl:when test="contains($text, $find)">
        <xsl:value-of select="substring-before($text,$find)"/>
        <xsl:value-of select="$replace"/>
        <xsl:call-template name="replace-all">
          <xsl:with-param name="text" select="substring-after($text,$find)"/>
          <xsl:with-param name="find" select="$find"/>
          <xsl:with-param name="replace" select="$replace"/>
        </xsl:call-template>
      </xsl:when>

      <xsl:otherwise>
        <xsl:value-of select="$text"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
