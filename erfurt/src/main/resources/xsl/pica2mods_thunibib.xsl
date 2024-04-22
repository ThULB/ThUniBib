<?xml version="1.0"?>
<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="info:srw/schema/5/picaXML-v1.0"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:zs="http://www.loc.gov/zing/srw/"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="xsl p xlink zs">
  <xsl:mode on-no-match="shallow-copy"/>

  <xsl:import href="default/pica2mods-default-titleInfo.xsl"/>
  <xsl:import href="default/pica2mods-default-name.xsl"/>
  <xsl:import href="default/pica2mods-default-identifier.xsl"/>
  <xsl:import href="default/pica2mods-default-language.xsl"/>
  <xsl:import href="default/pica2mods-default-location.xsl"/>
  <xsl:import href="default/pica2mods-default-physicalDescription.xsl"/>
  <xsl:import href="default/pica2mods-default-originInfo.xsl"/>
  <xsl:import href="default/pica2mods-default-genre.xsl"/>
  <xsl:import href="default/pica2mods-default-recordInfo.xsl"/>
  <xsl:import href="default/pica2mods-default-note.xsl"/>
  <xsl:import href="default/pica2mods-default-abstract.xsl"/>
  <xsl:import href="default/pica2mods-default-subject.xsl"/>
  <xsl:import href="default/pica2mods-default-relatedItem.xsl"/>

  <xsl:import href="_common/pica2mods-pica-PREPROCESSING.xsl"/>
  <xsl:import href="_common/pica2mods-functions.xsl"/>

  <xsl:import href="pica2mods_thunibib-common.xsl"/>

  <xsl:param name="MCR.PICA2MODS.CONVERTER_VERSION" select="'Pica2Mods 2.1'"/>
  <xsl:param name="MCR.PICA2MODS.DATABASE" select="'k10plus'"/>
  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match="zs:searchRetrieveResponse">
    <xsl:apply-templates select="//p:record"/>
  </xsl:template>

  <xsl:template match="p:record">
    <mods:mods>
      <xsl:call-template name="modsTitleInfo"/>
      <xsl:call-template name="modsAbstract"/>
      <xsl:call-template name="modsName"/>
      <xsl:call-template name="modsIdentifier"/>
      <xsl:call-template name="ubomodsLanguage"/>
      <xsl:call-template name="modsPhysicalDescription"/>
      <xsl:call-template name="modsOriginInfo"/>
      <xsl:call-template name="ubomodsGenre"/>
      <xsl:call-template name="modsLocation"/>
      <xsl:call-template name="modsRecordInfo"/>
      <xsl:call-template name="modsNote"/>
      <xsl:call-template name="modsRelatedItem"/>
      <xsl:call-template name="modsSubject"/>
      <xsl:call-template name="uboTypeOfResource"/>
      <xsl:call-template name="uboPeerReview"/>
      <xsl:call-template name="uboMediaType"/>
      <xsl:call-template name="uboOriginClassification"/>
    </mods:mods>
  </xsl:template>

  <xsl:template name="ubomodsGenre">
    <xsl:for-each
        select="p:datafield[@tag='244Z']/p:subfield[@code='9'][contains('584746741 1831417162 584747071 183104661X 178537799X 584747101 584747152 1831835967 584747012 584746539 1832938824 584746601 1831751658 1831753405 584746806 1832609855', text())]">

      <xsl:variable name="genre" select="text()"/>
      <mods:genre type="intern" authorityURI="{$WebApplicationBaseURL}classifications/ubogenre">
        <xsl:attribute name="valueURI">
          <xsl:choose>
            <xsl:when test="$genre='584746601'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#article')"/></xsl:when>
            <xsl:when test="$genre='1832609855'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#blog')"/></xsl:when>
            <xsl:when test="$genre='584746539'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#book')"/></xsl:when>
            <xsl:when test="$genre='584746741'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#chapter')"/></xsl:when>
            <xsl:when test="$genre='1831835967'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#conference_essay')"/></xsl:when>
            <xsl:when test="$genre='1831417162'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#diploma_thesis')"/></xsl:when>
            <xsl:when test="$genre='584747071'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#dissertation')"/></xsl:when>
            <xsl:when test="$genre='584747101'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#habilitation')"/></xsl:when>
            <xsl:when test="$genre='1831753405'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#magister_thesis')"/></xsl:when>
            <xsl:when test="$genre='584747012'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#master_thesis')"/></xsl:when>
            <xsl:when test="$genre='1831751658'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#preface')"/></xsl:when>
            <xsl:when test="$genre='584747152'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#proceedings')"/></xsl:when>
            <xsl:when test="$genre='178537799X'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#research_data')"/></xsl:when>
            <xsl:when test="$genre='1832938824'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#researchpaper')"/></xsl:when>
            <xsl:when test="$genre='584746806'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#review')"/></xsl:when>
            <xsl:when test="$genre='183104661X'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#video')"/></xsl:when>
          </xsl:choose>
        </xsl:attribute>
      </mods:genre>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="uboOriginClassification">
    <xsl:variable name="originClass" select="document('classification:metadata:-1:children:ORIGIN')"/>

    <xsl:for-each select="./p:datafield[@tag='244Z']">
      <xsl:for-each select="./p:subfield[@code='9']/text()">

        <xsl:variable name="txt" select="."/>

        <xsl:for-each select="$originClass//category[label[@xml:lang='x-lpp' and contains(@text, $txt)]]/@ID">
          <mods:classification valueURI="{$WebApplicationBaseURL}classifications/ORIGIN#{.}"
                               authorityURI="{$WebApplicationBaseURL}classifications/ORIGIN"/>
        </xsl:for-each>

      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
