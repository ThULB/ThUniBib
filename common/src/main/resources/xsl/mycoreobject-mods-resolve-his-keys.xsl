<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
                xmlns:fn="http://www.w3.org/2005/xpath-functions"
                xmlns:mcracl="http://www.mycore.de/xslt/acl"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="mcracl fn xlink xsl">

  <xsl:include href="resource:xslt/functions/acl.xsl"/>

  <xsl:output method="xml" indent="yes"/>

  <xsl:param name="MCR.user2.matching.lead_id"/>
  <xsl:param name="ThUniBib.HISinOne.BaseURL"/>
  <xsl:param name="ThUniBib.HISinOne.BaseURL.API.Path"/>
  <xsl:param name="ThUniBib.HISinOne.resolve.person.identifier.typeUniquename"/>
  <xsl:param name="ThUniBib.HISinOne.servflag.type"/>
  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:variable name="origin" select="document('classification:metadata:-1:children:ORIGIN')/mycoreclass/categories" />

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:mods">
    <xsl:copy>
      <xsl:comment>Begin - transformer 'xsl/mods-resolve-his-keys.xsl'</xsl:comment>

      <!-- Resolve the related item (HIS -> Journal, Übergeordnete Publikation)-->
      <xsl:call-template name="related-item-host"/>

      <!-- Type of resource -->
      <xsl:call-template name="publicationResource"/>

      <!-- Publisher -->
      <xsl:call-template name="publisher"/>

      <!-- PeerReviewedValue -->
      <xsl:call-template name="peerReviewed"/>

      <!-- PublicationAccessType (Zugangsrecht nach KDSF) -->
      <xsl:call-template name="publicationAccessType"/>

      <!-- Set research areas as of KDSF -->
      <xsl:call-template name="researchAreaKdsf"/>

      <!-- Set subjectArea class -->
      <xsl:call-template name="subjectArea"/>

      <!-- Set state class -->
      <xsl:call-template name="state"/>

      <!-- Set visibility class -->
      <xsl:call-template name="visibility"/>

      <!-- Set publicationCreatorType class -->
      <xsl:call-template name="creatorType"/>

      <!-- Map identifiers like doi, urn, ... -->
      <xsl:call-template name="globalIdentifiers"/>

      <xsl:comment>End - transformer 'xsl/mods-resolve-his-keys.xsl'</xsl:comment>
      <!-- Retain original mods:mods -->
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="mods:name[@type='personal']">
    <xsl:variable name="his-id">
      <xsl:choose>
        <xsl:when test="mods:nameIdentifier[@type = 'orcid']">
          <xsl:value-of select="fn:document(concat('hisinone:resolve:personId:person:orcid:', mods:nameIdentifier[@type = 'orcid'][1]/text()))"/>
        </xsl:when>
        <xsl:when test="mods:nameIdentifier[@type = $MCR.user2.matching.lead_id]">
          <xsl:value-of select="fn:document(concat('hisinone:resolve:personId:person:', $ThUniBib.HISinOne.resolve.person.identifier.typeUniquename, ':',  fn:encode-for-uri(mods:nameIdentifier[@type = $MCR.user2.matching.lead_id][1]/text())))"/>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>

    <xsl:copy>
      <xsl:copy-of select="*|@*"/>
      <xsl:if test="number($his-id)">
        <xsl:comment>Begin - transformer 'xsl/mods-resolve-his-keys.xsl'</xsl:comment>

        <mods:nameIdentifier typeURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}cs/psv/person/identifier">
          <xsl:value-of select="$his-id"/>
        </mods:nameIdentifier>

        <xsl:comment>End - transformer 'xsl/mods-resolve-his-keys.xsl'</xsl:comment>
      </xsl:if>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="related-item-host">
    <xsl:variable name="host" select="mods:relatedItem[@type = 'host']/@xlink:href"/>
    <xsl:variable name="host-genre" select="mods:relatedItem[@type = 'host']/mods:genre[@type='intern']/fn:substring-after(@valueURI, '#')"/>

    <xsl:if test="fn:string-length($host) &gt; 0">
      <xsl:variable name="resolve-of-type">
        <xsl:choose>
          <xsl:when test="contains('journal newspaper', $host-genre)">
            <xsl:value-of select="'journal'"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="'publication'"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="his-id" select="fn:document(concat('hisinone:resolve:id:', $resolve-of-type, ':', $host))"/>

      <mods:relatedItem otherType="host" otherTypeAuth="{$ThUniBib.HISinOne.BaseURL}" otherTypeAuthURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}fs/res/{$resolve-of-type}">
        <xsl:attribute name="xlink:href">
          <xsl:value-of select="$host"/>
        </xsl:attribute>

        <xsl:value-of select="$his-id"/>
      </mods:relatedItem>
    </xsl:if>
  </xsl:template>

  <xsl:template name="publicationResource">
    <xsl:if test="mods:typeOfResource">
      <xsl:variable name="typeOfResource" select="mods:typeOfResource"/>
      <xsl:variable name="his-id" select="fn:document(concat('hisinone:resolve:id:publicationResource:', $typeOfResource))"/>
      <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}cs/sys/values/publicationResourceValue">
        <xsl:value-of select="$his-id"/>
      </mods:classification>
    </xsl:if>
  </xsl:template>

  <xsl:template name="publisher">
    <xsl:if test="mods:originInfo/mods:publisher">
      <xsl:variable name="publisher-text" select="fn:encode-for-uri(mods:originInfo/mods:publisher)"/>
      <xsl:variable name="publisher-id" select="fn:document(concat('hisinone:resolve:id:publisher:', $publisher-text))"/>

      <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}fs/res/publisher">
        <xsl:value-of select="$publisher-id"/>
      </mods:classification>
    </xsl:if>
  </xsl:template>

  <xsl:template name="peerReviewed">
    <xsl:variable name="categId" select="fn:substring-after(mods:classification[fn:contains(@valueURI, 'peerreviewed#')]/@valueURI, '#')"/>
    <xsl:variable name="publication-peerreviewed-type-his-id" select="fn:document(concat('hisinone:resolve:id:peerReviewed:', $categId))"/>

    <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}cs/sys/values/peerReviewedValue">
      <xsl:value-of select="$publication-peerreviewed-type-his-id"/>
    </mods:classification>
  </xsl:template>

  <xsl:template name="publicationAccessType">
    <xsl:if test="mods:classification[fn:contains(@valueURI, 'accessrights#')]">

      <xsl:variable name="categId" select="fn:substring-after(mods:classification[fn:contains(@valueURI, 'accessrights#')]/@valueURI, '#')"/>
      <xsl:variable name="publication-access-type-his-id" select="fn:document(concat('hisinone:resolve:id:publicationAccessType:', $categId))"/>

      <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}cs/sys/values/publicationAccessTypeValue">
        <xsl:value-of select="$publication-access-type-his-id"/>
      </mods:classification>
    </xsl:if>
  </xsl:template>

  <xsl:template name="globalIdentifiers">
    <xsl:for-each select="mods:identifier[contains('doi isbn issn scopus url urn', @type)]">
      <xsl:variable name="global-identifier-type-id" select="fn:document(concat('hisinone:resolve:id:globalIdentifiers:', @type))"/>
      <mods:identifier type="{@type}" typeURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}fs/res/publication/globalIdentifierType#{$global-identifier-type-id}">
        <xsl:value-of select="."/>
      </mods:identifier>
    </xsl:for-each>

    <xsl:variable name="url-type-id" select="fn:document('hisinone:resolve:id:globalIdentifiers:url')"/>
    <mods:identifier type="url" typeURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}fs/res/publication/globalIdentifierType#{$url-type-id}">
      <xsl:value-of select="concat($WebApplicationBaseURL, 'receive/', //mycoreobject/@ID)"/>
    </mods:identifier>

    <xsl:variable name="repository-type-id" select="fn:document('hisinone:resolve:id:globalIdentifiers:Repositoriums%20ID')"/>
    <mods:identifier type="mcrid" typeURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}fs/res/publication/globalIdentifierType#{$repository-type-id}">
      <xsl:value-of select="//mycoreobject/@ID"/>
    </mods:identifier>

  </xsl:template>

  <xsl:template name="researchAreaKdsf">
    <xsl:choose>
      <xsl:when test="mods:classification[contains(@valueURI, 'researchAreaKdsf#')]">
        <xsl:for-each select="fn:substring-after(mods:classification[contains(@valueURI, 'researchAreaKdsf#')]/@valueURI, '#')">
          <xsl:variable name="research-area-kdsf-his-key" select="fn:document(concat('hisinone:resolve:id:researchAreaKdsf:', .))"/>
          <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}cs/sys/values/researchAreaKdsfValue">
            <xsl:value-of select="$research-area-kdsf-his-key"/>
          </mods:classification>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="research-area-kdsf-his-key" select="fn:document('hisinone:resolve:id:researchAreaKdsf:001')"/>
        <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}cs/sys/values/researchAreaKdsfValue">
          <xsl:value-of select="$research-area-kdsf-his-key"/>
        </mods:classification>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- TODO find the proper source value, currently mapping is fixed to 'Autor/-in'-->
  <xsl:template name="creatorType">
    <xsl:variable name="creator-type-his-key" select="fn:document('hisinone:resolve:id:creatorType:aut')"/>
    <xsl:if test="$creator-type-his-key">
      <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}cs/sys/values/publicationCreatorTypeValue">
        <xsl:value-of select="$creator-type-his-key"/>
      </mods:classification>
    </xsl:if>
  </xsl:template>

  <xsl:template name="subjectArea">
    <xsl:variable name="subject-area-value-uri" select="'cs/sys/values/subjectAreaValue'"/>
    <xsl:variable name="origin-id" select="fn:substring-after(mods:classification[contains(@valueURI, 'ORIGIN')]/@valueURI, '#')"/>

    <xsl:for-each select="mods:classification[fn:contains(@authorityURI, 'classifications/destatis')]">
      <xsl:variable name="categ-id" select="fn:substring-after(@valueURI, '#')"/>
      <xsl:variable name="subject-area-his-key" select="fn:document(concat('hisinone:resolve:id:subjectArea:', $categ-id))"/>

      <xsl:if test="$subject-area-his-key">
        <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}{$subject-area-value-uri}">
          <xsl:value-of select="$subject-area-his-key"/>
        </mods:classification>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="visibility">
    <xsl:param name="status" select="//servflags/servflag[@type='status']"/>
    <xsl:variable name="visibility-his-key" select="fn:document(concat('hisinone:resolve:id:visibility:', $status))"/>
    <xsl:if test="$visibility-his-key">
      <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}cs/sys/values/visibilityValue">
        <xsl:value-of select="$visibility-his-key"/>
      </mods:classification>
    </xsl:if>
  </xsl:template>

  <xsl:template name="state">
    <xsl:param name="status" select="//servflags/servflag[@type='status']"/>
    <xsl:variable name="status-his-key" select="fn:document(concat('hisinone:resolve:id:state:', $status))"/>

    <xsl:if test="$status-his-key">
      <mods:classification authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}fs/res/state/publication">
        <xsl:value-of select="$status-his-key"/>
      </mods:classification>
    </xsl:if>
  </xsl:template>

  <!--
   Maps the ubo genre to the HISinOne publicationType.
  -->
  <xsl:template match="mods:genre[not(parent::mods:relatedItem)]">
    <xsl:param name="genre" select="fn:substring-after(@valueURI, '#')"/>

    <xsl:copy>
      <xsl:copy-of select="*|@*"/>
    </xsl:copy>

    <xsl:comment>Begin - transformer 'xsl/mods-resolve-his-keys.xsl'</xsl:comment>

    <!-- publicationTypeValue -->
    <xsl:variable name="xpathmapping2kdsfPublicationType-mycore" select="fn:substring-after(//mods:mods/mods:classification[@generator='xpathmapping2kdsfPublicationType-mycore']/@valueURI, '#')"/>
    <xsl:variable name="his-key-publication-type-value" select="fn:document(concat('hisinone:resolve:id:publicationType:', $xpathmapping2kdsfPublicationType-mycore))"/>

    <xsl:if test="$his-key-publication-type-value">
      <mods:genre authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}cs/sys/values/publicationTypeValue" type="code">
        <xsl:value-of select="$his-key-publication-type-value"/>
      </mods:genre>
    </xsl:if>

    <!--
      documentType
    -->
    <xsl:variable name="xpathmapping2kdsfDocumentType-mycore" select="fn:substring-after(//mods:mods/mods:classification[@generator='xpathmapping2kdsfDocumentType-mycore']/@valueURI, '#')"/>
    <xsl:variable name="his-key-document-type-type-value" select="fn:document(concat('hisinone:resolve:id:documentType:', $xpathmapping2kdsfDocumentType-mycore))"/>
    <xsl:if test="$his-key-document-type-type-value">
      <mods:genre authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}fs/res/publication/documentTypes" type="code">
        <xsl:value-of select="$his-key-document-type-type-value"/>
      </mods:genre>
    </xsl:if>

    <!-- qualificationThesisValue -->
    <xsl:variable name="his-key-qualification-thesis-type-value" select="fn:document(concat('hisinone:resolve:id:thesisType:', $genre))"/>
    <xsl:if test="$his-key-publication-type-value">
      <mods:genre authorityURI="{$ThUniBib.HISinOne.BaseURL}" valueURI="{$ThUniBib.HISinOne.BaseURL}{$ThUniBib.HISinOne.BaseURL.API.Path}cs/sys/values/qualificationThesisValue" type="code">
        <xsl:value-of select="$his-key-qualification-thesis-type-value"/>
      </mods:genre>
    </xsl:if>

    <xsl:comment>End - transformer 'xsl/mods-resolve-his-keys.xsl'</xsl:comment>
  </xsl:template>

  <xsl:template match="mods:language">
    <xsl:variable name="rfc5646" select="mods:languageTerm[@type='code'][@authority='rfc5646']"/>

    <xsl:copy>
      <xsl:copy-of select="*|@*"/>
      <xsl:variable name="his-key" select="fn:document(concat('hisinone:resolve:id:language:', $rfc5646))"/>

      <xsl:if test="$his-key">
        <xsl:comment>Begin - transformer 'xsl/mods-resolve-his-keys.xsl'</xsl:comment>

        <mods:languageTerm authorityURI="{$ThUniBib.HISinOne.BaseURL}" type="code">
          <xsl:value-of select="$his-key"/>
        </mods:languageTerm>

        <xsl:comment>End - transformer 'xsl/mods-resolve-his-keys.xsl'</xsl:comment>
      </xsl:if>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="//servflags[servflag[@type = $ThUniBib.HISinOne.servflag.type]]">
    <xsl:copy>
      <xsl:copy-of select="*|@*"/>
      <xsl:variable name="genre" select="fn:substring-after(//mycoreobject//mods:mods/mods:genre[@type='intern']/@valueURI, '#')"/>

      <xsl:variable name="lockVersion">
        <xsl:choose>
          <xsl:when test="contains('journal newspaper', $genre)">
            <xsl:value-of select="fn:document(concat('hisinone:resolve:lockVersion:journal:', //mycoreobject/@ID))"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="fn:document(concat('hisinone:resolve:lockVersion:publication:', //mycoreobject/@ID))"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <servflag type="{$ThUniBib.HISinOne.servflag.type}-lockVersion">
        <xsl:value-of select="$lockVersion"/>
      </servflag>
    </xsl:copy>
  </xsl:template>
</xsl:stylesheet>
