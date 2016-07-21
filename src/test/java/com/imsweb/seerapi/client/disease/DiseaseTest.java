/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.seerapi.client.disease;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.imsweb.seerapi.client.SeerApi;
import com.imsweb.seerapi.client.publishable.PublishableSearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class DiseaseTest {

    private static DiseaseService _DISEASE;

    @BeforeClass
    public static void setup() {
        _DISEASE = new SeerApi.Builder().connect().disease();
    }

    @Test
    public void testDiseaseTypeCategory() {
        assertEquals(Disease.Type.SOLID_TUMOR, Disease.Type.valueOf("SOLID_TUMOR"));
    }

    @Test
    public void testDiseaseVersions() throws IOException {
        List<DiseaseVersion> versions = _DISEASE.versions().execute().body();

        assertTrue(versions.size() > 0);
        for (DiseaseVersion version : versions) {
            assertTrue(version.getName().length() > 0);
            assertTrue(version.getType().length() > 0);
            assertNotNull(version.getLastModified());
            if (version.getName().equals("latest")) {
                assertNotNull(version.getType());
                assertNotNull(version.getFirstPublished());
                assertNotNull(version.getCount());
            }
        }
    }

    @Test
    public void testDiseasePrimarySites() throws IOException {
        List<PrimarySite> sites = _DISEASE.primarySites().execute().body();

        assertTrue(sites.size() > 0);
        assertEquals("C000", sites.get(0).getValue());
        assertEquals("External upper lip", sites.get(0).getLabel());
    }

    @Test
    public void testDiseasePrimarySiteCode() throws IOException {
        List<PrimarySite> sites = _DISEASE.primarySiteCode("C021").execute().body();

        assertTrue(sites.size() > 0);
        assertEquals("C021", sites.get(0).getValue());
        assertEquals("Border of tongue", sites.get(0).getLabel());
    }

    @Test
    public void testDiseaseSiteCateogires() throws IOException {
        List<SiteCategory> categories = _DISEASE.siteCategories().execute().body();

        assertTrue(categories.size() > 0);
        assertEquals("head-and-neck", categories.get(0).getId());
        assertEquals("Head and Neck", categories.get(0).getLabel());
        assertEquals(2, categories.get(0).getSites().size());
        assertEquals("C000", categories.get(0).getSites().get(0).getLow());
        assertEquals("C148", categories.get(0).getSites().get(0).getHigh());
    }

    @Test
    public void testDiseaseById() throws IOException {
        Disease disease = _DISEASE.getById("latest", "51f6cf58e3e27c3994bd5408").execute().body();

        assertNotNull(disease);
        assertEquals("Acute erythroid leukemia", disease.getName());
        assertEquals(Disease.Type.HEMATO, disease.getType());
        assertEquals("9840/3", disease.getIcdO3Morphology());
        assertTrue(disease.getSamePrimaries().size() > 0);

        assertNotNull(disease.getId());
        assertEquals("latest", disease.getVersion());
        assertNull(disease.getHidden());
        assertNull(disease.getStatus());
        assertNull(disease.getAssignedTo());
        assertNotNull(disease.getFirstPublished());
        assertNotNull(disease.getLastModified());
        assertNotNull(disease.getFingerprint());
        assertNull(disease.getNote());
        assertNull(disease.getFieldNotes());
        assertNull(disease.getScore());
        assertNull(disease.getGlossaryMatches());
        //assertNull(disease.getHistory());

        //        assertTrue(disease.getHistory().size() > 0);
        //
        //        DiseaseHistoryEvent event = disease.getHistory().get(0);
        //        assertEquals("mayc@imsweb.com", event.getUser());
        //        assertNotNull(event.getDate());
        //        assertNull(event.getOld());
        //        assertNull(event.getNew());

        assertEquals(1, disease.getPrimarySite().size());
        assertEquals("C421", disease.getPrimarySite().get(0).getLow());
        assertEquals("C421", disease.getPrimarySite().get(0).getHigh());
        assertNull(disease.getSiteCategory());
        assertEquals(2001, disease.getValid().getStartYear().longValue());
        assertNull(disease.getValid().getEndYear());
        assertNull(disease.getObsoleteNewCode());
        assertEquals(1, disease.getAbstractorNote().size());
        assertEquals(2, disease.getTreatment().size());
        assertNull(disease.getGenetics());
        assertEquals(12, disease.getAlternateName().size());
        assertEquals("Acute erythremia [OBS]", disease.getAlternateName().get(0).getValue());
        assertTrue(disease.getDefinition().get(0).getValue().startsWith("Acute erythroid leukemia is characterized by a predominant erythroid"));
        assertEquals(Collections.singletonList("9840/3"), disease.getIcdO2Morphology());
        assertEquals(Collections.singletonList("9840/3"), disease.getIcdO1Morphology());
        assertEquals("C94.0 Acute erythroid leukemia", disease.getIcd10CmCode().get(0).getValue());
        assertEquals("C94.0 Acute erythremia and erythroleukemia", disease.getIcd10Code().get(0));
        assertEquals("207.0 Acute erythremia and erythroleukemia", disease.getIcd9Code().get(0));
        assertNotNull(disease.getSigns());
        assertNotNull(disease.getExams());
        assertNull(disease.getRecurrence());
        assertNotNull(disease.getMortality());
        assertEquals(2001, disease.getIcdO3Effective().getStartYear().longValue());
        assertNull(disease.getIcdO3Effective().getEndYear());
        assertEquals(1992, disease.getIcdO2Effective().getStartYear().longValue());
        assertEquals(2000, disease.getIcdO2Effective().getEndYear().longValue());
        assertEquals(1978, disease.getIcdO1Effective().getStartYear().longValue());
        assertEquals(1991, disease.getIcdO1Effective().getEndYear().longValue());
        assertNull(disease.getMissingPrimarySiteMessage());
        assertNull(disease.getGrade());
        assertNull(disease.getTransformFrom());
        assertNull(disease.getTransformTo());
        assertNull(disease.getImmunophenotype());
        assertEquals("Bone marrow biopsy", disease.getDiagnosisMethod().get(0).getValue());
        assertEquals("See abstractor notes", disease.getModuleId().get(0).getValue());
        assertNull(disease.getBiomarkers());
        assertNull(disease.getTreatmentText());
    }

    @Test
    public void testDiseaseSamePrimary() throws IOException {
        SamePrimaries same = _DISEASE.samePrimaries("latest", "9870/3", "9872/3", "2010").execute().body();

        assertNotNull(same);
        assertEquals(false, same.isSame());
        assertEquals(2010, same.getYear().longValue());
        assertEquals("9870/3", same.getDisease1());
        assertEquals("9872/3", same.getDisease2());
    }

    @Test
    public void testDiseaseSearch() throws IOException {
        DiseaseSearch search = new DiseaseSearch("basophilic", Disease.Type.HEMATO);

        DiseaseSearchResults results = _DISEASE.search("latest", search.paramMap()).execute().body();

        assertNotNull(results);
        assertEquals(25, results.getCount().longValue());
        assertEquals(3, results.getTotal().longValue());
        assertEquals(3, results.getResults().size());
        assertEquals(Collections.singletonList("basophilic"), results.getTerms());

        search.setSiteCategory("BAD_VALUE");
        results = _DISEASE.search("latest", search.paramMap()).execute().body();

        assertNotNull(results);
        assertEquals(25, results.getCount().longValue());
        assertEquals(0, results.getTotal().longValue());
        assertNull(results.getResults());

        // test a case where all search options are set
        search.setMode(PublishableSearch.SearchMode.OR);
        search.setStatus("TEST");
        search.setAssignedTo("user");
        search.setModifiedFrom("2014-01-01");
        search.setModifiedTo("2014-05-31");
        search.setPublishedFrom("2014-01-01");
        search.setPublishedTo("2014-05-31");
        search.setCount(100);
        search.setOffset(0);
        search.setOrderBy("name");
        search.setOutputType(PublishableSearch.OutputType.MIN);
        results = _DISEASE.search("latest", search.paramMap()).execute().body();

        assertNotNull(results);
        assertEquals(100, results.getCount().longValue());
        assertEquals(0, results.getTotal().longValue());
        assertNull(results.getResults());

        // test searching without type
        results = _DISEASE.search("latest", "basophilic").execute().body();

        assertNotNull(results);
        assertEquals(25, results.getCount().longValue());
        assertEquals(4, results.getTotal().longValue());
        assertEquals(4, results.getResults().size());
        assertEquals(Collections.singletonList("basophilic"), results.getTerms());
    }

    @Test
    public void testDiseaseSearchIterate() throws IOException {
        DiseaseSearch search = new DiseaseSearch();
        search.setOutputType(PublishableSearch.OutputType.FULL);
        search.setCount(100);
        search.setOffset(0);

        Integer total = null;

        while (total == null || search.getOffset() < total) {
            DiseaseSearchResults results = _DISEASE.search("latest", search.paramMap()).execute().body();
            assertNotNull(results);
            assertTrue(results.getTotal() > 0);
            assertTrue(results.getResults().size() > 0);

            // the first time through, set the total
            if (total == null)
                total = results.getTotal();

            search.setOffset(search.getOffset() + results.getResults().size());
        }
    }

    @Test
    public void testDiseaseReportability() throws IOException {
        Disease partial = new Disease();

        partial.setType(Disease.Type.HEMATO);
        partial.setIcdO3Morphology("9840/3");
        partial.setIcdO2Morphology(Collections.singletonList("9840/3"));
        partial.setIcdO1Morphology(Collections.singletonList("9840/3"));
        partial.setIcdO3Effective(new YearRange(2001, null));
        partial.setIcdO2Effective(new YearRange(1992, 2000));
        partial.setIcdO1Effective(new YearRange(1978, 2001));
        partial.setPrimarySite(Collections.singletonList(new SiteRange("C421", "C421")));

        Disease result = _DISEASE.reportability(partial).execute().body();

        assertNotNull(result);
        assertNotNull(result.getReportable());
    }

    @Test
    public void testDiseaseChangelog() throws IOException {
        DiseaseChangelogResults results = _DISEASE.diseaseChangelogs("latest", null, "2013-07-30", 1).execute().body();

        assertNotNull(results);
        assertEquals(1, results.getChangelogs().size());
        assertNotNull(results.getChangelogs().get(0).getUser());

        DiseaseChangelog changelog = results.getChangelogs().get(0);

        assertNotNull(changelog.getUser());
        assertEquals("latest", changelog.getVersion());
        assertEquals(300, changelog.getAdds().size());
        assertNull(changelog.getMods());
        assertNull(changelog.getDeletes());
        assertNotNull(changelog.getDate());
        assertEquals("Initial migration", changelog.getDescription());

        DiseaseChangelogEntry entry = changelog.getAdds().get(0);
        assertTrue(entry.getId().length() > 0);
        assertTrue(entry.getName().length() > 0);
        assertNull(entry.getOldVersion());
        assertNull(entry.getNewVersion());
    }

}
