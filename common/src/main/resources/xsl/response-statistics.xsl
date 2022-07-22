<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                exclude-result-prefixes="xsl xalan i18n">

  <xsl:include href="statistics.xsl" />
  <xsl:include href="statistics-oa.xsl" />
  <xsl:param name="WebApplicationBaseURL" />

  <xsl:template match="/">
    <html id="dozbib.search">
      <head>
        <title>
          <xsl:call-template name="page.title" />
        </title>
      </head>
      <body>
        <article class="card mb-3">
          <div class="card-body bg-alternative">
            <h3>
              <xsl:value-of select="i18n:translate('thunibib.statistic.hint')" />
            </h3>
            <p>
              <xsl:value-of select="i18n:translate('thunibib.statistic.generic.hint.text')" />
            </p>
          </div>
        </article>
        <xsl:apply-templates select="/" mode="oa-statistics" />
        <xsl:apply-templates select="response" />
      </body>
    </html>
  </xsl:template>

  <xsl:template name="page.title">
    <xsl:value-of select="i18n:translate('stats.page.title')" />
    <xsl:text>: </xsl:text>
    <xsl:value-of select="/response/result[@name='response']/@numFound" />
    <xsl:text> </xsl:text>
    <xsl:value-of select="i18n:translate('ubo.publications')" />
  </xsl:template>

  <xsl:template match="response" priority="1">
    <script src="{$WebApplicationBaseURL}webjars/highcharts/5.0.1/highcharts.src.js" type="text/javascript"></script>
    <script src="{$WebApplicationBaseURL}webjars/highcharts/5.0.1/themes/grid.js" type="text/javascript"></script>

    <div id="chartDialog" />

    <xsl:for-each select="lst[@name='facet_counts']">
      <xsl:apply-templates select="lst[@name='facet_fields']/lst[@name='year'][int]" />
      <xsl:apply-templates select="lst[@name='facet_fields']/lst[@name='subject'][int]" />
      <xsl:apply-templates select="lst[@name='facet_fields']/lst[@name='origin'][int]" />
      <xsl:apply-templates select="lst[@name='facet_fields']/lst[@name='genre'][int]" />
      <xsl:apply-templates select="lst[@name='facet_fields']/lst[@name='nid_connection'][int]" />
    </xsl:for-each>

  </xsl:template>

</xsl:stylesheet>
