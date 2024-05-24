<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:mods="http://www.loc.gov/mods/v3"
  exclude-result-prefixes="xsl mods">

  <xsl:include href="copynodes.xsl" />

  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:template match="mods:mods|mods:relatedItem">
    <xsl:copy>
      <xsl:apply-templates select="@*" />
      <xsl:call-template name="genres2genreIntern" />
      <xsl:apply-templates select="*[not(local-name()='genre')]" />
    </xsl:copy>
  </xsl:template>

  <xsl:template name="genres2genreIntern">
    <xsl:choose>
      <xsl:when test="mods:genre[@type='intern']">
        <mods:genre type="intern">
          <xsl:copy-of select="mods:genre[@type='intern'][1]"/>
        </mods:genre>
      </xsl:when>
      <xsl:otherwise>

        <xsl:variable name="mappedGenreIDs">
          <xsl:apply-templates select="mods:genre"/>
          <xsl:text> </xsl:text>
        </xsl:variable>

        <xsl:variable name="firstGenre" select="normalize-space(substring-before($mappedGenreIDs,' '))"/>

        <mods:genre type="intern" authorityURI="{concat($WebApplicationBaseURL,'classifications/ubogenre')}">
          <xsl:attribute name="valueURI">
            <xsl:choose>
              <xsl:when test="string-length($firstGenre) &gt; 0">
                <xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#', $firstGenre)"/>
              </xsl:when>
              <xsl:when test="@type='series'">
                <xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#series')"/>
              </xsl:when>
              <xsl:when test="mods:identifier[@type='issn']">
                <xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#journal')"/>
              </xsl:when>
              <xsl:when test="mods:identifier[@type='isbn']">
                <xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#collection')"/>
              </xsl:when>
              <xsl:when test="local-name()='relatedItem'">
                <xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#journal')"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="concat($WebApplicationBaseURL,'classifications/ubogenre#article')"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
        </mods:genre>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="mods:mods/mods:genre">
    <xsl:variable name="genre" select="normalize-space(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'))" />
    <xsl:choose>
      <xsl:when test="$genre='trade journal'">journal</xsl:when>
      <xsl:when test="$genre='journal'">journal</xsl:when>
      <xsl:when test="$genre='journals'">article</xsl:when>
      <xsl:when test="$genre='journal article'">article</xsl:when>
      <xsl:when test="$genre='journal-article'">article</xsl:when>
      <xsl:when test="$genre='abstract report'">others</xsl:when>
      <xsl:when test="$genre='article'">article</xsl:when>
      <xsl:when test="$genre='article in press'">article</xsl:when>
      <xsl:when test="$genre='business article'">article</xsl:when>
      <xsl:when test="$genre='book'">book</xsl:when>
      <xsl:when test="$genre='book review'">review</xsl:when>
      <xsl:when test="$genre='monograph'">book</xsl:when>
      <xsl:when test="$genre='book series'">series</xsl:when>
      <xsl:when test="$genre='conferences'">chapter</xsl:when>
      <xsl:when test="$genre='conference paper'">conference_essay</xsl:when>
      <xsl:when test="$genre='conference abstract'">abstract</xsl:when>
      <xsl:when test="$genre='conference review'">proceedings</xsl:when>
      <xsl:when test="$genre='conference proceedings'">proceedings</xsl:when>
      <xsl:when test="$genre='editorial'">preface</xsl:when>
      <xsl:when test="$genre='dissertation'">dissertation</xsl:when>
      <xsl:when test="$genre='working paper'">workingpaper</xsl:when>
      <xsl:when test="$genre='review'">review_article</xsl:when>
      <xsl:when test="$genre='report'">article</xsl:when>
      <xsl:when test="$genre='short survey'">article</xsl:when>
      <xsl:when test="$genre='book chapter'">chapter</xsl:when>
      <xsl:when test="$genre='book-chapter'">chapter</xsl:when>
      <xsl:when test="$genre='book-part'">chapter</xsl:when>
      <xsl:when test="$genre='letter'">others</xsl:when>
      <xsl:when test="$genre='note'">others</xsl:when>
      <xsl:when test="$genre='short survey'">article</xsl:when>
      <xsl:when test="$genre='erratum'">others</xsl:when>
      <xsl:when test="$genre='patent'">patent</xsl:when>
      <xsl:when test="$genre='press release'">others</xsl:when>
      <xsl:when test="$genre='biography'">interview</xsl:when>  
      <xsl:when test="$genre='case reports'">article</xsl:when>
      <xsl:when test="$genre='classical article'">article</xsl:when>  
      <xsl:when test="$genre='clinical study'">article</xsl:when>
      <xsl:when test="$genre='clinical trial'">article</xsl:when>  
      <xsl:when test="$genre='comparative study'">article</xsl:when>  
      <xsl:when test="$genre='controlled clinical trial'">article</xsl:when>
      <xsl:when test="$genre='corrected and republished article'">article</xsl:when>  
      <xsl:when test="$genre='evaluation studies'">article</xsl:when>  
      <xsl:when test="$genre='festschrift'">festschrift</xsl:when>   
      <xsl:when test="$genre='historical article'">article</xsl:when>    
      <xsl:when test="$genre='interview'">interview</xsl:when>
      <xsl:when test="$genre='introductory journal article'">preface</xsl:when>  
      <xsl:when test="$genre='lectures'">speech</xsl:when>  
      <xsl:when test="$genre='multicenter study'">article</xsl:when>  
      <xsl:when test="$genre='newspaper article'">article</xsl:when>  
      <xsl:when test="$genre='observational study'">article</xsl:when> 
      <xsl:when test="$genre='published erratum'">article</xsl:when>
      <xsl:when test="$genre='randomized controlled trial'">article</xsl:when> 
      <xsl:when test="$genre='study characteristics'">article</xsl:when>
      <xsl:when test="$genre='technical report'">article</xsl:when>
      <xsl:when test="$genre='conference'">chapter</xsl:when>
      <xsl:when test="$genre='incollection'">chapter</xsl:when>
      <xsl:when test="$genre='inproceedings'">chapter</xsl:when>
      <xsl:when test="$genre='inbook'">chapter</xsl:when>
      <xsl:when test="$genre='phdthesis'">dissertation</xsl:when>
      <xsl:when test="$genre='booklet'">book</xsl:when>
      <xsl:when test="$genre='proceedings'">proceedings</xsl:when>
      <xsl:when test="$genre='manual'">book</xsl:when>
      <xsl:when test="$genre='techreport'">book</xsl:when>
      <xsl:when test="$genre='data paper'">research_data</xsl:when>
      <xsl:when test="$genre='misc'">others</xsl:when>
      <xsl:when test="$genre='unpublished'">others</xsl:when>
      <xsl:when test="$genre='other'">others</xsl:when> 
    </xsl:choose>
    <xsl:text> </xsl:text>
  </xsl:template>

  <xsl:template match="mods:relatedItem[@type='host']/mods:genre">
    <xsl:variable name="child_genre" select="normalize-space(translate(../../mods:genre[1],'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'))" />
    <xsl:variable name="genre" select="normalize-space(translate(text(),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'))" />
    <xsl:choose>
      <xsl:when test="$child_genre='conference'">proceedings</xsl:when>
      <xsl:when test="$child_genre='conferences'">proceedings</xsl:when>
      <xsl:when test="$child_genre='incollection'">collection</xsl:when>
      <xsl:when test="$child_genre='inproceedings'">proceedings</xsl:when>
      <xsl:when test="$child_genre='inbook'">collection</xsl:when>
      <xsl:when test="$child_genre='edited-book'">collection</xsl:when>
      <xsl:when test="$child_genre='article'">journal</xsl:when>
      <xsl:when test="$child_genre='journal-article'">journal</xsl:when>
      <xsl:when test="$child_genre='chapter'">collection</xsl:when>
      <xsl:when test="$child_genre='book chapter'">collection</xsl:when>
      <xsl:when test="$child_genre='book-chapter'">collection</xsl:when>
      <xsl:when test="$child_genre='book-part'">collection</xsl:when>
      <xsl:when test="$child_genre='techreport'">collection</xsl:when>
      <xsl:when test="$child_genre='misc'">collection</xsl:when>
      <xsl:when test="$child_genre='unpublished'">collection</xsl:when>
      <xsl:when test="$child_genre='other'">collection</xsl:when> 
      <xsl:when test="$genre='trade journal'">journal</xsl:when>
      <xsl:when test="$genre='journal'">journal</xsl:when>
      <xsl:when test="$genre='journals'">journal</xsl:when>
      <xsl:when test="$genre='book'">book</xsl:when>
      <xsl:when test="$genre='monograph'">book</xsl:when>
      <xsl:when test="$genre='book series'">series</xsl:when>
      <xsl:when test="$genre='conferences'">proceedings</xsl:when>
      <xsl:when test="$genre='festschrift'">festschrift</xsl:when>   
      <xsl:when test="$genre='conference'">proceedings</xsl:when>
      <xsl:when test="$genre='collection'">collection</xsl:when>
      <xsl:when test="$genre='proceedings'">proceedings</xsl:when>
      <xsl:when test="$genre='booklet'">book</xsl:when>
      <xsl:when test="$genre='manual'">book</xsl:when>
      <xsl:when test="$genre='multi-volume reference works'">collection</xsl:when>
      <xsl:when test="$genre='newsletter'">journal</xsl:when>
      <xsl:when test="$genre='newspaper'">newspaper</xsl:when>
    </xsl:choose>
    <xsl:text> </xsl:text>
  </xsl:template>

  <xsl:template match="mods:relatedItem[@type='series']/mods:genre">series</xsl:template>

</xsl:stylesheet>
