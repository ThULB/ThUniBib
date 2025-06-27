<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl">

  <xsl:param name="WebApplicationBaseURL"/>
  <xsl:param name="facet"/>

  <xsl:variable name="chart-title-by-facet" select="document(concat('notnull:i18n:thunibib.statistics.chart.title.', $facet))/i18n/text()"/>

  <xsl:variable name="apos">
    <xsl:text>'</xsl:text>
  </xsl:variable>
</xsl:stylesheet>
