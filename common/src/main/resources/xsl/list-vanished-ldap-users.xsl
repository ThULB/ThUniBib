<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                exclude-result-prefixes="i18n">

  <xsl:param name="UBO.projectid.default"/>
  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match="/vanished-ldap-users">
    <webpage>
      <title xml:lang="de">Übersicht: nicht über LDAP findbare Nutzer</title>
      <title xml:lang="en">Overview: vanished LDAP Users</title>

      <article>
        <script src="{$WebApplicationBaseURL}js/LDAPUserManagement.js"/>

        <div class="card-body">
          <xsl:value-of select="i18n:translate('thunibib.vanished.users.intro')"/>
        </div>

        <div class="card-body">
          <xsl:apply-templates mode="heading"/>
          <xsl:apply-templates select="user"/>
        </div>

      </article>
    </webpage>
  </xsl:template>

  <xsl:template match="*" mode="heading">
    <div class="row font-weight-bold border-bottom pb-2 pt-2">
      <div class="col-2 text-truncate">
        <xsl:value-of select="'name'"/>
      </div>

      <div class="col-2">
        <xsl:value-of select="'realName'"/>
      </div>

      <div class="col-6 text-truncate">
        <xsl:value-of select="'eduPersonUniqueId'"/>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="user">
    <div class="row pt-2 pb-2 border-bottom">
      <div class="col-2 font-weight-bold text-truncate">
        <a href="{$WebApplicationBaseURL}servlets/MCRUserServlet?action=show&amp;id={id}">
          <xsl:value-of select="name"/>
        </a>
      </div>

      <div class="col-2">
        <xsl:value-of select="realName"/>
      </div>

      <div class="col-6 text-truncate">
        <xsl:attribute name="title">
          <xsl:for-each select="attributes/*">
            <xsl:sort select="local-name(.)"/>
            <xsl:value-of select="concat(local-name(.), ': ', .)"/>
            <xsl:value-of select="'&#xd;'"/>
          </xsl:for-each>
        </xsl:attribute>

        <span>
          <xsl:value-of select="attributes/*[local-name() = concat('id_', $UBO.projectid.default)][1]"/>
        </span>
      </div>

      <div class="col">
        <a class="btn btn-outline-danger" onclick="LDAPUserManagement.moveToLocalRealm('{id}')">
          <xsl:value-of select="i18n:translate('thunibib.vanished.users.move')"/>
        </a>
      </div>
    </div>
  </xsl:template>

</xsl:stylesheet>
