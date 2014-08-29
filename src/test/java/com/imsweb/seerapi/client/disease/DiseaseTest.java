/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.seerapi.client.disease;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.imsweb.seerapi.client.SeerApi;
import com.imsweb.seerapi.client.publishable.PublishableSearch;

public class DiseaseTest {

    @Test
    public void testDiseaseTypeCategory() {
        Assert.assertEquals(Disease.Type.SOLID_TUMOR, Disease.Type.valueOf("SOLID_TUMOR"));
    }

    @Test
    public void testDiseaseVersions() throws IOException {
        List<DiseaseVersion> versions = SeerApi.connect().diseaseVersions();

        Assert.assertTrue(versions.size() > 0);
        for (DiseaseVersion version : versions) {
            Assert.assertTrue(version.getName().length() > 0);
            Assert.assertTrue(version.getType().length() > 0);
            Assert.assertNotNull(version.getLastModified());
            if (version.getName().equals("latest")) {
                Assert.assertNotNull(version.getType());
                Assert.assertNotNull(version.getFirstPublished());
                Assert.assertNotNull(version.getCount());
            }
        }
    }

    @Test
    public void testDiseasePrimarySites() throws IOException {
        List<PrimarySite> sites = SeerApi.connect().diseasePrimarySites();

        Assert.assertTrue(sites.size() > 0);
        Assert.assertEquals("C000", sites.get(0).getValue());
        Assert.assertEquals("External upper lip", sites.get(0).getLabel());
    }

    @Test
    public void testDiseasePrimarySiteCode() throws IOException {
        List<PrimarySite> sites = SeerApi.connect().diseasePrimarySiteCode("C021");

        Assert.assertTrue(sites.size() > 0);
        Assert.assertEquals("C021", sites.get(0).getValue());
        Assert.assertEquals("Border of tongue", sites.get(0).getLabel());
    }

    @Test
    public void testDiseaseSiteCateogires() throws IOException {
        List<SiteCategory> categories = SeerApi.connect().diseaseSiteCategories();

        Assert.assertTrue(categories.size() > 0);
        Assert.assertEquals("head-and-neck", categories.get(0).getId());
        Assert.assertEquals("Head and Neck", categories.get(0).getLabel());
        Assert.assertEquals(2, categories.get(0).getSites().size());
        Assert.assertEquals("C000", categories.get(0).getSites().get(0).getLow());
        Assert.assertEquals("C148", categories.get(0).getSites().get(0).getHigh());
    }

    @Test
    public void testDiseaseById() throws IOException {
        Disease disease = SeerApi.connect().diseaseById("latest", "51f6cf58e3e27c3994bd5408");

        Assert.assertNotNull(disease);
        Assert.assertEquals("Acute erythroid leukemia", disease.getName());
        Assert.assertEquals(Disease.Type.HEMATO, disease.getType());
        Assert.assertEquals("9840/3", disease.getIcdO3Morphology());
        Assert.assertEquals(10, disease.getSamePrimaries().size());

        Assert.assertNotNull(disease.getId());
        Assert.assertEquals("latest", disease.getVersion());
        Assert.assertNull(disease.getHidden());
        Assert.assertNull(disease.getStatus());
        Assert.assertNull(disease.getAssignedTo());
        Assert.assertNotNull(disease.getFirstPublished());
        Assert.assertNotNull(disease.getLastModified());
        Assert.assertNotNull(disease.getFingerprint());
        Assert.assertNull(disease.getNote());
        Assert.assertNull(disease.getFieldNotes());
        Assert.assertNull(disease.getScore());
        Assert.assertNull(disease.getGlossaryMatches());

        Assert.assertEquals(7, disease.getHistory().size());

        DiseaseHistoryEvent event = disease.getHistory().get(0);
        Assert.assertEquals("mayc@imsweb.com", event.getUser());
        Assert.assertNotNull(event.getDate());
        Assert.assertNull(event.getOld());
        Assert.assertNull(event.getNew());

        Assert.assertEquals(1, disease.getPrimarySite().size());
        Assert.assertEquals("C421", disease.getPrimarySite().get(0).getLow());
        Assert.assertEquals("C421", disease.getPrimarySite().get(0).getHigh());
        Assert.assertNull(disease.getSiteCategory());
        Assert.assertEquals(2001, disease.getValid().getStartYear().longValue());
        Assert.assertNull(disease.getValid().getEndYear());
        Assert.assertNull(disease.getObsoleteNewCode());
        Assert.assertEquals(1, disease.getAbstractorNote().size());
        Assert.assertEquals(2, disease.getTreatment().size());
        Assert.assertNull(disease.getGenetics());
        Assert.assertEquals(12, disease.getAlternateName().size());
        Assert.assertEquals("Acute erythremia [OBS]", disease.getAlternateName().get(0).getValue());
        Assert.assertTrue(disease.getDefinition().get(0).getValue().startsWith("Acute erythroid leukemia is characterized by a predominant erythroid"));
        Assert.assertEquals("9840/3", disease.getIcdO2Morphology());
        Assert.assertEquals("9840/3", disease.getIcdO1Morphology());
        Assert.assertEquals("C94.0 Acute erythroid leukemia", disease.getIcd10CmCode().get(0));
        Assert.assertEquals("C94.0 Acute erythremia and erythroleukemia", disease.getIcd10Code().get(0));
        Assert.assertEquals("207.0 Acute erythremia and erythroleukemia", disease.getIcd9Code().get(0));
        Assert.assertNull(disease.getSigns());
        Assert.assertNull(disease.getExams());
        Assert.assertNull(disease.getRecurrence());
        Assert.assertNull(disease.getMortality());
        Assert.assertEquals(2001, disease.getIcdO3Effective().getStartYear().longValue());
        Assert.assertNull(disease.getIcdO3Effective().getEndYear());
        Assert.assertEquals(1992, disease.getIcdO2Effective().getStartYear().longValue());
        Assert.assertEquals(2000, disease.getIcdO2Effective().getEndYear().longValue());
        Assert.assertEquals(1978, disease.getIcdO1Effective().getStartYear().longValue());
        Assert.assertEquals(1991, disease.getIcdO1Effective().getEndYear().longValue());
        Assert.assertNull(disease.getMissingPrimarySiteMessage());
        Assert.assertNull(disease.getGrade());
        Assert.assertNull(disease.getTransformFrom());
        Assert.assertNull(disease.getTransformTo());
        Assert.assertNull(disease.getImmunophenotype());
        Assert.assertEquals("Bone marrow biopsy", disease.getDiagnosisMethod().get(0).getValue());
        Assert.assertEquals("M3 Module 5: PH10", disease.getModuleId().get(0).getValue());
        Assert.assertNull(disease.getBiomarkers());
        Assert.assertNull(disease.getTreatmentText());
    }

