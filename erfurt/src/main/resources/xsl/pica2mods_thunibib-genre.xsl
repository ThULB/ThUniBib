<?xml version="1.0"?>
<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="info:srw/schema/5/picaXML-v1.0"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:zs="http://www.loc.gov/zing/srw/"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="xsl p xlink zs">
  <xsl:mode on-no-match="shallow-copy"/>

  <xsl:include href="pica2mods_thunibib-common.xsl"/>

  <!-- overwrite common/pica2mods_thunibib-genre.xsl-->
  <xsl:template name="ubomodsGenre">
    <xsl:for-each
        select="p:datafield[@tag='244Z']/p:subfield[@code='9'][contains('584746741 1831417162 584747071 183104661X 178537799X 584747101 584747152 1831835967 584747012 584746539 1832938824 584746601 1831751658 1831753405 584746806 1832609855', text())]">

      <xsl:variable name="genre" select="text()"/>
      <mods:genre type="intern">
        <xsl:choose>
          <xsl:when test="$genre='584746601'">article</xsl:when>
          <xsl:when test="$genre='1832609855'">blog</xsl:when>
          <xsl:when test="$genre='584746539'">book</xsl:when>
          <xsl:when test="$genre='584746741'">chapter</xsl:when>
          <xsl:when test="$genre='1831835967'">conference_essay</xsl:when>
          <xsl:when test="$genre='1831417162'">diploma_thesis</xsl:when>
          <xsl:when test="$genre='584747071'">dissertation</xsl:when>
          <xsl:when test="$genre='584747101'">habilitation</xsl:when>
          <xsl:when test="$genre='1831753405'">magister_thesis</xsl:when>
          <xsl:when test="$genre='584747012'">master_thesis</xsl:when>
          <xsl:when test="$genre='1831751658'">preface</xsl:when>
          <xsl:when test="$genre='584747152'">proceedings</xsl:when>
          <xsl:when test="$genre='178537799X'">research_data</xsl:when>
          <xsl:when test="$genre='1832938824'">researchpaper</xsl:when>
          <xsl:when test="$genre='584746806'">review</xsl:when>
          <xsl:when test="$genre='183104661X'">video</xsl:when>
        </xsl:choose>
      </mods:genre>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
