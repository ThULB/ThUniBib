<?xml version="1.0" encoding="UTF-8"?>
<!-- Transforms MyCoRe object with MODS to ORCID works XML schema -->
<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:work="http://www.orcid.org/ns/work"
                xmlns:mcrstring="http://www.mycore.de/xslt/stringutils"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                exclude-result-prefixes="mcrstring fn mods xsl">

  <xsl:import href="resource:xslt/orcid2/v3/mcr2work_generic.xsl"/>

  <xsl:param name="MCR.ORCID2.Genre.Mapping.Default.Genre" select="'other'"/>

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

  <!-- prefer the mods:abstract in the same language as the mods:titleElement-->
  <xsl:template match="mods:abstract[@xml:lang = ../mods:titleInfo/@xml:lang][1][fn:string-length(text()) &gt; 0]">
    <work:short-description>
      <xsl:value-of select="mcrstring:shorten(text(), ($short-description-max-length - 1), '…')"/>
    </work:short-description>
  </xsl:template>

  <!-- transform only first abstract, when there is no abstract having the same language as the mods:titleInfo element -->
  <xsl:template match="mods:abstract[fn:string-length(text()) &gt; 0][position() = 1][not(..//mods:abstract/@xml:lang =..//mods:titleInfo/@xml:lang)]">
      <work:short-description>
        <xsl:value-of select="mcrstring:shorten(text(), ($short-description-max-length - 1), '…')"/>
      </work:short-description>
  </xsl:template>

  <!-- discard all other abstracts -->
  <xsl:template match="mods:abstract"/>

  <xsl:template name="workCitation">
    <xsl:variable name="mcr-object-id" select="//mycoreobject/@ID"/>

    <xsl:if test="document(concat('notnull:callJava:org.mycore.common.xml.MCRXMLFunctions:exists:',  $mcr-object-id))/string = 'true'">
      <work:citation>
        <work:citation-type>
          <xsl:value-of select="'bibtex'"/>
        </work:citation-type>

        <work:citation-value>
          <xsl:value-of select="document(concat('notnull:toString:xslTransform:bibtex:mcrobject:', $mcr-object-id))/str/text()"/>
        </work:citation-value>
      </work:citation>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
