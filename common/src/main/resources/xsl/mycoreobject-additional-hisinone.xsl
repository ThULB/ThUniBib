<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                exclude-result-prefixes="mcrxml mods xsl">

  <xsl:import href="xslImport:additional:mycoreobject-additional-hisinone.xsl"/>


  <xsl:param name="ThUniBib.HISinOne.BaseURL"/>
  <xsl:param name="ThUniBib.HISinOne.servflag.type"/>
  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match="mods:mods" mode="additional-metadata">
    <xsl:apply-imports/>

    <xsl:variable name="hisid" select="//servflag[@type = 'MyCoRe-HISinOne']"/>

    <xsl:if test="$hisid and mcrxml:isCurrentUserInRole('admin')">
      <div class="row">
        <div class="col-3">HISinOne:</div>
        <div class="col-9">
          <a href="{$ThUniBib.HISinOne.BaseURL}a/fs.res.frontend/pub/view/{$hisid}">
            <xsl:value-of select="$hisid"/>
          </a>
        </div>
      </div>
    </xsl:if>

  </xsl:template>

  <xsl:template match="mycoreobject" mode="additional-metadata-card">
    <xsl:apply-imports/>
  </xsl:template>
</xsl:stylesheet>
