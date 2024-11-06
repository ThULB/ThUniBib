<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:mods="http://www.loc.gov/mods/v3"
                exclude-result-prefixes="xsl">

  <xsl:template name="copy-and-apply"> <!-- copy supported elements and attributes calling this template -->
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*|@*"/> <!-- ignore all other unsupported elements and attributes -->

  <xsl:template match="mycoreobject"> <!-- copy also mycore object structure if given -->
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="structure|metadata|service"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="structure">
    <xsl:copy>
      <xsl:copy-of select="*|@*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="metadata|def.modsContainer|modsContainer">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="service">
    <xsl:copy>
      <xsl:copy-of select="*|@*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:mods">
    <xsl:copy>
      <xsl:apply-templates
          select="mods:genre|mods:accessCondition|mods:titleInfo|mods:typeOfResource|mods:name|mods:classification|mods:originInfo|mods:dateIssued|mods:physicalDescription|mods:identifier|mods:relatedItem|mods:note|mods:extension|mods:location|mods:subject|mods:abstract|mods:language"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template
      match="mods:titleInfo|mods:titleInfo/@type|mods:titleInfo/@xml:lang|mods:title|mods:subTitle|mods:nonSort">
    <xsl:call-template name="copy-and-apply"/>
  </xsl:template>

  <xsl:template match="mods:genre[@type='intern'][1]|mods:genre/@type|mods:genre/@authorityURI|mods:genre/@valueURI">
    <xsl:call-template name="copy-and-apply"/>
  </xsl:template>

  <xsl:template
      match="mods:classification[contains(@valueURI,'/classifications/') and contains(@authorityURI,'/classifications/')]|mods:classification/@valueURI|mods:classification/@authorityURI">
    <xsl:call-template name="copy-and-apply"/>
  </xsl:template>

  <xsl:template match="mods:classification/text()"/>

  <xsl:template
      match="mods:name[@type='personal']|mods:name[@type='corporate']|mods:name[@type='conference']|mods:name/@type">
    <xsl:call-template name="copy-and-apply"/>
  </xsl:template>

  <xsl:template
      match="mods:name[@type='personal']/mods:namePart[@type='family'][1]|mods:name[@type='personal']/mods:namePart[@type='given'][1]|mods:name[@type='corporate']/mods:namePart[not(@type)]|mods:name[@type='conference']/mods:namePart[not(@type)]|mods:namePart/@type">
    <xsl:call-template name="copy-and-apply"/>
  </xsl:template>

  <xsl:template
      match="mods:nameIdentifier[contains('|lsf|orcid|researcherid|gnd|scopus|',concat('|',@type,'|'))]|mods:nameIdentifier/@type">
    <xsl:call-template name="copy-and-apply"/>
  </xsl:template>

  <xsl:template
      match="mods:role[1]|mods:roleTerm[@type='code'][@authority='marcrelator'][1]|mods:role[1]|mods:roleTerm[@type='text']|mods:role[mods:roleTerm/text() = 'corresponding_author']|mods:roleTerm/@valueURI|mods:roleTerm/@authorityURI|mods:roleTerm/@type|mods:roleTerm/@authority">
    <xsl:call-template name="copy-and-apply"/>
  </xsl:template>

  <xsl:template match="mods:affiliation">
    <xsl:call-template name="copy-and-apply"/>
  </xsl:template>

<xsl:template match="mods:originInfo[1]|mods:edition[1]|mods:place[1]|mods:placeTerm[@type='text'][1]|mods:placeTerm/@type">
  <xsl:call-template name="copy-and-apply" />
</xsl:template>

