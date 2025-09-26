<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:mods="http://www.loc.gov/mods/v3" exclude-result-prefixes="xlink xsl" version="1.0">

  <xsl:template match='@*|node()'>
    <xsl:copy>
      <xsl:apply-templates select='@*|node()'/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//mods:mods/mods:accessCondition[contains(@xlink:href, 'licenses#')][@type='use and reproduction']">
    <xsl:variable name="old-licence-categid" select="substring-after(@xlink:href, '#')"/>
    <xsl:variable name="new-licence-categid" select="translate($old-licence-categid, '-', '_')"/>
    <xsl:variable name="base-url" select="substring-before(@xlink:href, '#')"/>
    <mods:accessCondition xlink:href="{$base-url}#{$new-licence-categid}" type="{@type}"/>
  </xsl:template>
</xsl:stylesheet>
