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

  <xsl:template name="uboOriginClassification">
    <xsl:variable name="origin" select="document('classification:metadata:-1:children:ORIGIN')"/>
    <xsl:for-each select="./p:datafield[@tag='244Z']">
      <xsl:for-each select="./p:subfield[@code='9']">

        <xsl:variable name="text" select="./text()"/>

        <xsl:choose>
          <xsl:when test="$text = '584746261'">
            <mods:classification valueURI="{$WebApplicationBaseURL}classifications/ORIGIN#0800"
                                 authorityURI="{$WebApplicationBaseURL}classifications/ORIGIN"/>
            <mods:classification valueURI="{$WebApplicationBaseURL}classifications/ORIGIN#1300"
                                 authorityURI="{$WebApplicationBaseURL}classifications/ORIGIN"/>
          </xsl:when>

          <xsl:when test="$origin//category/label[@xml:lang = 'x-lpp'][@text = $text]">
            <xsl:variable name="originCategory" select="$origin//category[label[@xml:lang='x-lpp'][@text=$text]]/@ID"/>

            <xsl:for-each select="$originCategory">
              <mods:classification valueURI="{$WebApplicationBaseURL}classifications/ORIGIN#{.}"
                                   authorityURI="{$WebApplicationBaseURL}classifications/ORIGIN"/>
            </xsl:for-each>

          </xsl:when>
        </xsl:choose>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
