# Changelog

## 2023.07.11 => 2023.08.11

- #386 Set mycore version to 2022.06.3-SNAPSHOT
- #388 Add xslt stylesheet to remove eduPersonUniqueId
- #396 Added command "schedule pica import for query {0} and filter {1}"
- #398 Added UI to list currently stored MCRJobs
- #400 Configured MCR.Cronjob.Jobs.UpdateSolrProjectCore
- Added null check in ThUniBibImportJobAction
- Added null check in de.uni_jena.thunibib.ThUniBibMailer
- FSU040THUL-1050 #392 Added order attributes to commands in EnrichmentByAffiliationCommands
- FSU040THUL-1066 #394 Added i18n ubo.relatedItem.format_other. Sort and formatted messages_en.properties and messages_de.properties
- FSU040THUL-383 #384 Angabe bei mehrtägiger Konferenzdauer
- FSU040THUL-841 #341 Import von Schlagwörtern erfolgt nur eingeschränkt
- FSU040THUL-909 #376 Allow multiple origin entries for a given ppn (Erfurt)
- FSU040THUL-913 #378 DESTATIS sollte nicht mehr während des Imports gesetzt werden
- FSU040THUL-914 #381 Aktualisierung Logo Hochschulbibliografie (Ilmenau)
- FSU040THUL-915 #380 Aktualisierung des Körperschaftsicons im IdentityPicker
- Set MCR.user2.matching.lead_id.skip = true (Ilmenau)


## 2023.06.26 => 2023.07.11

- #355 Added xslt-set-partOf-false.xsl
- #357 Added stylesheet to fix connections ids
- #362 Added CONTRIBUTING.md
- #368 Set MCR.user2.IdentityManagement.UserCreation.Unvalidated.Realm = local
- #371 Provide classification user_attributes.xml for every thunibib instance and unify message properties for local IDs
- FSU040THUL-618 #360 Anpassungen ADMIN-Editor
- FSU040THUL-835 #366 Set $primary to #024975 in _variables_ilmenau.scss. Set backround-color to $primary for #navigationWrapper in _navigation.scss
- Prefer GBV as source (catalog import)
- Remove local matcher from chain, is always configured
- Fix MCR.IdentityPicker.strategy, set LDAPWithLocal as default
- Updated CHANGELOG.md
---


## 2023.04.04 => 2023.06.26

- #266 Removed role 'wst', updated text for role 'his' and added role 'oth' to marcrelator-corporation.xml
- #306 Mapping K10+ Import Uni Erfurt
- #320 Update origin.xml for Jena
- #321 Removed restriction from xed:repeat element for classification 'fachreferate'
- #323 Allow all users to search for both confirmed and unchecked publications
- #324 List most recent documents on landing page (Weimar)
- #328 Added command 'list gone ldap users for realm {0}'
- #333 Apply external changes to accessibility.xml [Ilmenau]
- #335 Updated solr uris in index.xed (Ilmenau)
- #336 Set property MCR.IdentityPicker.strategy.Local.PID.Filter.enabled = false https://github.com/MyCoRe-Org/ubo/pull/290
- #337 Use oa colors provided by base ubo for statistics and badges
- #340 Use origin_exact facet for statistics
- #342 Set property UBO.Export.Status.Restriction = +(status:confirmed status:unchecked)
- #344 Enable import by dbt id via newPublication.xed
- #349 Update ORIGIN.xml
- #351 Load rfc5646 in pica2mods_thunibib-common.xsl by resource uri resolver
- Reflect changes in UBOs Java and MyCoRe version
- UBO-238 Added/altered i18n keys https://github.com/MyCoRe-Org/ubo/pull/278
---


## 2023.04.04

- Enable JSON support for MyCoRe REST-API #316
