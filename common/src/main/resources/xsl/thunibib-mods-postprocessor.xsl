<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:MCRXMLFunctions="xalan://org.mycore.common.xml.MCRXMLFunctions"
                exclude-result-prefixes="MCRXMLFunctions xalan xsl">

  <xsl:include href="resource:xsl/mods-postprocessor.xsl"/>
  <xsl:include href="resource:xsl/coreFunctions.xsl"/>

  <xsl:template match="mods:subject">
    <mods:subject>

      <xsl:variable name="topics">
        <xsl:call-template name="Tokenizer">
          <xsl:with-param name="string" select="mods:topic"/>
          <xsl:with-param name="delimiter" select="'::'"/>
        </xsl:call-template>
      </xsl:variable>

      <xsl:for-each select="xalan:nodeset($topics)/token">
        <mods:topic>
          <xsl:value-of select="MCRXMLFunctions:trim(.)"/>
        </mods:topic>
      </xsl:for-each>
    </mods:subject>
  </xsl:template>

</xsl:stylesheet>
