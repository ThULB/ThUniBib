<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:mods="http://www.loc.gov/mods/v3" exclude-result-prefixes="xlink xsl" version="1.0">

  <xsl:param name="lang" select="'de'"/>

  <xsl:template match='@*|node()'>
    <xsl:copy>
      <xsl:apply-templates select='@*|node()'/>
    </xsl:copy>
  </xsl:template>

  <xsl:variable name="origin" select="document('classification:metadata:-1:children:ORIGIN')/mycoreclass/categories"/>

  <xsl:template match="mods:classification[contains(@authorityURI, 'ORIGIN')]">
    <xsl:choose>
      <xsl:when test="string-length(substring-after(@valueURI, '#')) &gt; 0">
        <xsl:variable name="categid" select="substring-after(@valueURI, '#')"/>
        <xsl:variable name="label" select="$origin//category[@ID = $categid]/label[@xml:lang = $lang]/@text"/>
        <xsl:variable name="new-categid" select="$origin//category[ancestor::category[@ID='x-new']][not(@ID = $categid)][label[@xml:lang = $lang]/@text = $label]/@ID"/>

        <xsl:choose>
          <xsl:when test="string-length($new-categid) &gt; 0">
            <mods:classification valueURI="{concat(substring-before(@valueURI, '#'), '#', string($new-categid))}"
                                 authorityURI="{@authorityURI}"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy-of select="."/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
