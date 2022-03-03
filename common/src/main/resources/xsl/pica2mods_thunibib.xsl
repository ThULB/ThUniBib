<?xml version="1.0"?>
<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:p="info:srw/schema/5/picaXML-v1.0"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="xsl p xlink">
  <xsl:mode on-no-match="shallow-copy"/>
  
  <xsl:import href="default/pica2mods-default-titleInfo.xsl" />
  <xsl:import href="default/pica2mods-default-name.xsl" />
  <xsl:import href="default/pica2mods-default-identifier.xsl" />
  <xsl:import href="default/pica2mods-default-language.xsl" />
  <xsl:import href="default/pica2mods-default-location.xsl" />
  <xsl:import href="default/pica2mods-default-physicalDescription.xsl" />
  <xsl:import href="default/pica2mods-default-originInfo.xsl" />
  <xsl:import href="default/pica2mods-default-genre.xsl" />
  <xsl:import href="default/pica2mods-default-recordInfo.xsl" />
  <xsl:import href="default/pica2mods-default-note.xsl" />
  <xsl:import href="default/pica2mods-default-abstract.xsl" />
  <xsl:import href="default/pica2mods-default-subject.xsl" />
  <xsl:import href="default/pica2mods-default-relatedItem.xsl" />

  <xsl:import href="_common/pica2mods-pica-PREPROCESSING.xsl" />
  <xsl:import href="_common/pica2mods-functions.xsl" />

  <xsl:param name="MCR.PICA2MODS.CONVERTER_VERSION" select="'Pica2Mods 2.1'" />
  <xsl:param name="MCR.PICA2MODS.DATABASE" select="'k10plus'" />

  <xsl:template match="p:record">
    <mods:mods>
      <xsl:call-template name="modsTitleInfo" />
      <xsl:call-template name="modsAbstract" />
      <xsl:call-template name="modsName" />
      <xsl:call-template name="modsIdentifier" />
      <xsl:call-template name="ubomodsLanguage" />
      <xsl:call-template name="modsPhysicalDescription" />
      <xsl:call-template name="modsOriginInfo" />
      <xsl:call-template name="ubomodsGenre" />
      <xsl:call-template name="modsLocation" />
      <xsl:call-template name="modsRecordInfo" />
      <xsl:call-template name="modsNote" />
      <xsl:call-template name="modsRelatedItem" />
      <xsl:call-template name="modsSubject" />
    </mods:mods>
  </xsl:template>


  <!-- copied from pics2mods and changed rfc5646 to rfc4646 -->
  <xsl:template name="ubomodsLanguage">
    <!-- relative Pfade funktionieren nicht für Classpath-Resourcen: <xsl:variable name="rfc5646" select="document('../_common/rfc5646.xml')" -->
    <xsl:variable name="rfc5646" select="document('resource:_data/rfc5646.xml')" />
    <xsl:for-each select="./p:datafield[@tag='010@']"> <!-- 1500 Language -->
      <!-- weiter Unterfelder für Orginaltext / Zwischenübersetzung nicht abbildbar -->
      <xsl:for-each select="./p:subfield[@code='a']">
        <mods:language>
            <xsl:variable name="l" select="." />
            <xsl:choose>
              <xsl:when test="$rfc5646/mycoreclass/categories//category[label[@xml:lang='x-bibl']/@text=$l]">
                <mods:languageTerm type="code" authority="rfc4646">
                  <xsl:value-of
                    select="$rfc5646/mycoreclass/categories//category[label[@xml:lang='x-bibl']/@text=$l]/@ID" />
                </mods:languageTerm>  
              </xsl:when>
              <xsl:otherwise>
                <xsl:comment>unknown language code</xsl:comment>
                <mods:languageTerm type="code">
                  <xsl:value-of select="." />
                </mods:languageTerm>
              </xsl:otherwise>
            </xsl:choose>
        </mods:language>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="ubomodsGenre">
    <mods:genre type="intern">
      <xsl:variable name="genre" select="normalize-space(translate(p:datafield[@tag='041A']/p:subfield[@code='a'],'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'))" />
      <xsl:choose>
        <xsl:when test="$genre='bachelor-arbeit'">bachelor_thesis</xsl:when>
        <xsl:when test="$genre='buchbeitrag'">chapter</xsl:when>
        <xsl:when test="$genre='diplomarbeit'">diploma_thesis</xsl:when>
        <xsl:when test="$genre='dissertation'">dissertation</xsl:when>
        <xsl:when test="$genre='filmbeitrag'">video_contribution</xsl:when>
        <xsl:when test="$genre='filmwerk'">video</xsl:when>
        <xsl:when test="$genre='forschungsdaten'">research_data</xsl:when>
        <xsl:when test="$genre='habilitationsschrift'">habilitation</xsl:when>
        <xsl:when test="$genre='kongressband'">proceedings</xsl:when>
        <xsl:when test="$genre='kongressbeitrag'">conference_essay</xsl:when>
        <xsl:when test="$genre='masterarbeit'">master_thesis</xsl:when>
        <xsl:when test="$genre='monographie'">book</xsl:when>
        <xsl:when test="$genre='projektbericht'">researchpaper</xsl:when>
        <xsl:when test="$genre='rezension'">review</xsl:when>
        <xsl:when test="$genre='schriftenreihe'">series</xsl:when>
        <xsl:when test="$genre='tagungsbeitrag - Abstract'">abstract</xsl:when>
        <xsl:when test="$genre='zeitschrift'">journal</xsl:when>
        <xsl:when test="$genre='zeitschriftenaufsatz'">article</xsl:when>
      </xsl:choose>
    </mods:genre>
  </xsl:template>






</xsl:stylesheet>
