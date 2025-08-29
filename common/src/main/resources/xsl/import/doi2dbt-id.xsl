<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                exclude-result-prefixes="xsl">

  <xsl:template match="/response">
    <mods:mods>
      <xsl:apply-templates select="result/doc"/>
    </mods:mods>
  </xsl:template>

  <xsl:template match="doc">
    <mods:identifier type="dbt">
      <xsl:value-of select="str[@name='id']"/>
    </mods:identifier>
  </xsl:template>
</xsl:stylesheet>
