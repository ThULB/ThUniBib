<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl">
  <xsl:import href="thunibib-charts-common.xsl"/>

  <xsl:param name="horizontal" select="'true'"/>
  <xsl:param name="columnWidth" select="'35%'"/>

  <xsl:template match="/response">
    <xsl:apply-templates select="." mode="bar-chart" />
  </xsl:template>

  <xsl:template match="response" mode="bar-chart">
    <xsl:param name="chart-title" select="$chart-title-by-facet"/>
    <xsl:param name="facet-name" select="$facet"/>
    <xsl:param name="height" select="$default-height"/>
    <xsl:param name="horizontal-bars" select="$horizontal"/>
    <xsl:param name="width-bars" select="$columnWidth"/>
    <xsl:param name="classId" select="$classification"/>
    <xsl:param name="generate-labels-from-pivot" select="$labelsFromPivot"/>

    <xsl:if test="not($facet-name = 'nid_connection') or ($facet-name = 'nid_connection' and (document('notnull:callJava:org.mycore.common.xml.MCRXMLFunctions:isCurrentUserGuestUser') = 'false' or $ThUniBib.Statistics.Chart.nid_connection.always.visible = 'true'))">
      <div class="thunibib-chart-container thunibib-column-chart thunibib-column-chart-{$facet-name}">
      <xsl:variable name="labels">
        <xsl:apply-templates select="." mode="generate-chart-labels">
          <xsl:with-param name="facet-name" select="$facet-name"/>
          <xsl:with-param name="classId" select="$classId"/>
          <xsl:with-param name="generate-labels-from-pivot" select="$generate-labels-from-pivot"/>
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

      <xsl:variable name="chart-id" select="concat('chart-bar-', translate($facet-name, '.', '-'))"/>

      <div id="{$chart-id}" class="border border-primary rounded mb-3" data-values="{$values}" data-labels="{$labels}"/>

      <script>
        {
        let options = {
          series: [{
            name: <xsl:value-of select="concat($apos, document('notnull:i18n:ubo.publications')/i18n/text(), $apos)"/>,
            data: <xsl:value-of select="$values"/>
          }],
          chart: {
            type: 'bar',
            height: <xsl:value-of select="concat($apos, $height, 'px', $apos)"/>,
            toolbar: {
              show: false
            }
          },
          plotOptions: {
            bar: {
              borderRadius: 4,
              borderRadiusApplication: 'end',
              columnWidth: <xsl:value-of select="concat($apos, $width-bars, $apos)"/>,
              horizontal: <xsl:value-of select="$horizontal-bars"/>
            }
          },
          xaxis: {
            categories: <xsl:value-of select="$labels"/>,
          },
          yaxis: {
            labels: {
              align: 'left'
            }
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
