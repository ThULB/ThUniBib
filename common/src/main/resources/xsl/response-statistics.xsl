<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                exclude-result-prefixes="xsl xalan i18n">

  <xsl:import href="charts/thunibib-statistics-bar-chart.xsl"/>
  <xsl:import href="charts/thunibib-statistics-pie-chart.xsl"/>
  <xsl:import href="charts/thunibib-statistics-oa-chart.xsl" />

  <xsl:include href="statistics.xsl" />
  <xsl:include href="statistics-oa.xsl" />

  <xsl:param name="WebApplicationBaseURL" />
  <xsl:param name="RequestURL" />
  <xsl:param name="UBO.projectid.default"/>

  <xsl:template match="/">
    <html id="dozbib.search">
      <head>
        <title>
          <xsl:call-template name="page.title" />
        </title>

        <script src="{$WebApplicationBaseURL}assets/apexcharts/dist/apexcharts.js"/>
        <script src="{$WebApplicationBaseURL}webjars/highcharts/5.0.1/highcharts.src.js"/>
        <script src="{$WebApplicationBaseURL}webjars/highcharts/5.0.1/themes/grid.js"/>
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

        <xsl:apply-templates select="./response" mode="thunibib-oa-statistics" >
          <xsl:with-param name="facet-name" select="'mediaTypePerYearAndOA'"/>
        </xsl:apply-templates>
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
    <div id="chartDialog" />

    <xsl:apply-templates select="." mode="bar-chart">
      <xsl:with-param name="chart-title" select="document('notnull:i18n:thunibib.statistics.chart.title.year')/i18n/text()"/>
      <xsl:with-param name="facet-name" select="'year'"/>
      <xsl:with-param name="horizontal-bars" select="'false'"/>
    </xsl:apply-templates>

    <xsl:apply-templates select="." mode="bar-chart">
      <xsl:with-param name="chart-title" select="document('notnull:i18n:thunibib.statistics.chart.title.destatis')/i18n/text()"/>
      <xsl:with-param name="facet-name" select="'destatis'"/>
      <xsl:with-param name="height" select="1512"/>
      <xsl:with-param name="horizontal-bars" select="'true'"/>
    </xsl:apply-templates>

    <!-- This charts will only get displayed in Weimar -->
    <xsl:if test="$UBO.projectid.default = 'ubw'">
      <xsl:apply-templates select="." mode="bar-chart">
        <xsl:with-param name="chart-title" select="document('notnull:i18n:thunibib.statistics.chart.title.ORIGIN.1')/i18n/text()"/>
        <xsl:with-param name="classId" select="'ORIGIN'"/>
        <xsl:with-param name="facet-name" select="'ORIGIN.1'"/>
        <xsl:with-param name="horizontal-bars" select="'true'"/>
      </xsl:apply-templates>

      <xsl:apply-templates select="." mode="bar-chart">
        <xsl:with-param name="chart-title" select="document('notnull:i18n:thunibib.statistics.chart.title.ORIGIN.3')/i18n/text()"/>
        <xsl:with-param name="classId" select="'ORIGIN'"/>
        <xsl:with-param name="facet-name" select="'ORIGIN.2.statistics'"/>
        <xsl:with-param name="height" select="1024"/>
        <xsl:with-param name="horizontal-bars" select="'true'"/>
      </xsl:apply-templates>
    </xsl:if>

    <xsl:apply-templates select="." mode="bar-chart">
      <xsl:with-param name="chart-title" select="document('notnull:i18n:thunibib.statistics.chart.title.origin_exact')/i18n/text()"/>
      <xsl:with-param name="classId" select="'ORIGIN'"/>
      <xsl:with-param name="facet-name" select="'origin_exact'"/>
      <xsl:with-param name="height" select="1800"/>
      <xsl:with-param name="horizontal-bars" select="'true'"/>
    </xsl:apply-templates>

    <xsl:apply-templates select="." mode="pie-chart">
      <xsl:with-param name="chart-title" select="document('notnull:i18n:thunibib.statistics.chart.title.genre')/i18n/text()"/>
      <xsl:with-param name="classId" select="'ubogenre'"/>
      <xsl:with-param name="facet-name" select="'genre'"/>
    </xsl:apply-templates>

    <xsl:apply-templates select="." mode="pie-chart">
      <xsl:with-param name="chart-title" select="document('notnull:i18n:thunibib.statistics.chart.title.fundingType')/i18n/text()"/>
      <xsl:with-param name="facet-name" select="'fundingType'"/>
    </xsl:apply-templates>

    <xsl:apply-templates select="lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='nid_connection'][int]" />
  </xsl:template>

</xsl:stylesheet>
