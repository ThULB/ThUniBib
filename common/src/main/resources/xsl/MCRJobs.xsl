<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:encoder="xalan://java.net.URLEncoder"
                xmlns:i18n="xalan://org.mycore.services.i18n.MCRTranslation"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:mcr="xalan://org.mycore.common.xml.MCRXMLFunctions"
                xmlns:acl="xalan://org.mycore.access.MCRAccessManager"
                xmlns:mcrid="org.mycore.datamodel.metadata.MCRObjectID"
                exclude-result-prefixes="xalan i18n encoder mcr acl mcrid">

  <xsl:param name="WebApplicationBaseURL"/>

  <xsl:variable name="start" select="./MCRJobs/properties/start"/>
  <xsl:variable name="rows" select="./MCRJobs/properties/rows"/>
  <xsl:variable name="statusRestriction" select="./MCRJobs/properties/statusRestriction"/>
  <xsl:variable name="actionRestriction" select="./MCRJobs/properties/actionRestriction"/>

  <xsl:variable name="hits" select="./MCRJobs/properties/hits"/>
  <xsl:variable name="currentPage" select="ceiling((($start + 1) - $rows) div $rows)+1"/>
  <xsl:variable name="pageTotal">
    <xsl:choose>
      <xsl:when test="ceiling($hits div $rows) = 0">
        <xsl:value-of select="1"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="ceiling($hits div $rows )"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:template match="/MCRJobs">
    <webpage>
      <title xml:lang="de">MCRJob-Listing</title>
      <title xml:lang="en">MCRJob-Listing</title>

      <article>
        <div class="row mt-3">
          <div class="col">
            <form action="JobStatusServlet">
              <div class="form-group form-group-sm form-row">
                <div class="col-sm-6">
                  <input type="hidden" class="form-control" value="{$start}" name="start"/>
                  <input type="hidden" value="{$rows}" name="rows"/>

                  <!-- filter by job status -->
                  <xsl:value-of select="'State'"/>
                  <xsl:text>:</xsl:text>
                  <select name="statusRestriction"
                          class="custom-select form-control form-control-sm custom-select-sm">
                    <option value="">
                      <xsl:value-of select="i18n:translate('search.select')"/>
                    </option>
                    <option value="NEW">
                      <xsl:if test="$statusRestriction = 'NEW'">
                        <xsl:attribute name="selected">selected</xsl:attribute>
                      </xsl:if>
                      <xsl:value-of select="'NEW'"/>
                    </option>
                    <option value="PROCESSING">
                      <xsl:if test="$statusRestriction = 'PROCESSING'">
                        <xsl:attribute name="selected">selected</xsl:attribute>
                      </xsl:if>
                      <xsl:value-of select="'PROCESSING'"/>
                    </option>
                    <option value="FINISHED">
                      <xsl:if test="$statusRestriction = 'FINISHED'">
                        <xsl:attribute name="selected">selected</xsl:attribute>
                      </xsl:if>
                      <xsl:value-of select="'FINISHED'"/>
                    </option>
                  </select>

                  <xsl:value-of select="'Action'"/>
                  <xsl:text>:</xsl:text>

                  <!-- filter by action -->
                  <select name="actionRestriction"
                          class="custom-select form-control form-control-sm custom-select-sm">
                    <option value="">
                      <xsl:value-of select="i18n:translate('search.select')"/>
                    </option>
                    <xsl:for-each select="actions/action">
                      <option value="{.}">
                        <xsl:if test="$actionRestriction = .">
                          <xsl:attribute name="selected">selected</xsl:attribute>
                        </xsl:if>
                        <xsl:value-of select="."/>
                      </option>
                    </xsl:for-each>
                  </select>
                </div>
              </div>

              <div class="form-group form-group-sm form-row">
                <div class="col-sm-2">
                  <input type="submit" class="button form-control form-control-sm btn btn-primary btn-sm"
                         id="submitSearchButton" value="{i18n:translate('editor.search.search')}"/>
                </div>
              </div>
            </form>

            <div class="row mt-3 mb-3">
              <div class="col">
                <xsl:if test="MCRJob">
                  <div>
                    <xsl:value-of
                        select="concat('Page: ', $currentPage ,'/', $pageTotal )"/>
                  </div>
                </xsl:if>
                <div>
                  <xsl:value-of select="concat($hits, ' items were found')"/>
                </div>
              </div>
            </div>

            <xsl:if test="MCRJob">
              <table class="table table-sm table-hover">
                <tr class="font-weight-bold">
                  <th class="text-center">
                    <xsl:if test="mcr:isCurrentUserInRole('admin')">
                      <a href="{concat($WebApplicationBaseURL, 'servlets/JobStatusServlet?deleteAll=true')}"
                         title="Delete all jobs">
                        <i class="fas fa-eraser font-weight-bold"/>
                      </a>
                    </xsl:if>
                  </th>
                  <th>
                    <xsl:value-of select="'State'"/>
                  </th>
                  <th>
                    <xsl:value-of select="'ID'"/>
                  </th>
                  <th>
                    <xsl:value-of select="'Action'"/>
                  </th>
                  <th>
                    <xsl:value-of select="'Start'"/>
                  </th>
                  <th>
                    <xsl:value-of select="'End'"/>
                  </th>
                  <th>
                    <xsl:value-of select="'Duration'"/>
                  </th>
                  <th>
                    <xsl:value-of select="'Parameters'"/>
                  </th>
                </tr>
                <xsl:apply-templates select="MCRJob"/>
              </table>

              <div id="pageSelection">

                <ul class="pagination justify-content-center">
                  <xsl:if test="($start - $rows) &gt;= 0">
                    <xsl:variable name="startRecordPrevPage">
                      <xsl:value-of select="$start - $rows"/>
                    </xsl:variable>

                    <li class="page-item">
                      <a class="page-link" id="linkToPreviousPage" title="{i18n:translate('searchResults.prevPage')}"
                         href="{concat($WebApplicationBaseURL,'servlets/MCRJobStatus?', 'start=', $startRecordPrevPage, '&amp;rows=', $rows, '&amp;statusRestriction=', $statusRestriction)}">
                        <i class="fas fa-arrow-left thulb-default-text-color"/>
                      </a>
                    </li>
                  </xsl:if>

                  <xsl:variable name="maxNumClickablePages" select="10"/>
                  <xsl:variable name="lookAhead" select="5"/>
                  <xsl:variable name="lastPageNumberToDisplay">
                    <xsl:choose>
                      <xsl:when test="$currentPage + $lookAhead &gt; $pageTotal">
                        <xsl:value-of select="$pageTotal"/>
                      </xsl:when>

                      <xsl:when test="$currentPage + $lookAhead &lt; $maxNumClickablePages">
                        <xsl:choose>
                          <xsl:when test="$pageTotal &gt;= $maxNumClickablePages">
                            <xsl:value-of select="$maxNumClickablePages"/>
                          </xsl:when>
                          <xsl:when test="$pageTotal &lt; $maxNumClickablePages">
                            <xsl:value-of select="$pageTotal"/>
                          </xsl:when>
                        </xsl:choose>
                      </xsl:when>

                      <xsl:otherwise>
                        <xsl:value-of select="$currentPage + $lookAhead"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:variable>
                  <xsl:variable name="startPage">
                    <xsl:choose>
                      <xsl:when test="$currentPage - $lookAhead &lt; 0">
                        <xsl:value-of select="0"/>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="$currentPage - $lookAhead"/>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:variable>

                  <xsl:call-template name="displayPageNavigation">
                    <xsl:with-param name="i" select="$startPage"/>
                    <xsl:with-param name="lastPageNumberToDisplay" select="$lastPageNumberToDisplay"/>
                  </xsl:call-template>

                  <xsl:variable name="startRecordNextPage">
                    <xsl:value-of select="$start + $rows"/>
                  </xsl:variable>

                  <xsl:if test="$startRecordNextPage &lt; $hits">
                    <li class="page-item">
                      <a class="page-link" id="linkToNextPage" title="{i18n:translate('searchResults.nextPage')}"
                         href="{concat($WebApplicationBaseURL,'servlets/JobStatusServlet?', 'start=', $start + $rows, '&amp;rows=', $rows, '&amp;statusRestriction=', $statusRestriction)}">
                        <i class="fas fa-arrow-right thulb-default-text-color"/>
                      </a>
                    </li>
                  </xsl:if>
                </ul>
              </div>
            </xsl:if>
          </div>
        </div>
      </article>
    </webpage>
  </xsl:template>

  <xsl:template match="MCRJob">
    <tr>
      <td class="text-center">
        <xsl:if test="mcr:isCurrentUserInRole('admin')">
          <a class="align-top" href="{concat($WebApplicationBaseURL, 'servlets/JobStatusServlet?deleteJob=', id)}"
             title="{concat('Delete job with id', id)}">
            <i class="fas fa-trash"/>
          </a>
        </xsl:if>
      </td>

      <td class="align-top" title="{status}">
        <xsl:choose>
          <xsl:when test="status = 'FINISHED'">
            <i class="fas fa-check"/>
          </xsl:when>
          <xsl:when test="status = 'PROCESSING'">
            <i class="fas fa-spinner"/>
          </xsl:when>
          <xsl:when test="status = 'NEW'">
            <i class="far fa-star"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="status"/>
          </xsl:otherwise>
        </xsl:choose>
      </td>

      <td title="{id}">
        <xsl:value-of select="id"/>
      </td>

      <td title="{action}">
        <xsl:value-of select="action-simple"/>
      </td>

      <td title="{started}">
        <xsl:value-of select="started"/>
      </td>
      <td title="{finished}">
        <xsl:value-of select="finished"/>
      </td>
      <td title="{concat('Duration: ', format-number(duration div 1000, '#####.##'), ' ', i18n:translate('datetime.seconds'))}">
        <xsl:value-of select="concat(format-number(duration div 1000, '#####.##'), ' s')"/>
      </td>
      <td>
        <xsl:for-each select="parameters/parameter">
          <xsl:sort select="@name"/>
          <span class="d-block">
            <span class="font-weight-bold">
              <xsl:value-of select="concat(@name, ': ')"/>
            </span>
            <xsl:choose>
              <!-- when object is a mcrobject link to metadata page -->
              <xsl:when test="mcrid:isValid(.) and not (contains(. , '_derivate_'))">
                <a class="thulb-default-text-color font-weight-bold" href="{$WebApplicationBaseURL}receive/{.}"
                   title="{$WebApplicationBaseURL}receive/{.}">
                  <xsl:value-of select="."/>
                </a>
              </xsl:when>
              <!-- when object is a derivate link to details page -->
              <xsl:when test="mcrid:isValid(.) and (contains(. , '_derivate_'))">
                <a class="thulb-default-text-color font-weight-bold"
                   href="{$WebApplicationBaseURL}servlets/MCRFileNodeServlet/{.}/"
                   title="{$WebApplicationBaseURL}servlets/MCRFileNodeServlet/{.}/">
                  <xsl:value-of select="."/>
                </a>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="."/>
              </xsl:otherwise>
            </xsl:choose>
          </span>
        </xsl:for-each>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="displayPageNavigation">
    <xsl:param name="i"/>
    <xsl:param name="lastPageNumberToDisplay"/>

    <xsl:if test="$i &lt; $lastPageNumberToDisplay">
      <xsl:variable name="s" select="$i * $rows"/>

      <li>
        <xsl:attribute name="class">
          <xsl:choose>
            <xsl:when test="$s = $start">
              <xsl:value-of select="'page-item active'"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'page-item thulb-default-text-color'"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>

        <a href="{$WebApplicationBaseURL}servlets/JobStatusServlet?start={$s}&amp;rows={$rows}&amp;statusRestriction={$statusRestriction}&amp;actionRestriction={$actionRestriction}">
          <xsl:attribute name="class">
            <xsl:choose>
              <xsl:when test="$s = $start">
                <xsl:value-of select="'page-link thulb-bg-theme'"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="'page-link thulb-default-text-color'"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:attribute>
          <xsl:value-of select="$i + 1"/>
        </a>
      </li>

      <xsl:call-template name="displayPageNavigation">
        <xsl:with-param name="i" select="$i + 1"/>
        <xsl:with-param name="lastPageNumberToDisplay" select="$lastPageNumberToDisplay"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
