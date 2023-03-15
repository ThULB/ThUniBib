<?xml version="1.0"?>
<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="info:srw/schema/5/picaXML-v1.0"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:zs="http://www.loc.gov/zing/srw/"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="xsl p xlink zs">

  <xsl:template name="ubomodsGenre">
    <xsl:for-each
        select="p:datafield[@tag='144Z' or @tag='013D']/p:subfield[@code='9'][contains('480926107 477192068 476643694 476644615 746489978 1727299213 1713916851 47664321X 476643392 477191517 476643597 476643503 476643880 490019234 47664481X 516869523 476644992 476643090 105825778',text())]">
      <!-- ensure values in @tag='144Z' get checked first -->
      <xsl:sort select="../@tag" order="descending"/>

      <xsl:variable name="genre" select="text()"/>
      <mods:genre type="intern">
        <xsl:choose>
          <xsl:when test="$genre='105825778'">thesis</xsl:when>
          <xsl:when test="$genre='480926107'">bachelor_thesis</xsl:when>
          <xsl:when test="$genre='477192068'">chapter</xsl:when>
          <xsl:when test="$genre='476643694'">diploma_thesis</xsl:when>
          <xsl:when test="$genre='476644615'">dissertation</xsl:when>
          <xsl:when test="$genre='746489978'">video_contribution</xsl:when>
          <xsl:when test="$genre='1727299213'">video</xsl:when>
          <xsl:when test="$genre='1713916851'">research_data</xsl:when>
          <xsl:when test="$genre='47664321X'">habilitation</xsl:when>
          <xsl:when test="$genre='476643392'">proceedings</xsl:when>
          <xsl:when test="$genre='477191517'">conference_essay</xsl:when>
          <xsl:when test="$genre='476643597'">master_thesis</xsl:when>
          <xsl:when test="$genre='476643503'">book</xsl:when>
          <xsl:when test="$genre='476643880'">researchpaper</xsl:when>
          <xsl:when test="$genre='490019234'">review</xsl:when>
          <xsl:when test="$genre='47664481X'">series</xsl:when>
          <xsl:when test="$genre='516869523'">abstract</xsl:when>
          <xsl:when test="$genre='476644992'">journal</xsl:when>
          <xsl:when test="$genre='476643090'">article</xsl:when>
        </xsl:choose>
      </mods:genre>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
