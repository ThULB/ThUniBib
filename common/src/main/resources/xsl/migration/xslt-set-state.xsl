<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="xlink xsl" version="1.0">

  <xsl:param name="state" select="'confirmed'" />

  <xsl:template match='@*|node()'>
    <xsl:copy>
      <xsl:apply-templates select='@*|node()' />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="service/servflags/servflag[@type='status']">

    <xsl:element name="servflag">
      <xsl:copy-of select="@*" />
      <xsl:value-of select="$state" />
    </xsl:element>

  </xsl:template>

</xsl:stylesheet>
