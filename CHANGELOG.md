# Changelog

## 2025.03.17-2023.06.x
- [#671](https://github.com/ThULB/ThUniBib/issues/671) Removed ThUniBibAffiliationEventHandler from mycore.properties (Weimar) (#672)
- [#679](https://github.com/ThULB/ThUniBib/issues/679) Support diamond OA status (#680)
- [FSU040THUL-5727](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-5727) [#673](https://github.com/ThULB/ThUniBib/issues/673) Add additional role 'edt' to marcrelator_corporation.xml (#674)
- [FSU040THUL-6461](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-6461) [#675](https://github.com/ThULB/ThUniBib/issues/675) Added additional marc role 'orm' to marcrelator.xml (#676)
- [FSU040THUL-6795](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-6795) [#677](https://github.com/ThULB/ThUniBib/issues/677) Updated search.xed to support publication status reviewPending and unchecked (#678)
- [FSU040THUL-6890](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-6890) [#681](https://github.com/ThULB/ThUniBib/issues/681) Added K10PLUS enrichment source to list import (#682)
- [FSU040THUL-7015](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-7015) [#683](https://github.com/ThULB/ThUniBib/issues/683) Added licence type 'other' to licenses.xml (#684)
---

## 2024.12.12-2023.06.x
- [#665](https://github.com/ThULB/ThUniBib/issues/665) Reflect changes introduced with UBO-372 (#666)
- [#669](https://github.com/ThULB/ThUniBib/issues/669) Add status "Review pending" (Jena) (#670)
- [FSU040THUL-5394](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-5394) [#667](https://github.com/ThULB/ThUniBib/issues/667) Added 'Austellungskatalog' to ubogenre.xml (#668)
---

## 2024.12.03-2023.06.x
- [#614](https://github.com/ThULB/ThUniBib/issues/614) Updated solr request handlers to reflect recent changes due to migration from fachreferate to destatis classification (#649)
- [#614](https://github.com/ThULB/ThUniBib/issues/614) Use class mapping for determining destatis from ORIGIN.xml (#615)
- [#652](https://github.com/ThULB/ThUniBib/issues/652) Updated mycore parent version to 53 (#653)
- [#656](https://github.com/ThULB/ThUniBib/issues/656) Support search by lead id (#657)
- [#658](https://github.com/ThULB/ThUniBib/issues/658) Moved code from class ThUniBibUtils to Utilities class (#659)
- [#658](https://github.com/ThULB/ThUniBib/issues/658) Updated callJava uri in mycoreobject-mods-detailed.xsl (#660)
- [#661](https://github.com/ThULB/ThUniBib/issues/661) Updated mods:name xpath in thunibib-solr.xsl and updated solr-schema.json (#662)
- [FSU040THUL-5039](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-5039) [#632](https://github.com/ThULB/ThUniBib/issues/632) Add support for genre special issue (#633)
- [FSU040THUL-5314](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-5314) [#647](https://github.com/ThULB/ThUniBib/issues/347) Fixed size limits in display of facet counts (#648)
- [FSU040THUL-5393](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-5393) [#650](https://github.com/ThULB/ThUniBib/issues/650) Allow to indicate actual affiliation of author at time of publication (#651)
- [FSU040THUL-5393](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-5393) [#663](https://github.com/ThULB/ThUniBib/issues/663) Activated ThUniBibAffiliationEventHandler by setting MCR.EventHandler.MCRObject.019a.Class=de.uni_jena.thunibib.common.events.ThUniBibAffiliationEventHandler in mycore.properties (Weimar only) (#664)
- [FSU040THUL-5452](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-5452) [#654](https://github.com/ThULB/ThUniBib/issues/654) Excluded DBT as source of enrichment (#655)
---

## 2024.11.08-2023.06.x
- [#637](https://github.com/ThULB/ThUniBib/issues/637) Update ORIGIN.xml (Ilmenau)(#636) (#638)
- [FSU040THUL-5159](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-5159) [#641](https://github.com/ThULB/ThUniBib/issues/641) Hide person_ths option in import-search.xed (#642)
- [FSU040THUL-5245](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-5245) [#643](https://github.com/ThULB/ThUniBib/issues/643) Retain identfiers of type 'hdl' in thunibib-mods-filter-supported.xsl (#644)
- [FSU040THUL-5272](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-5272) [#645](https://github.com/ThULB/ThUniBib/issues/645) Updated status i18n keys (#646)
---

## 2024.10.24-2023.06.x
- [#464](https://github.com/ThULB/ThUniBib/issues/464) Migrate to MyCoRe 2023.06.x (#465)
- [#621](https://github.com/ThULB/ThUniBib/issues/621) Updated csl style for Ilmenau (#622) (#623)
- [#625](https://github.com/ThULB/ThUniBib/issues/625) Added log4j2.xml (#626)
- [#627](https://github.com/ThULB/ThUniBib/issues/627) Set MCR.user2.matching.chain to de.uni_jena.thunibib.matcher.ThUniBibMatcherLDAP (#628)
- [#629](https://github.com/ThULB/ThUniBib/issues/629) Fixed link to communication and marketing department in impressum.xml (Jena) (#631)
- [#629](https://github.com/ThULB/ThUniBib/issues/629) Updated impressum.xml (Jena) (#630)
- [#634](https://github.com/ThULB/ThUniBib/issues/634) Fixed IllegalStateException in ThUniBibImportJobAction (#635)
- [FSU040THUL-4406](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-4406) [#616](https://github.com/ThULB/ThUniBib/issues/616) [WE] Updated privacy-statement.xml (#617)
- [FSU040THUL-4412](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-4412) [#618](https://github.com/ThULB/ThUniBib/issues/618) Added CC0 1.0 to licenses.xml (#619)
---

## 2024.08.26.2
- [#621](https://github.com/ThULB/ThUniBib/issues/621) Updated csl style for Ilmenau (#623)
- [FSU040THUL-4412](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-4412) [#618](https://github.com/ThULB/ThUniBib/issues/618) Added CC0 1.0 to licenses.xml (#619)
---

## 2024.08.26.1
-  [FSU040THUL-4406](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-4406) [#616](https://github.com/ThULB/ThUniBib/issues/616) [WE] Updated privacy-statement.xml
---

## 2024.08.26
- [#608](https://github.com/ThULB/ThUniBib/issues/608) Simplified embedding of web fonts (#609)
- [FSU040THUL-4181](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-4181) [#610](https://github.com/ThULB/ThUniBib/issues/610) Support search by publisher in extended search.xed (#611)
- [FSU040THUL-4246](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-4246) [#612](https://github.com/ThULB/ThUniBib/issues/612) Fixed calculation of max value of y-axis (#613)
---

## 2024.08.13
- [#572](https://github.com/ThULB/ThUniBib/issues/572) Added solr fields accessrights and peerreviewed to UBO.Export.Fields (#573)
- [#574](https://github.com/ThULB/ThUniBib/issues/574) Overwrite i18n ubo.person.connected.sup for Ilmenau and Jena (#575)
- [#576](https://github.com/ThULB/ThUniBib/issues/576) Added information about author icons to faq.xml (Ilmenau) (#577)
- [#578](https://github.com/ThULB/ThUniBib/issues/578) Configured PPN2DBT-ID enrichment (#579)
- [#580](https://github.com/ThULB/ThUniBib/issues/580) Do not map publications of type "others" to all possible orcid types (#581)
- [#582](https://github.com/ThULB/ThUniBib/issues/582) Overwrite i18n ubo.person.connected.sup (Erfurt) (#583)
- [#586](https://github.com/ThULB/ThUniBib/issues/586) Fixed property MCR.MODS.EnrichmentResolver.DataSources.scopusImport (prepend missing % character) (#587)
- [#591](https://github.com/ThULB/ThUniBib/issues/591) Update index.xed (#590) (#592)
- [#596](https://github.com/ThULB/ThUniBib/issues/596) Update .*xml files (Ilmenau) (#597)
- [#606](https://github.com/ThULB/ThUniBib/issues/606) Updated privacy statement (Weimar) (#607)
- [FSU040THUL-3958](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-3958) [#584](https://github.com/ThULB/ThUniBib/issues/584) Reflect changes as in [UBO-348](https://mycore.atlassian.net/browse/UBO-348) (#585)
- [FSU040THUL-4069](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-4069) [#598](https://github.com/ThULB/ThUniBib/issues/598) Map thesis to dissertation during import from k10+ (Weimar) (#599)
- [FSU040THUL-4078](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-4078) [#588](https://github.com/ThULB/ThUniBib/issues/588) Fork mycoreobject-e-mail.xsl from UBO (#589)
- [FSU040THUL-4123](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-4123) [#601](https://github.com/ThULB/ThUniBib/issues/601) Added "f.year.facet.limit":-1 (no limit) to request handler configurations in solr-config.json (#603)
---

## 2024.07.09
- [#545](https://github.com/ThULB/ThUniBib/issues/545) Added flags indicating current language as svg graphics (#546)
- [#548](https://github.com/ThULB/ThUniBib/issues/548) [FSU040THUL-3594](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-3594) Use gvk for imports by ISBN (#549)
- [#553](https://github.com/ThULB/ThUniBib/issues/553) Updated maven-publish.yml and added maven-pr.yml (#554)
- [#567](https://github.com/ThULB/ThUniBib/issues/567) Update .*xed files for Ilmenau (#568)
- [#570](https://github.com/ThULB/ThUniBib/issues/570) [FSU040THUL-3728](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-3728) Added solr field to support wildcard queries in mods:note elements (#571)
___

## 2024.05.24
- [#462](https://github.com/ThULB/ThUniBib/issues/462) Enable genre mapping to/from orcid (#463)
- [#478](https://github.com/ThULB/ThUniBib/issues/478) Reflect changes of mods:genre handling in base UBO (#479)
- [#527](https://github.com/ThULB/ThUniBib/issues/527) Made request handler for statistics dynamic by adding /statistics-all handler (#528)
- [#531](https://github.com/ThULB/ThUniBib/issues/531) Added property MCR.user2.LDAP.Mapping.labeledURI.id_viaf.schema (#532)
- [#533](https://github.com/ThULB/ThUniBib/issues/533) Unified name of file containing the privacy statement for all instances, added and linked first version of privacy statement for Weimar (#534)
- [#537](https://github.com/ThULB/ThUniBib/issues/537) Add accessibility statement (Ilmenau) and improved overall accessibility(#538)
- [#541](https://github.com/ThULB/ThUniBib/issues/541) Improve import from DBT (#542)
- [FSU040THUL-2373](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-2373) [#535](https://github.com/ThULB/ThUniBib/issues/535) Updated json.facet parameter in for statistic request handlers in solr-config.json (#536)
- [FSU040THUL-292](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-292) [#529](https://github.com/ThULB/ThUniBib/issues/529) Adopted template <xed:template id="languages"/> to rfc5646 (#530)
- [FSU040THUL-2947](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-2947) [#525](https://github.com/ThULB/ThUniBib/issues/525) Updated ORIGIN.xml (Weimar) (#540)
- [FSU040THUL-3377](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-3377) [#543](https://github.com/ThULB/ThUniBib/issues/543) Überarbeitung Anmeldelink und Sprachauswahl (#544)
---

## 2024.03.26
- #359 Enable typo3 plugin for publication lists (#466)
- #484 Do not use MCRQL for querying solr on landing page (#485)
- #499 Fix odd display of orcid icon next to username in templates of Erfurt, Ilmenau and Weimar (#500)
- #503 Update ORIGIN.xml (Ilmenau) (#501)
- #504 Update newPublication.xed (Ilmenau) (#502)
- #505 Updated impressum.xml (Jena) (#506)
- #507 Set properties MCR.user2.IdentityManagement.UserCreation.Affiliation=Uni Erfurt and MCR.user2.IdentityManagement.UserCreation.LDAP.Realm=uni-erfurt.de (#508)
- #509 Fixed typo in ORIGIN.xml (Weimar) (#510)
- #513 Fixed ArrayIndexOutOfBoundsException in ThUniBibCommands#moveUserToRealm (#514)
- #522 Overwrite property MCR.user2.LDAP.searchFilter.base for Erfurt (#523)
- Bump org.apache.solr:solr-solrj from 8.11.1 to 8.11.3 (#512)
- [FSU040THUL-2452](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-2452) #488 Retain mods:note[type='@university_thesis_note'] (#489)
- [FSU040THUL-2569](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-2569) #517 Updated English labels related to UBO basket functionality (#518)
- [FSU040THUL-2670](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-2670) #497 Fixed template 'uboTypeOfResource' in pica2mods_thunibib-common.xsl (#498)
- [FSU040THUL-2889](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-2889) #515 Overwrite i18n 'facets.facet.subject' from base ubo (#516)
- [FSU040THUL-2934](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-2934) #520 Added accessrights, peerreviewed and mediaType as facets (#521)
- [thunibib-weimar] - Update ORIGIN.xml (#511)
---

## 2024.02.19

- #480 Added class ThUniBibMatcherLDAP (#481)
- #487 Update faq.xml (#486)
- #494 Removed property UBO.Login.Path from mycore.properties (Erfurt) (#495)
- [FSU040THUL-2604](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-2604) #490 Overwrite i18n ubo.person.connected of base UBO (#491)
- [FSU040THUL-2609](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-2609) #492 Updated ORIGIN.xml (Erfurt) (#493)
---

## 2024.01.02

- #469 table header in list of vanished ldap users is generated to often (#470)
- #475 Added properties MCR.user2.IdentityManagement.UserCreation.Affiliation and MCR.user2.IdentityManagement.UserCreation.LDAP.Realm (#477)
- #475 Removed MCR.user2.matching.chain=org.mycore.ubo.matcher.MCRUserMatcherDummy (#476)
- [FSU040THUL-2021](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-2021) #471 Update funding of publications via cron job, disabled by default (#472)
---

## 2023.12.05

- #330 Add ORCID integration from base UBO
- #425 Fixed error duplicate template 'layout.pageTitle'
- #441 Fixed resource leak in de.uni_jena.thunibib.ThUniBibCommands.updateProjects (#442)
- #443 Added classification nameIdentifier for each instance (#444)
- #451 Fixed error when multiple fundings are removed
- #455 Directory structure in thunibib-weimar/erfurt does not meet 'ubo-ansible' requirements
- #455 Implement transformer for detailed mods:name metadata
- #457 Applied changes needed to match orcid member api requirements
- [FSU040THUL-1197](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1197) #421 Fehlender Link bei Online-Publikation ohne DOI
- [FSU040THUL-1214](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1214) Probleme bei Import: Konferenz-/Tagungsband im Standardformular
- [FSU040THUL-1243](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1243) #417 Added field connection_nid_text as facet
- [FSU040THUL-1278](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1278) #427 PPN Import: Corresponding Author wird beim Enrichment über Scopus ignoriert
- [FSU040THUL-1278](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1278) #429 Für KDSF-Konformität nötige Erfassungsfelder ergänzen (#430)
- [FSU040THUL-1281](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1281) #436 Position "Access Rights" sollte zwischen "Open Access" und "Lizenztyp" liegen
- [FSU040THUL-1405](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1405) #460 Falsche Bezeichnung der Auflage als Ausgabe behoben
- [FSU040THUL-1564](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1564) Eingabemaske "Access Rights" und "Open Access" (#448)
- [FSU040THUL-1971](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1971) #467 Added i18n 'ubo.interviewer.abbreviated' (#468)
- [FSU040THUL-206](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-206) #433 Anreicherung mit URL nur bei Closed Access Publikationen
- [FSU040THUL-405](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-405) #423 "Lücke" in den Ergebnissen der Personensuche (#424)
- [FSU040THUL-972](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-972) Allow to set multiple fundings during import, made fundingType repeatable in form
- [FSU040THUL-1017](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1017) #413 Mark corresponding author during scopus import
- [FSU040THUL-1155](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1155) #404 Ergänzung ORIGIN.xml (Erfurt)
- [FSU040THUL-1155](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1155) #406 Schriftfarbe anpassen "Es gibt eventuell eine Dublette" und "Damit verbunden: 1 Publikation(en)"
- [FSU040THUL-1218](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1218) #411 Erweiterung CSV-Export
- [FSU040THUL-972](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-972) #408 Added command stub "thunibib update funding of publications from url {0}"
- Update impressum.xml for Weimar
- Updated CHANGELOG.md
- Updated ORIGIN.xml (Ilmenau)
- Updated contact.xml in thunibib-weimar
- add first version of KDSF mapping for ilmenau
- adopt font of bibentries export in html to CD of TU Ilmenau
- minor layout improvements: (#435)
---

## 2023.10.25

- #330 Add ORCID integration from base UBO
- #425 Fixed error duplicate template 'layout.pageTitle'
- #441 Fixed resource leak in de.uni_jena.thunibib.ThUniBibCommands.updateProjects
- #443 Added classification nameIdentifier for each instance
- #455 Implement transformer for detailed mods:name metadata
- [FSU040THUL-1197](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1197) #421 Fehlender Link bei Online-Publikation ohne DOI
- [FSU040THUL-1214](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1214) Probleme bei Import: Konferenz-/Tagungsband im Standardformular
- [FSU040THUL-1243](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1243) #417 Added field connection_nid_text as facet
- [FSU040THUL-1278](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1278) #427 PPN Import: Corresponding Author wird beim Enrichment über Scopus ignoriert
- [FSU040THUL-1281](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1281) #429 Für KDSF-Konformität nötige Erfassungsfelder ergänzen
- [FSU040THUL-1281](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1281) #436 Position "Access Rights" sollte zwischen "Open Access" und "Lizenztyp" liegen
- [FSU040THUL-1564](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1564) Eingabemaske "Access Rights" und "Open Access"
- [FSU040THUL-206](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-206) #433 Anreicherung mit URL nur bei Closed Access Publikationen
- [FSU040THUL-405](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-405) #423 "Lücke" in den Ergebnissen der Personensuche
- [FSU040THUL-972](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-972) Allow to set multiple fundings during import, made fundingType repeatable in form
- [FSU040THUL-1017](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1017) #413 Mark corresponding author during scopus import
- [FSU040THUL-1155](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1155) #404 Ergänzung ORIGIN.xml (Erfurt)
- [FSU040THUL-1198](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1198) #406 Schriftfarbe anpassen "Es gibt eventuell eine Dublette" und "Damit verbunden: 1 Publikation(en)"
- [FSU040THUL-1218](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1218) #411 Erweiterung CSV-Export
- [FSU040THUL-972](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-972) #408 Added command stub "thunibib update funding of publications from url {0}"
- Updated ORIGIN.xml (Ilmenau)
- Adopt font of bibentries export in html to CD of TU Ilmenau
- Minor layout improvements
---

## 2023.08.11.01

- [FSU040THUL-1048](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1048) #402 Added xslt-remove-subject-from-list.xsl
---


## 2023.08.11

- #386 Set mycore version to 2022.06.3-SNAPSHOT
- #388 Add xslt stylesheet to remove eduPersonUniqueId
- #396 Added command "schedule pica import for query {0} and filter {1}"
- #398 Added UI to list currently stored MCRJobs
- #400 Configured MCR.Cronjob.Jobs.UpdateSolrProjectCore
- Added null check in ThUniBibImportJobAction
- Added null check in de.uni_jena.thunibib.ThUniBibMailer
- [FSU040THUL-1050](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1050) #392 Added order attributes to commands in EnrichmentByAffiliationCommands
- [FSU040THUL-1066](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-1066) #394 Added i18n ubo.relatedItem.format_other. Sort and formatted messages_en.properties and messages_de.properties
- [FSU040THUL-383](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-383) #384 Angabe bei mehrtägiger Konferenzdauer
- [FSU040THUL-841](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-841) #341 Import von Schlagwörtern erfolgt nur eingeschränkt
- [FSU040THUL-909](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-909) #376 Allow multiple origin entries for a given ppn (Erfurt)
- [FSU040THUL-913](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-913) #378 DESTATIS sollte nicht mehr während des Imports gesetzt werden
- [FSU040THUL-914](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-914) #381 Aktualisierung Logo Hochschulbibliografie (Ilmenau)
- [FSU040THUL-915](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-915) #380 Aktualisierung des Körperschaftsicons im IdentityPicker
- Set MCR.user2.matching.lead_id.skip = true (Ilmenau)
---

## 2023.07.11

- #355 Added xslt-set-partOf-false.xsl
- #357 Added stylesheet to fix connections ids
- #362 Added CONTRIBUTING.md
- #368 Set MCR.user2.IdentityManagement.UserCreation.Unvalidated.Realm = local
- #371 Provide classification user_attributes.xml for every thunibib instance and unify message properties for local IDs
- [FSU040THUL-618](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-618) #360 Anpassungen ADMIN-Editor
- [FSU040THUL-835](https://servicedesk.uni-jena.de/plugins/servlet/desk/portal/140/FSU040THUL-835) #366 Set $primary to #024975 in _variables_ilmenau.scss. Set backround-color to $primary for #navigationWrapper in _navigation.scss
- Prefer GBV as source (catalog import)
- Remove local matcher from chain, is always configured
- Fix MCR.IdentityPicker.strategy, set LDAPWithLocal as default
- Updated CHANGELOG.md
---

## 2023.06.26

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
- [UBO-238](https://mycore.atlassian.net/browse/UBO-238) Added/altered i18n keys
---

## 2023.04.04

- Enable JSON support for MyCoRe REST-API #316
