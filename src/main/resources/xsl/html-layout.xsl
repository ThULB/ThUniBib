<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xalan="http://xml.apache.org/xalan"
  xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
  xmlns:encoder="xalan://java.net.URLEncoder" 
  exclude-result-prefixes="xsl xalan i18n encoder">

  <xsl:output method="html" encoding="UTF-8" media-type="text/html" indent="yes" xalan:indent-amount="2" />

  <xsl:param name="CurrentLang" />

  <xsl:variable name="jquery.version" select="'3.3.1'" />
  <xsl:variable name="jquery-ui.version" select="'1.12.1'" />
  <xsl:variable name="chosen.version" select="'1.8.7'" />
  <xsl:variable name="bootstrap.version" select="'4.1.3'" />
  <xsl:variable name="font-awesome.version" select="'5.5.0'" />

  <!-- ==================== IMPORTS ==================== -->
  <!-- additional stylesheets -->
  <xsl:include href="coreFunctions.xsl" />
  <xsl:include href="html-layout-backend.xsl" />

  <!-- ==================== HTML ==================== -->
  
  <xsl:template match="/html">
    <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html>

    </xsl:text>
    <html lang="{$CurrentLang}">
      <xsl:apply-templates select="head" />
      <xsl:call-template name="layout" />
    </html>
  </xsl:template>

  <xsl:template match="head">
    <head>
      <meta charset="utf-8" />

      <meta name="viewport" content="width=device-width, initial-scale=1" />
      <meta http-equiv="x-ua-compatible" content="ie=edge" />

      <link href="{$WebApplicationBaseURL}rsc/sass/scss/bootstrap-ubo.css" rel="stylesheet" />
      <link href="{$WebApplicationBaseURL}rsc/sass/scss/bootstrap-thunibib.css" rel="stylesheet" />
      <script type="text/javascript" src="{$WebApplicationBaseURL}webjars/jquery/{$jquery.version}/jquery.min.js"></script>
      <script type="text/javascript" src="{$WebApplicationBaseURL}webjars/bootstrap/{$bootstrap.version}/js/bootstrap.bundle.min.js"></script>
      <script type="text/javascript" src="{$WebApplicationBaseURL}webjars/chosen-js/{$chosen.version}/chosen.jquery.min.js"></script>
      <link href="{$WebApplicationBaseURL}webjars/chosen-js/{$chosen.version}/chosen.min.css" rel="stylesheet" />
      <script type="text/javascript" src="{$WebApplicationBaseURL}webjars/jquery-ui/{$jquery-ui.version}/jquery-ui.js"></script>
      <link rel="stylesheet" href="{$WebApplicationBaseURL}webjars/jquery-ui/{$jquery-ui.version}/jquery-ui.css" type="text/css"/>
      <link rel="stylesheet" href="{$WebApplicationBaseURL}webjars/font-awesome/{$font-awesome.version}/css/all.css" type="text/css"/>
      <link rel="stylesheet" href="{$WebApplicationBaseURL}webjars/bootstrap-glyphicons/bdd2cbfba0/css/bootstrap-glyphicons.css" type="text/css"/>
      <link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Droid+Sans|Droid+Sans+Mono" type="text/css" />
      <link rel="apple-touch-icon-precomposed" sizes="114x114" href="https://www.uni-due.de/imperia/md/images/cms/h/apple-touch-icon.png" />
      <link rel="apple-touch-icon-precomposed" sizes="72x72" href="https://www.uni-due.de/imperia/md/images/cms/m/apple-touch-icon.png" />
      <link rel="apple-touch-icon-precomposed" href="https://www.uni-due.de/imperia/md/images/cms/l/apple-touch-icon-precomposed.png" />
      <link rel="shortcut icon" href="https://www.uni-due.de/imperia/md/images/cms/l/apple-touch-icon.png" />
      <link rel="shortcut icon" href="{$WebApplicationBaseURL}images/favicon.ico" />

      <script type="text/javascript">var webApplicationBaseURL = '<xsl:value-of select="$WebApplicationBaseURL" />';</script>
      <script type="text/javascript">var currentLang = '<xsl:value-of select="$CurrentLang" />';</script>

      <xsl:copy-of select="node()" />
    </head>
  </xsl:template>

  <!-- all layout -->
  <xsl:template name="layout">
    <body class="d-flex flex-column">
      <!-- <xsl:call-template name="layout.headerline" /> -->
      <xsl:call-template name="layout.header" />
      <xsl:call-template name="layout.navigation" />
      <xsl:call-template name="layout.breadcrumbPath"/>
      <xsl:call-template name="layout.headline"/>
      <!-- <xsl:call-template name="layout.topcontainer" /> -->
      <xsl:call-template name="layout.body" />
      <!-- <xsl:call-template name="layout.footer" /> -->
    </body>
  </xsl:template>

  <xsl:template name="layout.headline">
    <div id="headlineWrapper">
      <div class="container w-50">
        <div class="row">
          <div class="col">
            <h3 id="seitentitel">
              <xsl:value-of select="head/title" disable-output-escaping="yes" />
            </h3>
          </div>
        </div>
      </div>
    </div>
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
            <xsl:call-template name="layout.inhalt" />
          </div>
          <xsl:if test="body/aside[@id='sidebar']">
            <div class="col-lg-3 pl-lg-0">
              <xsl:copy-of select="body/aside[@id='sidebar']" />
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
          <button class="navbar-toggler ml-auto" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
          </button>

          <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <ul class="navbar-nav" id="mainnav">
              <xsl:call-template name="layout.mainnav" />
            </ul>
          </div>
        </nav>
      </div>
    </div>
  </xsl:template>

  <xsl:template name="layout.headerline">
    <div id="headerLine" class="d-flex align-items-center bg-white">
      <div class="container" >
        <div class="row">
          <nav class="col">
            <ul class="nav">
              <li class="">
                <a href="#" class="dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" role="button">
                  <i class="far fa-fw fa-comments"></i>
                  <span class="icon-label">Kontakt</span>
                </a>
                <div class="dropdown-menu" x-placement="bottom-start">
                  <ul>
                    <li>
                      <a href="tel:+492011834031">
                        <i class="far fa-fw fa-phone"></i>
                        +49 201 18 34031
                      </a>
                    </li>
                    <li>
                      <a href="/">
                        <i class="far fa-fw fa-envelope"></i>
                        sekretariat.softec@paluno.uni-due.de
                      </a>
                    </li>
                    <li>
                      <a href="/kontakt/anfahrt-und-postanschrift/">
                        <i class="far fa-fw fa-map-signs"></i>
                        Anfahrt und Postanschrift
                      </a>
                    </li>
                  </ul>
                </div>
              </li>
              <li id="navigationStakeholder">
                <xsl:call-template name="layout.sub.navigation.information"/>
              </li>
            </ul>
          </nav>
          <nav class="col col-auto">
            <a href="#" class="social-toggler" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" role="button" id="socialDropDownButton">
              <i class="far fa-fw fa-share-square" aria-hidden="true"></i>
              <span class="icon-label">Social Media</span>
            </a>
            <div class="dropdown-menu dropdown-menu-right" id="navigationSocialContent" aria-labelledby="socialDropDownButton">
              <ul>
                <li>
                  <a href="https://www.uni-due.de/myude/" class="social social-myude" title="myUDE">
                    <i class="fab fa-fw fa-myude" aria-hidden="true"></i>
                  </a>
                </li>
                <li>
                  <a href="http://www.facebook.com/uni.due" class="social social-facebook" title="Facebook">
                    <i class="fab fa-fw fa-facebook-f" aria-hidden="true"></i>
                  </a>
                </li>
                <li>
                  <a href="http://twitter.com/unidue" class="social social-twitter" title="Twitter">
                    <i class="fab fa-fw fa-twitter" aria-hidden="true"></i>
                  </a>
                </li>
                <li>
                  <a href="https://www.softec.wiwi.uni-due.de/rss/" class="social social-rss" title="RSS">
                    <i class="fab fa-fw fa-rss" aria-hidden="true"></i>
                  </a>
                </li>
              </ul>
            </div>
          </nav>
          <nav class="col col-auto">
            <div class="nav nav-pills">
              <xsl:call-template name="layout.login"/>
            </div>
          </nav>
        </div>
      </div>
    </div>
  </xsl:template>

  <!-- custom navigation for additional information -->

  <xsl:template name="layout.sub.navigation.information">
    <xsl:for-each select="$navigation.tree/item[@menu='information']">
      <a href="#" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
        <i class="far fa-fw fa-user"></i>
        <span class="icon-label"><xsl:call-template name="output.label.for.lang"/></span>
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
          <div class="col py-3">
            <a title="Zur Startseite" class="imageLink" href="http://www.uni-jena.de/">
              <div id="wordmark" />
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
      <a href="{$ServletsBaseURL}MCRBasketServlet?action=show&amp;type=bibentries">
            <span class="fas fa-bookmark mr-1" aria-hidden="true" />
            <span class="mr-1"><xsl:value-of select="i18n:translate('basket')" />:</span>
            <span class="mr-1" id="basket-info-num">
              <xsl:value-of xmlns:basket="xalan://unidue.ubo.basket.BasketUtils" select="basket:size()" />
            </span>
            <span><xsl:value-of select="i18n:translate('ubo.publications')" /></span>
      </a>
    </div>
  </xsl:template>

  <!-- page content -->

  <xsl:template name="layout.inhalt">
    <section role="main" id="inhalt">
    
      <xsl:choose>
        <xsl:when test="$allowed.to.see.this.page = 'true'">
          <xsl:copy-of select="body/*[not(@id='sidebar')][not(@id='breadcrumb')]" />
        </xsl:when>
        <xsl:otherwise>
          <h3>
            <xsl:value-of select="i18n:translate('navigation.notAllowedToSeeThisPage')" />
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
                  <a href="https://www.uni-due.de/ub/">
                    <xsl:value-of select="i18n:translate('navigation.UB')" />
                  </a>
                </li>
                <li class="breadcrumb-item">
                  <a href="{$WebApplicationBaseURL}">
                    <xsl:value-of select="i18n:translate('navigation.Home')" />
                  </a>
                </li>
                <xsl:apply-templates mode="breadcrumb"
                                     select="$CurrentItem/ancestor-or-self::item[@label|label][ancestor-or-self::*=$navigation.tree[@role='main']]" />
                <xsl:for-each select="body/ul[@id='breadcrumb']/li">
                  <li class="breadcrumb-item">
                    <a href="#">
                      <xsl:copy-of select="node()" />
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
      <xsl:call-template name="output.item.label" />
    </li>
  </xsl:template>

  <!-- current user and login formular-->
  <xsl:template name="layout.login">

    <div class="nav-item mr-2">
      <span class="user btn p-0" style="cursor: default;">
        <xsl:text>[ </xsl:text>
        <xsl:choose>
          <xsl:when test="$CurrentUser = $MCR.Users.Guestuser.UserName">
            <xsl:value-of select="i18n:translate('component.user2.login.guest')" />
          </xsl:when>
          <xsl:when test="contains($CurrentUser,'@')">
            <a href="{$ServletsBaseURL}MCRUserServlet?action=show" style="text-decoration: none;">
              <xsl:value-of select="substring-before($CurrentUser,'@')" />
            </a>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$CurrentUser" />
          </xsl:otherwise>
        </xsl:choose>
        <xsl:text> ]</xsl:text>

        <xsl:call-template name="orcidUser" />        
      </span>
    </div>
    <div class="nav-item mr-2">
      <xsl:choose>
        <xsl:when test="/webpage/@id='login'" />
        <xsl:when test="$CurrentUser = $MCR.Users.Guestuser.UserName">
          <form action="{$WebApplicationBaseURL}login.xed" method="get">
            <input type="hidden" name="url" value="{$RequestURL}" />
            <button title="Anmelden" class="btn btn-link p-0" type="submit" name="{i18n:translate('component.user2.button.login')}" value="{i18n:translate('component.user2.button.login')}">
              <i class="fas fa-lock"></i>
            </button>
          </form>
        </xsl:when>
        <xsl:otherwise>
          <form action="{$ServletsBaseURL}logout" method="get">
            <input type="hidden" name="url" value="{$RequestURL}" />
            <button title="Anmelden" class="btn btn-link p-0" style="border:0;" type="submit" name="{i18n:translate('login.logOut')}" value="{i18n:translate('login.logOut')}">
              <i class="fas fa-lock-open"></i>
            </button>
          </form>
        </xsl:otherwise>
      </xsl:choose>
    </div>
    <div class="nav-item">
      <span class="btn p-0">
        <a>
          <xsl:attribute name="href">
            <xsl:choose>
              <xsl:when test="$CurrentLang='de'">
                <xsl:call-template name="UrlSetParam">
                  <xsl:with-param name="url" select="$RequestURL" />
                  <xsl:with-param name="par" select="'lang'" />
                  <xsl:with-param name="value" select="'en'" />
                </xsl:call-template>
              </xsl:when>
              <xsl:when test="$CurrentLang='en'">
                <xsl:call-template name="UrlSetParam">
                  <xsl:with-param name="url" select="$RequestURL" />
                  <xsl:with-param name="par" select="'lang'" />
                  <xsl:with-param name="value" select="'de'" />
                </xsl:call-template>
              </xsl:when>
            </xsl:choose>
          </xsl:attribute>
          <!-- <img src="{$WebApplicationBaseURL}images/lang_{$CurrentLang}.gif" alt="{i18n:translate('navigation.Language')}" /> -->
          <xsl:value-of select="i18n:translate('navigation.ende')"/>
        </a>
      </span>
    </div>

  </xsl:template>

  <!-- If current user has ORCID and we are his trusted party, display ORCID icon to indicate that -->  
  <xsl:param name="MCR.ORCID.LinkURL" />

  <xsl:template name="orcidUser">

    <xsl:variable name="orcidUser" select="orcidSession:getCurrentUser()" xmlns:orcidSession="xalan://org.mycore.orcid.user.MCRORCIDSession" />
    <xsl:variable name="userStatus" select="orcidUser:getStatus($orcidUser)" xmlns:orcidUser="xalan://org.mycore.orcid.user.MCRORCIDUser" />
    <xsl:variable name="trustedParty" select="userStatus:weAreTrustedParty($userStatus)" xmlns:userStatus="xalan://org.mycore.orcid.user.MCRUserStatus" />
    
    <xsl:if test="$trustedParty = 'true'">
      <xsl:variable name="orcid" select="orcidUser:getORCID($orcidUser)" xmlns:orcidUser="xalan://org.mycore.orcid.user.MCRORCIDUser" />
      <a href="{$MCR.ORCID.LinkURL}{$orcid}">
        <img alt="ORCID {$orcid}" src="{$WebApplicationBaseURL}images/orcid_icon.svg" class="orcid-icon" />
      </a>
    </xsl:if>
  </xsl:template>

  <xsl:template name="layout.pageTitle">
    <div class="card my-3">
      <div class="card-body py-2">
        <h3 id="seitentitel">
          <xsl:value-of select="head/title" disable-output-escaping="yes" />
        </h3>
      </div>
    </div>
  </xsl:template>
  
  <!-- Footer -->

  <xsl:template name="layout.footer">
    <footer>
      <div class="container py-4">
        <div class="row">
          <div class="col col-md-4">
            <h2 class="card-title">SOFTEC-Lehrstuhl</h2>
            <div class="csc-textpic csc-textpic-left csc-textpic-above">
              <div class="csc-textpic-imagewrap" data-csc-images="1" data-csc-cols="2">
                <figure class="csc-textpic-image csc-textpic-last">
                  <img src="https://static.wiwi.uni-due.de/user-upload/SOFTEC-logo-mono.svg" width="300" height="82" alt=""/>
                </figure>
              </div>
            </div>
          </div>
          <div class="col col-md-4">
            <xsl:value-of select="document('webapp:contact.xml')//article[lang($CurrentLang)]"/>
          </div>
          <div class="col col-md-4">
            <h2 class="card-title">Mitgliedschaften</h2>
            <ul>
              <li>
                <a href="https://www.wiwi.uni-due.de">Fakultät für Wirtschaftswissenschaften</a>
              </li>
              <li>
                <a href="https://www.wi.wiwi.uni-due.de/home/">Fachgebiet Wirtschaftsinformatik</a>
              </li>
              <li>
                <a href="https://www.icb.uni-due.de">Institut für Informatik und Wirtschaftsinformatik (ICB)</a>
              </li>
              <li>
                <a href="https://paluno.uni-due.de/">paluno - The Ruhr Institute for Software Technology</a>
              </li>
            </ul>
          </div>
        </div>
      </div>
      <xsl:call-template name="layout.imprintline"/>
    </footer>
  </xsl:template>

  <!-- Imprintline (below footer) -->
  <xsl:template name="layout.imprintline">
    <div class="imprintlinewrapper">
      <div class="container py-4">
        <div class="row">
          <div class="col" id="footerLogo">
            <a href="https://www.uni-due.de/">
              <img src="https://static.wiwi.uni-due.de/ude2018/Images/UDE-logo-claim-dark.svg" width="1052" height="414" alt="" />
            </a>
          </div>
          <div class="col col-md-auto justify-content-end">
            <nav id="navigationFooter" class="navbar">
              <ul>
                <li>
                  <a href="https://www.uni-due.de/infoline/">
                    <i class="fa fa-fw fa-phone"></i>Infoline
                  </a>
                </li>
                  <li>
                    <a href="https://www.uni-due.de/de/hilfe_im_notfall.php">
                      <i class="fa fa-fw fa-exclamation-triangle"></i>Hilfe im Notfall
                    </a>
                  </li>
                  <li>
                    <a href="/sitemap/">
                      <i class="fa fa-fw fa-sitemap"></i>Sitemap
                    </a>
                  </li>
                  <li>
                    <a href="/datenschutz/">
                      <i class="fa fa-fw fa-user-shield"></i>Datenschutz
                    </a>
                  </li>
                  <li>
                    <a href="/impressum/">
                      <i class="fa fa-fw fa-file-alt"></i>Impressum
                    </a>
                  </li>
              </ul>
            </nav>
            <div id="footerCopyright" class="navbar">
                <ul class="nav">
                    <li>© Universität Duisburg-Essen</li>
                    <li><xsl:apply-templates select="/html/@lastModified" /></li>
                </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  </xsl:template>

</xsl:stylesheet>