<xsl:template match="mods:publisher[1]">
  <xsl:choose>
    <xsl:when test="//mods:mods/mods:genre[contains(@valueURI, 'article')] and not(./ancestor::mods:relatedItem) and //mods:relatedItem//mods:publisher"/>
    <xsl:otherwise>
      <xsl:call-template name="copy-and-apply"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

  <xsl:template match="mods:dateIssued[@encoding='w3cdtf'][1]|mods:dateIssued/@encoding">
    <xsl:call-template name="copy-and-apply"/>
  </xsl:template>

  <xsl:template
      match="mods:part[1]|mods:detail[@type='volume'][1]|mods:detail[@type='issue'][1]|mods:detail[@type='article_number'][1]|mods:detail/@type|mods:number[1]">
    <xsl:call-template name="copy-and-apply"/>
  </xsl:template>

  <xsl:template match="mods:part/mods:extent[@unit='pages'][1]|mods:part/mods:extent/@unit">
    <xsl:call-template name="copy-and-apply"/>
  </xsl:template>

  <xsl:template
      match="mods:physicalDescription[1]|mods:physicalDescription/mods:extent[1]|mods:start[1]|mods:end[1]|mods:list[1]|mods:total[1]">
    <xsl:call-template name="copy-and-apply"/>
  </xsl:template>

  <xsl:template match="mods:identifier[contains('isbn issn doi hdl urn pubmed ieee arxiv hdl zdb isi evaluna hbz mms scopus duepublico duepublico2 dbt uri', @type)]|mods:identifier/@type">
    <xsl:call-template name="copy-and-apply"/>
  </xsl:template>

  <xsl:template match="mods:identifier[@type='url'][not(//mods:identifier[@type = 'doi'])]">
    <mods:location>
      <xsl:for-each select=".">
        <mods:url>
          <xsl:value-of select="."/>
        </mods:url>
      </xsl:for-each>
    </mods:location>
  </xsl:template>

  <xsl:template match="mods:relatedItem[@type='host'][1]|mods:relatedItem[@type='series']">
    <xsl:copy>
      <xsl:copy-of select="@type"/>
      <xsl:apply-templates
          select="mods:genre|mods:accessCondition|mods:titleInfo|mods:name|mods:classification|mods:originInfo|mods:dateIssued|mods:identifier|mods:relatedItem|mods:location|mods:part"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:typeOfResource">
    <xsl:call-template name="copy-and-apply"/>
  </xsl:template>

<xsl:template match="mods:note[not(contains(text(), 'Scopus-Subject:'))]">
  <xsl:if test="not(@type) or contains('intern source note other university_thesis_note', @type)">
    <xsl:call-template name="copy-and-apply" />
  </xsl:if>
</xsl:template>

  <xsl:template match="mods:extension[tag|dedup][1]|mods:extension/tag|mods:extension/dedup">
    <xsl:call-template name="copy-and-apply"/>
  </xsl:template>

  <!-- no shelflocator supported in thunibib -->
  <xsl:template match="mods:location|mods:location[not(//mods:identifier[@type = 'doi'])][//mods:classification[contains(@valueURI, 'closed')] or not(//mods:classification[contains(@authorityURI, 'classifications/oa')])]/mods:url">
    <xsl:call-template name="copy-and-apply"/>
  </xsl:template>

  <!-- template keeps order of multiple mods:topic within a mods:subject -->
  <xsl:template match="mods:subject">
    <mods:subject>
      <xsl:for-each select="mods:topic">
        <xsl:copy-of select="."/>
      </xsl:for-each>
    </mods:subject>
  </xsl:template>

  <xsl:template
      match="mods:language|mods:languageTerm[@type='code'][@authority='rfc5646']|mods:languageTerm/@type|mods:languageTerm/@authority">
    <xsl:call-template name="copy-and-apply"/>
  </xsl:template>

  <xsl:template match="mods:abstract|mods:abstract/@xml:lang|mods:abstract/@xlink:href">
    <xsl:call-template name="copy-and-apply"/>
  </xsl:template>

  <xsl:template
    match="mods:accessCondition|mods:accessCondition[@type='use and reproduction']|mods:accessCondition/@type|mods:accessCondition/@xlink:href[contains(.,'classifications/licenses')]|mods:accessCondition/@xlink:type">
    <xsl:call-template name="copy-and-apply"/>
  </xsl:template>

</xsl:stylesheet>
