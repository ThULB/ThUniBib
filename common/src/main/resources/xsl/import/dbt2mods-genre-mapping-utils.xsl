<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                exclude-result-prefixes="mcrxml mods xsl xalan xlink">

  <xsl:template match="mods:genre" mode="mir-genre-to-ubogenre">
    <xsl:variable name="genre" select="substring-after(@valueURI, '#')"/>

    <xsl:choose>
      <xsl:when test="mcrxml:isCategoryID('ubogenre', $genre)">
        <xsl:value-of select="$genre"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'others'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="mods:genre[@type = 'intern'][not(@authorityURI)][not(@valueURI)]" mode="mir-genre-to-ubogenre">
    <xsl:variable name="mir-genre" select="."/>

    <xsl:choose>
      <xsl:when test="mcrxml:isCategoryID('ubogenre', $mir-genre)">
        <xsl:value-of select="$mir-genre"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'others'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
