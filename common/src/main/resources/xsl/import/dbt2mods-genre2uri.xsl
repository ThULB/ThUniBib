<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                exclude-result-prefixes="mods xlink xsl">

  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match="@*|node()|comment()">
    <xsl:copy>
      <xsl:apply-templates select='@*|node()|comment()'/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:modsCollection/mods:mods">
    <xsl:copy>
      <xsl:apply-templates select="mods:genre[@type = 'intern']"/>
      <xsl:copy-of select="*[not(contains('genre', local-name()))]"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:genre[@type = 'intern'][not(@authorityURI)][not(@valueURI)]">
    <mods:genre type="intern" authorityURI="{$WebApplicationBaseURL}classifications/ubogenre"
                valueURI="{$WebApplicationBaseURL}classifications/ubogenre#{.}"/>
  </xsl:template>
</xsl:stylesheet>
