<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:mods="http://www.loc.gov/mods/v3" exclude-result-prefixes="xlink xsl" version="1.0">

  <xsl:template match='@*|node()'>
    <xsl:copy>
      <xsl:apply-templates select='@*|node()' />
    </xsl:copy>
  </xsl:template>

  <xsl:template
      match="mods:name[@type='corporate']/mods:role/mods:roleTerm[@authority='marcrelator_corporation']/@authority">
    <xsl:attribute name="authority">
      <xsl:value-of select="'marcrelator'" />
    </xsl:attribute>

  </xsl:template>
</xsl:stylesheet>
