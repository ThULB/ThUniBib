<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                exclude-result-prefixes="xsl xalan i18n">

  <xsl:param name="MCR.ORCID2.BaseURL"/>
  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match="/orcid-statistics">
    <xsl:apply-templates select="trusted-party" mode="orcid-statistics"/>
  </xsl:template>

  <xsl:template match="trusted-party[count(user) &gt; 0]" mode="orcid-statistics">
    <article class="card">
      <div class="card-body">
        <h3>
          <xsl:value-of select="i18n:translate('thunibib.statistic.orcid.trustedParty')"/>
        </h3>

        <div class="row">
          <div class="col-12 col-md-3">
            <xsl:value-of select="concat(i18n:translate('thunibib.statistic.orcid.trustedParty.introText'), ':')"/>
            <span class="font-weight-bold pl-3">
              <xsl:value-of select="count(user)"/>
            </span>
          </div>

          <div class="col-12 col-md">
            <xsl:for-each select="user">
              <div class="row thunibib-orcid-statistic-row thunibib-orcid-statistic-row-{position() mod 2}" title="{position()}">
                <div class="col-2 text-truncate" title="{@realName}">
                  <a href="{$WebApplicationBaseURL}servlets/MCRUserServlet?action=show&amp;id={@name}">
                    <xsl:value-of select="@realName"/>
                  </a>
                </div>

                <xsl:variable name="orcid-id" select="attributes/attribute[@name='id_orcid']/@value"/>
                <div class="col-3 text-truncate" title="{$orcid-id}">
                  <a href="{$MCR.ORCID2.BaseURL}/{$orcid-id}">
                    <xsl:value-of select="$orcid-id"/>
                  </a>
                </div>
              </div>
            </xsl:for-each>
          </div>
        </div>
      </div>
    </article>
  </xsl:template>
</xsl:stylesheet>
