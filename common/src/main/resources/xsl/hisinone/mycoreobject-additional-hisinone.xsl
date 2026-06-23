<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cerif="https://www.openaire.eu/cerif-profile/1.1/"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                exclude-result-prefixes="cerif mcrxml mods xsl">

  <xsl:import href="xslImport:additional:hisinone/mycoreobject-additional-hisinone.xsl"/>

  <xsl:param name="ThUniBib.HISinOne.BaseURL"/>
  <xsl:param name="ThUniBib.HISinOne.metadata.display.card" select="'false'"/>
  <xsl:param name="ThUniBib.HISinOne.metadata.display.integrated" select="'false'"/>
  <xsl:param name="ThUniBib.HISinOne.servflag.type"/>

  <xsl:param name="WebApplicationBaseURL"/>
  <xsl:param name="display-hisinone-metadata" select="mcrxml:isCurrentUserInRole('admin') = 'true'"/>

  <xsl:template match="mods:mods" mode="additional-metadata">
    <xsl:apply-imports/>

    <xsl:if test="$display-hisinone-metadata and $ThUniBib.HISinOne.metadata.display.integrated = 'true'">
      <xsl:call-template name="hisinone-metadata"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mycoreobject" mode="additional-metadata-card">
    <xsl:apply-imports/>

    <xsl:if test="$display-hisinone-metadata and $ThUniBib.HISinOne.metadata.display.card = 'true'">
      <div class="ubo_details card mt-2">
        <div class="card-body">
          <xsl:call-template name="hisinone-metadata"/>
        </div>
      </div>
    </xsl:if>
  </xsl:template>

  <xsl:template name="hisinone-metadata">
    <xsl:apply-templates select="//servflag[@type = $ThUniBib.HISinOne.servflag.type]" mode="metadata-hisinone"/>
    <xsl:apply-templates select="//mods:mods/mods:extension/cerif:Project" mode="metadata-hisinone"/>
  </xsl:template>

  <!-- Additional HISinOne metadata -->
  <xsl:template match="servflag[@type = $ThUniBib.HISinOne.servflag.type]" mode="metadata-hisinone">
    <div class="row">
      <div class="col-3">
        <xsl:value-of select="concat(document('i18n:thunibib.editor.hisinone.id')/i18n/text(), ': ')"/>
      </div>

      <div class="col-9">
        <a href="{$ThUniBib.HISinOne.BaseURL}a/fs.res.frontend/pub/view/{.}">
          <xsl:value-of select="."/>
        </a>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="cerif:Project" mode="metadata-hisinone">
    <xsl:variable name="project-id" select="cerif:Identifier"/>
    <xsl:variable name="project-title" select="cerif:Title"/>
    <xsl:variable name="project-acronym" select="cerif:Acronym"/>

    <div class="row">
      <div class="col-3">
        <xsl:value-of select="concat(document('i18n:thunibib.editor.label.project.information')/i18n/text(), ': ')"/>
      </div>

      <div class="col-9">
        <a href="{$ThUniBib.HISinOne.BaseURL}a/fs.res.frontend/finance/project/view/{$project-id}">
          <xsl:value-of select="$project-title"/>

          <xsl:if test="$project-acronym">
            <xsl:value-of select="concat(' [', $project-acronym, ']')"/>
          </xsl:if>
        </a>
      </div>
    </div>
  </xsl:template>

</xsl:stylesheet>
