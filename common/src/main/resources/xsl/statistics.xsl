<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:mcrxsl="xalan://org.mycore.common.xml.MCRXMLFunctions"
                exclude-result-prefixes="xsl xalan i18n mcrxsl">

  <xsl:template match="/response">
    <xsl:apply-templates select="lst[@name='facet_counts']/lst[@name='facet_pivot']/arr[@name='name_id_type,name_id_type']"/>
  </xsl:template>

  <xsl:template match="lst/arr[@name='name_id_type,name_id_type']">
    <xsl:if test="not(mcrxsl:isCurrentUserGuestUser())">
      <xsl:variable name="base" select="." />

      <article class="card">
        <div class="card-body">
          <h3>
            <xsl:value-of select="concat(i18n:translate('thunibib.statistic.aut.ids.used'), ': ')"/>
          </h3>

          <table class="table table-bordered">
            <tr class="text-center">
              <th scope="col">/</th>
              <xsl:for-each select="$base/lst">
                <th scope="col">
                  <xsl:value-of select="translate(str[@name='value'],'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')" />
                </th>
              </xsl:for-each>
            </tr>
            <xsl:for-each select="$base/lst">
              <xsl:variable name="a" select="str[@name='value']" />
              <tr class="text-right">
                <th class="identifier" scope="row">
                  <xsl:value-of select="translate(str[@name='value'],'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')" />
                </th>
                <xsl:for-each select="$base/lst">
                  <xsl:variable name="b" select="str[@name='value']" />
                  <td class="identifier">
                    <xsl:value-of select="$base/lst[str[@name='value']=$a]/arr/lst[str[@name='value']=$b]/int[@name='count']" />
                  </td>
                </xsl:for-each>
              </tr>
            </xsl:for-each>
          </table>
        </div>
      </article>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
