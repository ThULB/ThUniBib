<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="xlink xsl" version="1.0">

  <xsl:template match='@*|node()'>
    <xsl:copy>
      <xsl:apply-templates select='@*|node()' />
    </xsl:copy>
  </xsl:template>

  <xsl:template match="service/servflags/servflag[@type='importID'][contains(text(), 'SCOPUS-')]">
    <xsl:element name="servflag">
      <xsl:copy-of select="@*" />
      <xsl:value-of select="concat(substring-after(text(), 'SCOPUS-'), '-SCOPUS')" />
    </xsl:element>
  </xsl:template>

  <xsl:template match="service/servflags/servflag[@type='importID'][contains(text(), 'SRU-PPN-')]">
    <xsl:element name="servflag">
      <xsl:copy-of select="@*" />
      <xsl:value-of select="concat(substring-after(text(), 'SRU-PPN-'), '-SRU-PPN')" />
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
