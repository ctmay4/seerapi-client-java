/*
 * Copyright (C) 2014 Information Management Services, Inc.
 */
package com.imsweb.seerapi.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.imsweb.seerapi.client.cs.CsCodeValidity;
import com.imsweb.seerapi.client.cs.CsInput;
import com.imsweb.seerapi.client.cs.CsResult;
import com.imsweb.seerapi.client.cs.CsSchema;
import com.imsweb.seerapi.client.cs.CsSchemaExistence;
import com.imsweb.seerapi.client.cs.CsSchemaName;
import com.imsweb.seerapi.client.cs.CsTable;
import com.imsweb.seerapi.client.cs.CsVersion;
import com.imsweb.seerapi.client.disease.Disease;
import com.imsweb.seerapi.client.disease.DiseaseSearch;
import com.imsweb.seerapi.client.disease.DiseaseSearchResults;
import com.imsweb.seerapi.client.disease.DiseaseVersion;
import com.imsweb.seerapi.client.disease.PrimarySite;
import com.imsweb.seerapi.client.disease.SamePrimaries;
import com.imsweb.seerapi.client.disease.SiteCategory;
import com.imsweb.seerapi.client.naaccr.NaaccrField;
import com.imsweb.seerapi.client.naaccr.NaaccrFieldName;
import com.imsweb.seerapi.client.naaccr.NaaccrVersion;
import com.imsweb.seerapi.client.shared.Version;
import com.imsweb.seerapi.client.siterecode.SiteRecode;

/**
 * Entry point for Java API into SEER*API.
 */
public final class SeerApi {

    // default base URL
    private static final String _SEERAPI_URL = "https://api.seer.cancer.gov/rest";

    // output all dates in ISO-8061 format and UTC time
    private static final DateFormat _DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    // define the JSON provider which uses Jackson and a customized ObjectMapper
    private static final JacksonJsonProvider _JACKSON_PROVIDER = new JacksonJsonProvider();

