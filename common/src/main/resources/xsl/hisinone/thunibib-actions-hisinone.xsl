<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cerif="https://www.openaire.eu/cerif-profile/1.1/"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                exclude-result-prefixes="cerif mcrxml mods xsl">

  <xsl:import href="xslImport:uboActionButtons:hisinone/thunibib-actions-hisinone.xsl"/>

  <xsl:param name="ThUniBib.HISinOne.BaseURL"/>
  <xsl:param name="ThUniBib.HISinOne.servflag.type"/>

  <xsl:param name="WebApplicationBaseURL"/>
  <xsl:param name="InteractionURL" select="concat($WebApplicationBaseURL,'servlets/HISinOneInteractionServlet?')"/>
  <xsl:param name="isAdmin" select="mcrxml:isCurrentUserInRole('admin') = 'true'"/>

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
      <a href="{$InteractionURL}id={//mycoreobject/@ID}&amp;action={$action}" class="action btn btn-sm btn-outline-primary mb-1" title="{$tooltip}">
        <i class="{$icon-class} mr-1"/>
        <xsl:value-of select="'HISinOne'"/>
      </a>

      <xsl:if test="$servflag-present">
        <xsl:variable name="delete-tooltip" select="document('i18n:thunibib.editor.hisinone.button.delete.tooltip')/i18n/text()"/>
        <a href="{$InteractionURL}id={//mycoreobject/@ID}&amp;action=delete" class="action btn btn-sm btn-outline-danger mb-1" title="{$delete-tooltip}">
          <i class="fas fa-minus-square mr-1"/>
          <xsl:value-of select="'HISinOne'"/>
        </a>
      </xsl:if>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
