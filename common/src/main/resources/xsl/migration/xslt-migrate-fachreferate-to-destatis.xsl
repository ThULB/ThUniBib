<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:mods="http://www.loc.gov/mods/v3" exclude-result-prefixes="xlink xsl" version="1.0">

  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match='@*|node()'>
    <xsl:copy>
      <xsl:apply-templates select='@*|node()'/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:classification[contains(@authorityURI, 'fachreferate')]">
    <xsl:variable name="destatis-categid" select="substring-after(@valueURI, '#')"/>
    <mods:classification valueURI="{$WebApplicationBaseURL}classifications/destatis#{$destatis-categid}" authorityURI="{$WebApplicationBaseURL}classifications/destatis"/>
  </xsl:template>
</xsl:stylesheet>
