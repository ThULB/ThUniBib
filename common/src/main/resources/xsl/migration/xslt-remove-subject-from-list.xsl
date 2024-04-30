<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="xlink xsl">

  <xsl:template match='@*|node()'>
    <xsl:copy>
      <xsl:apply-templates select='@*|node()'/>
    </xsl:copy>
  </xsl:template>

  <xsl:variable name="blacklist"
                select="'Verfasser Bachelor-Arbeit Masterarbeit Dissertation Monographie Buchbeitrag Kongressbeitrag Tagungsbeitrag - Abstract Zeitschriftenaufsatz referiert Buchhandelsausgabe'"/>

  <xsl:template match="mods:subject">
    <xsl:variable name="blacklisted-topic-count" select="count(mods:topic[contains($blacklist, text()) ])"/>
    <xsl:variable name="whitelisted-topic-count" select="count(mods:topic) - $blacklisted-topic-count"/>

    <xsl:choose>
      <xsl:when test="$whitelisted-topic-count = 0"/>
      <xsl:when test="$blacklisted-topic-count = 0">
        <mods:subject>
          <xsl:copy-of select="@*"/>
          <xsl:copy-of select="*"/>
        </mods:subject>
      </xsl:when>
      <xsl:otherwise>
        <mods:subject>
          <xsl:copy-of select="@*"/>
          <xsl:for-each select="mods:topic[not(contains($blacklist, text()))]">
            <mods:topic>
              <xsl:copy-of select="@*"/>
              <xsl:value-of select="."/>
            </mods:topic>
          </xsl:for-each>
        </mods:subject>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>
</xsl:stylesheet>
