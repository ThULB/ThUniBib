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

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!--
   Maps the ubo genre to the his publicationType.

   TODO In HISinOne there is an additional documentType. Possible values are depending on the publicationType.
   TODO GET /fs/res/publication/documentTypes/article
   TODO GET /fs/res/publication/documentTypes/book
  -->
  <xsl:template match="mods:genre[not(parent::mods:relatedItem)]">
    <xsl:copy>
      <xsl:copy-of select="*|@*"/>
    </xsl:copy>

    <xsl:variable name="genre" select="fn:substring-after(@valueURI, '#')"/>
    <xsl:variable name="his-key" select="fn:document(concat('HISinOne:genre:', $genre))"/>
    <xsl:if test="$his-key">
      <mods:genre authorityURI="{$ThUniBib.HISinOne.BaseURL}" type="code">
        <xsl:value-of select="$his-key"/>
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

  <xsl:template match="//servflags/servflag[@type='status']">
    <xsl:copy>
      <xsl:copy-of select="*|@*"/>
    </xsl:copy>

    <xsl:variable name="status" select="."/>
    <xsl:variable name="his-key" select="fn:document(concat('HISinOne:state:', $status))"/>

    <xsl:if test="$his-key">
      <servflag type="status" authorityURI="{$ThUniBib.HISinOne.BaseURL}">
        <xsl:value-of select="$his-key"/>
      </servflag>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
