<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:mcracl="http://www.mycore.de/xslt/acl"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="mcracl fn xsl">

  <xsl:include href="resource:xsl/functions/acl.xsl"/>

  <xsl:output method="xml" indent="yes"/>

  <xsl:param name="ThUniBib.HISinOne.BaseURL"/>
  <xsl:param name="ThUniBib.HISinOne.BaseURL.API.Path"/>

  <xsl:variable name="origin" select="document('classification:metadata:-1:children:ORIGIN')/mycoreclass/categories" />

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:mods">
    <xsl:copy>

      <!-- Set research areas as of KDSF -->
      <xsl:call-template name="researchArea"/>

      <!-- Set subjectArea class -->
      <xsl:call-template name="subjectArea"/>

      <!-- Set state class -->
      <xsl:call-template name="state"/>

      <!-- Set visibility class -->
      <xsl:call-template name="visibility"/>

      <!-- Set publicationCreatorType class -->
      <xsl:call-template name="creatorType"/>

      <!-- Retain original mods:mods -->
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="researchArea">
    <xsl:choose>
      <xsl:when test="mods:classification[contains(@valueURI, 'researchAreaKdsf#')]">
        <xsl:for-each select="fn:substring-after(mods:classification[contains(@valueURI, 'researchAreaKdsf#')]/@valueURI, '#')">
          <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}cs/sys/values/researchAreaKdsfValue">
            <xsl:value-of select="."/>
          </mods:classification>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}cs/sys/values/researchAreaKdsfValue">
          <xsl:value-of select="number(61)"/>
        </mods:classification>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- TODO find the proper source value, currently mapping is fixed to 'Autor/-in'-->
  <xsl:template name="creatorType">
    <xsl:variable name="creator-type-his-key" select="fn:document('HISinOne:creatorType:aut')"/>
    <xsl:if test="$creator-type-his-key">
      <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}cs/sys/values/publicationCreatorTypeValue">
        <xsl:value-of select="$creator-type-his-key"/>
      </mods:classification>
    </xsl:if>
  </xsl:template>

  <xsl:template name="subjectArea">
    <xsl:variable name="subject-area-value-uri" select="'cs/sys/values/subjectAreaValue'"/>
    <xsl:variable name="origin-id" select="fn:substring-after(mods:classification[contains(@valueURI, 'ORIGIN')]/@valueURI, '#')"/>
    <xsl:variable name="destatis-from-origin" select="$origin//category[@ID=$origin-id]/label[@xml:lang='x-destatis']/@text"/>

    <xsl:if test="$destatis-from-origin">
      <xsl:variable name="subject-area-his-key" select="fn:document(concat('HISinOne:subjectArea:', $destatis-from-origin))"/>

      <xsl:if test="$subject-area-his-key">
        <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}{$subject-area-value-uri}">
          <xsl:value-of select="$subject-area-his-key"/>
        </mods:classification>
      </xsl:if>
    </xsl:if>

    <xsl:for-each select="//mods:classification[contains(@authorityURI, 'fachreferate')]/@valueURI">
      <xsl:variable name="subject-area" select="fn:substring-after(., '#')"/>
      <xsl:variable name="subject-area-his-key" select="fn:document(concat('HISinOne:subjectArea:', $subject-area))"/>

      <xsl:if test="$subject-area-his-key">
        <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}{$subject-area-value-uri}">
          <xsl:value-of select="$subject-area-his-key"/>
        </mods:classification>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="visibility">
    <xsl:param name="status" select="//servflags/servflag[@type='status']"/>
    <xsl:variable name="visibility-his-key" select="fn:document(concat('HISinOne:visibility:', $status))"/>
    <xsl:if test="$visibility-his-key">
      <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}cs/sys/values/visibilityValue">
        <xsl:value-of select="$visibility-his-key"/>
      </mods:classification>
    </xsl:if>
  </xsl:template>

  <xsl:template name="state">
    <xsl:param name="status" select="//servflags/servflag[@type='status']"/>
    <xsl:variable name="status-his-key" select="fn:document(concat('HISinOne:state:', $status))"/>

    <xsl:if test="$status-his-key">
      <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}fs/res/state/publication">
        <xsl:value-of select="$status-his-key"/>
      </mods:classification>
    </xsl:if>
  </xsl:template>

  <!--
   Maps the ubo genre to the his publicationType.

   TODO In HISinOne there is an additional documentType. Possible values are depending on the publicationType.
   TODO GET /fs/res/publication/documentTypes/article
   TODO GET /fs/res/publication/documentTypes/book
  -->
  <xsl:template match="mods:genre[not(parent::mods:relatedItem)]">
    <xsl:param name="genre" select="fn:substring-after(@valueURI, '#')"/>

    <xsl:copy>
      <xsl:copy-of select="*|@*"/>
    </xsl:copy>

    <!-- publicationTypeValue -->
    <xsl:variable name="his-key-publication-type-value" select="fn:document(concat('HISinOne:genre:', $genre))"/>
    <xsl:if test="$his-key-publication-type-value">
      <mods:genre authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}cs/sys/values/publicationTypeValue" type="code">
        <xsl:value-of select="$his-key-publication-type-value"/>
      </mods:genre>
    </xsl:if>

    <!-- qualificationThesisValue -->
    <xsl:variable name="his-key-qualification-thesis-type-value" select="fn:document(concat('HISinOne:thesisType:', $genre))"/>
    <xsl:if test="$his-key-publication-type-value">
      <mods:genre authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}cs/sys/values/qualificationThesisValue" type="code">
        <xsl:value-of select="$his-key-qualification-thesis-type-value"/>
      </mods:genre>
    </xsl:if>

  </xsl:template>

  <xsl:template match="mods:language">
    <xsl:variable name="rfc5646" select="mods:languageTerm[@type='code'][@authority='rfc5646']"/>

    <xsl:copy>
      <xsl:copy-of select="*|@*"/>
      <xsl:variable name="his-key" select="fn:document(concat('HISinOne:language:', $rfc5646))"/>

      <xsl:if test="$his-key">
        <mods:languageTerm authorityURI="{$ThUniBib.HISinOne.BaseURL}" type="code">
          <xsl:value-of select="$his-key"/>
        </mods:languageTerm>
      </xsl:if>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
