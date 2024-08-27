<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:mcracl="http://www.mycore.de/xslt/acl"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="mcracl fn xsl">

  <xsl:include href="resource:xslt/functions/acl.xsl"/>

  <xsl:output method="xml" indent="yes"/>

  <xsl:param name="ThUniBib.HISinOne.BaseURL"/>
  <xsl:param name="ThUniBib.HISinOne.BaseURL.API.Path"/>
  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:mods">
    <xsl:copy>
      <xsl:comment>Begin - transformer 'mods-create-unresolved-his-keys'</xsl:comment>
      <!-- TODO Check user role -->
      <xsl:apply-templates select="mods:classification[@authorityURI = $ThUniBib.HISinOne.BaseURL][text() = '-1']" mode="create"/>
      <xsl:comment>End - mods-create-unresolved-his-keys'</xsl:comment>
      <!-- Retain mods from previous step, but exclude unresolved values -->
      <xsl:apply-templates select="@*|node()[not(text() = '-1')]"/>
    </xsl:copy>
  </xsl:template>

  <!-- Create unresolved publisher -->
  <xsl:template match="mods:classification[fn:contains(@valueURI, 'fs/res/publisher') and @authorityURI = $ThUniBib.HISinOne.BaseURL]" mode="create">
    <xsl:variable name="publisher-text" select="fn:encode-for-uri(../mods:originInfo/mods:publisher)"/>
    <xsl:variable name="publisher-id" select="fn:document(concat('hisinone:create:id:publisher:', $publisher-text))"/>

    <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}fs/res/publisher">
      <xsl:value-of select="$publisher-id"/>
    </mods:classification>
  </xsl:template>

  <xsl:template match="node()[text() = '-1']"/>
</xsl:stylesheet>
