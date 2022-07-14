<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:mods="http://www.loc.gov/mods/v3" exclude-result-prefixes="xlink xsl" version="1.0">

  <xsl:param name="WebApplicationBaseURL" select="'http://localhost:8080/'" />
  <xsl:param name="classid" select="'project'" />
  <xsl:param name="categid" select="'nbe'" />

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select='@*|node()' />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:mods">
    <xsl:copy>
      <xsl:apply-templates select='@*|node()' />

      <xsl:if test="count(mods:classification[contains(@valueURI, $classid)]) = 0">
        <mods:classification valueURI="{$WebApplicationBaseURL}classifications/{$classid}#{$categid}" authorityURI="{$WebApplicationBaseURL}classifications/{$classid}" />
      </xsl:if>

    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
