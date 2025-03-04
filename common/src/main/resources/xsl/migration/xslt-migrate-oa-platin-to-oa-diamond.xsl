<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:mods="http://www.loc.gov/mods/v3" exclude-result-prefixes="xlink xsl" version="1.0">

  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match='@*|node()'>
    <xsl:copy>
      <xsl:apply-templates select='@*|node()'/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:classification[contains(@valueURI, 'classifications/oa#platin')]">
    <mods:classification authorityURI="{$WebApplicationBaseURL}classifications/oa"
                         valueURI="{$WebApplicationBaseURL}classifications/oa#diamond"/>
  </xsl:template>
</xsl:stylesheet>
