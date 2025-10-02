<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                exclude-result-prefixes="i18n mcrxml xalan xsl">

  <xsl:import href="charts/bar-chart.xsl"/>
  <xsl:import href="charts/oa-chart.xsl" />
  <xsl:import href="charts/pie-chart.xsl"/>
  <xsl:import href="charts/bar-stacked-oa-chart.xsl" />

  <xsl:include href="statistics.xsl" />

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
    <!--
    <xsl:apply-templates select="." mode="charts-common-oa-statistics">
      <xsl:with-param name="facet-name" select="'mediaTypePerYearAndOA'"/>
    </xsl:apply-templates>
    -->
    <xsl:apply-templates select="." mode="stacked-bar-oa-chart"/>

    <xsl:apply-templates select="." mode="bar-chart">
      <xsl:with-param name="chart-title" select="document('notnull:i18n:ChartsCommon.chart.title.year')/i18n/text()"/>
      <xsl:with-param name="facet-name" select="'year'"/>
      <xsl:with-param name="horizontal-bars" select="'false'"/>
    </xsl:apply-templates>

    <xsl:apply-templates select="." mode="bar-chart">
      <xsl:with-param name="chart-title" select="document('notnull:i18n:ChartsCommon.chart.title.destatis')/i18n/text()"/>
      <xsl:with-param name="facet-name" select="'destatis'"/>
      <xsl:with-param name="height" select="1512"/>
    </xsl:apply-templates>

    <xsl:apply-templates select="." mode="bar-chart">
      <xsl:with-param name="chart-title" select="document('notnull:i18n:ChartsCommon.chart.title.origin_exact')/i18n/text()"/>
      <xsl:with-param name="classId" select="'ORIGIN'"/>
      <xsl:with-param name="facet-name" select="'origin_exact'"/>
      <xsl:with-param name="height" select="1800"/>
    </xsl:apply-templates>

    <xsl:apply-templates select="." mode="pie-chart">
      <xsl:with-param name="chart-title" select="document('notnull:i18n:ChartsCommon.chart.title.genre')/i18n/text()"/>
      <xsl:with-param name="classId" select="'ubogenre'"/>
      <xsl:with-param name="facet-name" select="'genre'"/>
    </xsl:apply-templates>

    <!-- This chart will only get displayed in Jena -->
    <xsl:if test="contains('fsu',  $UBO.projectid.default)">
      <xsl:apply-templates select="." mode="pie-chart">
        <xsl:with-param name="chart-title" select="document('notnull:i18n:ChartsCommon.chart.title.fundingType')/i18n/text()"/>
        <xsl:with-param name="facet-name" select="'fundingType'"/>
      </xsl:apply-templates>
    </xsl:if>

    <xsl:apply-templates select="." mode="bar-chart">
      <xsl:with-param name="chart-title" select="document('notnull:i18n:ChartsCommon.chart.title.nid_connection')/i18n/text()"/>
      <xsl:with-param name="facet-name" select="'nid_connection'"/>
      <xsl:with-param name="generate-labels-from-pivot" select="'true'"/>
      <xsl:with-param name="height" select="1800"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="response[$UBO.projectid.default = 'ubw']" priority="1">
    <xsl:apply-templates select="." mode="charts-common-oa-statistics">
      <xsl:with-param name="facet-name" select="'mediaTypePerYearAndOA'"/>
    </xsl:apply-templates>

    <xsl:apply-templates select="." mode="stacked-bar-oa-chart"/>

    <xsl:apply-templates select="." mode="bar-chart">
      <xsl:with-param name="chart-title" select="document('notnull:i18n:ChartsCommon.chart.title.year')/i18n/text()"/>
      <xsl:with-param name="facet-name" select="'year'"/>
      <xsl:with-param name="horizontal-bars" select="'false'"/>
    </xsl:apply-templates>

    <xsl:if test="mcrxml:isCurrentUserInRole('admin')">
      <xsl:apply-templates select="." mode="bar-chart">
        <xsl:with-param name="chart-title" select="document('notnull:i18n:ChartsCommon.chart.title.destatis')/i18n/text()"/>
        <xsl:with-param name="facet-name" select="'destatis'"/>
        <xsl:with-param name="height" select="1512"/>
      </xsl:apply-templates>
    </xsl:if>

    <xsl:apply-templates select="." mode="bar-chart">
      <xsl:with-param name="chart-title" select="document('notnull:i18n:ChartsCommon.chart.title.ORIGIN.1')/i18n/text()"/>
      <xsl:with-param name="classId" select="'ORIGIN'"/>
      <xsl:with-param name="facet-name" select="'ORIGIN.1'"/>
    </xsl:apply-templates>

    <xsl:apply-templates select="." mode="bar-chart">
      <xsl:with-param name="chart-title" select="document('notnull:i18n:ChartsCommon.chart.title.ORIGIN.3')/i18n/text()"/>
      <xsl:with-param name="classId" select="'ORIGIN'"/>
      <xsl:with-param name="facet-name" select="'ORIGIN.2.statistics'"/>
      <xsl:with-param name="height" select="1024"/>
    </xsl:apply-templates>

    <xsl:apply-templates select="." mode="pie-chart">
      <xsl:with-param name="chart-title" select="document('notnull:i18n:ChartsCommon.chart.title.genre')/i18n/text()"/>
      <xsl:with-param name="classId" select="'ubogenre'"/>
      <xsl:with-param name="facet-name" select="'genre'"/>
    </xsl:apply-templates>

    <xsl:apply-templates select="." mode="pie-chart">
      <xsl:with-param name="chart-title" select="document('notnull:i18n:ChartsCommon.chart.title.licenses_facet')/i18n/text()"/>
      <xsl:with-param name="classId" select="'licenses'"/>
      <xsl:with-param name="facet-name" select="'licenses_facet'"/>
    </xsl:apply-templates>

    <xsl:if test="mcrxml:isCurrentUserInRole('admin')">
      <xsl:apply-templates select="." mode="bar-chart">
        <xsl:with-param name="chart-title" select="document('notnull:i18n:ChartsCommon.chart.title.nid_connection')/i18n/text()"/>
        <xsl:with-param name="facet-name" select="'nid_connection'"/>
        <xsl:with-param name="generate-labels-from-pivot" select="'true'"/>
        <xsl:with-param name="height" select="1800"/>
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
