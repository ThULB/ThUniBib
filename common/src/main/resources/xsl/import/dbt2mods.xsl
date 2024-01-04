<?xml version="1.0" encoding="UTF-8"?>

<!-- ==============================================================================
 Übernahme einer E-Dissertation von db-thueringen.de zur Universitätsbibliographie
 =============================================================================== -->

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
    <xsl:variable name="genre" select="substring-after(@valueURI,'#')"/>
    <mods:genre type="intern" authorityURI="{$WebApplicationBaseURL}classifications/ubogenre">
      <xsl:attribute name="valueURI">
        <xsl:choose>
          <xsl:when test="$genre='article'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#article')"/></xsl:when>
          <xsl:when test="$genre='chapter'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#chapter')"/></xsl:when>
          <xsl:when test="$genre='entry'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#entry')"/></xsl:when>
          <xsl:when test="$genre='preface'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#preface')"/></xsl:when>
          <xsl:when test="$genre='speech'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#speech')"/></xsl:when>
          <xsl:when test="$genre='review'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#review')"/></xsl:when>
          <xsl:when test="$genre='thesis'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#dissertation')"/></xsl:when> <!-- ? -->
          <xsl:when test="$genre='exam'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#dissertation')"/></xsl:when> <!-- ? -->
          <xsl:when test="$genre='dissertation'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#dissertation')"/></xsl:when>
          <xsl:when test="$genre='habilitation'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#dissertation')"/></xsl:when> <!-- ? -->
          <xsl:when test="$genre='diploma_thesis'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#dissertation')"/></xsl:when> <!-- ? -->
          <xsl:when test="$genre='master_thesis'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#dissertation')"/></xsl:when> <!-- ? -->
          <xsl:when test="$genre='bachelor_thesis'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#dissertation')"/></xsl:when> <!-- ? -->
          <xsl:when test="$genre='student_research_project'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#dissertation')"/></xsl:when> <!-- ? -->
          <xsl:when test="$genre='magister_thesis'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#dissertation')"/></xsl:when> <!-- ? -->
          <xsl:when test="$genre='collection'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#collection')"/></xsl:when>
          <xsl:when test="$genre='festschrift'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#festschrift')"/></xsl:when>
          <xsl:when test="$genre='proceedings'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#proceedings')"/></xsl:when>
          <xsl:when test="$genre='lexicon'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#lexicon')"/></xsl:when>
          <xsl:when test="$genre='report'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#article')"/></xsl:when> <!-- ? -->
          <xsl:when test="$genre='research_results'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#researchpaper')"/></xsl:when> <!-- ? -->
          <xsl:when test="$genre='in_house'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#workingpaper')"/></xsl:when> <!-- ? -->
          <xsl:when test="$genre='press_release'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#article')"/></xsl:when> <!-- ? -->
          <xsl:when test="$genre='declaration'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#article')"/></xsl:when> <!-- ? -->
          <xsl:when test="$genre='teaching_material'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#article')"/></xsl:when> <!-- ? -->
          <xsl:when test="$genre='lecture_resource'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#article')"/></xsl:when> <!-- ? -->
          <xsl:when test="$genre='course_resources'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#article')"/></xsl:when> <!-- ? -->
          <xsl:when test="$genre='book'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#book')"/></xsl:when>
          <xsl:when test="$genre='journal'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#journal')"/></xsl:when>
          <xsl:when test="$genre='newspaper'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#article')"/></xsl:when> <!-- ? -->
          <xsl:when test="$genre='series'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#series')"/></xsl:when>
          <xsl:when test="$genre='interview'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#interview')"/></xsl:when>
          <xsl:when test="$genre='research_data'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#researchpaper')"/></xsl:when> <!-- ? -->
          <xsl:when test="$genre='patent'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#researchpaper')"/></xsl:when> <!-- ? -->
          <xsl:when test="$genre='poster'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#poster')"/></xsl:when>
          <xsl:when test="$genre='audio'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#audio')"/></xsl:when> <!-- ? to be filtered -->
          <xsl:when test="$genre='video'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#video')"/></xsl:when> <!-- ? to be filtered -->
          <xsl:when test="$genre='picture'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#picture')"/></xsl:when> <!-- ? to be filtered -->
          <xsl:when test="$genre='broadcasting'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#broadcasting')"/></xsl:when> <!-- ? to be filtered -->
          <xsl:when test="$genre='lecture'"><xsl:value-of select="concat($WebApplicationBaseURL, 'classifications/ubogenre#article')"/></xsl:when> <!-- ? -->
        </xsl:choose>
      </xsl:attribute>
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
