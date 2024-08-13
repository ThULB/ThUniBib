<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:str="http://exslt.org/strings"
                xmlns:ubo="xalan://org.mycore.ubo.DozBibEntryServlet"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="xsl xalan str ubo mods">

  <xsl:output method="xml"/>

  <xsl:include href="mods-display.xsl"/>
  <xsl:include href="mycoreobject-html.xsl"/>
  <xsl:include href="coreFunctions.xsl"/>

  <xsl:param name="WebApplicationBaseURL"/>
  <xsl:param name="MCR.Mail.Address"/>
  <xsl:param name="UBO.Mail.From"/>

  <xsl:variable name="user" select="document('notnull:user:current')"/>
  <xsl:variable name="submitter-email" select="$user/user/eMail"/>

  <xsl:template match="/mycoreobject">
    <email>
      <from>
        <xsl:value-of select="$UBO.Mail.From"/>
      </from>

      <xsl:if test="string-length($MCR.Mail.Address) &gt; 0">
        <xsl:for-each select="str:tokenize($MCR.Mail.Address, ',')">
          <to>
            <xsl:value-of select="."/>
          </to>
        </xsl:for-each>
      </xsl:if>

      <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">
        <xsl:call-template name="build.to"/>
        <xsl:call-template name="build.subject"/>
        <xsl:call-template name="build.body"/>
      </xsl:for-each>
    </email>
  </xsl:template>

  <xsl:template name="build.to">
    <xsl:if test="string-length($submitter-email) &gt; 0">
      <to>
        <xsl:value-of select="$submitter-email"/>
      </to>
    </xsl:if>
  </xsl:template>

  <xsl:template name="build.subject">
    <subject>
      <xsl:text>Universitätsbibliographie: </xsl:text>
      <xsl:value-of select="number(substring-after(/mycoreobject/@ID,'_mods_'))"/>
      <xsl:text> / </xsl:text>
      <xsl:for-each select="mods:name[mods:role/mods:roleTerm='aut'][1]">
        <xsl:value-of select="mods:namePart[@type='family']"/>
        <xsl:if test="position() != last()">;</xsl:if>
      </xsl:for-each>
      <xsl:text> / </xsl:text>
      <xsl:apply-templates select="mods:titleInfo[1]"/>
    </subject>
  </xsl:template>

  <xsl:template name="salutation">
    <text>
      Liebe Kollegin, lieber Kollege,
    </text>
  </xsl:template>

  <xsl:template name="intro-text">
    <text>
      <xsl:value-of
        select="concat('der folgende Eintrag ist per Selbsteingabe von ', $user/user/realName, ' (', $submitter-email, ') an die Universitätsbibliographie gemeldet worden:')"/>
    </text>
  </xsl:template>

  <xsl:template name="complimentary-close-1">
    <xsl:text>
      Mit freundlichen Grüßen,
    </xsl:text>
  </xsl:template>

  <xsl:template name="complimentary-close-2">
    <xsl:text>
       Ihre Universitätsbibliographie
    </xsl:text>
  </xsl:template>

  <xsl:template name="link-to-entry">
    <xsl:value-of
      select="concat('Sie können sich den Eintrag hier ansehen: ', $WebApplicationBaseURL, 'servlets/DozBibEntryServlet?id=', /mycoreobject/@ID)"/>
  </xsl:template>

  <xsl:template name="user-info">
    <text>
      <xsl:value-of select="concat('Eingereicht von: ', $user/user/realName, ' (', $submitter-email, ')')"/>
    </text>
  </xsl:template>

  <xsl:template name="build.body">
    <body type="text">
      <xsl:call-template name="salutation"/>

      <xsl:call-template name="intro-text"/>

      <xsl:variable name="bibentry.html">
        <xsl:apply-templates select="." mode="html-export"/>
      </xsl:variable>

      <xsl:apply-templates select="xalan:nodeset($bibentry.html)"/>

      <xsl:call-template name="link-to-entry"/>

      <xsl:call-template name="complimentary-close-1"/>

      <xsl:call-template name="complimentary-close-2"/>
    </body>

    <body type="html">
      <xsl:variable name="message">

        <style type="text/css">
          <![CDATA[
              .cite {
                font-family: monospace;
                font-size: 90%;
              }
            ]]>
        </style>

        <p>
          <xsl:call-template name="salutation"/>
        </p>

        <p>
          <xsl:call-template name="intro-text"/>
        </p>


        <div class="cite">
          <xsl:apply-templates select="." mode="html-export"/>
        </div>

        <p>
          <xsl:call-template name="link-to-entry"/>
        </p>

        <p>
          <xsl:call-template name="complimentary-close-1"/>
        </p>
        <p>
          <xsl:call-template name="complimentary-close-2"/>
        </p>
      </xsl:variable>

      <!-- converts html to plain text -->
      <xsl:apply-templates select="xalan:nodeset($message)" mode="serialize"/>
    </body>
  </xsl:template>

  <xsl:template match="*" mode="serialize">
    <xsl:text>&lt;</xsl:text>
    <xsl:value-of select="name()"/>
    <xsl:apply-templates select="@*" mode="serialize"/>
    <xsl:choose>
      <xsl:when test="node()">
        <xsl:text>&gt;</xsl:text>
        <xsl:apply-templates mode="serialize"/>
        <xsl:text>&lt;/</xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:text>&gt;</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text> /&gt;</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@*" mode="serialize">
    <xsl:text> </xsl:text>
    <xsl:value-of select="name()"/>
    <xsl:text>="</xsl:text>
    <xsl:value-of select="."/>
    <xsl:text>"</xsl:text>
  </xsl:template>

  <xsl:template match="text()" mode="serialize">
    <xsl:value-of select="."/>
  </xsl:template>
</xsl:stylesheet>
