/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.seerapi.client.siterecode;

import java.io.IOException;

import javax.ws.rs.BadRequestException;

import org.junit.Assert;
import org.junit.Test;

import com.imsweb.seerapi.client.SeerApi;

public class SiteRecodeTest {

    @Test(expected = BadRequestException.class)
    public void testBadRequestExceptiion() throws IOException {
        SeerApi.connect().siteRecode("C379", null);
    }

    @Test
    public void testExceptionMessages() throws IOException {
        String message = "";

        try {
            SeerApi.connect().siteRecode("C379", null);
        }
        catch (BadRequestException e) {
            message = e.getMessage();
        }

        // the API call works out to:
        //     https://api.seer.cancer.gov/rest/recode/sitegroup?site=C379
        // and the full message returned should be
        //     {"code":400,"message":"Site and histology must be supplied"}
        Assert.assertEquals("Site and histology must be supplied", message);
    }

    @Test
    public void testSiteRecordVersion() throws IOException {
        String version = SeerApi.connect().siteRecodeVersion();
        Assert.assertNotNull(version);
        Assert.assertTrue(version.length() > 0);
    }

    @Test
    public void testSiteRecode() throws IOException {
        SiteRecode recode = SeerApi.connect().siteRecode("C379", "9650");

        Assert.assertEquals("C379", recode.getSite());
        Assert.assertEquals("9650", recode.getHist());
        Assert.assertEquals("33011", recode.getSiteGroup());
    }

}