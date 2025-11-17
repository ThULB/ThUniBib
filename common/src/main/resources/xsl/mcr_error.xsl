<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions"
                exclude-result-prefixes="i18n mcrxsl xalan">

  <xsl:template match="/">
    <html>
      <head>
        <title>
          <xsl:value-of select="i18n:translate('error.title', concat(' ', /mcr_error/@HttpError))"/>
        </title>
      </head>
      <body>
        <xsl:if test=".//exception[@type = 'de.uni_jena.thunibib.publication.DuplicatePrimaryIdException']">
          <div class="jumbotron text-center">
            <p class="text-monospace">
              <xsl:value-of disable-output-escaping="yes" select=".//exception[@type = 'de.uni_jena.thunibib.publication.DuplicatePrimaryIdException'][1]/message[1]/text()"/>
            </p>
          </div>
        </xsl:if>
        <xsl:apply-templates select="mcr_error"/>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="mcr_error">
    <div class="jumbotron text-center">
      <xsl:variable name="message">
        <xsl:if test="@errorServlet and string-length(text()) &gt; 1">
          <xsl:call-template name="lf2br">
            <xsl:with-param name="string" select="text()"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:variable>

      <p class="text-monospace">
        <xsl:value-of disable-output-escaping="yes" select="i18n:translate(concat('thunibib.error.codes.', @HttpError), @requestURI)"/>
      </p>

      <h1>
        <span class="text-danger align-middle display-3 pr-3">
          <xsl:value-of select="@HttpError"/>
        </span>
        <span>
          <xsl:value-of select="$message"/>
        </span>
      </h1>

      <xsl:if test="mcrxsl:isCurrentUserInRole('admin')">
        <xsl:choose>
          <xsl:when test="@errorServlet and string-length(text()) &gt; 1 or exception">
            <xsl:if test="exception">
              <div class="card">
                <div class="card-header bg-warning text-white ubo-hover-pointer" data-toggle="collapse"
                     href="#stacktrace" role="button" aria-expanded="false" aria-controls="stacktrace">
                  <xsl:value-of select="i18n:translate('thunibib.error.show.stacktrace')"/>
                </div>

                <div id="stacktrace" class="card-body text-left collapse">
                  <xsl:for-each select="exception/trace">
                    <pre class="small">
                      <xsl:value-of select="."/>
                    </pre>
                  </xsl:for-each>
                </div>
              </div>
            </xsl:if>
          </xsl:when>

          <xsl:otherwise>
            <p>
              <small>
                <xsl:value-of select="i18n:translate('error.noInfo')"/>
              </small>
            </p>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </div>
  </xsl:template>

  <xsl:template match="mcr_error[contains('401 403', @HttpError)]">
    <div class="jumbotron text-center">
      <p class="text-monospace">
        <xsl:value-of disable-output-escaping="yes" select="i18n:translate(concat('thunibib.error.codes.', @HttpError), @requestURI)"/>
      </p>

      <h1>
        <span class="text-danger align-middle display-3 pr-3">
          <xsl:value-of select="@HttpError"/>
        </span>
        <span>
          <xsl:value-of select="i18n:translate('component.base.webpage.notLoggedIn')"/>
        </span>
      </h1>
    </div>
  </xsl:template>

  <xsl:template name="lf2br">
    <xsl:param name="string"/>
    <xsl:choose>
      <xsl:when test="contains($string,'&#xA;')">
        <xsl:value-of select="substring-before($string,'&#xA;')"/>
        <!-- replace line break character by xhtml tag -->
        <br/>
        <xsl:call-template name="lf2br">
          <xsl:with-param name="string" select="substring-after($string,'&#xA;')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$string"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
