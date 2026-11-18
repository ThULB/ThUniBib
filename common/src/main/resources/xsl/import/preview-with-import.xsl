<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions"
                exclude-result-prefixes="mcrxsl mods i18n xsl xalan">

  <xsl:include href="coreFunctions.xsl"/>
  <xsl:include href="mods-display.xsl"/>
  <xsl:include href="mycoreobject-html.xsl"/>

  <xsl:param name="CurrentLang"/>
  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:variable name="ORCIDPathSourceType" select="'ORCIDWorksPath'"/>

  <xsl:template match="/export[@submitted]">
    <webpage>
      <title>
        <xsl:value-of select="i18n:translate('ubo.import.list.form.headline')"/>
      </title>

      <div class="card">
        <div class="card-body">
          <div class="row mb-3">
            <div class="col">
              <xsl:value-of select="i18n:translate('ubo.import.list.submission.success')"/>
            </div>
          </div>
        </div>
      </div>
    </webpage>
  </xsl:template>

  <xsl:template match="/export[not(@submitted)]">
    <webpage>
      <title>
        <xsl:value-of select="i18n:translate('ubo.import.list.form.headline')"/>
      </title>

      <div class="card">
        <div class="card-body">
          <xsl:choose>
            <xsl:when test="count(mycoreobject) &gt; 0">
              <script>
                <![CDATA[
                function toggleSubmit() {
                  let submitBtn = document.getElementById('submit');

                  // all checked
                  if(!document.querySelector('input[type="checkbox"]:checked') == false){
                    submitBtn.classList.remove('disabled');
                    submitBtn.classList.remove('thunibib-pointer-events-none');
                  } else {
                    submitBtn.classList.add('disabled');
                    submitBtn.classList.add('thunibib-pointer-events-none');
                  }
                }
              ]]>
              </script>

              <div class="row thunibib-preview-for-import-list-form-data mb-3">
                <div class="col">
                  <xsl:apply-templates select="import"/>
                </div>
              </div>

              <form action="{$WebApplicationBaseURL}servlets/ThUniBibListImportServlet" method="post">
                <xsl:apply-templates select="import" mode="hidden-params"/>

                <div class="row mb-3 thunibib-preview-for-import-list">
                  <div class="col">
                    <xsl:apply-templates select="mycoreobject"/>
                  </div>
                </div>

                <div class="row pt-3 mb-3 ubo-sticky-editor-buttons">
                  <div class="col-sm d-none d-sm-block">
                    <xsl:if test="import/partOf | import/status | import/ubogenre | import/origin">
                      <xsl:value-of select="concat(i18n:translate('thunibib.list.import.select.explain.defaults'), ': ')"/>

                      <xsl:if test="import/partOf">
                        <xsl:value-of select="concat(i18n:translate('ubo.partOf'), ': ')"/>
                        <span class="font-weight-bold">
                          <xsl:value-of select="mcrxsl:getDisplayName('partOf', import/partOf)"/>
                        </span>,
                      </xsl:if>

                      <xsl:if test="import/status">
                        <xsl:value-of select="concat(i18n:translate('ubo.status'), ': ')"/>
                        <span class="font-weight-bold">
                          <xsl:value-of select="mcrxsl:getDisplayName('state', import/status)"/>
                        </span>,
                      </xsl:if>

                      <xsl:if test="import/ubogenre">
                        <xsl:value-of select="concat(i18n:translate('ubo.genre'), ': ')"/>
                        <span class="font-weight-bold">
                          <xsl:value-of select=" mcrxsl:getDisplayName('ubogenre', import/ubogenre)"/>
                        </span>,
                      </xsl:if>

                      <xsl:if test="import/origin">
                        <xsl:value-of select="concat(i18n:translate('ubo.department'), ': ')"/>
                        <span class="font-weight-bold">
                          <xsl:value-of select=" mcrxsl:getDisplayName('ORIGIN', import/origin)"/>
                        </span>
                      </xsl:if>
                    </xsl:if>
                  </div>

                  <div class="col-12 col-sm-3 text-left text-sm-right">
                    <button id="submit" type="submit" class="btn btn-primary">
                      <xsl:value-of select="i18n:translate('ubo.import.list.form.headline')"/>
                    </button>
                  </div>
                </div>
              </form>
            </xsl:when>
            <xsl:otherwise>
              <div class="row mb-3">
                <div class="col">
                  <xsl:value-of select="i18n:translate('ubo.newPublicationWizard.notFound')"/>
                </div>
              </div>
            </xsl:otherwise>
          </xsl:choose>
        </div>
      </div>
    </webpage>
  </xsl:template>

  <xsl:template match="import">
    <div class="row mb-3">
      <div class="col-12 col-sm-9">
        <xsl:value-of select="i18n:translate('thunibib.list.import.select.intro')" disable-output-escaping="yes"/>
      </div>

      <div class="col-12 col-sm mt-3 mt-sm-0 text-center btn-group">
        <button class="btn btn-outline-success" onclick="document.querySelectorAll('input[type=&quot;checkbox&quot;]').forEach(cb => cb.checked = true); toggleSubmit();" title="{i18n:translate('thunibib.button.select.all.tooltip')}">
          <i class="far fa-check-square pr-1"/>
          <xsl:value-of select="i18n:translate('thunibib.button.select.all')"/>
        </button>

        <button class="btn btn-outline-warning" onclick="document.querySelectorAll('input[type=&quot;checkbox&quot;]').forEach(cb => cb.checked = false); toggleSubmit();" title="{i18n:translate('thunibib.button.select.none.tooltip')}">
          <i class="far fa-square pr-1"/>
          <xsl:value-of select="i18n:translate('thunibib.button.select.none')"/>
        </button>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="mycoreobject">
    <xsl:variable name="source-type">
      <xsl:choose>
        <xsl:when test="../import/@sourceType = 'ORCID'">
          <xsl:value-of select="$ORCIDPathSourceType"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="../import/@sourceType"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="identifier">
      <xsl:choose>
        <xsl:when test="$source-type = 'PPNList'">
          <xsl:value-of select="metadata//mods:mods/mods:identifier[@type='ppn']/text()"/>
        </xsl:when>
        <xsl:when test="$source-type = 'DOIList'">
          <xsl:value-of select="metadata//mods:mods/mods:identifier[@type='doi']/text()"/>
        </xsl:when>
        <xsl:when test="$source-type = 'DBTList'">
          <xsl:value-of select="metadata//mods:mods/mods:identifier[@type='dbt']/text()"/>
        </xsl:when>
        <xsl:when test="$source-type = $ORCIDPathSourceType">
          <xsl:value-of select="metadata//mods:mods/mods:identifier[@type='orcid:work']/text()"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="''"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <div class="row border-top pt-3 mb-3 justify-content-center align-items-center">
      <div class="col-12 col-sm-8" data-source-type="{$source-type}">
        <xsl:attribute name="data-identifier">
          <xsl:value-of select="$identifier"/>
        </xsl:attribute>

        <xsl:apply-templates select="." mode="html-export"/>
      </div>

      <div class="col text-right text-sm-center">
        <xsl:if test="string-length($identifier) &gt; 0">
          <div class="form-group form-check">
            <input type="checkbox" checked="true" class="form-check-input" name="{$source-type}" value="{$identifier}" onchange="toggleSubmit();"/>
          </div>
        </xsl:if>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="import" mode="hidden-params">
    <xsl:for-each select="@*">
      <xsl:choose>
        <!-- Re-map source type from ORCID to $ORCIDPathSourceType which triggers the ORCIDPath2WorksTransformer -->
        <xsl:when test="'sourceType' = local-name() and . = 'ORCID'">
          <input type="hidden" name="import.{local-name()}" value="{$ORCIDPathSourceType}"/>
        </xsl:when>
        <xsl:otherwise>
          <input type="hidden" name="import.{local-name()}" value="{.}"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>

    <xsl:for-each select="*">
      <xsl:variable name="nodename" select="local-name()"/>
      <xsl:variable name="nodevalue" select="."/>

      <xsl:if test="string-length($nodevalue) &gt; 0 and not(@*)">
        <input type="hidden" name="{$nodename}" value="{$nodevalue}"/>
      </xsl:if>

      <xsl:for-each select="@*">
        <xsl:if test="string-length($nodevalue) &gt; 0">
          <input type="hidden" name="{$nodename}.{local-name()}.{.}" value="{$nodevalue}"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
