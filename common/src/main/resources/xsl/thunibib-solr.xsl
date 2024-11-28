<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:ThUniBibUtils="xalan://de.uni_jena.thunibib.user.ThUniBibUtils"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="ThUniBibUtils mods xalan xsl">

  <xsl:import href="xslImport:solr-document:thunibib-solr.xsl"/>

  <xsl:template match="mycoreobject">
    <xsl:apply-imports/>

    <xsl:apply-templates select="//mods:nameIdentifier[@type='connection']" mode="thunibib-solr-fields"/>
  </xsl:template>

  <xsl:template match="//mods:mods/mods:name/mods:nameIdentifier[@type='connection']" mode="thunibib-solr-fields">
    <xsl:variable name="leadid-scoped" select="ThUniBibUtils:getLeadId('id_connection', .)"/>

    <field name="leadid.scoped">
      <xsl:value-of select="$leadid-scoped"/>
    </field>
    <field name="leadid">
      <xsl:value-of select="substring-before($leadid-scoped, '@')"/>
    </field>
  </xsl:template>

</xsl:stylesheet>
