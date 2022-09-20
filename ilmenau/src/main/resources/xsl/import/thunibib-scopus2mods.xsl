<?xml version="1.0" encoding="UTF-8"?>

<!-- Converts Scopus abstracts-retrieval-response format to MODS -->
<!-- http://api.elsevier.com/content/abstract/scopus_id/84946429507?apikey=... -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="import/thunibib-scopus2mods-common.xsl"/>

  <!-- Disable destatis mapping in ilmenau -->
  <xsl:template name="thunibib_scopus2destatis"/>

</xsl:stylesheet>
