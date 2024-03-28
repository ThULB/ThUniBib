<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions"
                exclude-result-prefixes="mcrxsl xalan xsl">

  <xsl:variable name="solrRequestHandler">
    <xsl:choose>
      <xsl:when test="mcrxsl:isCurrentUserInRole('admin')">
        <xsl:value-of select="'search-all?'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'search?'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="solrStatisticRequestHandler">
    <xsl:choose>
      <xsl:when test="mcrxsl:isCurrentUserInRole('admin')">
        <xsl:value-of select="'statistics-all?'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'statistics?'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

</xsl:stylesheet>
