<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:MCRXMLFunctions="xalan://org.mycore.common.xml.MCRXMLFunctions"
                exclude-result-prefixes="MCRXMLFunctions xalan xsl">

  <xsl:include href="resource:xsl/mods-postprocessor.xsl"/>
  <xsl:include href="resource:xsl/coreFunctions.xsl"/>

  <xsl:param name="WebApplicationBaseURL"/>

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

  <xsl:template match="mods:affiliation[@authorityURI][text() = 'false']">
      <mods:affiliation authorityURI="{@authorityURI}" valueURI="{$WebApplicationBaseURL}classifications/isAffiliated#false"/>
  </xsl:template>

  <xsl:template match="mods:name[not(mods:namePart)]/mods:displayForm">
    <xsl:choose>
      <xsl:when test="contains(., ',')">
        <mods:namePart type="family">
          <xsl:value-of select="translate(substring-before(., ','), ' ', '')"/>
        </mods:namePart>
        <mods:namePart type="given">
          <xsl:value-of select="translate(substring-after(., ','), ' ', '')"/>
        </mods:namePart>
      </xsl:when>
      <xsl:otherwise>
        <mods:namePart type="family">
          <xsl:value-of select="."/>
        </mods:namePart>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
