<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:solr="xalan://org.mycore.solr.MCRSolrUtils"
                xmlns:encoder="xalan://java.net.URLEncoder"
                exclude-result-prefixes="xsl mods xalan i18n encoder solr">

  <xsl:import href="resource:xsl/response-sidebar-admin.xsl"/>

  <xsl:param name="ThUniBib.response-sidebar-lastimported.max.entries" select="30"/>

  <xsl:template match="lst[@name='facet_fields']/lst[@name='importID']">
    <hgroup>
      <h3><xsl:value-of select="i18n:translate('response.sidebar.lastimported')"/>:
      </h3>
    </hgroup>
    <ul class="list-group list-group-flush thunibib-response-sidebar-lastimported">
      <xsl:for-each select="int">
        <xsl:sort select="@name" order="descending"/>
        <xsl:if test="position() &lt;= $ThUniBib.response-sidebar-lastimported.max.entries">
          <xsl:call-template name="output.value">
            <xsl:with-param name="label" select="@name"/>
            <xsl:with-param name="value" select="text()"/>
            <xsl:with-param name="query" select="concat('importID:', $quote,solr:escapeSearchValue(@name), $quote)"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:for-each>
    </ul>
  </xsl:template>

  <xsl:template name="output.value">
    <xsl:param name="label"/>
    <xsl:param name="value"/>
    <xsl:param name="query"/>

    <li class="list-group-item py-0 px-0 border-0">
      <a href="{$ServletsBaseURL}solr/select?q={encoder:encode($query,'UTF-8')}">
        <div class="row">
          <div class="col-9 text-right">
            <xsl:value-of select="$label"/>:
          </div>
          <div class="col text-left">
            <xsl:value-of select="$value"/>
          </div>
        </div>
      </a>
    </li>
  </xsl:template>
</xsl:stylesheet>
