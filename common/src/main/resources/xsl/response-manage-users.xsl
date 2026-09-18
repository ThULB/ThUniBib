<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="i18n xalan xsl">

  <xsl:include href="resource:xsl/thunibib-users-additional.xsl"/>

  <xsl:param name="CurrentLang"/>
  <xsl:param name="SolrBase" select="concat($WebApplicationBaseURL, 'servlets/solr/select?core=users&amp;XSL.Style=manage-users')"/>
  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:variable name="numFound" select="//result/@numFound"/>
  <xsl:variable name="q" select="//lst[@name='params']/str[@name='q']"/>

  <xsl:variable name="rows">
    <xsl:choose>
      <xsl:when test="//lst[@name='params']/str[@name='rows']">
        <xsl:value-of select="//lst[@name='params']/str[@name='rows']"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="10"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="start">
    <xsl:choose>
      <xsl:when test="//lst[@name='params']/str[@name='start']">
        <xsl:value-of select="//lst[@name='params']/str[@name='start']"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="0"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:template match="/">
    <webpage>
      <title>
        <xsl:value-of select="i18n:translate('component.user2.admin.users.title')"/>
        <xsl:value-of
          select="concat(' (',count(//doc), '/', $numFound, i18n:translate('thunibib.user.management.user.shown'), ')')"/>
      </title>

      <article class="card mb-3">
        <div class="card-body">
          <div class="row">
            <div class="col-12 col-sm-9">
              <form action="{$WebApplicationBaseURL}servlets/solr/select">
                <input type="hidden" name="XSL.Style" value="manage-users"/>
                <input type="hidden" name="core" value="users"/>
                <input type="hidden" name="rows" value="10"/>
                <input type="hidden" name="start" value="0"/>

                <div class="row">
                  <div class="col input-group">
                    <input class="mycore-form-input" id="q" name="q" type="text" value="{$q}"
                           placeholder="{translate(i18n:translate('editor.search.inall.label'), ':', '')}"/>
                    <div class="input-group-append">
                      <button class="btn btn-primary">
                        <i class="fas fa-search"/>
                      </button>
                    </div>
                  </div>
                </div>
              </form>
            </div>

            <div class="col text-right">
              <a href="{$WebApplicationBaseURL}authorization/new-user.xed?action=save" class="btn btn-outline-primary">
                <xsl:value-of select="i18n:translate('component.user2.admin.create.title')"/>
              </a>
            </div>
          </div>
        </div>
      </article>

      <article id="thunibib-user-management-card" class="card mb-3">
        <div class="card-body">
          <!-- Table Header -->
          <div class="row border-bottom pb-3">
            <div class="col-12 col-sm-2 font-weight-bold">
              <xsl:value-of select="translate(i18n:translate('component.user2.admin.userAccount'), ':', '')"/>
            </div>
            <div class="col-12 col-sm-2 font-weight-bold">
              <xsl:value-of select="i18n:translate('component.user2.admin.user.realm')"/>
            </div>
            <div class="col-12 col-sm-3 font-weight-bold">
              <xsl:value-of select="translate(i18n:translate('component.user2.admin.user.name'), ':', '')"/>
            </div>
            <div class="col-12 col-sm-3 font-weight-bold">
              <xsl:value-of select="translate(i18n:translate('component.user2.admin.email'), ':', '')"/>
            </div>
            <div class="col-12 col-sm text-sm-right font-weight-bold">
              <xsl:value-of select="i18n:translate('component.acl.accesskey.frontend.label.actions')"/>
            </div>
          </div>

          <!-- User entries -->
          <xsl:apply-templates select="response/result/doc"/>

          <xsl:variable name="next-start" select="$start + $rows"/>
          <xsl:variable name="prev-start" select="$start - $rows"/>

          <xsl:variable name="next-href">
            <xsl:value-of
              select="concat($SolrBase, '&amp;q=', $q, '&amp;rows=', $rows, '&amp;start=', $next-start, '#thunibib-user-management-card')"/>
          </xsl:variable>

          <xsl:variable name="pref-href">
            <xsl:value-of
              select="concat($SolrBase, '&amp;q=', $q, '&amp;rows=', $rows, '&amp;start=', $prev-start, '#thunibib-user-management-card')"/>
          </xsl:variable>

          <xsl:variable name="next-disabled-class">
            <xsl:if test="$next-start &gt; $numFound">
              <xsl:value-of select="'disabled'"/>
            </xsl:if>
          </xsl:variable>

          <xsl:variable name="prev-disabled-class">
            <xsl:if test="$prev-start &lt; 0">
              <xsl:value-of select="'disabled'"/>
            </xsl:if>
          </xsl:variable>

          <!-- Simple Paging -->
          <div class="row pt-3">
            <div class="col offset-sm-1">
              <nav>
                <ul class="pagination justify-content-center">
                  <li class="page-item {$prev-disabled-class}">
                    <a class="page-link" href="{$pref-href}" tabindex="-1">
                      <xsl:value-of select="i18n:translate('component.solr.searchresult.prev')"/>
                    </a>
                  </li>

                  <li class="page-item {$next-disabled-class}">
                    <a class="page-link" href="{$next-href}">
                      <xsl:value-of select="i18n:translate('component.solr.searchresult.next')"/>
                    </a>
                  </li>
                </ul>
              </nav>
            </div>
            <div class="col col-sm-1 text-right">
              <a href="{$WebApplicationBaseURL}servlets/TriggerRebuildUsersIndexServlet"
                 class="btn btn-sm btn-danger"
                 title="{i18n:translate('thunibib.user.rebuild.index')}"
                 onclick="this.classList.add('thunibib-pointer-events-none');this.classList.add('disabled')">
                <i class="fas fa-sync"/>
              </a>
            </div>
          </div>
        </div>
      </article>

      <h3>
        <xsl:value-of select="i18n:translate('thunibib.users.additional.views')"/>
      </h3>
      <xsl:apply-templates select="." mode="headAdditional" />
    </webpage>
  </xsl:template>

  <xsl:template match="doc">
    <xsl:variable name="bg-color-class">
      <xsl:choose>
        <xsl:when test="position() mod 2 = 0">
          <xsl:value-of select="'bg-light'"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'bg-white'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="border-last">
      <xsl:if test="position() = last()">
        <xsl:value-of select="'border-bottom'"/>
      </xsl:if>
    </xsl:variable>

    <div class="row pt-3 pb-3 {$bg-color-class} {$border-last}">
      <div class="col-12 col-sm-2 text-truncate" title="{str[@name='id']}">
        <a href="{$WebApplicationBaseURL}servlets/MCRUserServlet?action=show&amp;id={str[@name='id']}">
          <xsl:value-of select="str[@name='id']"/>
        </a>
      </div>

      <div class="col-12 col-sm-2" title="{str[@name='realmId']}">
        <xsl:value-of select="str[@name='realmId']"/>
      </div>

      <div class="col-12 col-sm-3 text-truncate" title="{str[@name='realName']}">
        <xsl:value-of select="str[@name='realName']"/>
      </div>

      <div class="col-12 col-sm-3 text-truncate" title="{arr[@name='mail']/str[1]}">
        <a href="mailto:{arr[@name='mail']/str[1]}">
          <xsl:value-of select="arr[@name='mail']/str[1]"/>
        </a>
      </div>

      <div class="col text-right">
        <a class="btn btn-sm btn-outline-primary mr-1" title="{i18n:translate('component.user2.admin.change.title')}"
           href="{$WebApplicationBaseURL}authorization/change-user.xed?action=save&amp;id={str[@name='id']}">
          <i class="far fa-edit"/>
        </a>

        <a class="btn btn-sm btn-outline-danger" title="{i18n:translate('component.user2.admin.userDeleteYes')}"
           href="{$WebApplicationBaseURL}servlets/MCRUserServlet?action=show&amp;id={str[@name='id']}&amp;XSL.step=confirmDelete">
          <i class="far fa-trash-alt"/>
        </a>
      </div>
    </div>
  </xsl:template>

</xsl:stylesheet>
