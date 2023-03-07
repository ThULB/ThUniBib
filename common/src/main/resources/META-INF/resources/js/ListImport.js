let ListImport = {
  submit: async function () {
    ListImport.disableUI();

    const url = webApplicationBaseURL + "servlets/PPNListImportServlet";
    const response = await fetch(url + "?list=" + encodeURIComponent($("#ppn-list").val()))
        .then(r => r.json())
        .then(json => {
          let list = json.list;
          let q = "id_ppn:(";

          json.list.forEach(function (ppn, index) {
            q += ppn;
            if (index < list.length - 1) {
              q += " OR ";
            }
          });

          q += ")";

          /* wait until indexing is done */
          setInterval(function () {
            ListImport.enableUI();
            location.assign(webApplicationBaseURL + "servlets/solr/select?q=" + encodeURIComponent(q));
          }, 5000);

        }).catch(error => {
          console.error(error);
          ListImport.enableUI();
        });
  },

  enableUI: function () {
    $("#ppn-list-submit").removeAttr("disabled");
    $("#ppn-list-submit").removeClass("thunibib-cursor-wait");

    $("textarea").removeAttr("disabled");
    $("textarea").removeClass("thunibib-cursor-wait");
  },

  disableUI: function () {
    $("#ppn-list-submit").attr("disabled", "disabled");
    $("#ppn-list-submit").addClass("thunibib-cursor-wait");

    $("textarea").attr("disabled", "disabled");
    $("textarea").addClass("thunibib-cursor-wait");
  }
}
