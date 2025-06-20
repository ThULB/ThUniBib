<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:utilities="xalan://de.uni_jena.thunibib.Utilities"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="mods utilities xalan xsl">

  <xsl:import href="xslImport:solr-document:thunibib-solr.xsl"/>

  <xsl:template match="mycoreobject">
    <xsl:apply-imports/>

    <xsl:apply-templates select="metadata/def.modsContainer/modsContainer/mods:mods/mods:name/mods:nameIdentifier[@type='connection']" mode="thunibib-solr-fields"/>
    <xsl:apply-templates select="metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier" mode="thunibib-solr-fields"/>
    <xsl:apply-templates select="metadata/def.modsContainer/modsContainer/mods:mods/mods:relatedItem[(@type='host') or (@type='series')]/mods:identifier[@type='uri']" mode="thunibib-solr-fields"/>
  </xsl:template>

  <xsl:template match="mods:nameIdentifier[@type='connection']" mode="thunibib-solr-fields">
    <xsl:variable name="leadid-scoped" select="utilities:getLeadId('id_connection', .)"/>

    <xsl:if test="string-length($leadid-scoped) &gt; 0">
      <field name="leadid.scoped">
        <xsl:value-of select="$leadid-scoped"/>
      </field>

      <field name="leadid">
        <xsl:choose>
          <xsl:when test="contains($leadid-scoped, '@')">
            <xsl:value-of select="substring-before($leadid-scoped, '@')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$leadid-scoped"/>
          </xsl:otherwise>
        </xsl:choose>
      </field>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mods:identifier[@type = 'uri']" mode="thunibib-solr-fields">
    <xsl:if test="contains(text(), 'uri.gbv.de/document') and contains(text(), ':ppn:')">
      <xsl:variable name="ppn" select="substring-after(text(), ':ppn:')"/>
      <field name="pub_id_ppn">
        <xsl:value-of select="$ppn"/>
      </field>
      <field name="pub_id">
        <xsl:value-of select="$ppn"/>
      </field>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mods:identifier[not(@type = 'uri')]" mode="thunibib-solr-fields">
    <field name="pub_id">
      <xsl:value-of select="."/>
    </field>
  </xsl:template>

  <xsl:template match="mods:relatedItem[(@type='host') or (@type='series')]/mods:identifier[@type='uri']" mode="thunibib-solr-fields">
    <xsl:if test="contains(text(), 'uri.gbv.de/document') and contains(text(), ':ppn:')">
      <field name="host_id_ppn">
        <xsl:value-of select="substring-after(text(), ':ppn:')"/>
      </field>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
