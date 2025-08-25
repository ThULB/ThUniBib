<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl">

  <xsl:import href="thunibib-charts-common.xsl"/>
  <xsl:param name="json-facet-name"/>

  <xsl:variable name="chart-title" select="document('notnull:i18n:thunibib.statistics.title.chart.oa.by.year')/i18n/text()"/>

  <xsl:template match="/response">
    <xsl:apply-templates mode="thunibib-oa-statistics" select=".">
      <xsl:with-param name="facet-name" select="$json-facet-name"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="response" mode="thunibib-oa-statistics">
    <xsl:param name="facet-name"/>

    <xsl:variable name="x-axis">
      <xsl:text>[</xsl:text>
      <xsl:for-each select="lst[@name = 'facets']/lst[@name=$facet-name]/arr[@name='buckets']/lst/int[@name='val']">
        <xsl:value-of select="concat($apos, text(), $apos)"/>
        <xsl:if test="not(position() = last())">
          <xsl:text>,</xsl:text>
        </xsl:if>
      </xsl:for-each>
      <xsl:text>]</xsl:text>
    </xsl:variable>

    <xsl:variable name="values-oa-status-oa">
      <xsl:call-template name="values-oa-by-type">
        <xsl:with-param name="type" select="'oa'"/>
        <xsl:with-param name="bucket" select="'oa_status'"/>
        <xsl:with-param name="facet-name" select="$facet-name"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="values-oa-status-closed">
      <xsl:call-template name="values-oa-by-type">
        <xsl:with-param name="type" select="'closed'"/>
        <xsl:with-param name="bucket" select="'oa_status'"/>
        <xsl:with-param name="facet-name" select="$facet-name"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="values-oa-status-unchecked">
      <xsl:call-template name="values-oa-by-type">
        <xsl:with-param name="type" select="'unchecked'"/>
        <xsl:with-param name="bucket" select="'oa_status'"/>
        <xsl:with-param name="facet-name" select="$facet-name"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="values-mediaType-online">
      <xsl:call-template name="values-oa-by-type">
        <xsl:with-param name="bucket" select="'mediaType'"/>
        <xsl:with-param name="type" select="'online'"/>
        <xsl:with-param name="facet-name" select="$facet-name"/>
      </xsl:call-template>
    </xsl:variable>

    <xsl:variable name="values-mediaType-other">
      <xsl:call-template name="values-oa-by-type">
        <xsl:with-param name="bucket" select="'mediaType'"/>
        <xsl:with-param name="type" select="'other'"/>
        <xsl:with-param name="facet-name" select="$facet-name"/>
      </xsl:call-template>
    </xsl:variable>

    <div class="border border-primary mb-3 rounded">
      <div id="thunibib-statistics-oa-chart" class="thunibib-chart-container"
           data-values-oa-status-oa="{$values-oa-status-oa}"
           data-values-oa-status-closed="{$values-oa-status-closed}"
           data-values-oa-status-unchecked="{$values-oa-status-unchecked}"
           data-values-mediaType-online="{$values-mediaType-online}"
           data-values-mediaType-other="{$values-mediaType-other}"/>

      <script>
        let options = {
          series: [
          {
            name: 'Online',
            group: 'online.status',
            data: <xsl:value-of select="$values-mediaType-online"/>
          },
          {
            name: 'Open Access',
            group: 'oa-status',
            data: <xsl:value-of select="$values-oa-status-oa"/>
          },
          {
            name: 'Closed Access',
            group: 'oa-status',
            data: <xsl:value-of select="$values-oa-status-closed"/>
          },
          {
            name: <xsl:value-of select="concat($apos, document('notnull:i18n:thunibib.statistics.label.chart.other')/i18n/text(), $apos)"/>,
            group: 'online.status',
            data: <xsl:value-of select="$values-mediaType-other"/>
          },
          {
            name: <xsl:value-of select="concat($apos, document('notnull:i18n:thunibib.statistics.label.chart.unchecked')/i18n/text(), $apos)"/>,
            group: 'oa-status',
            data: <xsl:value-of select="$values-oa-status-unchecked"/>
          }],
          chart: {
            type: 'bar',
            height: 500,
            background: '#FFFFFF',
            stacked: true,
            toolbar: {
              show: false
            }
          },
          stroke: {
            width: 1,
            colors: ['#fff']
          },
          plotOptions: {
            bar: {
              horizontal: false
            }
          },
          xaxis: {
            categories: <xsl:value-of select="$x-axis"/>
          },
          fill: {
            opacity: 1
          },
          colors: ['#0000ff', '#ee9f27', '#8f989d', '#00BFFF', '#d3d3d3'],
          legend: {
            position: 'bottom',
            horizontalAlign: 'center',
            clusterGroupedSeries: false
          },
          dataLabels: {
            enabled: false
          },
          title: {
            text: <xsl:value-of select="concat($apos, $chart-title, $apos)"/>,
            align: 'center',
            style: {
              fontSize:  '16px',
              fontWeight: 'bold',
              fontFamily: 'Trebuchet MS',
              color:  '#000'
            }
          }
        };

        let chart = new ApexCharts(document.querySelector("#thunibib-statistics-oa-chart"), options);
        chart.render();
      </script>
    </div>
  </xsl:template>

  <xsl:template name="values-oa-by-type">
    <xsl:param name="type"/>
    <xsl:param name="bucket"/>
    <xsl:param name="facet-name"/>

    <xsl:text>[</xsl:text>
    <xsl:for-each select="lst[@name = 'facets']/lst[@name=$facet-name]/arr[@name='buckets']/lst/int[@name='val']"> <!-- year -->
      <xsl:choose>
        <xsl:when test="../lst[@name=$bucket]/arr/lst[str[@name='val' and text() = $type]]/int[@name='count']">
          <xsl:value-of select="../lst[@name=$bucket]/arr/lst[str[@name='val' and text() = $type]]/int[@name='count']"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="number(0)"/>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:if test="not(position() = last())">
        <xsl:text>,</xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text>]</xsl:text>
  </xsl:template>

</xsl:stylesheet>
