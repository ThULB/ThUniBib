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

  <xsl:include href="pica2mods_thunibib-genre.xsl"/>

  <xsl:param name="MCR.PICA2MODS.CONVERTER_VERSION" select="'Pica2Mods 2.1'"/>
  <xsl:param name="MCR.PICA2MODS.DATABASE" select="'k10plus'"/>
  <xsl:param name="WebApplicationBaseURL"/>

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


  <!-- copied from pics2mods and changed rfc5646 to rfc4646 -->
  <xsl:template name="ubomodsLanguage">
    <!-- relative Pfade funktionieren nicht für Classpath-Resourcen: <xsl:variable name="rfc5646" select="document('../../_data/rfc5646.xml')" /> -->
    <xsl:variable name="rfc5646" select="document('resource:_data/rfc5646.xml')"/>
    <xsl:for-each select="./p:datafield[@tag='010@']"> <!-- 1500 Language -->
      <!-- weiter Unterfelder für Originaltext / Zwischenübersetzung nicht abbildbar -->
      <xsl:for-each select="./p:subfield[@code='a']">
        <mods:language>
          <xsl:variable name="l" select="."/>
          <xsl:choose>
            <xsl:when test="$rfc5646/mycoreclass/categories//category[label[@xml:lang='x-bibl']/@text=$l]">
              <mods:languageTerm type="code" authority="rfc4646">
                <xsl:value-of
                    select="$rfc5646/mycoreclass/categories//category[label[@xml:lang='x-bibl']/@text=$l]/@ID"/>
              </mods:languageTerm>
            </xsl:when>
            <xsl:otherwise>
              <xsl:comment>unknown language code</xsl:comment>
              <mods:languageTerm type="code">
                <xsl:value-of select="."/>
              </mods:languageTerm>
            </xsl:otherwise>
          </xsl:choose>
        </mods:language>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="uboTypeOfResource">
    <xsl:variable name="resourceType" select="p:datafield[@tag='002C']/p:subfield[@code='b']"/>
    <mods:typeOfResource>
      <xsl:choose>
        <xsl:when test="$resourceType=cod">dat</xsl:when>
        <xsl:when test="$resourceType=cop">mul</xsl:when>
        <xsl:when test="$resourceType=crd">car</xsl:when>
        <xsl:when test="$resourceType=crf">car</xsl:when>
        <xsl:when test="$resourceType=cri">car</xsl:when>
        <xsl:when test="$resourceType=crm">car</xsl:when>
        <xsl:when test="$resourceType=crn">car</xsl:when>
        <xsl:when test="$resourceType=crt">car</xsl:when>
        <xsl:when test="$resourceType=ntm">not</xsl:when>
        <xsl:when test="$resourceType=ntv">txt</xsl:when>
        <xsl:when test="$resourceType=prm">aud</xsl:when>
        <xsl:when test="$resourceType=snd">aud</xsl:when>
        <xsl:when test="$resourceType=spw">aud</xsl:when>
        <xsl:when test="$resourceType=sti">img</xsl:when>
        <xsl:when test="$resourceType=tcf">tac</xsl:when>
        <xsl:when test="$resourceType=tci">tac</xsl:when>
        <xsl:when test="$resourceType=tcm">tac</xsl:when>
        <xsl:when test="$resourceType=tcn">tac</xsl:when>
        <xsl:when test="$resourceType=tct">tac</xsl:when>
        <xsl:when test="$resourceType=tdf">art</xsl:when>
        <xsl:when test="$resourceType=tdi">mov</xsl:when>
        <xsl:when test="$resourceType=tdm">mov</xsl:when>
        <xsl:when test="$resourceType=txt">txt</xsl:when>
        <xsl:when test="$resourceType=xxx">unk</xsl:when>
        <xsl:when test="$resourceType=zzz">unk</xsl:when>
        <xsl:otherwise>txt</xsl:otherwise>
      </xsl:choose>
    </mods:typeOfResource>
  </xsl:template>

  <xsl:template name="uboMediaType">
    <xsl:choose>
      <xsl:when test="count(p:datafield[@tag='002E']) &gt; 1">
        <mods:classification valueURI="{$WebApplicationBaseURL}classifications/mediaType#mixed"
                             authorityURI="{WebApplicationBaseURL}classifications/mediaType"/>
      </xsl:when>

      <xsl:when test="p:datafield[@tag='002E']/p:subfield[@code='b']">
        <xsl:variable name="mediaType" select="p:datafield[@tag='002E']/p:subfield[@code='b']"/>

        <xsl:variable name="uboMediaType">
          <xsl:choose>
            <xsl:when test="contains('nc nb nn nr no na nz' , $mediaType)">print</xsl:when>
            <xsl:when test="contains('cr' , $mediaType)">online</xsl:when>
            <xsl:otherwise>technical</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <mods:classification valueURI="{$WebApplicationBaseURL}classifications/mediaType#{$uboMediaType}"
                             authorityURI="{$WebApplicationBaseURL}classifications/mediaType"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="uboPeerReview">
    <!-- KXP :  144Z/01-99 $9 -->
    <xsl:if test="p:datafield[@tag='144Z']/p:subfield[@code='9'] = '480733066'">
      <mods:classification valueURI="{$WebApplicationBaseURL}classifications/peerreviewed#true"
                           authorityURI="{$WebApplicationBaseURL}classifications/peerreviewed"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="zs:searchRetrieveResponse">
    <xsl:apply-templates select="//p:record"/>
  </xsl:template>

  <xsl:template name="uboOriginClassification">
    <!-- Struktur-Daten aus Ilmenauer Katalogeintrag übernehmen (ID steht in ORIGIN-Klassifikation) -->
    <!-- Destatis-Mapping für Ilmenau anhand origin.xml -->
    <xsl:variable name="origin" select="document('classification:metadata:-1:children:ORIGIN')"/>
    <xsl:for-each select="./p:datafield[@tag='144Z']">
      <xsl:for-each select="./p:subfield[@code='9']">

        <xsl:variable name="text" select="./text()"/>

        <xsl:if test="$origin//category/label[@xml:lang='x-lpp']/@text=$text">
          <xsl:variable name="originCategory"
                        select="$origin//category[label[@xml:lang='x-lpp'][@text=$text]]/@ID"/>
          <xsl:variable name="destatisCategory"
                        select="$origin//category[label[@xml:lang='x-lpp'][@text=$text]]/label[@xml:lang='x-destatis']/@text"/>

          <mods:classification valueURI="{$WebApplicationBaseURL}classifications/ORIGIN#{$originCategory}"
                               authorityURI="{$WebApplicationBaseURL}classifications/ORIGIN"/>
          <mods:classification valueURI="{$WebApplicationBaseURL}classifications/fachreferate#{$destatisCategory}"
                               authorityURI="{$WebApplicationBaseURL}classifications/fachreferate"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>


</xsl:stylesheet>
