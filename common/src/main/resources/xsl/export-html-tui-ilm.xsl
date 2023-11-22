<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:mods="http://www.loc.gov/mods/v3"
                exclude-result-prefixes="mods xsl xalan">

  <xsl:output method="html" encoding="UTF-8" media-type="text/html"
              doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN"
              doctype-system="http://www.w3.org/TR/html401/loose.dtd"
              indent="yes" xalan:indent-amount="2"/>

  <xsl:include href="export-html.xsl"/>

  <xsl:template match="mycoreobject">
    <li>
      <div class="bibentry">
        <xsl:apply-templates select="." mode="data-attributes"/>
        <xsl:apply-templates select="." mode="html-export"/>
      </div>
    </li>
  </xsl:template>

  <xsl:template match="mycoreobject" mode="data-attributes">
    <xsl:for-each select="./metadata/def.modsContainer/modsContainer/mods:mods/mods:identifier">
      <xsl:sort select="@type"/>
      <xsl:attribute name="data-{@type}">
        <xsl:value-of select="."/>
      </xsl:attribute>
    </xsl:for-each>

    <xsl:attribute name="data-abstract">
      <xsl:value-of select="./metadata/def.modsContainer/modsContainer/mods:mods/mods:abstract[1]"/>
    </xsl:attribute>
  </xsl:template>
</xsl:stylesheet>
