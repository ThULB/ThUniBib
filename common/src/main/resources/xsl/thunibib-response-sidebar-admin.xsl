<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:solr="xalan://org.mycore.solr.MCRSolrUtils"
                xmlns:encoder="xalan://java.net.URLEncoder"
                xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions"
                exclude-result-prefixes="xsl mods xalan i18n encoder solr mcrxsl">

  <xsl:param name="ServletsBaseURL"/>
  <xsl:param name="ThUniBib.response-sidebar-lastimported.max.entries" select="20"/>

  <xsl:variable name="quote">"</xsl:variable>

  <xsl:template match="/response">
    <xsl:if xmlns:check="xalan://org.mycore.ubo.AccessControl" test="check:currentUserIsAdmin()">
      <article class="card mb-2">
        <div class="card-body">
          <xsl:apply-templates select="lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='status']"/>
          <xsl:apply-templates
            select="lst[@name='facet_counts']/lst[@name='facet_ranges']/lst[@name='modified']/lst[@name='counts']"/>
          <xsl:apply-templates
            select="lst[@name='facet_counts']/lst[@name='facet_ranges']/lst[@name='created']/lst[@name='counts']"/>
          <xsl:apply-templates select="lst[@name='facet_counts']/lst[@name='facet_fields']/lst[@name='importID']"/>
        </div>
      </article>
    </xsl:if>
  </xsl:template>

  <xsl:template match="lst[@name='facet_fields']/lst[@name='status']">
    <hgroup>
      <h3><xsl:value-of select="i18n:translate('response.sidebar.pubperstate')"/>:
      </h3>
    </hgroup>
    <ul class="list-group list-group-flush">
      <xsl:for-each select="int">
        <xsl:call-template name="output.value">
          <xsl:with-param name="label" select="i18n:translate(concat('search.dozbib.status.',@name))"/>
          <xsl:with-param name="value" select="text()"/>
          <xsl:with-param name="query" select="concat('status:',@name)"/>
        </xsl:call-template>
      </xsl:for-each>
    </ul>
  </xsl:template>

  <xsl:template match="lst[@name='facet_fields']/lst[@name='importID']">
    <hgroup>
      <h3><xsl:value-of select="i18n:translate('response.sidebar.lastimported')"/>:
      </h3>
    </hgroup>
    <ul class="list-group list-group-flush thunibib-response-sidebar-lastimported">
      <xsl:for-each select="int">
        <xsl:sort select="mcrxsl:regexp(@name, '[^(\d\d\d\d)]*', '')" order="descending"/>
        <xsl:if test="position() &lt;= $ThUniBib.response-sidebar-lastimported.max.entries">
          <xsl:call-template name="output.value">
            <xsl:with-param name="label" select="@name"/>
            <xsl:with-param name="value" select="text()"/>
            <xsl:with-param name="query" select="concat('importID:',$quote,solr:escapeSearchValue(@name),$quote)"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:for-each>
    </ul>
  </xsl:template>

  <xsl:template match="lst[@name='facet_ranges']/lst/lst[@name='counts']">
    <xsl:variable name="numDays" select="count(int)"/>
    <xsl:variable name="dateField" select="../@name"/> <!-- created|modified -->
    <hgroup>
      <h3><xsl:value-of select="i18n:translate(concat('facets.facet.',$dateField))"/>:
      </h3>
    </hgroup>
    <ul class="list-group list-group-flush">
      <xsl:call-template name="output.value">
        <xsl:with-param name="label">
          <xsl:value-of select="i18n:translate('response.sidebar.last14days')"/>
        </xsl:with-param>
        <xsl:with-param name="value" select="sum(int[position() &gt; ($numDays - 14)])"/>
        <xsl:with-param name="query" select="concat($dateField,':[NOW/DAY-13DAY TO NOW]')"/>
      </xsl:call-template>
      <xsl:call-template name="output.value">
        <xsl:with-param name="label">
          <xsl:value-of select="i18n:translate('response.sidebar.last7days')"/>
        </xsl:with-param>
        <xsl:with-param name="value" select="sum(int[position() &gt; ($numDays - 7)])"/>
        <xsl:with-param name="query" select="concat($dateField,':[NOW/DAY-6DAY TO NOW]')"/>
      </xsl:call-template>
      <xsl:call-template name="output.value">
        <xsl:with-param name="label">
          <xsl:value-of select="i18n:translate('response.sidebar.yesterday')"/>
        </xsl:with-param>
        <xsl:with-param name="value" select="sum(int[position() &gt; ($numDays - 2)])"/>
        <xsl:with-param name="query" select="concat($dateField,':[NOW/DAY-1DAY TO NOW]')"/>
      </xsl:call-template>
      <xsl:call-template name="output.value">
        <xsl:with-param name="label">
          <xsl:value-of select="i18n:translate('response.sidebar.today')"/>
        </xsl:with-param>
        <xsl:with-param name="value" select="sum(int[position() &gt; ($numDays - 1)])"/>
        <xsl:with-param name="query" select="concat($dateField,':[NOW/DAY TO NOW]')"/>
      </xsl:call-template>
    </ul>
  </xsl:template>

  <xsl:template name="output.value">
    <xsl:param name="label"/>
    <xsl:param name="value"/>
    <xsl:param name="query"/>

    <li class="list-group-item py-0 px-0 border-0">
      <a href="{$ServletsBaseURL}solr/select?q={encoder:encode($query,'UTF-8')}">
        <div class="row">
          <div class="col-9 text-right">
            <xsl:value-of select="$label"/>:
          </div>
          <div class="col text-left">
            <xsl:value-of select="$value"/>
          </div>
        </div>
      </a>
    </li>
  </xsl:template>

</xsl:stylesheet>
