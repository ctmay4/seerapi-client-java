package com.imsweb.seerapi.client.glossary;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.imsweb.seerapi.client.publishable.Publishable;

public class Glossary extends Publishable {

    // values for the datasources
    public enum Category {
        GENERAL,
        SOLID_TUMOR,
        HEMATO,
        SEERRX,
        SEER_TRAINING,
        LYMPH_NODES
    }

    @JsonProperty("history")
    protected List<GlossaryHistoryEvent> _history;

    /**
     * Glossary-specific fields
     */
    @JsonProperty("definition")
    protected String _definition;
    @JsonProperty("alternate_name")
    protected List<String> _alternateName;
    @JsonProperty("abstractor_note")
    protected String _abstractorNote;
    @JsonProperty("histology")
    protected List<String> _histology;
    @JsonProperty("primary_site")
    protected List<String> _primarySite;
    @JsonProperty("category")
    protected List<Category> _categories;
    @JsonProperty("resource")
    protected List<GlossaryResource> _resources;

    public List<GlossaryHistoryEvent> getHistory() {
        return _history;
    }

    public String getDefinition() {
        return _definition;
    }

    public List<String> getAlternateName() {
        return _alternateName;
    }

    public String getAbstractorNote() {
        return _abstractorNote;
    }

    public List<String> getHistology() {
        return _histology;
    }

    public List<String> getPrimarySite() {
        return _primarySite;
    }

    public List<Category> getCategories() {
        return _categories;
    }

    public List<GlossaryResource> getResources() {
        return _resources;
    }

    public void setResources(List<GlossaryResource> resources) {
        _resources = resources;
    }
}
