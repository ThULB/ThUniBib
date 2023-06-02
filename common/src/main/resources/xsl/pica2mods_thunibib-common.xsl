<?xml version="1.0"?>
<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="info:srw/schema/5/picaXML-v1.0"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:zs="http://www.loc.gov/zing/srw/"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                exclude-result-prefixes="xsl p xlink zs">
  <xsl:mode on-no-match="shallow-copy"/>

  <!-- copied from pics2mods and changed rfc5646 to rfc4646 -->
  <xsl:template name="ubomodsLanguage">
    <xsl:variable name="rfc5646" select="document('resource:mycore-classifications/rfc5646.xml')" />

    <xsl:for-each select="./p:datafield[@tag='010@']"> <!-- 1500 Language -->
      <!-- weiter Unterfelder für Originaltext / Zwischenübersetzung nicht abbildbar -->
      <xsl:for-each select="./p:subfield[@code='a']">
        <mods:language>
          <xsl:variable name="l" select="."/>
          <xsl:choose>
            <xsl:when test="$rfc5646/mycoreclass/categories//category[label[@xml:lang='x-bibl']/@text=$l]">
              <mods:languageTerm type="code" authority="rfc4646">
                <xsl:value-of
                    select="$rfc5646/mycoreclass/categories//category[label[@xml:lang='x-bibl']/@text=$l]/@ID"/>
              </mods:languageTerm>
            </xsl:when>
            <xsl:otherwise>
              <xsl:comment>unknown language code</xsl:comment>
              <mods:languageTerm type="code">
                <xsl:value-of select="."/>
              </mods:languageTerm>
            </xsl:otherwise>
          </xsl:choose>
        </mods:language>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="uboTypeOfResource">
    <xsl:variable name="resourceType" select="p:datafield[@tag='002C']/p:subfield[@code='b']"/>
    <mods:typeOfResource>
      <xsl:choose>
        <xsl:when test="$resourceType=cod">dat</xsl:when>
        <xsl:when test="$resourceType=cop">mul</xsl:when>
        <xsl:when test="$resourceType=crd">car</xsl:when>
        <xsl:when test="$resourceType=crf">car</xsl:when>
        <xsl:when test="$resourceType=cri">car</xsl:when>
        <xsl:when test="$resourceType=crm">car</xsl:when>
        <xsl:when test="$resourceType=crn">car</xsl:when>
        <xsl:when test="$resourceType=crt">car</xsl:when>
        <xsl:when test="$resourceType=ntm">not</xsl:when>
        <xsl:when test="$resourceType=ntv">txt</xsl:when>
        <xsl:when test="$resourceType=prm">aud</xsl:when>
        <xsl:when test="$resourceType=snd">aud</xsl:when>
        <xsl:when test="$resourceType=spw">aud</xsl:when>
        <xsl:when test="$resourceType=sti">img</xsl:when>
        <xsl:when test="$resourceType=tcf">tac</xsl:when>
        <xsl:when test="$resourceType=tci">tac</xsl:when>
        <xsl:when test="$resourceType=tcm">tac</xsl:when>
        <xsl:when test="$resourceType=tcn">tac</xsl:when>
        <xsl:when test="$resourceType=tct">tac</xsl:when>
        <xsl:when test="$resourceType=tdf">art</xsl:when>
        <xsl:when test="$resourceType=tdi">mov</xsl:when>
        <xsl:when test="$resourceType=tdm">mov</xsl:when>
        <xsl:when test="$resourceType=txt">txt</xsl:when>
        <xsl:when test="$resourceType=xxx">unk</xsl:when>
        <xsl:when test="$resourceType=zzz">unk</xsl:when>
        <xsl:otherwise>txt</xsl:otherwise>
      </xsl:choose>
    </mods:typeOfResource>
  </xsl:template>

  <xsl:template name="uboMediaType">
    <xsl:choose>
      <xsl:when test="count(p:datafield[@tag='002E']) &gt; 1">
        <mods:classification valueURI="{$WebApplicationBaseURL}classifications/mediaType#mixed"
                             authorityURI="{WebApplicationBaseURL}classifications/mediaType"/>
      </xsl:when>

      <xsl:when test="p:datafield[@tag='002E']/p:subfield[@code='b']">
        <xsl:variable name="mediaType" select="p:datafield[@tag='002E']/p:subfield[@code='b']"/>

        <xsl:variable name="uboMediaType">
          <xsl:choose>
            <xsl:when test="contains('nc nb nn nr no na nz' , $mediaType)">print</xsl:when>
            <xsl:when test="contains('cr' , $mediaType)">online</xsl:when>
            <xsl:otherwise>technical</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <mods:classification valueURI="{$WebApplicationBaseURL}classifications/mediaType#{$uboMediaType}"
                             authorityURI="{$WebApplicationBaseURL}classifications/mediaType"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="uboPeerReview">
    <!-- KXP :  144Z/01-99 $9 -->
    <xsl:if test="p:datafield[@tag='144Z']/p:subfield[@code='9'] = '480733066'">
      <mods:classification valueURI="{$WebApplicationBaseURL}classifications/peerreviewed#true"
                           authorityURI="{$WebApplicationBaseURL}classifications/peerreviewed"/>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
