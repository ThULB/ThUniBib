<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                exclude-result-prefixes="i18n mcrxml">

  <xsl:param name="UBO.projectid.default"/>
  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match="/thunibib-multiple-identifiers">
    <webpage>
      <title xml:lang="de">Übersicht: Nutzer mit mehreren Identifikatoren gleichen Typs</title>
      <title xml:lang="en">Overview: Users with multiple identifiers of the same type</title>

      <article>
        <xsl:choose>
          <xsl:when test="not(mcrxml:isCurrentUserInRole('admin'))">
            <h5>
              <xsl:value-of select="i18n:translate('component.base.webpage.notLoggedIn')"/>
            </h5>
          </xsl:when>

          <xsl:otherwise>
            <div class="card-body">
              <xsl:value-of select="i18n:translate('thunibib.users.multiple.identifiers.intro')"/>
            </div>

            <div class="card-body">
              <xsl:choose>
                <xsl:when test="identifier">
                  <xsl:apply-templates select="identifier"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="i18n:translate('thunibib.users.multiple.identifiers.all.set')"/>
                </xsl:otherwise>
              </xsl:choose>
            </div>
          </xsl:otherwise>
        </xsl:choose>
      </article>
    </webpage>
  </xsl:template>

  <xsl:template match="identifier[count(user) &gt; 0]">
    <div class="row pt-2 pb-2 border-bottom">
      <div class="col-2 font-weight-bold text-truncate ubo-hover-pointer">

        <button class="btn btn-sm btn-primary text-truncate w-50" data-toggle="collapse" href="#collapse-{@type}"
                role="button" aria-expanded="false" aria-controls="collapse-{@type}" title="{@type} ({count(user)})">
          <xsl:value-of select="@type"/>
        </button>
      </div>
    </div>

    <span class="collapse" id="collapse-{@type}">
      <xsl:for-each select="user">
        <xsl:sort select="@values"/>
        <div class="row border-bottom thunibib-grouped-identifier">
          <div class="col-3 font-weight-bold text-truncate" title="{@values}">
            <xsl:value-of select="@values"/>
          </div>

          <div class="col">
            <div class="row">
              <div class="col-4">
                <xsl:value-of select="@realname"/>
              </div>
              <div class="col">
                <a href="{$WebApplicationBaseURL}servlets/MCRUserServlet?action=show&amp;id={@username}">
                  <xsl:value-of select="@username"/>
                </a>
              </div>
            </div>
          </div>
        </div>
      </xsl:for-each>
    </span>
  </xsl:template>
</xsl:stylesheet>
