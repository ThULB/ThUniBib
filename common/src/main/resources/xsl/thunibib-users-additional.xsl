<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                exclude-result-prefixes="i18n xalan xsl">

  <xsl:include href="resource:xsl/users.xsl"/>

  <xsl:param name="CurrentLang"/>
  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match="*" mode="headAdditional" priority="1">
    <article class="card mb-3" lang="{$CurrentLang}">
      <div class="card-body">
        <div class="row">
          <div class="col-12 col-sm pb-3 pb-sm-0 thunibib-hyphens-auto">
            <xsl:value-of select="i18n:translate('thunibib.vanished.users.intro.link')"/>

            <a href="{$WebApplicationBaseURL}servlets/ListVanishedLDAPUsersServlet?lang={$CurrentLang}">
              <xsl:value-of select="concat(i18n:translate('thunibib.vanished.users.intro.link.here'), '.')"/>
            </a>
          </div>

          <div class="col-12 col-sm pb-3 pb-sm-0 thunibib-hyphens-auto">
            <xsl:value-of select="i18n:translate('thunibib.users.sharing.identifiers.link')"/>
            <a href="{$WebApplicationBaseURL}servlets/ListUsersSharingIdentifiersServlet?lang={$CurrentLang}">
              <xsl:value-of
                select="concat(i18n:translate('thunibib.users.sharing.identifiers.intro.link.here'), '.')"/>
            </a>
          </div>

          <div class="col-12 col-sm pb-3 pb-sm-0 thunibib-hyphens-auto">
            <xsl:value-of select="i18n:translate('thunibib.users.multiple.identifiers.link')"/>
            <a href="{$WebApplicationBaseURL}servlets/ListUsersHavingMultipleIdentifiersServlet?lang={$CurrentLang}">
              <xsl:value-of
                select="concat(i18n:translate('thunibib.users.multiple.identifiers.intro.link.here'), '.')"/>
            </a>
          </div>
        </div>
      </div>
    </article>
  </xsl:template>
</xsl:stylesheet>
