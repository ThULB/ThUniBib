<?xml version="1.0" encoding="UTF-8"?>

<!-- ==============================================================================
 Übernahme einer E-Dissertation von db-thueringen.de zur Universitätsbibliographie
 =============================================================================== -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:mcrxml="xalan://org.mycore.common.xml.MCRXMLFunctions"
                exclude-result-prefixes="mcrxml mods xsl xalan xlink">

  <xsl:param name="CurrentLang"/>
  <xsl:param name="MCR.user2.IdentityManagement.UserCreation.Affiliation"/>
  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match="/mycoreobject">
    <xsl:apply-templates select="metadata/def.modsContainer/modsContainer/mods:mods"/>
  </xsl:template>

  <xsl:template match="mods:mods">
    <xsl:copy>
      <xsl:apply-templates select="/mycoreobject/@ID"/>
      <xsl:apply-templates select="mods:genre[contains(@authorityURI, 'mir_genres')][1]"/>
      <xsl:apply-templates
        select="mods:name[contains(@authorityURI, 'mir_institutes')][@valueURI][@type = 'corporate'][1]"/>
      <xsl:apply-templates select="mods:titleInfo"/>
      <xsl:apply-templates select="mods:name[@type='personal']"/>
      <xsl:apply-templates select="mods:originInfo[@eventType='publication']"/>
      <xsl:apply-templates select="mods:originInfo[@eventType='creation']"/>
      <xsl:apply-templates select="mods:identifier[@type='doi']"/>
      <xsl:copy-of select="mods:identifier[@type='urn']"/>
      <xsl:apply-templates select="mods:identifier[@type='uri'][contains(. ,'ppn')]"/>
      <xsl:copy-of select="mods:language"/>
      <xsl:apply-templates select="mods:subject"/>
      <xsl:apply-templates select="mods:abstract"/>
      <xsl:apply-templates select="mods:note"/>
      <xsl:apply-templates select="mods:relatedItem"/>
      <xsl:apply-templates select="mods:physicalDescription"/>
      <xsl:apply-templates select="mods:accessCondition"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mycoreobject/@ID">
    <mods:identifier type="dbt">
      <xsl:value-of select="."/>
    </mods:identifier>
  </xsl:template>

  <xsl:template match="mods:genre[contains(@authorityURI, 'mir_genres')]">
    <xsl:variable name="genre" select="substring-after(@valueURI, '#')"/>
    <!-- matches mods:genre in select-genre.xed -->
    <mods:genre type="intern">
      <xsl:value-of select="$genre"/>
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
      <xsl:apply-templates select="mods:genre[contains(@authorityURI, 'mir_genres')][1]"/>
      <!-- Open Access omitted -->
      <xsl:apply-templates select="mods:titleInfo"/>
      <!-- omitting "conference" -> mods:name[@type='conference']/mods:namePart, seemingly not in DBT data -->
      <xsl:apply-templates
        select="mods:name[@type='personal'][contains('aut ths rev',mods:role/mods:roleTerm)]"/> <!-- unclear if this is in DBT -->
      <xsl:apply-templates select="mods:part"/>
      <xsl:apply-templates select="mods:originInfo[@eventType='publication']"/>
      <xsl:apply-templates select="mods:originInfo[@eventType='creation']"/>

      <!-- only using doi, issn and urn but from DBT also come at least: zdbib, oclc and more-->
      <xsl:copy-of select="mods:identifier[@type='doi']"/>
      <xsl:copy-of select="mods:identifier[@type='issn']"/>
      <xsl:copy-of select="mods:identifier[@type='urn']"/>

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

  <xsl:template match="mods:name[contains(@authorityURI, 'mir_institutes')]">
    <xsl:variable name="categId" select="substring-after(@valueURI, '#')"/>
    <xsl:variable name="uri" select="concat($WebApplicationBaseURL, 'classifications/ORIGIN')"/>

    <xsl:choose>
      <!-- check for matching ids mir_institutes <=> ORIGIN, applies to Weimar only -->
      <xsl:when test="mcrxml:isCategoryID('ORIGIN', $categId)">
        <mods:classification authorityURI="{$uri}" valueURI="{$uri}#{$categId}"/>
      </xsl:when>
      <!-- try matching via string comparison -->
      <xsl:otherwise>
        <xsl:variable name="textFromDBT"
                      select="document(concat('notnull:', @valueURI))//category[@ID = $categId]/label[@xml:lang = $CurrentLang]/@text"/>
        <xsl:variable name="guessedOriginCategId"
                      select="document('notnull:classification:metadata:-1:children:ORIGIN')//category[@text = $textFromDBT]/@ID[1]"/>

        <xsl:if test="string-length($guessedOriginCategId) &gt; 0">
          <mods:classification authorityURI="{$uri}" valueURI="{$uri}#{$guessedOriginCategId}"/>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='uri'][contains(. , 'ppn')]">
    <mods:identifier type="uri">
      <!-- convert http:// uris to https:// uris -->
      <xsl:value-of select="concat('https://', substring-after(., '://'))"/>
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

      <xsl:for-each select="mods:nameIdentifier[@type]">
        <mods:nameIdentifier type="{@type}">
          <xsl:value-of select="."/>
        </mods:nameIdentifier>
      </xsl:for-each>
    </mods:name>
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
      <xsl:value-of
        select="concat('Dissertation, ', $MCR.user2.IdentityManagement.UserCreation.Affiliation, ', ', substring-before(.,'-'))"/>
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

  <xsl:template match="mods:physicalDescription">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="mods:note">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="mods:accessCondition[@type = 'use and reproduction']">
    <mods:accessCondition type="use and reproduction"
                          xlink:href="{$WebApplicationBaseURL}classifications/licenses#{substring-after(@xlink:href, '#')}"/>
  </xsl:template>
</xsl:stylesheet>
