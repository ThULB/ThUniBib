<?xml version="1.0" encoding="UTF-8"?>
<!-- Transforms MyCoRe object with MODS to ORCID works XML schema -->
<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:work="http://www.orcid.org/ns/work"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                exclude-result-prefixes="fn mods xsl">

  <xsl:import href="resource:xsl/orcid2/v3/mcr2work_generic.xsl"/>

  <xsl:param name="MCR.ORCID2.Genre.Mapping.Default.Genre" select="'journal-article'"/>

  <xsl:template name="workType">
    <xsl:choose>
      <xsl:when test="mods:classification[@generator = 'ubogenre2orcidWorkType-mycore'][@valueURI][@authorityURI]">
        <work:type>
          <xsl:value-of
            select="fn:substring-after(mods:classification[@generator = 'ubogenre2orcidWorkType-mycore']/@valueURI, '#')"/>
        </work:type>
      </xsl:when>
      <xsl:otherwise>
        <work:type>
          <xsl:value-of select="$MCR.ORCID2.Genre.Mapping.Default.Genre"/>
        </work:type>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="journal-title">
    <xsl:if test="mods:relatedItem[@type='host']/mods:titleInfo">
      <work:journal-title>
        <xsl:if test="mods:relatedItem/mods:titleInfo/mods:nonSort">
          <xsl:value-of select="mods:relatedItem/mods:titleInfo/mods:nonSort/text()"/>
          <xsl:text> </xsl:text>
        </xsl:if>
        <xsl:value-of select="mods:relatedItem/mods:titleInfo/mods:title/text()"/>
        <xsl:if test="mods:relatedItem/mods:titleInfo/mods:subTitle">
          <xsl:text>: </xsl:text>
          <xsl:value-of select="mods:relatedItem/mods:titleInfo/mods:subTitle/text()"/>
        </xsl:if>
      </work:journal-title>
    </xsl:if>
  </xsl:template>
  <xsl:template name="workCitation"/>
</xsl:stylesheet>