    static {
        _DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));

        ObjectMapper mapper = new ObjectMapper();

        // do not write null values
        mapper.configure(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES, false);
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

        // set Date objects to output in readable customized format
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.setDateFormat(_DATE_FORMAT);

        mapper.setVisibility(JsonMethod.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);

        _JACKSON_PROVIDER.setMapper(mapper);
    }

    private Client _client;
    private String _baseUrl;
    private String _apiKey;

    /**
     * Creates a client API root object
     * @param baseUrl
     * @param apiKey
     * @throws IOException
     */
    private SeerApi(String baseUrl, String apiKey) throws IOException {
        if (baseUrl.endsWith("/"))
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);

        this._baseUrl = baseUrl;
        this._apiKey = apiKey;

        _client = ClientBuilder.newClient().register(_JACKSON_PROVIDER).register(GzipInterceptor.class).register(ErrorResponseFilter.class);
    }

    /**
     * Creates a connection to the API using the key stored in ~/.seerapi
     * @return a new SeerApi instance
     * @throws IOException
     */
    public static SeerApi connect() throws IOException {
        Properties props = new Properties();
        FileInputStream in = new FileInputStream(new File(System.getProperty("user.home"), ".seerapi"));

        try {
            props.load(in);
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (IOException ioe) {
                // ignore
            }
        }

        return new SeerApi(_SEERAPI_URL, props.getProperty("apikey"));
    }

    /**
     * Creates a connection to the API
     * @param apiKey
     * @return a new SeerApi instance
     * @throws IOException
     */
    public static SeerApi connect(String apiKey) throws IOException {
        return new SeerApi(_SEERAPI_URL, apiKey);
    }

    /**
     * Creates a connection to the API
     * @param baseUrl
     * @param apiKey
     * @return a new SeerApi instance
     * @throws IOException
     */
    public static SeerApi connect(String baseUrl, String apiKey) throws IOException {
        return new SeerApi(baseUrl, apiKey);
    }

    /**
     * Helper method to return a base web target
     * @param path
     * @return a WebTarget using the base URL and the passed path
     */
    private WebTarget createTarget(String path) {
        if (_apiKey == null || _apiKey.isEmpty())
            throw new IllegalStateException("This operation requires a credential but none is given to the SeerApi constructor");

        return _client.target(_baseUrl).path(path);
    }

    /**
     * Builds the default invocation builder.  All requests are currently JSON and are GZIP encoded.
     * @param target
     * @return a Builder instance using the passed target and including default header information that is used on all our calls
     */
    private Invocation.Builder getBuilder(WebTarget target) {
        return target.request().header("X-SEERAPI-Key", _apiKey).accept(MediaType.APPLICATION_JSON_TYPE).acceptEncoding("gzip");
    }

    /**
     * Return the version of the SEER Site Recode database.
     * @return a String representing the database version
     */
    public String siteRecodeVersion() {
        WebTarget target = createTarget("/recode/version");

        return getBuilder(target).get(Version.class).getVersion();
    }

    /**
     * Return the SEER Site Group for the site/histology combination, or 99999 if the combination is unknown.
     * @param site
     * @param histology
     * @return a SiteRecode object based on the site and histology
     * @throws IOException
     */
    public SiteRecode siteRecode(String site, String histology) throws IOException {
        WebTarget target = createTarget("/recode/sitegroup").queryParam("site", site).queryParam("hist", histology);

        return getBuilder(target).get(SiteRecode.class);
    }

    /**
     * Return a collection of NaaccrVersion objects which descibe the available versions
     * @return a list of the available NAACCR versions and information about each of them
     */
    public List<NaaccrVersion> naaccrVersions() {
        WebTarget target = createTarget("/naaccr/versions");

        return getBuilder(target).get(new GenericType<List<NaaccrVersion>>() {});
    }

    /**
     * Return a list of all the field identifiers and names from a specified NAACCR version
     * @param version
     * @return a list of NaaccrFieldName objects
     */
    public List<NaaccrFieldName> naaccrFieldNames(String version) {
        WebTarget target = createTarget("/naaccr/{version}").resolveTemplate("version", version);

        return getBuilder(target).get(new GenericType<List<NaaccrFieldName>>() {});
    }

    /**
     * Return a list of all the field identifiers and names from a specified NAACCR version
     * @param version
     * @return a list of NaaccrFieldName objects
     */
    public NaaccrField naaccrField(String version, Integer item) {
        WebTarget target = createTarget("/naaccr/{version}/item/{item}").resolveTemplate("version", version).resolveTemplate("item", item);

        return getBuilder(target).get(NaaccrField.class);
    }

    /**
     * Return a collection of CsVersion objects which descibe the available versions
     * @return a list of the available Collaborative Staging versions and information about each of them
     */
    public List<CsVersion> csVersions() {
        WebTarget target = createTarget("/cstage/versions");

        return getBuilder(target).get(new GenericType<List<CsVersion>>() {});
    }

    /**
     * Return the list of schema names and identifiers for the specifid version
     * @param version
     * @return a list of CsCschemaName objects
     */
    public List<CsSchemaName> csSchemas(String version) {
        WebTarget target = createTarget("/cstage/{version}").resolveTemplate("version", version);

        return getBuilder(target).get(new GenericType<List<CsSchemaName>>() {});
    }

    /**
     * Checks whether a site/histology combination maps to an existing schema for the passed CS version
     * @param version
     * @param site
     * @param histology
     * @return a CsSchemaExistence object which includes information about the matching schema
     */
    public CsSchemaExistence csSchemaExists(String version, String site, String histology) {
        WebTarget target = createTarget("/cstage/{version}/check_schema_exists").resolveTemplate("version", version).queryParam("site", site).queryParam("hist", histology);

        return getBuilder(target).get(CsSchemaExistence.class);
    }

    /**
     * Return a full schema based on schema number
     * @param version
     * @param schemaNumber
     * @return a csSchema object
     */
    public CsSchema csSchema(String version, Integer schemaNumber) {
        WebTarget target = createTarget("/cstage/{version}/schema").resolveTemplate("version", version).queryParam("id", schemaNumber);

        return getBuilder(target).get(CsSchema.class);
    }

    /**
     * Return a full schema based on primary site, histology and site-specific factor 25
     * @param version
     * @param site
     * @param histology
     * @param ssf25
     * @return a CsSchema object
     */
    public CsSchema csSchema(String version, String site, String histology, String ssf25) {
        WebTarget target = createTarget("/cstage/{version}/schema").resolveTemplate("version", version).queryParam("site", site).queryParam("hist", histology).queryParam("ssf25", ssf25);

        return getBuilder(target).get(CsSchema.class);
    }

    /**
     * Tests whether a code is valid or obsolete.
     * @param version
     * @param schemaNumber
     * @param tableNumber
     * @param code
     * @return a CsCodeValidity object indicating validity and whether the code is obsolete
     */
    public CsCodeValidity csValidCode(String version, Integer schemaNumber, Integer tableNumber, String code) {
        WebTarget target = createTarget("/cstage/{version}/is_code_valid").resolveTemplate("version", version).queryParam("id", schemaNumber).queryParam("table", tableNumber).queryParam("code", code);

        return getBuilder(target).get(CsCodeValidity.class);
    }

    /**
     * Return the specified CS table
     * @param version
     * @param schemaNumber
     * @param tableNumber
     * @return a CsTable object
     */
    public CsTable csTable(String version, Integer schemaNumber, Integer tableNumber) {
        WebTarget target = createTarget("/cstage/{version}/table").resolveTemplate("version", version).queryParam("id", schemaNumber).queryParam("table", tableNumber);

        return getBuilder(target).get(CsTable.class);
    }

    /**
     * Calculate collaborative stage
     * @param version
     * @param input
     * @return a CsResult object containing the staging results
     */
    public CsResult csCalculate(String version, CsInput input) {
        WebTarget target = createTarget("/cstage/{version}/calculate").resolveTemplate("version", version);

        target = target.queryParam("site", input.getSite()).queryParam("hist", input.getHistology()).queryParam("diagnosis_year", input.getDiagnosisYear()).queryParam("csver_original", input.getCsVersionOriginal()).queryParam("behav", input.getBehavior()).queryParam("grade", input.getGrade()).queryParam("age", input.getAge()).queryParam("lvi", input.getLvi()).queryParam("size", input.getTumorSize()).queryParam("ext", input.getExtension()).queryParam("exteval", input.getExtensionEval()).queryParam("nodes", input.getLymphNodes()).queryParam("nodeseval", input.getLymphNodesEval()).queryParam("lnpos", input.getLymphNodesPositive()).queryParam("lnexam", input.getLymphNodesExamined()).queryParam("mets", input.getMetsAtDx()).queryParam("metseval", input.getMetsEval()).queryParam("ssf1", input.getSsf1()).queryParam("ssf2", input.getSsf2()).queryParam("ssf3", input.getSsf3()).queryParam("ssf4", input.getSsf4()).queryParam("ssf5", input.getSsf5()).queryParam("ssf6", input.getSsf6()).queryParam("ssf7", input.getSsf7()).queryParam("ssf8", input.getSsf8()).queryParam("ssf9", input.getSsf9()).queryParam("ssf10", input.getSsf10()).queryParam("ssf11", input.getSsf11()).queryParam("ssf12", input.getSsf12()).queryParam("ssf13", input.getSsf13()).queryParam("ssf14", input.getSsf14()).queryParam("ssf15", input.getSsf15()).queryParam("ssf16", input.getSsf16()).queryParam("ssf17", input.getSsf17()).queryParam("ssf18", input.getSsf18()).queryParam("ssf19", input.getSsf19()).queryParam("ssf10", input.getSsf20()).queryParam("ssf21", input.getSsf21()).queryParam("ssf22", input.getSsf22()).queryParam("ssf23", input.getSsf23()).queryParam("ssf24", input.getSsf24()).queryParam("ssf25", input.getSsf25());

        return getBuilder(target).get(CsResult.class);
    }

    /**
     * Return a list of all disease versions and information about them
     * @return a list of DiseaseVersion objects
     */
    public List<DiseaseVersion> diseaseVersions() {
        WebTarget target = createTarget("/disease/versions");

        return getBuilder(target).get(new GenericType<List<DiseaseVersion>>() {});
    }

    // TODO version changelog

    public DiseaseSearchResults diseaseSearch(String version, DiseaseSearch search) {
        WebTarget target = createTarget("/disease/{version}").resolveTemplate("version", version);

        target = target.queryParam("q", search.getQuery()).queryParam("type", search.getType()).queryParam("site_category", search.getSiteCategory()).queryParam("mode", search.getMode()).queryParam("status", search.getStatus()).queryParam("assigned_to", search.getAssignedTo()).queryParam("modified_from", search.getModifiedFrom()).queryParam("modified_to", search.getModifiedTo()).queryParam("published_from", search.getPublishedFrom()).queryParam("published_to", search.getPublishedTo()).queryParam("been_published", search.getBeenPublished()).queryParam("hidden", search.getHidden()).queryParam("count", search.getCount()).queryParam("count_only", search.getCountOnly()).queryParam("glossary", search.getIncludeGlossary()).queryParam("output_type", search.getOutputType());

        return getBuilder(target).get(DiseaseSearchResults.class);
    }

    /**
     * Return a complete disease entity based in identifier
     * @param version
     * @param id
     * @return a Disease object
     */
    public Disease diseaseById(String version, String id) {
        WebTarget target = createTarget("/disease/{version}/id/{id}").resolveTemplate("version", version).resolveTemplate("id", id);

        return getBuilder(target).get(Disease.class);
    }

    /**
     * Return a list of all primary sites and labels
     * @return a List of PrimarySite objects
     */
    public List<PrimarySite> diseasePrimarySites() {
        WebTarget target = createTarget("/disease/primary_site");

        return getBuilder(target).get(new GenericType<List<PrimarySite>>() {});
    }

    /**
     * Return a single primary site and label
     * @param primarySite
     * @return a PrimarySite object
     */
    public List<PrimarySite> diseasePrimarySiteCode(String primarySite) {
        WebTarget target = createTarget("/disease/primary_site/{code}").resolveTemplate("code", primarySite);

        return getBuilder(target).get(new GenericType<List<PrimarySite>>() {});
    }

    /**
     * Return a complete list of site categories and definitions
     * @return a list of SiteCategory objects
     */
    public List<SiteCategory> diseaseSiteCategories() {
        WebTarget target = createTarget("/disease/site_categories");

        return getBuilder(target).get(new GenericType<List<SiteCategory>>() {});
    }

    /**
     * Return whether the 2 morphologies represent the same primary for the given year.
     * @param version
     * @param morphology1
     * @param morphology2
     * @param year
     * @return a SamePrimary object
     */
    public SamePrimaries diseaseSamePrimaries(String version, String morphology1, String morphology2, String year) {
        WebTarget target = createTarget("/disease/{version}/same_primary").resolveTemplate("version", version).queryParam("d1", morphology1).queryParam("d2", morphology2).queryParam("year", year);

        return getBuilder(target).get(SamePrimaries.class);
    }

    /**
     * Returns the reportable year range of the supplied disease.
     * @param disease
     * @return a Disease object with the reportability field filled in
     */
    public Disease diseaseReportability(Disease disease) {
        WebTarget target = createTarget("/disease/reportability");

        Entity<Disease> entity = Entity.json(disease);
        return getBuilder(target).post(entity, Disease.class);

    }

}
