async function renderByYearChart(chartId) {
  const t = await fetch("servlets/solr/select?q=partOf%3Atrue+AND+%2B(status%3Aconfirmed+status%3Aunchecked+status%3AreviewPending)+AND+year%3A%5B2018+TO+*%5D&rows=0&facet.field=year&wt=json")
  const json = await t.json();

  const source = json.facet_counts.facet_fields.year;
  let labels = new Array();
  let values = new Array()

  for (let i = 0; i < source.length; i += 2) {
    labels.push(source[i]);
    values.push(source[i + 1]);
  }

  const resp = await fetch(webApplicationBaseURL + "rsc/locale/translate/ChartsCommon.chart.title.year");
  const title = await resp.text();

  let chartDom = document.getElementById(chartId);
  let chart = echarts.init(chartDom, null, {renderer: 'svg'});
  let option;

  option = {
    title: {
      text: title,
      textStyle: {
        fontFamily: 'Trebuchet MS',
        fontSize: '16px',
        fontWeight: 'bold'
      }
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },

    xAxis: {
      type: 'category',
      data: labels,
    },
    yAxis: {
      type: 'value',
      axisTick: {
        show: false,
      },
      axisLabel: {
        show: false
      },
      axisLine: {
        show: false
      }
    },
    grid: {
      containLabel: false,
      left: 0,
      right: 0
    },
    series: [
      {
        data: values,
        type: 'bar',
        color: '#058DC7',
        label: {
          show: true,
          position: 'inside'
        }
      }
    ]
  };

  chart.setOption(option);
  window.addEventListener('resize', function () {
    chart.resize();
  });
}

