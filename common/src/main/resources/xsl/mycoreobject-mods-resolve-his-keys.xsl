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
  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:variable name="origin" select="document('classification:metadata:-1:children:ORIGIN')/mycoreclass/categories" />

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:mods">
    <xsl:copy>
      <xsl:call-template name="globalIdentifiers"/>

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

  <xsl:template name="globalIdentifiers">
    <xsl:for-each select="mods:identifier[contains('doi scopus url', @type)]">
      <xsl:variable name="global-identifier-type-id" select="fn:document(concat('hisinone:globalIdentifiers:', @type))"/>
      <mods:identifier type="{@type}" typeURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}fs/res/publication/globalIdentifierType#{$global-identifier-type-id}">
        <xsl:value-of select="."/>
      </mods:identifier>
    </xsl:for-each>

    <xsl:variable name="url-type-id" select="fn:document('hisinone:globalIdentifiers:url')"/>
    <mods:identifier type="url" typeURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}fs/res/publication/globalIdentifierType#{$url-type-id}">
      <xsl:value-of select="concat($WebApplicationBaseURL, 'receive/', //mycoreobject/@ID)"/>
    </mods:identifier>
  </xsl:template>

  <!-- TODO obtain from /api/v1/cs/sys/values/researchAreaKdsfValue -->
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
    <xsl:variable name="creator-type-his-key" select="fn:document('hisinone:creatorType:aut')"/>
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
      <xsl:variable name="subject-area-his-key" select="fn:document(concat('hisinone:subjectArea:', $destatis-from-origin))"/>

      <xsl:if test="$subject-area-his-key">
        <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}{$subject-area-value-uri}">
          <xsl:value-of select="$subject-area-his-key"/>
        </mods:classification>
      </xsl:if>
    </xsl:if>

    <xsl:for-each select="//mods:classification[contains(@authorityURI, 'fachreferate')]/@valueURI">
      <xsl:variable name="subject-area" select="fn:substring-after(., '#')"/>
      <xsl:variable name="subject-area-his-key" select="fn:document(concat('hisinone:subjectArea:', $subject-area))"/>

      <xsl:if test="$subject-area-his-key">
        <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}{$subject-area-value-uri}">
          <xsl:value-of select="$subject-area-his-key"/>
        </mods:classification>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="visibility">
    <xsl:param name="status" select="//servflags/servflag[@type='status']"/>
    <xsl:variable name="visibility-his-key" select="fn:document(concat('hisinone:visibility:', $status))"/>
    <xsl:if test="$visibility-his-key">
      <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}cs/sys/values/visibilityValue">
        <xsl:value-of select="$visibility-his-key"/>
      </mods:classification>
    </xsl:if>
  </xsl:template>

  <xsl:template name="state">
    <xsl:param name="status" select="//servflags/servflag[@type='status']"/>
    <xsl:variable name="status-his-key" select="fn:document(concat('hisinone:state:', $status))"/>

    <xsl:if test="$status-his-key">
      <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}fs/res/state/publication">
        <xsl:value-of select="$status-his-key"/>
      </mods:classification>
    </xsl:if>
  </xsl:template>

  <!--
   Maps the ubo genre to the HISinOne publicationType.
  -->
  <xsl:template match="mods:genre[not(parent::mods:relatedItem)]">
    <xsl:param name="genre" select="fn:substring-after(@valueURI, '#')"/>

    <xsl:copy>
      <xsl:copy-of select="*|@*"/>
    </xsl:copy>

    <!-- publicationTypeValue -->
    <xsl:variable name="his-key-publication-type-value" select="fn:document(concat('hisinone:genre:', $genre))"/>
    <xsl:if test="$his-key-publication-type-value">
      <mods:genre authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}cs/sys/values/publicationTypeValue" type="code">
        <xsl:value-of select="$his-key-publication-type-value"/>
      </mods:genre>
    </xsl:if>

    <!-- qualificationThesisValue -->
    <xsl:variable name="his-key-qualification-thesis-type-value" select="fn:document(concat('hisinone:thesisType:', $genre))"/>
    <xsl:if test="$his-key-publication-type-value">
      <mods:genre authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}cs/sys/values/qualificationThesisValue" type="code">
        <xsl:value-of select="$his-key-qualification-thesis-type-value"/>
      </mods:genre>
    </xsl:if>

    <!--
      documentType

      TODO In HISinOne there is an additional documentType. Possible values are depending on the publicationType.
      TODO GET /fs/res/publication/documentTypes/article
      TODO GET /fs/res/publication/documentTypes/book
    -->
    <xsl:variable name="his-key-document-type-type-value" select="fn:document(concat('hisinone:documentType:', $genre))"/>
    <xsl:if test="$his-key-document-type-type-value">
      <mods:genre authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}fs/res/publication/documentTypes" type="code">
        <xsl:value-of select="$his-key-document-type-type-value"/>
      </mods:genre>
    </xsl:if>

  </xsl:template>

  <xsl:template match="mods:language">
    <xsl:variable name="rfc5646" select="mods:languageTerm[@type='code'][@authority='rfc5646']"/>

    <xsl:copy>
      <xsl:copy-of select="*|@*"/>
      <xsl:variable name="his-key" select="fn:document(concat('hisinone:language:', $rfc5646))"/>

      <xsl:if test="$his-key">
        <mods:languageTerm authorityURI="{$ThUniBib.HISinOne.BaseURL}" type="code">
          <xsl:value-of select="$his-key"/>
        </mods:languageTerm>
      </xsl:if>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
