<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                exclude-result-prefixes="xsl xalan i18n">

  <xsl:import href="charts/thunibib-statistics-horizontal-bar-chart.xsl"/>

  <xsl:include href="statistics.xsl" />
  <xsl:include href="statistics-oa.xsl" />

  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="RequestURL" />


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
            <xsl:if
                test="not(response/lst[@name='responseHeader']/lst[@name='params']/str[@name='fq'] = 'partOf:&quot;true&quot;'
                            or response/lst[@name='responseHeader']/lst[@name='params']/arr[@name='fq']/str = 'partOf:&quot;true&quot;')">
              <h3>
                <xsl:value-of select="i18n:translate('thunibib.statistic.hint')" />
              </h3>

              <p>
                <xsl:value-of select="i18n:translate('thunibib.statistic.generic.hint.text')" />
              </p>
            </xsl:if>
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
    <script src="{$WebApplicationBaseURL}assets/apexcharts/dist/apexcharts.js"/>
    <script src="{$WebApplicationBaseURL}webjars/highcharts/5.0.1/highcharts.src.js" type="text/javascript"/>
    <script src="{$WebApplicationBaseURL}webjars/highcharts/5.0.1/themes/grid.js" type="text/javascript"/>

    <div id="chartDialog" />
    <xsl:apply-templates select="lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='year'][int]" />
    <xsl:apply-templates select="lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='destatis'][int]" />
    <xsl:call-template name="horizontal-bar-chart">
      <xsl:with-param name="response" select="."/>
      <xsl:with-param name="facet-name" select="'ORIGIN.1'"/>
      <xsl:with-param name="chart-title" select="document('notnull:i18n:thunibib.statistics.chart.title.ORIGIN.1')/i18n/text()"/>
    </xsl:call-template>

    <xsl:call-template name="horizontal-bar-chart">
      <xsl:with-param name="response" select="."/>
      <xsl:with-param name="facet-name" select="'ORIGIN.3'"/>
      <xsl:with-param name="chart-title" select="document('notnull:i18n:thunibib.statistics.chart.title.ORIGIN.3')/i18n/text()"/>
    </xsl:call-template>
    <xsl:apply-templates select="lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='origin_exact'][int]" />
    <xsl:apply-templates select="lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='genre'][int]" />
    <xsl:apply-templates select="lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='nid_connection'][int]" />
  </xsl:template>

</xsl:stylesheet>
