<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="mods xalan xsl">

  <xsl:import href="xslImport:solr-document:thunibib-jena-solr.xsl"/>

  <xsl:template match="mycoreobject">
    <xsl:apply-imports/>
    <xsl:comment>
      thunibib-jena-solr.xsl -&gt;
    </xsl:comment>

    <xsl:apply-templates select="metadata/def.modsContainer/modsContainer/mods:mods" mode="thunibib-jena-solr-fields"/>

    <xsl:comment>
      &lt;- thunibib-jena-solr.xsl
    </xsl:comment>
  </xsl:template>

  <xsl:template match="mods:mods" mode="thunibib-jena-solr-fields">
    <xsl:if test="mods:classification[contains(@valueURI, 'ORIGIN')]">
      <xsl:variable name="mcrid" select="../../../../@ID"/>

      <field name="statistics.univercity.ukj">
        <xsl:value-of select="document(concat('notnull:callJava:de.uni_jena.thunibib.Utilities:isPartOfUKJ:', $mcrid))"/>
      </field>

      <field name="statistics.univercity.core">
        <xsl:value-of select="document(concat('notnull:callJava:de.uni_jena.thunibib.Utilities:isPartOfCoreUniversity:', $mcrid))"/>
      </field>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
