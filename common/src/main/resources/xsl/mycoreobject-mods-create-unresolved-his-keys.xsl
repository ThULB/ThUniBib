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
      <xsl:choose>
        <xsl:when test="mcracl:check-permission('POOLPRIVILEGE', 'read-user-attributes')">
          <xsl:comment>Begin - transformer 'xsl/mods-create-unresolved-his-keys.xsl'</xsl:comment>

          <xsl:apply-templates select="mods:classification[@authorityURI = $ThUniBib.HISinOne.BaseURL][fn:number() &lt; 0]" mode="create"/>
          <xsl:apply-templates select="mods:relatedItem[@otherTypeAuth = $ThUniBib.HISinOne.BaseURL][fn:number() &lt; 0]" mode="create"/>
          <xsl:apply-templates select="mods:name[@type='conference'][mods:nameIdentifier[contains(@typeURI, $ThUniBib.HISinOne.BaseURL)][fn:number() &lt; 0]]" mode="create"/>

          <xsl:comment>End - transformer 'xsl/mods-create-unresolved-his-keys.xsl'</xsl:comment>
        </xsl:when>
        <xsl:otherwise>
          <xsl:comment>Access Denied: Will not create values for unresolved keys.</xsl:comment>
        </xsl:otherwise>
      </xsl:choose>

      <!-- Retain mods from previous step, but exclude unresolved values -->
      <xsl:apply-templates select="@*|node()[not(fn:number() &lt; 0)]"/>
    </xsl:copy>
  </xsl:template>

  <!-- Create unresolved host -->
  <xsl:template match="mods:relatedItem[@xlink:href][@otherType='host'][@otherTypeAuth = $ThUniBib.HISinOne.BaseURL][@otherTypeAuthURI][1]" mode="create">
    <xsl:comment>Begin - create related item - transformer 'xsl/mods-create-unresolved-his-keys.xsl'</xsl:comment>

    <xsl:variable name="host" select="@xlink:href"/>
    <xsl:variable name="host-genre" select="fn:substring-after(@otherTypeAuthURI, '/fs/res/')"/>

    <xsl:variable name="resolve-of-type">
      <xsl:choose>
        <xsl:when test="contains('journal', $host-genre)">
          <xsl:value-of select="'journal'"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'publication'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="host-his-id" select="fn:document(concat('hisinone:create:id:', $resolve-of-type, ':', @xlink:href))"/>

    <xsl:if test="fn:number($host-his-id) &gt; 0">
      <mods:relatedItem>
        <xsl:copy-of select="@*"/>
        <xsl:value-of select="$host-his-id"/>
      </mods:relatedItem>
    </xsl:if>

    <xsl:comment>End - create related item - transformer 'xsl/mods-create-unresolved-his-keys.xsl'</xsl:comment>
  </xsl:template>

  <!-- Create unresolved publisher -->
  <xsl:template match="mods:classification[fn:contains(@valueURI, 'fs/res/publisher') and @authorityURI = $ThUniBib.HISinOne.BaseURL]" mode="create" >
    <xsl:comment>Begin - create publisher - transformer 'xsl/mods-create-unresolved-his-keys.xsl'</xsl:comment>

    <xsl:variable name="publisher-text" select="fn:encode-for-uri(../mods:originInfo/mods:publisher)"/>
    <xsl:variable name="publisher-id" select="fn:document(concat('hisinone:create:id:publisher:', $publisher-text))"/>

    <mods:classification>
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="$publisher-id"/>
    </mods:classification>
    <xsl:comment>End - create publisher - transformer 'xsl/mods-create-unresolved-his-keys.xsl'</xsl:comment>
  </xsl:template>

  <!-- Create unresolved conferences-->
  <xsl:template match="mods:name[@type='conference'][mods:nameIdentifier[contains(@typeURI, $ThUniBib.HISinOne.BaseURL)]]" mode="create" >
    <xsl:comment>Begin - create conference - transformer 'xsl/mods-create-unresolved-his-keys.xsl'</xsl:comment>
    <xsl:variable name="conference-text" select="fn:encode-for-uri(mods:namePart)"/>
    <xsl:variable name="conference-id" select="fn:document(concat('hisinone:create:id:conference:', $conference-text))"/>

    <mods:name type="conference">
      <xsl:copy-of select="@*|node()[not(fn:local-name() = 'nameIdentifier')]"/>

      <xsl:if test="fn:number($conference-id) &gt; 0">
        <mods:nameIdentifier typeURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}cs/psv/conference/identifier">
          <xsl:value-of select="$conference-id"/>
        </mods:nameIdentifier>
      </xsl:if>
    </mods:name>
    <xsl:comment>End - create conference - transformer 'xsl/mods-create-unresolved-his-keys.xsl'</xsl:comment>
  </xsl:template>

  <!-- Remove all elements with unresolved values -->
    <xsl:template match="*[fn:number() &lt; 0]"/>
</xsl:stylesheet>
