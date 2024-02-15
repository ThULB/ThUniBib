<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xalan="http://xml.apache.org/xalan" xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions"
                exclude-result-prefixes="i18n mcrxsl mods xsl xalan">

  <xsl:include href="resource:xsl/response-get-handler.xsl"/>

  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match="/landing-page">
    <form role="form" method="get" action="servlets/solr/{$solrRequestHandler}">
      <div class="form-group form-inline mb-0">
        <label for="input" class="mycore-form-label">
          <xsl:value-of select="i18n:translate('ubo.search.simple')"/>
        </label>
        <input id="input" name="q" type="text" class="mycore-form-input form-control-sm mr-2"/>
        <button class="btn btn-sm btn-primary" type="submit">
          <xsl:value-of select="i18n:translate('button.search')"/>
        </button>
      </div>
    </form>
  </xsl:template>

</xsl:stylesheet>
