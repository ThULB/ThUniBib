<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:mods="http://www.loc.gov/mods/v3"
                exclude-result-prefixes="xlink xsl">

  <xsl:param name="WebApplicationBaseURL" select="'https://bibliographie.tu-ilmenau.de/'"/>

  <xsl:template match='@*|node()'>
    <xsl:copy>
      <xsl:apply-templates select='@*|node()'/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:mods">
    <xsl:copy>
      <xsl:apply-templates select='@*|node()'/>

      <mods:classification valueURI="{$WebApplicationBaseURL}classifications/accessrights#moa" authorityURI="{$WebApplicationBaseURL}classifications/accessrights"/>
      <mods:accessCondition xlink:href="{$WebApplicationBaseURL}classifications/licenses#rights_reserved" type="use and reproduction"/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove existing access rights elements -->
  <xsl:template match="mods:classification[contains(@authorityURI, '/classifications/accessrights')]" />

  <!-- Remove existing license elements -->
  <xsl:template match="mods:accessCondition[contains(@xlink:href, '/classifications/licenses') and @type='use and reproduction']" />
</xsl:stylesheet>
