<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl">

  <xsl:import href="thunibib-charts-common.xsl"/>

  <xsl:template match="/response">
    <xsl:apply-templates select="." mode="pie-chart"/>
  </xsl:template>

  <xsl:template match="response" mode="pie-chart">
    <xsl:param name="chart-title" select="$chart-title-by-facet"/>
    <xsl:param name="facet-name" select="$facet"/>
    <xsl:param name="classId" select="$classification"/>
    <xsl:param name="height" select="$default-height"/>


    <xsl:if test="result/@numFound &gt; 0">
      <div class="thunibib-chart-container thunibib-pie-chart thunibib-pie-chart-{$facet-name}">
        <xsl:variable name="labels">
          <xsl:apply-templates select="." mode="generate-chart-labels">
            <xsl:with-param name="facet-name" select="$facet-name"/>
            <xsl:with-param name="classId" select="$classId"/>
          </xsl:apply-templates>
        </xsl:variable>

        <xsl:variable name="values">
          <xsl:text>[</xsl:text>
          <xsl:for-each select="//lst[@name = $facet-name]/int">
            <xsl:value-of select="text()"/>
            <xsl:if test="not(position()=last())">
              <xsl:text>,</xsl:text>
            </xsl:if>
          </xsl:for-each>
          <xsl:text>]</xsl:text>
        </xsl:variable>

        <xsl:variable name="chart-id" select="concat('chart-pie-', translate($facet-name, '.', '-'))"/>

        <div id="{$chart-id}" class="border border-primary rounded mb-3" data-labels="{$labels}" data-values="{$values}"/>

        <script>
          {
            let options = {
              chart: {
                type: 'pie',
                height: <xsl:value-of select="$height"/>
              },
              dataLabels: {
                enabled: true
              },
              plotOptions: {
                pie: {
                  expandOnClick: false,
                  donut: {
                    labels: {
                      show: true,
                      total: {
                        show: false,
                        label: <xsl:value-of select="concat($apos, document('notnull:i18n:thunibib.statistics.total')/i18n/text(), $apos)"/>
                      }
                    }
                  }
                }
              },
              series: <xsl:value-of select="$values"/>,
              labels: <xsl:value-of select="$labels"/>,
              title: {
                text: <xsl:value-of select="concat($apos, $chart-title, $apos)"/>,
                align: 'center'
              },
              legend: {
                floating: true
              }
            };

            let chart = new ApexCharts(document.querySelector(
            <xsl:value-of select="concat($apos, '#', $chart-id, $apos)"/>
            ), options);
            chart.render();
          }
        </script>
      </div>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
