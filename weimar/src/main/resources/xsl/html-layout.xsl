<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:encoder="xalan://java.net.URLEncoder"
                xmlns:mcrver="xalan://org.mycore.common.MCRCoreVersion"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:utilities="xalan://de.uni_jena.thunibib.Utilities"
                exclude-result-prefixes="xsl xalan i18n encoder mcrver mcrxml utilities">

  <xsl:output method="xml" encoding="UTF-8"/>

  <xsl:param name="CurrentLang"/>
  <xsl:param name="UBO.Login.Path"/>
  <xsl:param name="UBO.TestInstance"/>

  <xsl:param name="UBO.Frontend.jquery.version"/>
  <xsl:param name="UBO.Frontend.bootstrap-select.version"/>
  <xsl:param name="UBO.Frontend.bootstrap.version"/>
  <xsl:param name="UBO.Frontend.font-awesome.version"/>

  <!-- ==================== IMPORTS ==================== -->
  <!-- additional stylesheets -->
  <xsl:include href="coreFunctions.xsl"/>
  <xsl:include href="html-layout-backend.xsl"/>
  <xsl:include href="servicedesk.xsl"/>
  <xsl:include href="user-orcid.xsl"/>

  <!-- ==================== HTML ==================== -->

  <xsl:template match="/html">
    <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html>

    </xsl:text>
    <html lang="{$CurrentLang}">
      <xsl:apply-templates select="head"/>
      <!-- include Internet Explorer warning -->
      <xsl:call-template name="msie-note"/>
      <xsl:call-template name="layout"/>
    </html>
  </xsl:template>

  <xsl:template match="head">
    <head>
      <meta charset="utf-8"/>

      <meta name="viewport" content="width=device-width, initial-scale=1"/>
      <meta http-equiv="x-ua-compatible" content="ie=edge"/>

      <link rel="shortcut icon" href="{$WebApplicationBaseURL}images/favicon.ico"/>
      <link href="{$WebApplicationBaseURL}rsc/sass/scss/bootstrap-ubo.css" rel="stylesheet"/>

      <script src="{$WebApplicationBaseURL}webjars/jquery/{$UBO.Frontend.jquery.version}/jquery.min.js"/>
      <script src="{$WebApplicationBaseURL}webjars/bootstrap/{$UBO.Frontend.bootstrap.version}/js/bootstrap.bundle.min.js"/>

      <script src="{$WebApplicationBaseURL}webjars/bootstrap-select/{$UBO.Frontend.bootstrap-select.version}/js/bootstrap-select.min.js"/>
      <link href="{$WebApplicationBaseURL}webjars/bootstrap-select/{$UBO.Frontend.bootstrap-select.version}/css/bootstrap-select.min.css" rel="stylesheet" />

      <link rel="stylesheet" href="{$WebApplicationBaseURL}webjars/jquery-ui/{$UBO.Frontend.jquery-ui.version}/jquery-ui.css" type="text/css"/>
      <link rel="stylesheet" href="{$WebApplicationBaseURL}webjars/font-awesome/{$UBO.Frontend.font-awesome.version}/css/all.css" type="text/css"/>

      <script>
        var webApplicationBaseURL = '<xsl:value-of select="$WebApplicationBaseURL"/>';
        var currentLang = '<xsl:value-of select="$CurrentLang"/>';
      </script>

      <script src="{$WebApplicationBaseURL}js/session-polling.js"/>
      <script src="{$WebApplicationBaseURL}js/person-popover.js"/>
      <xsl:copy-of select="node()"/>
    </head>
  </xsl:template>

  <!-- html body -->
  <xsl:template name="layout">
    <body class="d-flex flex-column">
      <xsl:call-template name="layout.header"/>
      <xsl:call-template name="layout.navigation"/>
      <xsl:call-template name="layout.breadcrumbPath"/>
      <xsl:call-template name="layout.headline"/>
      <xsl:call-template name="layout.body"/>
      <xsl:call-template name="layout.footer"/>
      <xsl:if test="contains($UBO.TestInstance, 'true')">
        <div id="watermark_testenvironment">Testumgebung</div>
      </xsl:if>
    </body>
  </xsl:template>

  <xsl:template name="layout.headline">
    <div id="headlineWrapper">
      <div class="container">
        <div class="row">
          <div class="col">
            <h3 id="seitentitel">
              <xsl:copy-of select="head/title/node()"/>
            </h3>
          </div>
        </div>
      </div>
    </div>

    <xsl:if test="mcrxml:isCurrentUserInRole('admin')">
      <xsl:apply-templates select="." mode="servicedesk">
        <xsl:with-param name="affiliation" select="'16982'"/>
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>

  <xsl:template name="layout.topcontainer">
    <div id="topcontainer">
      <div class="container">
        <div class="row">
          <div class="col">
            <xsl:call-template name="layout.pageTitle"/>
          </div>
        </div>
        <div class="row">
          <div class="col-lg-9">
            <xsl:call-template name="layout.breadcrumbPath"/>
          </div>
          <div class="col-lg-3 pl-0">
            <xsl:call-template name="layout.basket.info"/>
          </div>
        </div>
      </div>
    </div>
  </xsl:template>

  <!-- html body -->
  <xsl:template name="layout.body">
    <div class="bodywrapper pt-4">
      <div class="container d-flex flex-column flex-grow-1">
        <div class="row">
          <div class="col-lg">
            <xsl:call-template name="layout.inhalt"/>
          </div>
          <xsl:if test="body/aside[@id='sidebar']">
            <div class="col-lg-3 pl-lg-0">
              <xsl:copy-of select="body/aside[@id='sidebar']"/>
            </div>
          </xsl:if>
        </div>
        <div class="row">
          <div class="col">
            <hr class="mb-0"/>
          </div>
        </div>
      </div>
    </div>
  </xsl:template>

  <xsl:template name="layout.navigation">
    <div id="navigationWrapper">
      <div class="container">
        <nav class="navbar navbar-expand-lg p-0" role="navigation" id="hauptnavigation">
          <button class="navbar-toggler ml-auto" type="button" data-toggle="collapse"
                  data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false"
                  aria-label="Toggle navigation">
            <span class="navbar-toggler-icon">
              <i class="fas fa-lg fa-bars"></i>
            </span>
          </button>

          <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <ul class="navbar-nav" id="mainnav">
              <xsl:call-template name="layout.mainnav"/>
            </ul>
          </div>
        </nav>
      </div>
    </div>
  </xsl:template>

  <!-- custom navigation for additional information -->
  <xsl:template name="layout.sub.navigation.information">
    <xsl:for-each select="$navigation.tree/item[@menu='information']">
      <a href="#" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
        <i class="far fa-fw fa-user"></i>
        <span class="icon-label">
          <xsl:call-template name="output.label.for.lang"/>
        </span>
      </a>
    </xsl:for-each>
    <ul class="dropdown-menu">
      <xsl:for-each select="$navigation.tree/item[@menu='information']/item">
        <li>
          <xsl:call-template name="output.item.label"/>
        </li>
      </xsl:for-each>
    </ul>
  </xsl:template>

  <xsl:template name="layout.header">
    <header class="">
      <div class="container" id="">
        <div class="row">
          <div class="col header-brand">
            <a title="Zur Startseite" class="imageLink" href="https://www.uni-weimar.de/{$CurrentLang}/">
              <div id="wordmark"/>
            </a>
          </div>
          <nav class="col col-auto">
            <div class="nav nav-pills">
              <xsl:call-template name="layout.login"/>
            </div>
          </nav>
        </div>
      </div>
    </header>
  </xsl:template>

  <xsl:template name="layout.basket.info">
    <div id="basketWrapper">
      <a href="{$ServletsBaseURL}MCRBasketServlet?action=show&amp;type=objects">
        <span class="fas fa-bookmark mr-1" aria-hidden="true"/>
        <span class="mr-1"><xsl:value-of select="i18n:translate('basket')"/>:
        </span>
        <span class="mr-1" id="basket-info-num">
          <xsl:value-of xmlns:basket="xalan://org.mycore.ubo.basket.BasketUtils" select="basket:size()"/>
        </span>
        <span>
          <xsl:value-of select="i18n:translate('ubo.publications')"/>
        </span>
      </a>
    </div>
  </xsl:template>

  <!-- page content -->

  <xsl:template name="layout.inhalt">
    <section role="main" id="inhalt">

      <xsl:choose>
        <xsl:when test="$allowed.to.see.this.page = 'true'">
          <xsl:copy-of select="body/*[not(@id='sidebar')][not(@id='breadcrumb')]"/>
        </xsl:when>
        <xsl:otherwise>
          <h3>
            <xsl:value-of select="i18n:translate('navigation.notAllowedToSeeThisPage')"/>
          </h3>
        </xsl:otherwise>
      </xsl:choose>
    </section>
  </xsl:template>

  <!-- Brotkrumen-Navigation -->
  <xsl:template name="layout.breadcrumbPath">
    <div id="breadcrumbWrapper">
      <div class="container">
        <div class="row">
          <div class="col">
            <nav aria-label="breadcrumb">
              <ol class="breadcrumb">
                <li class="breadcrumb-item">
                  <i class="fas fa-home pr-1"></i>
                  <a
                    href="https://www.uni-weimar.de/de/universitaet/struktur/zentrale-einrichtungen/universitaetsbibliothek/">
                    <xsl:value-of select="i18n:translate('navigation.UB')"/>
                  </a>
                </li>
                <li class="breadcrumb-item">
                  <a href="{$WebApplicationBaseURL}">
                    <xsl:value-of select="i18n:translate('navigation.Home')"/>
                  </a>
                </li>
                <xsl:apply-templates mode="breadcrumb"
                                     select="$CurrentItem/ancestor-or-self::item[@label|label][ancestor-or-self::*=$navigation.tree[@role='main']]"/>
                <xsl:for-each select="body/ul[@id='breadcrumb']/li">
                  <li class="breadcrumb-item">
                    <a>
                      <xsl:attribute name="href">
                        <xsl:choose>
                          <xsl:when test="@data-href">
                            <xsl:value-of select="@data-href"/>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:value-of select="'#'"/>
                          </xsl:otherwise>
                        </xsl:choose>
                      </xsl:attribute>
                      <xsl:copy-of select="node()"/>
                    </a>
                  </li>
                </xsl:for-each>
              </ol>
            </nav>
          </div>
          <div class="col col-auto">
            <xsl:call-template name="layout.basket.info"/>
          </div>
        </div>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="item" mode="breadcrumb">
    <li class="breadcrumb-item">
      <xsl:call-template name="output.item.label"/>
    </li>
  </xsl:template>

  <!-- current user and login formular-->
  <xsl:template name="layout.login">
    <div class="nav-item mr-2">
      <xsl:if test="not($CurrentUser = $MCR.Users.Guestuser.UserName)">
        <xsl:variable name="userData" select="document('user:current')/user"/>
        <xsl:variable name="userId">
          <xsl:choose>
            <xsl:when test="contains($CurrentUser,'@')">
              <xsl:value-of select="substring-before($CurrentUser,'@')"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$CurrentUser"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <a aria-expanded="false" aria-haspopup="true" data-toggle="dropdown"
           role="button" id="mcrFunctionsDropdown" href="#"
           class="user nav-link dropdown-toggle p-0 ubo-hover-pointer d-inline-block">
          <xsl:choose>
            <xsl:when test="$userData/realName">
              <xsl:value-of select="$userData/realName"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$userId"/>
            </xsl:otherwise>
          </xsl:choose>
        </a>
        <div aria-labeledby="mcrFunctionsDropdown" class="dropdown-menu">
          <xsl:call-template name="layout.usernav"/>
        </div>
      </xsl:if>

      <xsl:call-template name="orcidUser"/>
    </div>

    <div class="nav-item mr-2">
      <xsl:choose>
        <xsl:when test="/webpage/@id='login'"/>
        <xsl:when test="$CurrentUser = $MCR.Users.Guestuser.UserName">
          <a class="btn btn-link p-0" title="{i18n:translate('component.user2.button.login')}"
             href="{$WebApplicationBaseURL}{$UBO.Login.Path}?url={encoder:encode($RequestURL)}">
            <xsl:value-of select="i18n:translate('thunibib.signIn')"/>
          </a>
        </xsl:when>
      </xsl:choose>
      <span class="text-primary ml-2 mr-1">
        <xsl:value-of select="'|'"/>
      </span>
    </div>

    <div class="nav-item dropdown">
      <xsl:variable name="href-lang-toggle">
        <xsl:choose>
          <xsl:when test="$CurrentLang='de'">
            <xsl:call-template name="UrlSetParam">
              <xsl:with-param name="url" select="$RequestURL"/>
              <xsl:with-param name="par" select="'lang'"/>
              <xsl:with-param name="value" select="'en'"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:when test="$CurrentLang='en'">
            <xsl:call-template name="UrlSetParam">
              <xsl:with-param name="url" select="$RequestURL"/>
              <xsl:with-param name="par" select="'lang'"/>
              <xsl:with-param name="value" select="'de'"/>
            </xsl:call-template>
          </xsl:when>
        </xsl:choose>
      </xsl:variable>
      <span class="dropdown-toggle" type="button" id="dropdownMenuButton" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
        <i class="border flag flag-{$CurrentLang}" style="vertical-align:middle"/>
        <span class="align-middle">
          <xsl:value-of select="utilities:toUpperCase($CurrentLang)"/>
        </span>
      </span>

      <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
        <a class="dropdown-item" href="{$href-lang-toggle}">
          <xsl:variable name="other-lang">
            <xsl:choose>
              <xsl:when test="$CurrentLang = 'de'">
                <xsl:value-of select="'en'"/>
              </xsl:when>
              <xsl:when test="$CurrentLang = 'en'">
                <xsl:value-of select="'de'"/>
              </xsl:when>
            </xsl:choose>
          </xsl:variable>
          <i class="border flag flag-{$other-lang}"/>
          <span class="align-middle">
            <xsl:value-of select="i18n:translate('navigation.ende')"/>
          </span>
        </a>
      </div>
    </div>
  </xsl:template>

  <xsl:template name="layout.pageTitle">
    <div class="card my-3">
      <div class="card-body py-2">
        <h3 id="seitentitel">
          <xsl:copy-of select="head/title/node()"/>
        </h3>
      </div>
    </div>
  </xsl:template>

  <!-- Footer -->

  <xsl:template name="layout.footer">
    <footer>
      <div class="container">
        <div class="row">
          <div class="col-12 col-sm-6 col-lg-8 hgn-footer-menu">
            <xsl:call-template name="layout.imprintline"/>
          </div>
          <div class="col-12 col-sm-6 col-lg-4">
            <xsl:call-template name="powered_by"/>
          </div>
        </div>
      </div>
    </footer>
  </xsl:template>

  <xsl:template name="layout.imprintline">
    <!-- TODO: use navigation.xml to generate this AND use correct language! -->
    <xsl:variable name="navigation" select="document('webapp:navigation.xml')"/>
    <div class="imprintlinewrapper">
      <xsl:for-each select="$navigation/navigation/item[@role='meta']/item[not(@xml:lang) or lang($CurrentLang)]">
        <span>
          <a href="{$WebApplicationBaseURL}{./@ref}">
            <xsl:attribute name="href">
              <xsl:choose>
                <xsl:when test="contains(@ref, '://')">
                  <xsl:value-of select="@ref"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="concat($WebApplicationBaseURL, @ref)"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:attribute>
            <xsl:value-of select="label[lang($CurrentLang)]"/>
          </a>
        </span>
      </xsl:for-each>
    </div>
  </xsl:template>

  <xsl:template name="powered_by">
    <xsl:variable name="mcr_version" select="concat('MyCoRe ', mcrver:getCompleteVersion())"/>
    <div id="powered_by">
      <a href="http://www.mycore.de">
        <img src="{$WebApplicationBaseURL}images/mycore_logo_small_invert.png" title="{$mcr_version}"
             alt="powered by MyCoRe"/>
      </a>
    </div>
  </xsl:template>
</xsl:stylesheet>
