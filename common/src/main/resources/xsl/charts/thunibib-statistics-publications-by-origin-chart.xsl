<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl">

  <xsl:import href="thunibib-charts-common.xsl"/>

  <xsl:template match="/response">
    <xsl:if test="result/@numFound &gt; 0">
      <div class="thunibib-chart-container thunibib-column-chart thunibib-column-chart-{$facet}">
        <xsl:variable name="labels">
          [
          <xsl:for-each select="//lst[@name = $facet]/int">
            <xsl:choose>
              <xsl:when test="document(concat('callJava:org.mycore.common.xml.MCRXMLFunctions:isCategoryID:ORIGIN:', @name)) = 'true'">
                <xsl:value-of select="concat($apos, document(concat('callJava:org.mycore.common.xml.MCRXMLFunctions:getDisplayName:ORIGIN:', @name)), $apos)"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="concat($apos, @name, $apos)"/>
              </xsl:otherwise>
            </xsl:choose>

            <xsl:if test="not(position() = last())">
              ,
            </xsl:if>
          </xsl:for-each>
          ]
        </xsl:variable>
        <xsl:variable name="values">
          [
          <xsl:for-each select="//lst[@name = $facet]/int">
            <xsl:value-of select="text()"/>
            <xsl:if test="not(position()=last())">
              ,
            </xsl:if>
          </xsl:for-each>
          ]
        </xsl:variable>

        <xsl:variable name="chart-id" select="concat('chart-bar-', translate($facet, '.', '-'))"/>

        <div id="{$chart-id}" class="border border-primary rounded mb-3"/>

        <script>
          {
          let options = {
            series: [{
              name: <xsl:value-of select="concat($apos, document('notnull:i18n:ubo.publications')/i18n/text(), $apos)"/>,
              data: <xsl:value-of select="$values"/>
            }],
            chart: {
              type: 'bar',
              height: 450,
              toolbar: {
                show: false
              }
            },
            plotOptions: {
              bar: {
                borderRadius: 4,
                borderRadiusApplication: 'end',
                horizontal: true,
              }
            },
            xaxis: {
              categories: <xsl:value-of select="$labels"/>,
            },
            title: {
              text: <xsl:value-of select="concat($apos, $chart-title-by-facet, $apos)"/>,
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
