<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                exclude-result-prefixes="mods xsl">

  <xsl:import href="xslImport:additional:mycoreobject-additional-hisinone.xsl"/>

  <xsl:template match="mods:mods" mode="additional-metadata">
    <xsl:apply-imports/>
  </xsl:template>

  <xsl:template match="mycoreobject" mode="additional-metadata-card">
    <xsl:apply-imports/>
  </xsl:template>
</xsl:stylesheet>
