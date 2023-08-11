<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3"
                exclude-result-prefixes="xsl">

  <xsl:include href="resource:xsl/mods-preprocessor.xsl"/>

  <xsl:template match="mods:subject">
    <mods:subject>
      <mods:topic>
        <xsl:for-each select="mods:topic">
          <xsl:value-of select="text()"/>
          <xsl:if test="not(position() = last())">
            <xsl:text>::</xsl:text>
          </xsl:if>
        </xsl:for-each>
      </mods:topic>
    </mods:subject>
  </xsl:template>

</xsl:stylesheet>
