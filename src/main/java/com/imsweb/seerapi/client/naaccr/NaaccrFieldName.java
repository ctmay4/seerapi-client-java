/*
 * Copyright (C) 2011 Information Management Services, Inc.
 */
package com.imsweb.seerapi.client.naaccr;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NaaccrFieldName {

    @JsonProperty("item")
    protected Integer _item;
    @JsonProperty("name")
    protected String _name;

    public Integer getItem() {
        return _item;
    }

    public void setItem(Integer item) {
        _item = item;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }
}
