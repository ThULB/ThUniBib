<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:work="http://www.orcid.org/ns/work"
                xmlns:common="http://www.orcid.org/ns/common"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                exclude-result-prefixes="fn xsl">

  <xsl:import href="resource:xsl/orcid2/v3/work2mcr_generic.xsl"/>

  <xsl:param name="WebApplicationBaseURL"/>
  <xsl:param name="MCR.ORCID2.Genre.Mapping.Classification" select="'ubogenre'"/>
  <xsl:param name="MCR.ORCID2.Genre.Mapping.Classification.Prefix" select="'orcidWorkType'"/>
  <xsl:param name="MCR.ORCID2.Genre.Mapping.Default.Genre" select="'article'"/>
  <xsl:param name="MCR.ORCID2.Genre.Mapping.Standalone.xHost" select="'standalone'"/>

  <xsl:variable name="mapping-classification"
                select="fn:document(fn:concat('notnull:classification:metadata:-1:children:', $MCR.ORCID2.Genre.Mapping.Classification))"/>

  <!-- ORCID.org work types to MODS genre mapping -->
  <xsl:template match="work:type">
    <xsl:variable name="orcid-work-type" select="text()"/>
    <xsl:variable name="x-mapping"
                  select="fn:concat($MCR.ORCID2.Genre.Mapping.Classification.Prefix, ':', $orcid-work-type)"/>
    <xsl:variable name="genre-mapped-from-classification"
                  select="$mapping-classification//category[1][label[@xml:lang='x-mapping' and contains(@text, $x-mapping)]][not(descendant::category[label[@xml:lang='x-mapping' and contains(@text, $x-mapping)]])]/@ID"/>

    <xsl:variable name="genre">
      <xsl:choose>
        <xsl:when test="fn:string-length($genre-mapped-from-classification) &gt; 0">
          <xsl:value-of select="$genre-mapped-from-classification"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$MCR.ORCID2.Genre.Mapping.Default.Genre"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:variable name="host">
      <xsl:choose>
        <xsl:when test="$MCR.ORCID2.Genre.Mapping.Classification and $mapping-classification">
          <xsl:call-template name="determine-x-host">
            <xsl:with-param name="orcid-work-type" select="$orcid-work-type"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="'journal'"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:if test="string-length($genre) &gt; 0">
      <mods:genre type="intern"
                  authorityURI="{$WebApplicationBaseURL}classifications/ubogenre"
                  valueURI="{$WebApplicationBaseURL}classifications/ubogenre#{$genre}">
        <xsl:value-of select="$genre"/>
      </mods:genre>
    </xsl:if>

    <xsl:if test="string-length($host) &gt; 0">
      <mods:relatedItem type="host">
        <mods:genre type="intern">
          <xsl:value-of select="$host"/>
        </mods:genre>
        <xsl:apply-templates select="../work:journal-title"/>
        <xsl:apply-templates
          select="../common:external-ids/common:external-id[common:external-id-relationship='part-of']"/>
      </mods:relatedItem>
    </xsl:if>
  </xsl:template>

  <xsl:template name="determine-x-host">
    <xsl:param name="orcid-work-type"/>
    <xsl:variable name="lookup"
                  select="fn:concat($MCR.ORCID2.Genre.Mapping.Classification.Prefix, ':', $orcid-work-type)"/>

    <xsl:variable name="x-hosts"
                  select="$mapping-classification//category[1][label[@xml:lang = 'x-mapping'][contains(@text, $lookup)]][not(descendant::category[label[@xml:lang = 'x-mapping'][contains(@text, $lookup)]])]/label[@xml:lang='x-hosts']/@text"/>

    <xsl:if test="$x-hosts">
      <xsl:variable name="mapped-host">
        <xsl:choose>
          <!-- @x-hosts contains multiple entries separated by blanks: take the first one -->
          <xsl:when test="fn:contains($x-hosts, ' ')">
            <xsl:value-of select="fn:substring-before($x-hosts, ' ')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$x-hosts"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:if test="not($mapped-host = $MCR.ORCID2.Genre.Mapping.Standalone.xHost)">
        <xsl:value-of select="$mapped-host"/>
      </xsl:if>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
