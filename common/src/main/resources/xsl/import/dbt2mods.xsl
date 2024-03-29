<?xml version="1.0" encoding="ISO-8859-1"?>

<!-- ==================================================
 Übernahme einer E-Dissertation von DuEPublico zur Universitätsbibliographie
 ================================================== -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="xsl mods xlink">

  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match="/mycoreobject">
    <xsl:apply-templates select="metadata/def.modsContainer/modsContainer/mods:mods"/>
  </xsl:template>

  <xsl:template match="mods:mods">
    <xsl:copy>
      <xsl:apply-templates select="mods:genre[contains(@authorityURI,'mir_genres')][1]"/>
      <xsl:copy-of select="mods:classification[contains(@valueURI,'classifications/ORIGIN#')]"/>
      <xsl:apply-templates select="mods:name[contains(@authorityURI,'mir_institutes')]"/>
      <xsl:apply-templates select="mods:titleInfo"/>
      <xsl:apply-templates select="mods:name[@type='personal'][contains('aut ths rev',mods:role/mods:roleTerm)]"/>
      <xsl:apply-templates select="mods:originInfo[@eventType='publication']"/>
      <xsl:apply-templates select="mods:originInfo[@eventType='creation']"/>
      <xsl:apply-templates select="mods:identifier[@type='doi']"/>
      <xsl:copy-of select="mods:identifier[@type='urn']"/>
      <xsl:apply-templates select="/mycoreobject/@ID"/>
      <xsl:copy-of select="mods:language"/>
      <xsl:apply-templates select="mods:subject"/>
      <xsl:apply-templates select="mods:abstract"/>
      <xsl:apply-templates select="mods:relatedItem"/>
      <xsl:apply-templates select="mods:identifier[@type='uri'][contains(. ,'ppn')]"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mycoreobject/@ID">
    <mods:identifier type="dbt">
      <xsl:value-of select="."/>
    </mods:identifier>
  </xsl:template>

  <!-- for dbt -->
  <xsl:template match="mods:genre[contains(@authorityURI, 'mir_genres')]">
    <mods:genre type="intern">
      <xsl:variable name="genre" select="substring-after(@valueURI,'#')"/>
      <xsl:choose>
        <xsl:when test="$genre='article'">article</xsl:when>
        <xsl:when test="$genre='chapter'">chapter</xsl:when>
        <xsl:when test="$genre='entry'">entry</xsl:when>
        <xsl:when test="$genre='preface'">preface</xsl:when>
        <xsl:when test="$genre='speech'">speech</xsl:when>
        <xsl:when test="$genre='review'">review</xsl:when>
        <xsl:when test="$genre='thesis'">dissertation</xsl:when> <!-- ? -->
        <xsl:when test="$genre='exam'">dissertation</xsl:when> <!-- ? -->
        <xsl:when test="$genre='dissertation'">dissertation</xsl:when>
        <xsl:when test="$genre='habilitation'">dissertation</xsl:when> <!-- ? -->
        <xsl:when test="$genre='diploma_thesis'">dissertation</xsl:when> <!-- ? -->
        <xsl:when test="$genre='master_thesis'">dissertation</xsl:when> <!-- ? -->
        <xsl:when test="$genre='bachelor_thesis'">dissertation</xsl:when> <!-- ? -->
        <xsl:when test="$genre='student_research_project'">dissertation</xsl:when> <!-- ? -->
        <xsl:when test="$genre='magister_thesis'">dissertation</xsl:when> <!-- ? -->
        <xsl:when test="$genre='collection'">collection</xsl:when>
        <xsl:when test="$genre='festschrift'">festschrift</xsl:when>
        <xsl:when test="$genre='proceedings'">proceedings</xsl:when>
        <xsl:when test="$genre='lexicon'">lexicon</xsl:when>
        <xsl:when test="$genre='report'">article</xsl:when> <!-- ? -->
        <xsl:when test="$genre='research_results'">researchpaper</xsl:when> <!-- ? -->
        <xsl:when test="$genre='in_house'">workingpaper</xsl:when> <!-- ? -->
        <xsl:when test="$genre='press_release'">article</xsl:when> <!-- ? -->
        <xsl:when test="$genre='declaration'">article</xsl:when> <!-- ? -->
        <xsl:when test="$genre='teaching_material'">article</xsl:when> <!-- ? -->
        <xsl:when test="$genre='lecture_resource'">article</xsl:when> <!-- ? -->
        <xsl:when test="$genre='course_resources'">article</xsl:when> <!-- ? -->
        <xsl:when test="$genre='book'">book</xsl:when>
        <xsl:when test="$genre='journal'">journal</xsl:when>
        <xsl:when test="$genre='newspaper'">article</xsl:when> <!-- ? -->
        <xsl:when test="$genre='series'">series</xsl:when>
        <xsl:when test="$genre='interview'">interview</xsl:when>
        <xsl:when test="$genre='research_data'">researchpaper</xsl:when> <!-- ? -->
        <xsl:when test="$genre='patent'">researchpaper</xsl:when> <!-- ? -->
        <xsl:when test="$genre='poster'">poster</xsl:when>
        <xsl:when test="$genre='audio'">audio</xsl:when> <!-- ? to be filtered -->
        <xsl:when test="$genre='video'">video</xsl:when> <!-- ? to be filtered -->
        <xsl:when test="$genre='picture'">picture</xsl:when> <!-- ? to be filtered -->
        <xsl:when test="$genre='broadcasting'">broadcasting</xsl:when> <!-- ? to be filtered -->
        <xsl:when test="$genre='lecture'">article</xsl:when> <!-- ? -->
      </xsl:choose>
    </mods:genre>
  </xsl:template>

  <xsl:template match="mods:relatedItem">
    <mods:relatedItem>
      <xsl:if test="./@type">
        <xsl:attribute name="type">
          <xsl:value-of select="./@type"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="./@xlink:type">
        <xsl:attribute name="xlink:type">
          <xsl:value-of select="./@xlink:type"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="mods:genre[contains(@authorityURI,'mir_genres')][1]"/>
      <!-- Open Access omitted -->
      <xsl:apply-templates select="mods:titleInfo"/>
      <!-- omitting "conference" -> mods:name[@type='conference']/mods:namePart, seemingly not in DBT data -->
      <xsl:apply-templates
          select="mods:name[@type='personal'][contains('aut ths rev',mods:role/mods:roleTerm)]"/> <!-- unclear if this is in DBT -->
      <xsl:apply-templates select="mods:part"/>
      <xsl:apply-templates select="mods:originInfo[@eventType='publication']"/>
      <xsl:apply-templates select="mods:originInfo[@eventType='creation']"/>

      <!-- only using doi, issn and urn but from DBT also come at least: zdbib, oclc and more-->
      <xsl:apply-templates select="mods:identifier[@type='doi']"/>
      <xsl:apply-templates select="mods:identifier[@type='issn']"/>
      <xsl:apply-templates select="mods:identifier[@type='urn']"/>

      <!-- omitting mods:location with "Library shelfmark -> mods:shelfLocator" and "WWW URL -> mods:url -->
      <xsl:apply-templates select="mods:subject"/>
      <xsl:apply-templates select="mods:abstract"/>
    </mods:relatedItem>
  </xsl:template>

  <xsl:template match="mods:subject">
    <xsl:for-each select="./mods:topic">
      <mods:subject>
        <mods:topic>
          <xsl:value-of select="."/>
        </mods:topic>
      </mods:subject>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="mods:name[contains(@authorityURI,'mir_institutes')]">
    <xsl:variable name="id" select="substring-after(@valueURI, '#')"/>
    <xsl:variable name="uri" select="concat($WebApplicationBaseURL, 'classifications/ORIGIN')"/>
    <mods:classification authorityURI="{$uri}" valueURI="{$uri}#{$id}"/>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='uri'][contains(. ,'ppn')]">
    <mods:identifier type="ppn">
      <xsl:value-of select="text()"/>
    </mods:identifier>
  </xsl:template>

  <xsl:template match="mods:titleInfo">
    <mods:titleInfo>
      <xsl:if test="@type">
        <xsl:attribute name="type">
          <xsl:value-of select="@type"/>
        </xsl:attribute>
      </xsl:if>
      <mods:title> <!-- dozbib has no field mods:nonSort -->
        <xsl:apply-templates select="mods:nonSort"/>
        <xsl:value-of select="mods:title"/>
      </mods:title>
      <xsl:copy-of select="mods:subTitle"/>
    </mods:titleInfo>
  </xsl:template>

  <xsl:template match="mods:nonSort">
    <xsl:value-of select="text()"/>
    <xsl:text> </xsl:text>
  </xsl:template>

  <xsl:template match="mods:name">
    <mods:name>
      <xsl:copy-of select="@type"/>
      <xsl:copy-of select="mods:role"/>
      <xsl:copy-of select="mods:namePart"/>
      <xsl:apply-templates select="@valueURI"/>
      <xsl:copy-of select="mods:nameIdentifier[@type='lsf']"/>
      <xsl:copy-of select="mods:nameIdentifier[@type='gnd']"/>
      <xsl:copy-of select="mods:nameIdentifier[@type='orcid']"/>
    </mods:name>
  </xsl:template>

  <!-- get LSF PID via legalEntityID -->
  <xsl:template match="mods:name/@valueURI">
    <xsl:variable name="legalEntityID" select="substring-after(.,'#')"/>
    <xsl:for-each select="document(concat('notnull:legalEntity:',$legalEntityID))/legalEntity/@pid">
      <mods:nameIdentifier type="lsf">
        <xsl:value-of select="."/>
      </mods:nameIdentifier>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="mods:originInfo[@eventType='publication']">
    <mods:originInfo>
      <mods:place>
        <mods:placeTerm type="text">Jena</mods:placeTerm>
      </mods:place>
      <xsl:copy-of select="mods:dateIssued"/>
    </mods:originInfo>
  </xsl:template>

  <xsl:template match="mods:originInfo[@eventType='creation']">
    <xsl:apply-templates select="mods:dateOther[@type='accepted']"/>
  </xsl:template>

  <xsl:template match="mods:dateOther">
    <mods:note>
      <xsl:text>Dissertation, Friedrich-Schiller-Universitaet Jena, </xsl:text>
      <xsl:value-of select="substring-before(.,'-')"/>
    </mods:note>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='doi']">
    <xsl:copy>
      <xsl:copy-of select="@type"/>
      <xsl:choose>
        <!-- legacy miless DOI entry starts with "doi:" -->
        <xsl:when test="starts-with(.,'doi:')">
          <xsl:value-of select="substring-after(.,'doi:')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:abstract">
    <xsl:if test="string-length(text()) &gt; 0">
      <xsl:copy>
        <xsl:copy-of select="@xml:lang"/>
        <xsl:copy-of select="text()"/>
      </xsl:copy>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
