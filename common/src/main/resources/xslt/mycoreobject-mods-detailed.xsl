<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:mcracl="http://www.mycore.de/xslt/acl"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="mcracl fn xsl">

  <xsl:include href="resource:xslt/functions/acl.xsl"/>

  <xsl:output method="xml" indent="yes"/>

  <xsl:param name="MCR.user2.matching.lead_id"/>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:name[mods:nameIdentifier[@type = 'connection']]">
    <xsl:variable name="connection-id" select="mods:nameIdentifier[@type = 'connection']"/>
    <xsl:variable name="lead-id" select="fn:document(concat('callJava:de.uni_jena.thunibib.user.ThUniBibUtils:getLeadId:id_connection:', $connection-id))"/>

    <xsl:copy>
      <xsl:copy-of select="*|@*"/>

      <xsl:if
        test="mcracl:check-permission('POOLPRIVILEGE', 'read-user-attributes') and string-length($lead-id) &gt; 0">
        <mods:nameIdentifier type="{$MCR.user2.matching.lead_id}">
          <xsl:value-of select="$lead-id"/>
        </mods:nameIdentifier>
      </xsl:if>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
