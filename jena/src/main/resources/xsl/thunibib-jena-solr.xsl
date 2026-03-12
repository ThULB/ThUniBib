<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:utilities="xalan://de.uni_jena.thunibib.Utilities"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions"
                exclude-result-prefixes="mcrxsl mods utilities xalan xsl">

  <xsl:import href="xslImport:solr-document:thunibib-jena-solr.xsl"/>

  <xsl:param name="UBO.projectid.default"/>

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
    <field name="ukj">
      <xsl:value-of select="utilities:isPartOfUKJ(../../../../@ID)"/>
    </field>

    <field name="core.univercity">
      <xsl:value-of select="utilities:isPartOfUKJ(../../../../@ID)"/>
    </field>
  </xsl:template>
</xsl:stylesheet>
