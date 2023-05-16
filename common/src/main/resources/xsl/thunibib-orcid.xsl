<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:encoder="xalan://java.net.URLEncoder"
                xmlns:mcrver="xalan://org.mycore.common.MCRCoreVersion"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:orcidUtils="xalan://org.mycore.ubo.orcid.DozBibORCIDUtils"
                exclude-result-prefixes="xsl xalan i18n encoder mcrver mcrxml orcidUtils">

  <xsl:output method="xml" encoding="UTF-8"/>

  <xsl:param name="WebApplicationBaseURL"/>

  <!-- If current user has ORCID, and we are his trusted party, display ORCID icon to indicate that -->
  <xsl:template name="orcidUser">
    <xsl:if test="orcidUtils:weAreTrustedParty() = 'true'">
      <img alt="ORCID" src="{$WebApplicationBaseURL}images/orcid_icon.svg" class="orcid-icon"/>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
