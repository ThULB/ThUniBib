<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:encoder="xalan://java.net.URLEncoder"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:mcrver="xalan://org.mycore.common.MCRCoreVersion"
                exclude-result-prefixes="xsl xalan encoder i18n mcrxml mcrver">

  <xsl:output method="xml" encoding="UTF-8"/>

  <xsl:param name="RequestURL"/>

  <xsl:param name="ThUniBib.ServiceDesk.enabled"/>
  <xsl:param name="ThUniBib.ServiceDesk.BaseURL" select="'https://servicedesk.uni-jena.de/'"/>
  <xsl:param name="ThUniBib.ServiceDesk.FormId" select="'847'"/>
  <xsl:param name="ThUniBib.ServiceDesk.PortalId" select="'140'"/>
  <xsl:param name="ThUniBib.ServiceDesk.Path"
             select="concat('plugins/servlet/desk/portal/', $ThUniBib.ServiceDesk.PortalId, '/create/', $ThUniBib.ServiceDesk.FormId)"/>
  <xsl:param name="ThUniBib.ServiceDesk.RequestURL"
             select="concat($ThUniBib.ServiceDesk.BaseURL, $ThUniBib.ServiceDesk.Path)"/>

  <!--
    component/customfield_1192:
     16988:Archivanwendungen, DAM (Collections)
     16989:Bibliografien (ThUniBib)
     16990:Digitale Bibliothek Thüringen (DBT)
     16991:Zeitschriften, Periodika (JPortal)
     16992:Sonstige (Typo3)

    issue-type/customfield_12342:
     16985:Allgemeine Anfrage
     16961:Bug / Fehlermeldung
     16986:Erweiterung Funktion
     16963:Sonstiges
     16987:Zugang / Account

    affiliation/customfield_13206:
     16982:Extern
     16983:ThULB
     16984:Universität Jena
  -->

  <xsl:template match="*" mode="servicedesk">
    <xsl:param name="summary" select="concat(i18n:translate('thunibib.servicedesk.default.summary'),' ',$RequestURL)"/>
    <xsl:param name="description" select="''"/>
    <xsl:param name="affected-url" select="$RequestURL"/>
    <xsl:param name="affiliation" select="''"/>
    <xsl:param name="component" select="'16989'"/>
    <xsl:param name="issue-type" select="'16985'"/>

    <xsl:if test="$ThUniBib.ServiceDesk.enabled = 'true'">
      <xsl:variable name="params">
        <xsl:value-of select="concat('summary=', encoder:encode($summary), '&amp;')"/>
        <xsl:value-of select="concat('description=', encoder:encode($description), '&amp;')"/>
        <xsl:value-of select="concat('customfield_10300=', encoder:encode($affected-url),'&amp;')"/>
        <xsl:value-of select="concat('customfield_11920=', encoder:encode($component), '&amp;')"/>
        <xsl:value-of select="concat('customfield_13206=', encoder:encode($affiliation), '&amp;')"/>
        <xsl:value-of select="concat('customfield_12342=', encoder:encode($issue-type))"/>
      </xsl:variable>

      <xsl:variable name="href" select="concat($ThUniBib.ServiceDesk.RequestURL, '?', $params)"/>

      <div id="servicedesk">
        <a href="{$href}" class="thunibib-btn-servicedesk" target="_blank">
          ServiceDesk
        </a>
      </div>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
