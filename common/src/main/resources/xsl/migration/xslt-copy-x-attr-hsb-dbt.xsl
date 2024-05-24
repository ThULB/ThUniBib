<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="xlink xsl" version="1.0">
  <xsl:output method="xml" version="1.0" indent="yes" encoding="UTF-8"/>

  <xsl:template match="@*|node()|comment()">
    <xsl:copy>
      <xsl:apply-templates select='@*|node()|comment()'/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mycoreclass">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="category[ancestor::category[@ID='x-new']]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>

      <xsl:variable name="text-de" select="label[@xml:lang = 'de']/@text"/>
      <xsl:variable name="text-en" select="//category[label[@text = $text-de]]/label[@xml:lang = 'en']/@text[1]"/>
      <xsl:variable name="x-destatis" select="//category[label[@text = $text-de]]/label[@xml:lang = 'x-destatis']/@text[1]"/>
      <xsl:variable name="x-gnd" select="//category[label[@text = $text-de]]/label[@xml:lang = 'x-gnd']/@text[1]"/>

      <xsl:if test="not(label[@xml:lang = 'en']) and string-length($text-en) &gt; 0">
        <label xml:lang="en" text="{$text-en}"/>
      </xsl:if>

      <xsl:if test="not(label[@xml:lang = 'x-gnd']) and string-length($x-gnd) &gt; 0">
        <label xml:lang="x-gnd" text="{$x-gnd}"/>
      </xsl:if>

      <xsl:if test="not(label[@xml:lang = 'x-destatis']) and string-length($x-destatis) &gt; 0">
        <label xml:lang="x-destatis" text="{$x-destatis}"/>
      </xsl:if>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
