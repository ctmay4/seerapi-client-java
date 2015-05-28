/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.seerapi.client.surgery;

public class SurgeryTest {

    //    private static SeerApi _SEERAPI;
    //
    //    @BeforeClass
    //    public static void setup() {
    //        _SEERAPI = new SeerApiBuilder().connect();
    //    }
    //
    //    @Test
    //    public void testSiteSpecificSurgeryVersions() throws IOException {
    //        List<Version> versions = _SEERAPI.siteSpecificSurgeryVersions();
    //
    //        Assert.assertTrue(versions.size() > 0);
    //        for (Version version : versions) {
    //            Assert.assertTrue(version.getVersion().length() > 0);
    //            Assert.assertTrue(version.getCount() > 0);
    //        }
    //    }
    //
    //    @Test
    //    public void testSiteSpecificSurgeryTables() throws IOException {
    //        List<String> titles = _SEERAPI.siteSpecificSurgeryTables("2014");
    //
    //        Assert.assertTrue(titles.size() > 0);
    //        Assert.assertTrue(titles.contains("Oral Cavity"));
    //    }
    //
    //    @Test
    //    public void testSiteSpecificSurgeryTable() throws IOException {
    //        SurgeryTable table = _SEERAPI.siteSpecificSurgeryTable("2014", "Oral Cavity", null, null);
    //
    //        Assert.assertNotNull(table);
    //        Assert.assertEquals("Oral Cavity", table.getTitle());
    //        Assert.assertNotNull(table.getSiteInclusions());
    //        Assert.assertNotNull(table.getHistExclusions());
    //        Assert.assertNull(table.getHistInclusions());
    //        Assert.assertNotNull(table.getPreNote());
    //        Assert.assertNull(table.getPostNote());
    //        SurgeryRow row = table.getRows().get(0);
    //        Assert.assertEquals("00", row.getCode());
    //        Assert.assertNotNull(row.getDescription());
    //        Assert.assertEquals(Integer.valueOf(0), row.getLevel());
    //        Assert.assertFalse(row.getLineBreak());
    //
    //        table = _SEERAPI.siteSpecificSurgeryTable("2014", null, "C001", "8000");
    //        Assert.assertEquals("Oral Cavity", table.getTitle());
    //    }
}
