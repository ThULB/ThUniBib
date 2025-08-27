<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" exclude-result-prefixes="xsl">
  <xsl:import href="thunibib-charts-common.xsl"/>

  <!-- The name of the inner bucket, the outer bucket is the year of publication by default -->
  <xsl:param name="inner-bucket-name" select="'oa'"/>

  <!-- The name of the class to resolve the labels of the entries of the inner bucket -->
  <xsl:param name="inner-bucket-class" select="'oa'"/>

  <xsl:variable name="inner-bucket-values-categories" select="document(concat('notnull:classification:metadata:-1:children:', $inner-bucket-class))//mycoreclass/categories//category"/>

  <xsl:template match="/response">
    <xsl:apply-templates select="." mode="stacked-bar-oa-chart" />
  </xsl:template>

  <xsl:template match="response" mode="stacked-bar-oa-chart">
    <xsl:param name="chart-title" select="document('notnull:i18n:stats.oa.title')/i18n/text()"/>

    <xsl:if test="result/@numFound &gt; 0">
      <div class="thunibib-chart-container thunibib-stacked-column-chart thunibib-stacked-column-chart-{$facet}">
        <xsl:variable name="labels">
          <xsl:text>[</xsl:text>
          <xsl:for-each select="//lst[@name='facets']/lst[@name='year']/arr[@name='buckets']/lst/int[@name='val']">
            <xsl:value-of select="concat($apos, text(), $apos)"/>
            <xsl:if test="not(position() = last())">
              <xsl:text>, </xsl:text>
            </xsl:if>
          </xsl:for-each>
          <xsl:text>]</xsl:text>
        </xsl:variable>

        <xsl:variable name="colors">
          <xsl:variable name="classification" select="document(concat('notnull:classification:metadata:-1:children:', $inner-bucket-class))"/>
          <xsl:text>[</xsl:text>
          <xsl:for-each select="$classification//category">
            <xsl:if test="label[@xml:lang = 'x-color-chart']">
              <xsl:value-of select="concat($apos, label[@xml:lang = 'x-color-chart']/@text, $apos)"/>
              <xsl:if test="not(position() = last())">
                <xsl:text>, </xsl:text>
              </xsl:if>
            </xsl:if>
          </xsl:for-each>
          <xsl:value-of select="concat(', ', $apos, '#5858FA', $apos)"/>
          <xsl:text>]</xsl:text>
        </xsl:variable>

        <xsl:variable name="r" select="."/>
        <xsl:variable name="series">
          <xsl:text>[</xsl:text>
          <xsl:for-each select="$inner-bucket-values-categories">
            <xsl:variable name="bucket-label">
              <xsl:choose>
                <xsl:when test="@ID = 'oa'">
                  <xsl:value-of select="concat(document(concat('notnull:callJava:org.mycore.common.xml.MCRXMLFunctions:getDisplayName:', $inner-bucket-class, ':', @ID)), ' ', document('notnull:i18n:stats.oa.unspecified')/i18n/text())"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="document(concat('notnull:callJava:org.mycore.common.xml.MCRXMLFunctions:getDisplayName:', $inner-bucket-class, ':', @ID))"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:variable>

            <xsl:call-template name="create-data-array">
              <xsl:with-param name="bucket-label" select="$bucket-label"/>
              <xsl:with-param name="bucket" select="@ID"/>
              <xsl:with-param name="r" select="$r"/>
            </xsl:call-template>
            <xsl:if test="not(position()=last())">
              <xsl:text>, </xsl:text>
            </xsl:if>
          </xsl:for-each>

          <xsl:text>, </xsl:text>

          <xsl:call-template name="create-no-oa-data-array">
            <xsl:with-param name="r" select="$r"/>
          </xsl:call-template>
          <xsl:text>]</xsl:text>
        </xsl:variable>

        <xsl:variable name="chart-id" select="concat('chart-stacked-bar-oa', translate($facet, '.', '-'))"/>

        <div id="{$chart-id}" class="border border-primary rounded mb-3" data-label="{$labels}" data-series="{$series}" data-colors="{$colors}"/>

        <script>
          {
            let options = {
              series: <xsl:value-of select="$series"/>,
              colors: <xsl:value-of select="$colors"/>,
              chart: {
                type: 'bar',
                stacked: true,
                toolbar: {
                  show: false
                }
              },
              plotOptions: {
                bar: {
                  borderRadius: 0,
                  borderRadiusApplication: 'end'
                }
              },
              dataLabels: {
                style: {
                  colors: ['#123']
                },
                dropShadow: {
                  enabled: true,
                  color: '#000',
                  blur: 0,
                  opacity: 0
                }
              },
              xaxis: {
                stepSize: 1,
                categories: <xsl:value-of select="$labels"/>,
                labels:{
                  show: true,
                  rotate: 0,
                  hideOverlappingLabels: false,
                  trim: true
                }
              },
              title: {
                text: <xsl:value-of select="concat($apos, $chart-title, $apos)"/>,
                align: 'center'
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

  <xsl:template name="create-data-array">
    <xsl:param name="bucket-label"/>
    <xsl:param name="bucket"/>
    <xsl:param name="r"/>

    <xsl:value-of select="concat('{name:', $apos, $bucket-label, $apos, ', ' )"/>
    <xsl:text>data: [</xsl:text>

    <!-- for every year-->
    <xsl:for-each select="$r//lst[@name='facets']/lst[@name='year']/arr[@name='buckets']/lst">
      <xsl:choose>
        <xsl:when test="lst[@name=$inner-bucket-name]/arr/lst[str[@name='val'][text()=$bucket]]">
          <xsl:value-of select="lst[@name=$inner-bucket-name]/arr/lst[str[@name='val'][text()=$bucket]]/int"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'0'"/>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:if test="not(position()=last())">
        <xsl:text>, </xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text>]}</xsl:text>
  </xsl:template>

  <xsl:template name="create-no-oa-data-array">
    <xsl:param name="bucket-label" select="document('notnull:i18n:stats.oa.notOA')/i18n/text()"/>
    <xsl:param name="r"/>

    <xsl:variable name="oa-identifiers">
      <xsl:for-each select="$inner-bucket-values-categories/@ID">
        <xsl:value-of select="."/>
        <xsl:if test="not(position()=last())">
          <xsl:value-of select="' '"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <xsl:value-of select="concat('{name:', $apos, $bucket-label, $apos, ', ' )"/>
    <xsl:text>data: [</xsl:text>
    <!-- for every year-->
    <xsl:for-each select="$r//lst[@name='facets']/lst[@name='year']/arr[@name='buckets']/lst">
      <xsl:variable name="total" select="int[@name='count']"/>
      <xsl:variable name="with-oa" select="sum(lst[@name=$inner-bucket-name]/arr/lst[(contains($oa-identifiers, str[@name='val']))]/int[@name='count'])"/>
      <xsl:value-of select="$total - $with-oa"/>
      <xsl:if test="not(position()=last())">
        <xsl:text>, </xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text>]}</xsl:text>
  </xsl:template>
</xsl:stylesheet>
