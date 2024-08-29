<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:mcracl="http://www.mycore.de/xslt/acl"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="fn mcracl xlink xsl">

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
      <xsl:apply-templates select="mods:classification[@authorityURI = $ThUniBib.HISinOne.BaseURL][text() = '-1']"
                           mode="create"/>
      <xsl:apply-templates select="mods:relatedItem[@otherTypeAuth = $ThUniBib.HISinOne.BaseURL][text() = '-1']"
                           mode="create"/>

      <xsl:comment>End - mods-create-unresolved-his-keys'</xsl:comment>
      <!-- Retain mods from previous step, but exclude unresolved values -->
      <xsl:apply-templates select="@*|node()[not(text() = '-1')]"/>
    </xsl:copy>
  </xsl:template>

  <!-- Create unresolved journal-->
  <xsl:template mode="create"
                match="mods:relatedItem[@xlink:href][@otherType='host'][@otherTypeAuth = $ThUniBib.HISinOne.BaseURL][contains(@otherTypeAuthURI, 'journal')][1]">

    <xsl:variable name="journal-id" select="fn:document(concat('hisinone:create:id:journal:', @xlink:href))"/>

    <xsl:if test="fn:number($journal-id) &gt; 0">
      <mods:relatedItem>
        <xsl:copy-of select="@*"/>
        <xsl:value-of select="$journal-id"/>
      </mods:relatedItem>
    </xsl:if>
  </xsl:template>

  <!-- Create unresolved publisher -->
  <xsl:template mode="create"
                match="mods:classification[fn:contains(@valueURI, 'fs/res/publisher') and @authorityURI = $ThUniBib.HISinOne.BaseURL]">

    <xsl:variable name="publisher-text" select="fn:encode-for-uri(../mods:originInfo/mods:publisher)"/>
    <xsl:variable name="publisher-id" select="fn:document(concat('hisinone:create:id:publisher:', $publisher-text))"/>

    <mods:classification>
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="$publisher-id"/>
    </mods:classification>
  </xsl:template>

  <!-- Remove all elements with unresolved values -->
  <xsl:template match="node()[text() = '-1']"/>

</xsl:stylesheet>
