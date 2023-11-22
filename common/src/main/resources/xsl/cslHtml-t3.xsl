<?xml version="1.0"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:html="http://www.w3.org/1999/xhtml" xmlns:fn="http://www.w3.org/2005/xpath-functions"
                exclude-result-prefixes="fn">

  <xsl:output method="html"
              doctype-system="about:legacy-compat"
              indent="yes" omit-xml-declaration="yes" media-type="text/html"
              version="5"/>

  <xsl:param name="RequestURL"/>
  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match='@*|node()'>
    <xsl:copy>
      <xsl:apply-templates select='@*|node()'/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="html:body">
    <xsl:variable name="tokens" select="fn:tokenize($RequestURL, '&amp;')"/>
    <xsl:variable name="q" select="fn:substring-after($tokens[1], 'q=')"/>
    <xsl:variable name="numFound" select="fn:document(fn:concat('solr:q=', $q))/response/result/@numFound"/>

    <body>
      <div>
        <p>
          <xsl:value-of select="fn:concat('numFound: ',$numFound)"/>
        </p>

        <xsl:for-each select="$tokens">
          <p>
            <xsl:value-of select="concat(position(), ': ', .)"/>
          </p>
        </xsl:for-each>
      </div>
      <xsl:apply-templates select="*"/>
    </body>
  </xsl:template>

  <xsl:template match="html:div">
    <xsl:copy>
      <xsl:call-template name="applyStyle"/>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="applyStyle">
    <xsl:variable name="style">
      <xsl:if test="contains(@class, 'csl-entry')">
        <xsl:text>font-size: 12pt;padding-top: 10px;width: 90%;</xsl:text>
      </xsl:if>
      <xsl:if test="contains(@class, 'csl-left-margin')">
        <xsl:text>display:none</xsl:text>
      </xsl:if>
      <xsl:if test="contains(@class, 'csl-right-inline')">
        <xsl:text>display: inline;</xsl:text>
      </xsl:if>
    </xsl:variable>
    <xsl:if test="string-length($style)&gt;0">
      <xsl:attribute name="style">
        <xsl:value-of select="$style"/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
