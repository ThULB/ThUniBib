<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:encoder="xalan://java.net.URLEncoder"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                exclude-result-prefixes="encoder i18n mods xsl">

  <xsl:import href="xslImport:badges:badges/thunibib-badges-oa.xsl"/>
  <xsl:include href="resource:xsl/badges/badges-oa.xsl"/>

  <xsl:param name="WebApplicationBaseURL"/>
  <xsl:param name="CurrentLang"/>

  <xsl:variable name="oa" select="document('classification:metadata:-1:children:oa')/mycoreclass/categories"/>

  <xsl:template match="mods:mods" mode="badges">
    <xsl:apply-imports/>

    <xsl:variable name="media-type" select="substring-after(mods:classification[contains(@authorityURI, 'mediaType')]/@valueURI, '#')"/>

    <xsl:choose>
      <!-- hide OA badge when media type is print -->
      <xsl:when test="$media-type and contains('print', $media-type)"/>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="mods:classification[contains(@authorityURI,'oa')]">
            <xsl:apply-templates select="mods:classification[contains(@authorityURI,'oa')]" mode="label-info"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="mods:relatedItem[@type='host']/mods:classification[contains(@authorityURI,'oa')]" mode="label-info"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
