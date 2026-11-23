<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cerif="https://www.openaire.eu/cerif-profile/1.1/"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                exclude-result-prefixes="cerif mcrxml mods xsl">

  <xsl:import href="xslImport:uboActionButtons:hisinone/thunibib-actions-hisinone.xsl"/>

  <xsl:variable name="ThUniBib.HISinOne.BaseURL" select="document('property:ThUniBib.HISinOne.BaseURL')/entry"/>
  <xsl:variable name="ThUniBib.HISinOne.ClientKey" select="document('property:ThUniBib.HISinOne.ClientKey')/entry"/>
  <xsl:variable name="ThUniBib.HISinOne.ClientSecret" select="document('property:ThUniBib.HISinOne.ClientSecret')/entry"/>
  <xsl:variable name="ThUniBib.HISinOne.servflag.type" select="document('property:ThUniBib.HISinOne.servflag.type')/entry"/>

  <xsl:variable name="WebApplicationBaseURL" select="document('property:WebApplicationBaseURLL')/entry"/>
  <xsl:variable name="InteractionURL" select="concat($WebApplicationBaseURL,'servlets/HISinOneInteractionServlet?')"/>
  <xsl:variable name="isAdmin" select="mcrxml:isCurrentUserInRole('admin') = 'true'"/>

  <xsl:template match="*" mode="ubo-actions">
    <xsl:apply-imports/>

    <xsl:variable name="servflag-present" select="//servflag[@type = $ThUniBib.HISinOne.servflag.type]"/>

    <xsl:variable name="action">
      <xsl:choose>
        <xsl:when test="not($servflag-present)">
          <xsl:value-of select="'publish'"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'update'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="icon-class">
      <xsl:choose>
        <xsl:when test="$action='publish'">
          <xsl:value-of select="'fas fa-plus-square'"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'fas fa-sync'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="tooltip">
      <xsl:value-of select="document(concat('i18n:thunibib.editor.hisinone.button.', $action, '.tooltip'))/i18n/text()"/>
    </xsl:variable>

    <xsl:if test="$isAdmin = true()">
      <xsl:choose>
        <xsl:when test="document(concat('notnull:callJava:de.uni_jena.thunibib.his.cli.HISinOneCommands:isTransferable:', //mycoreobject/@ID)) = 'false'">
          <a class="btn btn-sm btn-outline-secondary mb-1" type="button" title="{document('i18n:thunibib.editor.hisinone.button.disabled.tooltip')/i18n/text()}">
            HISinOne
          </a>
        </xsl:when>

        <xsl:otherwise>
          <a class="action btn btn-sm btn-outline-secondary mb-1 dropdown-toggle" type="button" data-bs-toggle="dropdown" aria-expanded="false">
            HISinOne
          </a>

          <div class="dropdown-menu">
            <a href="{$InteractionURL}id={//mycoreobject/@ID}&amp;action={$action}" title="{$tooltip}"
               class="dropdown-item"
               onclick="this.classList.add('disabled'); this.classList.add('thunibib-pointer-events-none');">
              <i class="{$icon-class} me-1"/>
              <xsl:value-of select="'HISinOne'"/>
            </a>

            <xsl:if test="$servflag-present">
              <xsl:variable name="delete-tooltip"
                            select="document('i18n:thunibib.editor.hisinone.button.delete.tooltip')/i18n/text()"/>
              <a href="{$InteractionURL}id={//mycoreobject/@ID}&amp;action=delete" class="dropdown-item text-danger"
                 title="{$delete-tooltip}"
                 onclick="this.classList.add('disabled'); this.classList.add('thunibib-pointer-events-none');">
                <i class="fas fa-minus-square me-1"/>
                <xsl:value-of select="'HISinOne'"/>
              </a>
            </xsl:if>

            <xsl:if
              test="document('notnull:callJava:org.mycore.common.xml.MCRXMLFunctions:isCurrentUserSuperUser') = 'true'">

              <div class="dropdown-divider"/>

              <xsl:variable name="mods-r-tooltip"
                            select="document('i18n:thunibib.editor.hisinone.button.modsr.tooltip')/i18n/text()"/>
              <a
                href="{$WebApplicationBaseURL}receive/{//mycoreobject/@ID}?XSL.Transformer=mods-resolve-his-keys-detailed"
                class="dropdown-item" title="{$mods-r-tooltip}">
                <i class="fas fa-layer-group"/>
                MODS R
              </a>

              <xsl:variable name="mods-rc-tooltip"
                            select="document('i18n:thunibib.editor.hisinone.button.modsrc.tooltip')/i18n/text()"/>
              <a
                href="{$WebApplicationBaseURL}receive/{//mycoreobject/@ID}?XSL.Transformer=mods-resolve-create-his-keys"
                class="dropdown-item text-danger" title="{$mods-rc-tooltip}">
                <i class="fas fa-layer-group"/>
                MODS RC
              </a>

              <div class="dropdown-divider"/>

              <xsl:variable name="json-tooltip"
                            select="document('i18n:thunibib.editor.hisinone.button.json.tooltip')/i18n/text()"/>
              <a
                href="{$WebApplicationBaseURL}receive/{//mycoreobject/@ID}?XSL.Transformer=res-publication-json-detailed"
                class="dropdown-item text-danger" title="{$json-tooltip}">
                <i class="fas fa-layer-group"/>
                JSON
              </a>
            </xsl:if>
          </div>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <!-- Default template when HISinOne is not configured -->
  <xsl:template match="*[string-length($ThUniBib.HISinOne.BaseURL) = 0 or string-length($ThUniBib.HISinOne.ClientKey) = 0 or string-length($ThUniBib.HISinOne.ClientSecret) = 0]" mode="ubo-actions">
    <xsl:apply-imports/>
  </xsl:template>
</xsl:stylesheet>
