/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.seerapi.client.staging;

import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.imsweb.seerapi.client.SeerApi;
import com.imsweb.seerapi.client.staging.cs.CsSchemaLookup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StagingTest {

    private static final String _ALGORITHM = "cs";
    private static final String _VERSION = "02.05.50";

    private static StagingService _STAGING;

    @BeforeClass
    public static void setup() {
        _STAGING = new SeerApi.Builder().connect().staging();
    }

    @Test
    public void testGetAlgorithms() throws IOException {
        List<StagingAlgorithm> algorithms = _STAGING.algorithms().execute().body();

        assertTrue(algorithms.size() > 0);
    }

    @Test
    public void testGetAlgorithmVersions() throws IOException {
        List<StagingVersion> versions = _STAGING.versions(_ALGORITHM).execute().body();

        assertTrue(versions.size() > 0);
    }

    @Test
    public void testListSchemas() throws IOException {
        List<StagingSchemaInfo> schemaInfos = _STAGING.schemas(_ALGORITHM, _VERSION).execute().body();
        assertTrue(schemaInfos.size() > 0);

        schemaInfos = _STAGING.schemas(_ALGORITHM, _VERSION, "skin").execute().body();
        assertTrue(schemaInfos.size() > 0);
    }

    @Test
    public void testSchemaLookup() throws IOException {
        // first test simple lookup that returns a single schema with site/hist only
        List<StagingSchemaInfo> schemas = _STAGING.schemaLookup(_ALGORITHM, _VERSION, new CsSchemaLookup("C509", "8000").getInputs()).execute().body();
        assertEquals(1, schemas.size());
        assertEquals("breast", schemas.get(0).getId());

        // now test just site
        SchemaLookup data = new SchemaLookup();
        data.setInput(StagingData.PRIMARY_SITE_KEY, "C111");
        assertEquals(7, _STAGING.schemaLookup(_ALGORITHM, _VERSION, data.getInputs()).execute().body().size());

        // add histology
        data.setInput(StagingData.HISTOLOGY_KEY, "8000");
        assertEquals(2, _STAGING.schemaLookup(_ALGORITHM, _VERSION, data.getInputs()).execute().body().size());

        // add discriminator
        data.setInput("ssf25", "010");
        schemas = _STAGING.schemaLookup(_ALGORITHM, _VERSION, data.getInputs()).execute().body();
        assertEquals(1, schemas.size());
        assertEquals("nasopharynx", schemas.get(0).getId());

        // test with the CsStaging class
        schemas = _STAGING.schemaLookup(_ALGORITHM, _VERSION, new CsSchemaLookup("C111", "8000", "010").getInputs()).execute().body();
        assertEquals(1, schemas.size());
        assertEquals("nasopharynx", schemas.get(0).getId());
    }

    @Test
    public void testSchemaById() throws IOException {
        StagingSchema schema = _STAGING.schemaById(_ALGORITHM, _VERSION, "brain").execute().body();

        assertEquals("cs", schema.getAlgorithm());
        assertEquals("02.05.50", schema.getVersion());
        assertEquals("brain", schema.getId());
    }

    @Test
    public void testSchemaInvolvedTables() throws IOException {
        List<StagingTable> tables = _STAGING.involvedTables(_ALGORITHM, _VERSION, "brain").execute().body();

        assertTrue(tables.size() > 0);
    }

    @Test
    public void testListTables() throws IOException {
        List<StagingTable> tables = _STAGING.tables(_ALGORITHM, _VERSION, "ssf1").execute().body();

        assertTrue(tables.size() > 0);
    }

    @Test
    public void testTableById() throws IOException {
        StagingTable table = _STAGING.tableById(_ALGORITHM, _VERSION, "primary_site").execute().body();

        assertEquals("cs", table.getAlgorithm());
        assertEquals("02.05.50", table.getVersion());
        assertEquals("primary_site", table.getId());
    }

    @Test
    public void testTableInvoledSchemas() throws IOException {
        List<StagingSchema> schemas = _STAGING.involvedSchemas(_ALGORITHM, _VERSION, "extension_baa").execute().body();

        assertTrue(schemas.size() > 0);
    }

    @Test
    public void testStaging() throws IOException {
        // test this case:  http://seer.cancer.gov/seertools/cstest/?mets=10&lnexam=99&diagnosis_year=2013&grade=9&exteval=9&age=060&site=C680&metseval=9&hist=8000&ext=100&version=020550&nodeseval=9&behav=3&lnpos=99&nodes=100&csver_original=020440&lvi=9&ssf1=020&size=075
        StagingData data = new StagingData();
        data.setInput("site", "C680");
        data.setInput("hist", "8000");
        data.setInput("behavior", "3");
        data.setInput("grade", "9");
        data.setInput("year_dx", "2013");
        data.setInput("cs_input_version_original", "020550");
        data.setInput("size", "075");
        data.setInput("extension", "100");
        data.setInput("extension_eval", "9");
        data.setInput("nodes", "100");
        data.setInput("nodes_eval", "9");
        data.setInput("nodes_pos", "99");
        data.setInput("nodes_exam", "99");
        data.setInput("mets", "10");
        data.setInput("mets_eval", "9");
        data.setInput("lvi", "9");
        data.setInput("age_dx", "060");
        data.setInput("ssf1", "020");

        // perform the staging
        StagingData output = _STAGING.stage(_ALGORITHM, _VERSION, data.getInput()).execute().body();

        assertEquals(StagingData.Result.STAGED, output.getResult());
        assertEquals(0, output.getErrors().size());
        assertEquals(37, output.getPath().size());

        // check output
        assertEquals("129", output.getOutput("schema_number"));
        assertEquals("urethra", output.getSchemaId());
        assertEquals("020550", output.getOutput("csver_derived"));

        // AJCC 6
        assertEquals("T1", output.getOutput("ajcc6_t"));
        assertEquals("10", output.getOutput("stor_ajcc6_t"));
        assertEquals("c", output.getOutput("ajcc6_tdescriptor"));
        assertEquals("c", output.getOutput("stor_ajcc6_tdescriptor"));
        assertEquals("N1", output.getOutput("ajcc6_n"));
        assertEquals("10", output.getOutput("stor_ajcc6_n"));
        assertEquals("c", output.getOutput("ajcc6_ndescriptor"));
        assertEquals("c", output.getOutput("stor_ajcc6_ndescriptor"));
        assertEquals("M1", output.getOutput("ajcc6_m"));
        assertEquals("10", output.getOutput("stor_ajcc6_m"));
        assertEquals("c", output.getOutput("ajcc6_mdescriptor"));
        assertEquals("c", output.getOutput("stor_ajcc6_mdescriptor"));
        assertEquals("IV", output.getOutput("ajcc6_stage"));
        assertEquals("70", output.getOutput("stor_ajcc6_stage"));

        // AJCC 7
        assertEquals("T1", output.getOutput("ajcc7_t"));
        assertEquals("100", output.getOutput("stor_ajcc7_t"));
        assertEquals("c", output.getOutput("ajcc7_tdescriptor"));
        assertEquals("c", output.getOutput("stor_ajcc7_tdescriptor"));
        assertEquals("N1", output.getOutput("ajcc7_n"));
        assertEquals("100", output.getOutput("stor_ajcc7_n"));
        assertEquals("c", output.getOutput("ajcc7_ndescriptor"));
        assertEquals("c", output.getOutput("stor_ajcc7_ndescriptor"));
        assertEquals("M1", output.getOutput("ajcc7_m"));
        assertEquals("100", output.getOutput("stor_ajcc7_m"));
        assertEquals("c", output.getOutput("ajcc7_mdescriptor"));
        assertEquals("c", output.getOutput("stor_ajcc7_mdescriptor"));
        assertEquals("IV", output.getOutput("ajcc7_stage"));
        assertEquals("700", output.getOutput("stor_ajcc7_stage"));

        // Summary Stage
        assertEquals("L", output.getOutput("t77"));
        assertEquals("RN", output.getOutput("n77"));
        assertEquals("D", output.getOutput("m77"));
        assertEquals("D", output.getOutput("ss77"));
        assertEquals("7", output.getOutput("stor_ss77"));
        assertEquals("L", output.getOutput("t2000"));
        assertEquals("RN", output.getOutput("n2000"));
        assertEquals("D", output.getOutput("m2000"));
        assertEquals("D", output.getOutput("ss2000"));
        assertEquals("7", output.getOutput("stor_ss2000"));
    }

    @Test
    public void testStagingWithErrors() throws IOException {
        StagingData data = new StagingData();
        data.setInput("site", "C181");
        data.setInput("hist", "8093");
        data.setInput("year_dx", "2015");
        data.setInput("extension", "670");

        // perform the staging
        StagingData output = _STAGING.stage(_ALGORITHM, _VERSION, data.getInput()).execute().body();

        assertEquals(StagingData.Result.STAGED, output.getResult());
        assertEquals(9, output.getErrors().size());
    }

}
