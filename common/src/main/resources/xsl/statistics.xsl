<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:encoder="xalan://java.net.URLEncoder"
  xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions"

  exclude-result-prefixes="xsl xalan i18n encoder mcrxsl">

  <xsl:param name="CurrentLang" />
  <xsl:param name="ServletsBaseURL" />
  <xsl:param name="UBO.LSF.Link" />

  <xsl:param name="UBO.Statistics.Color.Bar" />
  <xsl:param name="UBO.Statistics.Style.Labels" />

  <xsl:variable name="count" select="concat(i18n:translate('stats.count'),' ',i18n:translate('ubo.publications'))" />

  <xsl:template match="/response">
    <xsl:for-each select="lst[@name='facet_counts']">
      <xsl:apply-templates select="lst[@name='facet_fields']/lst[@name='year'][int]" />
      <xsl:apply-templates select="lst[@name='facet_fields']/lst[@name='subject'][int]" />
      <xsl:apply-templates select="lst[@name='facet_fields']/lst[@name='origin'][int]" />
      <xsl:apply-templates select="lst[@name='facet_fields']/lst[@name='genre'][int]" />
      <xsl:apply-templates select="lst[@name='facet_fields']/lst[@name='oa'][int]" />
      <xsl:apply-templates select="lst[@name='facet_fields']/lst[@name='facet_person'][int]" />
      <xsl:apply-templates select="lst[@name='facet_fields']/lst[@name='nid_connection'][int]" />
      <xsl:apply-templates select="lst[@name='facet_pivot']/arr[@name='name_id_type,name_id_type']" />
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="lst[@name='facet_fields']/lst[@name='origin']">
    <xsl:variable name="title" select="concat(i18n:translate('ubo.publications'),' / ',i18n:translate('ubo.department'))"/>

    <section class="card mb-3">
      <div class="card-body">
        <div id="chartOrigin" style="width:100%; height:{50 + count(int) * 30}px" />
        <script type="text/javascript">
              $(document).ready(function() {
                new Highcharts.Chart({
                  chart: {
                    renderTo: 'chartOrigin',
                    type: 'bar',
                    backgroundColor: 'transparent',
                    borderWidth: 0,
                    shadow: false,
                    events: {
                      click: function(e) {
                        $('#chartDialog').dialog({
                          position: 'center',
                          width: $(window).width() - 80,
                          height: $(window).height() - 80,
                          draggable: false,
                          resizable: false,
                          modal: false
                        });
                        var dialogOptions = this.options;
                        dialogOptions.chart.renderTo = 'chartDialog';
                        dialogOptions.chart.events = null;
                        dialogOptions.chart.zoomType = 'x';
                        new Highcharts.Chart(dialogOptions);
                      }
                    }
                  },
                  title: { text: '<xsl:value-of select="$title" />' },
                  legend: { enabled: false },
                  xAxis: { categories: [
                    <xsl:for-each select="int">
                      <xsl:sort select="text()" data-type="number" order="descending" />
                      <xsl:variable name="cid" select="@name"/>
                      '<xsl:value-of select="mcrxsl:getDisplayName('ORIGIN', $cid)" />'
                      <xsl:if test="position() != last()">, </xsl:if>
                    </xsl:for-each>
                    ],
                    labels: {
                      align: 'right',
                      style: <xsl:value-of select="$UBO.Statistics.Style.Labels" />
                    }
                  },
                  yAxis: {
                     title: { text: '<xsl:value-of select="$count" />' },
                     labels: { formatter: function() { return this.value; } },
                     endOnTick: false,
                     max: <xsl:value-of select="floor(number(int[1]))" /> <!-- +5% -->
                  },
                  tooltip: { formatter: function() { return '<b>' + this.x +'</b>: '+ this.y; } },
                  plotOptions: { series: { pointWidth: 15 } },
                  series: [{
                    name: '<xsl:value-of select="$title" />',
                    data: [
                      <xsl:for-each select="int">
                        <xsl:sort select="text()" data-type="number" order="descending" />
                        <xsl:value-of select="text()"/>
                        <xsl:if test="position() != last()">, </xsl:if>
                      </xsl:for-each>
                    ],
                    color: '<xsl:value-of select="$UBO.Statistics.Color.Bar" />',
                    dataLabels: {
                      enabled: true,
                      align: 'right',
                      formatter: function() { return this.y; },
                      style: <xsl:value-of select="$UBO.Statistics.Style.Labels" />
                }
              }]
            });
          });
        </script>
      </div>
    </section>
  </xsl:template>

  <xsl:template match="lst[@name='facet_fields']/lst[@name='year']">
    <xsl:variable name="title" select="concat(i18n:translate('ubo.publications'),' / ',i18n:translate('facets.facet.year'))" />

    <section class="card mb-3">
      <div class="card-body">
      <div id="chartYear" style="width:100%; height:350px;" />

      <script type="text/javascript">
        $(document).ready(function() {
          new Highcharts.Chart({
            chart: {
              renderTo: 'chartYear',
              defaultSeriesType: 'column',
              backgroundColor: 'transparent',
              borderWidth: 0,
              shadow: false,
              events: {
                click: function(e) {
                  $('#chartDialog').dialog({
                    position: 'center',
                    width: $(window).width() - 80,
                    height: $(window).height() - 80,
                    draggable: false,
                    resizable: false,
                    modal: false
                  });
                  var dialogOptions = this.options;
                  dialogOptions.chart.renderTo = 'chartDialog';
                  dialogOptions.chart.events = null;
                  dialogOptions.chart.zoomType = 'x';
                  new Highcharts.Chart(dialogOptions);
                }
              }
            },
            title: { text: '<xsl:value-of select="$title" />' },
            legend: { enabled: false },
            tooltip: { formatter: function() { return '<b>' + Highcharts.dateFormat('%Y', this.point.x) +'</b>: '+ this.point.y; } },
            xAxis: {
              type: 'datetime',
              dateTimeLabelFormats: { day: '%Y' }
            },
             yAxis: {
               title: { text: '<xsl:value-of select="$count" />' },
               labels: { formatter: function() { return this.value; } },
               endOnTick: false,
               max: <xsl:value-of select="floor(number(int[1]) * 1.05)" /> <!-- +5% -->
             },
             plotOptions: {
                column: {
                  pointPadding: 0.2,
                  borderWidth: 0,
                  minPointLength: 3
                }
             },
             series: [{
                  name: '<xsl:value-of select="$count" />',
                  data: [
                    <xsl:for-each select="int">
                      <xsl:sort select="@name" data-type="number" order="ascending" />
                      [Date.UTC(<xsl:value-of select="@name"/>, 0, 1), <xsl:value-of select="text()"/>]
                      <xsl:if test="position() != last()">, </xsl:if>
                    </xsl:for-each>
                  ],
                  color: '<xsl:value-of select="$UBO.Statistics.Color.Bar" />'
              }]
            });
          });
      </script>
      </div>
    </section>
  </xsl:template>

  <xsl:template match="lst[@name='facet_fields']/lst[@name='subject']">
    <xsl:variable name="title" select="concat(i18n:translate('ubo.publications'),' / ',i18n:translate('facets.facet.subject'))" />

    <section class="card mb-3">
      <div class="card-body">
        <div id="chartSubject" style="width:100%; height:{50 + count(int) * 30}px" />
        <script type="text/javascript">
          $(document).ready(function() {
            new Highcharts.Chart({
              chart: {
                renderTo: 'chartSubject',
                type: 'bar',
                backgroundColor: 'transparent',
                borderWidth: 0,
                shadow: false,
                events: {
                  click: function(e) {
                    $('#chartDialog').dialog({
                      position: 'center',
                      width: $(window).width() - 80,
                      height: $(window).height() - 80,
                      draggable: false,
                      resizable: false,
                      modal: false
                    });
                    var dialogOptions = this.options;
                    dialogOptions.chart.renderTo = 'chartDialog';
                    dialogOptions.chart.events = null;
                    dialogOptions.chart.zoomType = 'x';
                    new Highcharts.Chart(dialogOptions);
                  }
                }
              },
              title: { text: '<xsl:value-of select="$title" />' },
              legend: { enabled: false },
              xAxis: { categories: [
                <xsl:for-each select="int">
                  <xsl:sort select="text()" data-type="number" order="descending" />
                  '<xsl:value-of select="mcrxsl:getDisplayName('fachreferate', @name)" />'
                  <xsl:if test="position() != last()">, </xsl:if>
                </xsl:for-each>
                ],
                labels: {
                  align: 'right',
                  style: <xsl:value-of select="$UBO.Statistics.Style.Labels" />
                }
              },
              yAxis: {
                 title: { text: '<xsl:value-of select="$count" />' },
                 labels: { formatter: function() { return this.value; } },
                 endOnTick: false,
                 max: <xsl:value-of select="floor(number(int[1]) * 1.05)" /> <!-- +5% -->
              },
              tooltip: { formatter: function() { return '<b>' + this.x +'</b>: '+ this.y; } },
              plotOptions: { series: { pointWidth: 15, minPointLength: 3 } },
              series: [{
                name: '<xsl:value-of select="$title" />',
                data: [
                  <xsl:for-each select="int">
                    <xsl:sort select="text()" data-type="number" order="descending" />
                    <xsl:value-of select="text()"/>
                    <xsl:if test="position() != last()">, </xsl:if>
                  </xsl:for-each>
                ],
                color: '<xsl:value-of select="$UBO.Statistics.Color.Bar" />',
                dataLabels: {
                  enabled: true,
                  align: 'right',
                  formatter: function() { return this.y; },
                  style: <xsl:value-of select="$UBO.Statistics.Style.Labels" />
            }
          }]
        });
      });
        </script>
      </div>
    </section>
  </xsl:template>

  <xsl:variable name="genres" select="document('classification:metadata:-1:children:ubogenre')/mycoreclass/categories" />

  <xsl:template match="lst[@name='facet_fields']/lst[@name='genre']">
    <xsl:variable name="title" select="concat(i18n:translate('ubo.publications'),' / ',i18n:translate('facets.facet.genre'))" />

    <section class="card mb-3">
      <div class="card-body">
        <div id="chartGenre" style="width:100%; height:350px" />

        <script type="text/javascript">
         $(document).ready(function() {
          Highcharts.getOptions().plotOptions.pie.colors = [
          <xsl:for-each select="int">
            <xsl:sort select="text()" data-type="number" order="descending" />
            <xsl:sort data-type="number" order="descending" />
            <xsl:text>'</xsl:text>
            <xsl:value-of select="$genres//category[@ID=current()/@name]/label[lang('x-color')]/@text" />
            <xsl:text>'</xsl:text>
            <xsl:if test="position() != last()">, </xsl:if>
          </xsl:for-each>
          ];
          new Highcharts.Chart({
             chart: {
                renderTo: 'chartGenre',
                type: 'pie',
                backgroundColor: 'transparent',
                borderWidth: 0,
                shadow: false,
                plotBackgroundColor: null,
                plotBorderWidth: null,
                plotShadow: false,
                events: {
                  click: function(e) {
                    $('#chartDialog').dialog({
                      position: 'center',
                      width: $(window).width() - 80,
                      height: $(window).height() - 80,
                      draggable: false,
                      resizable: false,
                      modal: false
                    });
                    var dialogOptions = this.options;
                    dialogOptions.chart.renderTo = 'chartDialog';
                    dialogOptions.chart.events = null;
                    dialogOptions.chart.zoomType = 'x';
                    new Highcharts.Chart(dialogOptions);
                  }
                }
             },
             title: { text: '<xsl:value-of select="$title" />' },
             legend: { enabled: false },
             tooltip: {
                formatter: function() {
                  return '<b>'+ this.point.name +'</b>: '+ this.y;
                }
             },
             plotOptions: {
                pie: {
                  allowPointSelect: true,
                  cursor: 'pointer',
                  dataLabels: {
                     enabled: true,
                     formatter: function() {
                        return '<b>'+ this.point.name +'</b>: '+ this.y;
                     }
                  }
                }
             },
             series: [{
                  name: '<xsl:value-of select="$title" />',
                  data: [
                    <xsl:for-each select="int">
                      <xsl:sort select="text()" data-type="number" order="descending" />
                      ['<xsl:value-of select="$genres//category[@ID=current()/@name]/label[lang($CurrentLang)]/@text"/>' , <xsl:value-of select="text()"/>]
                      <xsl:if test="position()!=last()">,</xsl:if>
                    </xsl:for-each>
                  ]
             }]
         });
     });
        </script>
      </div>
    </section>
  </xsl:template>

  <xsl:variable name="oa" select="document('classification:metadata:-1:children:oa')/mycoreclass/categories" />

  <xsl:template match="lst[@name='facet_fields']/lst[@name='oa']">
    <xsl:variable name="title">Open Access?</xsl:variable>

    <section class="card mb-3">
      <div class="card-body">
      <div id="chartOA" style="width:100%; height:350px" />

      <xsl:variable name="numOAdirect" select="int[@name='oa'] - sum(int[contains('green gold hybrid embargo bronze',@name)])" />
      <xsl:variable name="numOther" select="/response/result/@numFound - int[@name='oa']" />

      <script type="text/javascript">
       $(document).ready(function() {
         Highcharts.getOptions().plotOptions.pie.colors = [
           <xsl:if test="$numOther &gt; 0">'<xsl:value-of select="$oa/../label[lang('x-color')]/@text" />',</xsl:if>
           <xsl:for-each select="int[not(@name='oa') or ($numOAdirect &gt; 0)]">
             <xsl:sort data-type="number" order="descending" />
             <xsl:text>'</xsl:text>
             <xsl:value-of select="$oa//category[@ID=current()/@name]/label[lang('x-color')]/@text" />
             <xsl:text>'</xsl:text>
             <xsl:if test="position() != last()">, </xsl:if>
           </xsl:for-each>
         ];
         new Highcharts.Chart({
           chart: {
              renderTo: 'chartOA',
              type: 'pie',
              backgroundColor: 'transparent',
              borderWidth: 0,
              shadow: false,
              plotBackgroundColor: null,
              plotBorderWidth: null,
              plotShadow: false,
              defaultSeriesType: 'pie',
              events: {
                click: function(e) {
                  $('#chartDialog').dialog({
                    position: 'center',
                    width: $(window).width() - 80,
                    height: $(window).height() - 80,
                    draggable: false,
                    resizable: false,
                    modal: false
                  });
                  var dialogOptions = this.options;
                  dialogOptions.chart.renderTo = 'chartDialog';
                  dialogOptions.chart.events = null;
                  dialogOptions.chart.zoomType = 'x';
                  new Highcharts.Chart(dialogOptions);
                }
              }
           },
           title: { text: '<xsl:value-of select="$title" />' },
           legend: { enabled: false },
           tooltip: {
              formatter: function() {
                return '<b>'+ this.point.name +'</b>: '+ this.y;
              }
           },
           plotOptions: {
              pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                   enabled: true,
                   format: '<b>{point.name}</b>: {point.percentage:.1f} % ({y})'
                }
              }
           },
           series: [{
                name: '<xsl:value-of select="$title" />',
                data: [
                  <xsl:if test="$numOther &gt; 0">
                    ['nicht OA / unbekannt' , <xsl:value-of select="$numOther"/>],
                  </xsl:if>

                 <xsl:for-each select="int[not(@name='oa') or ($numOAdirect &gt; 0)]">
                   <xsl:sort data-type="number" order="descending" />
                   <xsl:text>['</xsl:text>
                   <xsl:value-of select="$oa//category[@ID=current()/@name]/label[lang($CurrentLang)]/@text" />
                   <xsl:text>', </xsl:text>
                   <xsl:choose>
                     <xsl:when test="@name='oa'">
                       <xsl:value-of select="$numOAdirect" />
                     </xsl:when>
                     <xsl:otherwise>
                       <xsl:value-of select="text()" />
                     </xsl:otherwise>
                   </xsl:choose>
                   <xsl:text>]</xsl:text>
                   <xsl:if test="position() != last()">, </xsl:if>
                </xsl:for-each>

                ]
            }
          ]
      });
   });
      </script>
      </div>
    </section>
  </xsl:template>

  <xsl:template match="lst[@name='facet_fields']/lst[@name='facet_person']">
    <xsl:variable name="title" select="concat(i18n:translate('ubo.publications'),' / ',i18n:translate('facets.facet.person'))" />

    <section class="card">
      <div class="card-body">
      <div id="chartPerson" style="width:100%; height:{50 + count(int) * 30}px" />

      <script type="text/javascript">
        $(document).ready(function() {
          new Highcharts.Chart({
            chart: {
              renderTo: 'chartPerson',
              type: 'bar',
              backgroundColor: 'transparent',
              borderWidth: 0,
              shadow: false,
              events: {
                click: function(e) {
                  $('#chartDialog').dialog({
                    position: 'center',
                    width: $(window).width() - 80,
                    height: $(window).height() - 80,
                    draggable: false,
                    resizable: false,
                    modal: false
                  });
                  var dialogOptions = this.options;
                  dialogOptions.chart.renderTo = 'chartDialog';
                  dialogOptions.chart.events = null;
                  dialogOptions.chart.zoomType = 'x';
                  new Highcharts.Chart(dialogOptions);
                }
              }
            },
            title: { text: '<xsl:value-of select="$title" />' },
            legend: { enabled: false },
            xAxis: { categories: [
              <xsl:for-each select="int">
                <xsl:sort select="text()" data-type="number" order="descending" />
                "<xsl:value-of select="@name"/>"
                <xsl:if test="position() != last()">, </xsl:if>
              </xsl:for-each>
              ],
              labels: {
                align: 'right',
                style: <xsl:value-of select="$UBO.Statistics.Style.Labels" />
              }
            },
            yAxis: {
               title: { text: '<xsl:value-of select="$count" />' },
               labels: { formatter: function() { return this.value; } },
               endOnTick: false,
               max: <xsl:value-of select="floor(number(int[1]) * 1.05)" /> <!-- +5% -->
            },
            tooltip: { formatter: function() { return '<b>' + this.x +'</b>: '+ this.y; } },
            plotOptions: { series: { pointWidth: 15, minPointLength: 3 } },
            series: [{
              name: '<xsl:value-of select="$title" />',
              data: [
                <xsl:for-each select="int">
                  <xsl:sort select="text()" data-type="number" order="descending" />
                  <xsl:value-of select="text()"/>
                  <xsl:if test="position() != last()">, </xsl:if>
                </xsl:for-each>
              ],
              color: '<xsl:value-of select="$UBO.Statistics.Color.Bar" />',
              dataLabels: {
                enabled: true,
                align: 'right',
                formatter: function() { return this.y; },
                style: <xsl:value-of select="$UBO.Statistics.Style.Labels" />
              }
            }]
          });
        });
      </script>
      </div>
    </section>
  </xsl:template>

  <xsl:template match="lst[@name='facet_fields']/lst[@name='nid_connection']">

    <xsl:if test="not(mcrxsl:isCurrentUserGuestUser())">

    <!-- The facet is a list of top connected Person IDs matching the restricted query, e.g. status=confirmed, year > 2012 -->
    <!-- To find the corresponding names, build a pivot facet with Connection ID and name variants, use most frequent name  -->
    <xsl:variable name="uri">
       <xsl:text>solr:q=objectKind%3Aname+AND+(</xsl:text>
       <xsl:for-each select="int">
         <xsl:text>name_id_connection%3A</xsl:text>
         <xsl:value-of select="@name" />
         <xsl:if test="position() != last()">+OR+</xsl:if>
       </xsl:for-each>
       <xsl:text>)&amp;rows=0&amp;facet.pivot=name_id_connection,name&amp;facet.limit=</xsl:text>
       <xsl:value-of select="count(int)" />
    </xsl:variable>
    <xsl:variable name="response" select="document($uri)/response" />
    <xsl:variable name="id2name" select="$response/lst[@name='facet_counts']/lst[@name='facet_pivot']/arr[@name='name_id_connection,name']" />

    <xsl:variable name="q" select="encoder:encode(/response/lst[@name='responseHeader']/lst[@name='params']/str[@name='q'],'UTF-8')" />

    <xsl:variable name="title" select="concat(i18n:translate('ubo.publications'),' / ',i18n:translate('facets.facet.nid_connection'))" />

    <article class="card mb-3">
      <div class="card-body">
        <h3>
          <xsl:value-of select="concat(i18n:translate('thunibib.statistic.aut.ids.used.most'), ': ')"/>
        </h3>
        <section class="card">
          <div class="card-body">
            <div id="chartID" style="width:100%; height:{50 + count(int) * 30}px" />
    
            <script type="text/javascript">
              $(document).ready(function() {
                new Highcharts.Chart({
                  chart: {
                    renderTo: 'chartID',
                    type: 'bar',
                    events: {
                      click: function(e) {
                        $('#chartDialog').dialog({
                          position: 'center',
                          width: $(window).width() - 80,
                          height: $(window).height() - 80,
                          draggable: false,
                          resizable: false,
                          modal: false
                        });
                        var dialogOptions = this.options;
                        dialogOptions.chart.renderTo = 'chartDialog';
                        dialogOptions.chart.events = null;
                        dialogOptions.chart.zoomType = 'x';
                        new Highcharts.Chart(dialogOptions);
                      }
                    }
                  },
                  title: { text: '<xsl:value-of select="$title" />' },
                  legend: { enabled: false },
                  xAxis: { categories: [
                    <xsl:for-each select="int">
                      <xsl:sort select="text()" data-type="number" order="descending" />
                      <xsl:variable name="connection_id" select="@name" />
                      <xsl:variable name="name"   select="$id2name/lst[str[@name='value']=$connection_id]/arr/lst[1]/str[@name='value']" />
                      "<xsl:value-of select="$name"/>"
                      <xsl:if test="position() != last()">, </xsl:if>
                    </xsl:for-each>
                    ],
                    labels: {
                      align: 'right',
                      style: <xsl:value-of select="$UBO.Statistics.Style.Labels" />
                    }
                  },
                  yAxis: {
                     title: { text: '<xsl:value-of select="$count" />' },
                     labels: { formatter: function() { return this.value; } },
                     endOnTick: false,
                     max: <xsl:value-of select="floor(number(int[1]) * 1.05)" /> <!-- +5% -->
                  },
                  tooltip: { formatter: function() { return '<b>' + this.x +'</b>: '+ this.y; } },
                  plotOptions: { series: { pointWidth: 15, minPointLength: 3 } },
                  series: [{
                    name: '<xsl:value-of select="$title" />',
                    data: [
                      <xsl:for-each select="int">
                        <xsl:sort select="text()" data-type="number" order="descending" />
                        <xsl:value-of select="text()"/>
                        <xsl:if test="position() != last()">, </xsl:if>
                      </xsl:for-each>
                    ],
                    color: '<xsl:value-of select="$UBO.Statistics.Color.Bar" />',
                    dataLabels: {
                      enabled: true,
                      align: 'right',
                      formatter: function() { return this.y; },
                      style: <xsl:value-of select="$UBO.Statistics.Style.Labels" />
                    }
                  }]
                });
              });
              </script>
            </div>
          </section>
        </div>
      </article>
    </xsl:if>
  </xsl:template>

  <xsl:template match="lst/arr[@name='name_id_type,name_id_type']">
    <xsl:if test="not(mcrxsl:isCurrentUserGuestUser())">
      <xsl:variable name="base" select="." />

      <article class="card">
        <div class="card-body">
          <h3>
            <xsl:value-of select="concat(i18n:translate('thunibib.statistic.aut.ids.used'), ': ')"/>
          </h3>

          <table class="table table-bordered">
            <tr class="text-center">
              <th>/</th>
              <xsl:for-each select="$base/lst">
                <th>
                  <xsl:value-of select="translate(str[@name='value'],'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')" />
                </th>
              </xsl:for-each>
            </tr>
            <xsl:for-each select="$base/lst">
              <xsl:variable name="a" select="str[@name='value']" />
              <tr class="text-right">
                <th class="identifier">
                  <xsl:value-of select="translate(str[@name='value'],'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')" />
                </th>
                <xsl:for-each select="$base/lst">
                  <xsl:variable name="b" select="str[@name='value']" />
                  <td class="identifier">
                    <xsl:value-of select="$base/lst[str[@name='value']=$a]/arr/lst[str[@name='value']=$b]/int[@name='count']" />
                  </td>
                </xsl:for-each>
              </tr>
            </xsl:for-each>
          </table>

        </div>
      </article>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