    @Test
    public void testDiseaseSamePrimary() throws IOException {
        SamePrimaries same = SeerApi.connect().diseaseSamePrimaries("latest", "9870/3", "9872/3", "2010");

        Assert.assertNotNull(same);
        Assert.assertEquals(false, same.isSame());
        Assert.assertEquals(2010, same.getYear().longValue());
        Assert.assertEquals("9870/3", same.getDisease1());
        Assert.assertEquals("9872/3", same.getDisease2());
    }

    @Test
    public void testDiseaseSearch() throws IOException {
        DiseaseSearch search = new DiseaseSearch();

        search.setType(Disease.Type.HEMATO);
        search.setQuery("basophilic");
        DiseaseSearchResults results = SeerApi.connect().diseaseSearch("latest", search);

        Assert.assertNotNull(results);
        Assert.assertEquals(3, results.getCount().longValue());
        Assert.assertEquals(3, results.getResults().size());
        Assert.assertEquals(Arrays.asList("basophilic"), results.getTerms());

        search.setSiteCategory("BAD_VALUE");
        results = SeerApi.connect().diseaseSearch("latest", search);

        Assert.assertNotNull(results);
        Assert.assertEquals(0, results.getCount().longValue());
        Assert.assertEquals(0, results.getResults().size());

        // test a case where all search options are set
        search.setMode(PublishableSearch.SearchMode.OR);
        search.setStatus("TEST");
        search.setAssignedTo("user");
        search.setModifiedFrom("2014-01-01");
        search.setModifiedTo("2014-05-31");
        search.setPublishedFrom("2014-01-01");
        search.setPublishedTo("2014-05-31");
        search.setBeenPublished(true);
        search.setHidden(false);
        search.setCount(100);
        search.setCountOnly(false);
        search.setIncludeGlossary(true);
        search.setOutputType(PublishableSearch.OutputType.MIN);
        results = SeerApi.connect().diseaseSearch("latest", search);

        Assert.assertNotNull(results);
        Assert.assertEquals(0, results.getCount().longValue());
        Assert.assertEquals(0, results.getResults().size());
    }

    @Test
    public void testDiseaseReportability() throws IOException {
        Disease partial = new Disease();

        partial.setType(Disease.Type.HEMATO);
        partial.setIcdO3Morphology("9840/3");
        partial.setIcdO2Morphology("9840/3");
        partial.setIcdO1Morphology("9840/3");
        partial.setIcdO3Effective(new YearRange(2001, null));
        partial.setIcdO2Effective(new YearRange(1992, 2000));
        partial.setIcdO1Effective(new YearRange(1978, 2001));
        partial.setPrimarySite(Arrays.asList(new SiteRange("C421", "C421")));

        Disease result = SeerApi.connect().diseaseReportability(partial);

        Assert.assertNotNull(result);
        Assert.assertNotNull(result.getReportable());
    }

    @Test
    public void testDiseaseChangelog() throws IOException {
        List<DiseaseChangelog> changes = SeerApi.connect().diseaseChangelogs("latest", null, "2013-07-30", 1);

        Assert.assertNotNull(changes);
        Assert.assertEquals(1, changes.size());
        Assert.assertNotNull(changes.get(0).getUser());

        DiseaseChangelog changelog = changes.get(0);

        Assert.assertNotNull(changelog.getUser());
        Assert.assertEquals("latest", changelog.getVersion());
        Assert.assertTrue(changelog.getId().length() > 0);
        Assert.assertEquals(300, changelog.getAdds().size());
        Assert.assertNull(changelog.getMods());
        Assert.assertNull(changelog.getDeletes());
        Assert.assertNotNull(changelog.getDate());
        Assert.assertEquals("Initial migration", changelog.getDescription());

        DiseaseChangelogEntry entry = changelog.getAdds().get(0);
        Assert.assertTrue(entry.getId().length() > 0);
        Assert.assertTrue(entry.getName().length() > 0);
        Assert.assertNull(entry.getOldVersion());
        Assert.assertNull(entry.getNewVersion());
    }

}
