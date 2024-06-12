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
