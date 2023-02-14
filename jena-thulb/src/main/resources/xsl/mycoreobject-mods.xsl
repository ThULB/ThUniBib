<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:mods="http://www.loc.gov/mods/v3"
                exclude-result-prefixes="xsl xalan">

  <xsl:include href="output-category.xsl"/>

  <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="2"/>

  <xsl:template match="mycoreobject">
    <mods:mods ID="{@ID}" version="3.6"
               xsi:schemaLocation="http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/v3/mods-3-6.xsd">
      <xsl:apply-templates select="." mode="mods"/>
    </mods:mods>
  </xsl:template>

  <xsl:template match="mycoreobject" mode="mods">
    <xsl:for-each select="metadata/def.modsContainer/modsContainer/mods:mods">
      <xsl:apply-templates select="mods:*" mode="copy-mods"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:variable name="genres" select="document('classification:metadata:-1:children:ubogenre')/mycoreclass/categories"/>

  <!-- Map internal mods:genre to standard marcgt -->
  <xsl:template match="mods:genre[@type='intern']" mode="copy-mods">
    <xsl:variable name="genre" select="$genres//category[@ID=current()]"/>
    <mods:genre authority="marcgt">
      <xsl:variable name="marcgt" select="$genre/label[lang('x-marcgt')]/@text"/>
      <xsl:choose>
        <xsl:when test="string-length($marcgt) &gt; 0">
          <xsl:value-of select="$marcgt"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </mods:genre>
    <xsl:if test=".='chapter'">
      <mods:genre authority="local">Contribution</mods:genre>
    </xsl:if>
    <xsl:copy-of select="."/>
    <mods:genre xml:lang="de">
      <xsl:value-of select="$genre/label[lang('de')]/@text"/>
    </mods:genre>
    <mods:genre xml:lang="en">
      <xsl:value-of select="$genre/label[lang('en')]/@text"/>
    </mods:genre>
  </xsl:template>

  <xsl:template match='@*|node()' mode="copy-mods">
    <xsl:copy>
      <xsl:apply-templates select='@*|node()' mode="copy-mods"/>
    </xsl:copy>
  </xsl:template>

  <xsl:param name="UBO.LSF.Link"/>

  <xsl:template match="mods:nameIdentifier[@type='lsf']" mode="copy-mods">
    <xsl:copy>
      <xsl:apply-templates select="@*" mode="copy-mods"/>
      <xsl:attribute name="typeURI">http://www.lsf.uni-due.de</xsl:attribute>
      <xsl:apply-templates select="node()" mode="copy-mods"/>
    </xsl:copy>
  </xsl:template>

  <xsl:variable name="marcrelators" select="document('classification:metadata:-1:children:marcrelator')/mycoreclass"/>

  <xsl:template match="mods:roleTerm[@authority='marcrelator'][@type='code']" mode="copy-mods">
    <xsl:copy-of select="."/>
    <mods:roleTerm authority="marcrelator" type="text">
      <xsl:variable name="code" select="."/>
      <xsl:variable name="label" select="$marcrelators//category[@ID=$code]/label[lang('en')]/@text"/>
      <xsl:value-of select="translate($label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')"/>
    </mods:roleTerm>
  </xsl:template>

  <xsl:template match='mods:dateIssued' mode="copy-mods">
    <xsl:copy>
      <xsl:attribute name="keyDate">yes</xsl:attribute>
      <xsl:apply-templates select='@*|node()' mode="copy-mods"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match='mods:originInfo' mode="copy-mods">
    <xsl:copy>
      <xsl:apply-templates select='@*|node()' mode="copy-mods"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:identifier[(@type='duepublico') or (@type='duepublico2')]" mode="copy-mods">
    <xsl:if test="not(../mods:location)">
      <mods:location>
        <xsl:apply-templates select="." mode="duepublico.url"/>
      </mods:location>
    </xsl:if>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='duepublico']" mode="duepublico.url">
    <mods:url access="object in context">
      <xsl:text>https://duepublico.uni-due.de/servlets/DocumentServlet?id=</xsl:text>
      <xsl:value-of select="."/>
    </mods:url>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='duepublico2']" mode="duepublico.url">
    <mods:url access="object in context">
      <xsl:text>https://duepublico2.uni-due.de/receive/</xsl:text>
      <xsl:value-of select="."/>
    </mods:url>
  </xsl:template>

  <xsl:template match="mods:location[../mods:identifier[(@type='duepublico') or (@type='duepublico2')]]"
                mode="copy-mods">
    <xsl:copy>
      <xsl:apply-templates select="node()" mode="copy-mods"/>
      <xsl:apply-templates select="../mods:identifier[(@type='duepublico') or (@type='duepublico2')]"
                           mode="duepublico.url"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:classification[contains(@authorityURI,'fachreferate')]" mode="copy-mods">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:variable name="categoryID" select="substring-after(@valueURI,'#')"/>
      <xsl:variable name="uri" select="concat('classification:editor:0:parents:fachreferate:',$categoryID)"/>
      <xsl:value-of select="document($uri)/items/item/label[lang($CurrentLang)]"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:classification[contains(@authorityURI,'ORIGIN')]" mode="copy-mods">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:variable name="categoryID" select="substring-after(@valueURI,'#')"/>
      <xsl:call-template name="output.category">
        <xsl:with-param name="classID" select="'ORIGIN'"/>
        <xsl:with-param name="categID" select="$categoryID"/>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:extension/dedup" mode="copy-mods"/>

</xsl:stylesheet>
