<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                exclude-result-prefixes="i18n xalan xsl">

  <xsl:include href="resource:xsl/users.xsl"/>

  <xsl:param name="CurrentLang"/>
  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match="/users" mode="headAdditional" priority="1">
    <div class="mt-5 mb-5">
      <xsl:value-of select="i18n:translate('thunibib.vanished.users.intro.link')"/>

      <a href="{$WebApplicationBaseURL}servlets/ListVanishedLDAPUsersServlet?lang={$CurrentLang}">
        <xsl:value-of select="concat(i18n:translate('thunibib.vanished.users.intro.link.here'), '.')"/>
      </a>
    </div>
  </xsl:template>

</xsl:stylesheet>
