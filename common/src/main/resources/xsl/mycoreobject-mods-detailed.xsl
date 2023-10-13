<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:ThUniBibUtils="xalan://de.uni_jena.thunibib.user.ThUniBibUtils"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="acl ThUniBibUtils xalan xsl">

  <xsl:output method="xml" indent="yes"/>

  <xsl:param name="MCR.user2.matching.lead_id"/>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:name[mods:nameIdentifier[@type = 'connection']]">
    <xsl:variable name="connection-id" select="mods:nameIdentifier[@type = 'connection']"/>
    <xsl:variable name="lead-id" select="ThUniBibUtils:getLeadId('id_connection', $connection-id)"/>

    <xsl:copy>
      <xsl:copy-of select="*|@*"/>

      <xsl:if test="acl:checkPermission('POOLPRIVILEGE', 'read-user-attributes') and $lead-id != 'null'">
        <mods:nameIdentifier type="{$MCR.user2.matching.lead_id}">
          <xsl:value-of select="$lead-id"/>
        </mods:nameIdentifier>
      </xsl:if>

    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
