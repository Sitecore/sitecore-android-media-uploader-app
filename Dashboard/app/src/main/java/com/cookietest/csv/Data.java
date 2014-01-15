package com.cookietest.csv;

import java.util.Date;

import org.jsefa.csv.annotation.CsvDataType;
import org.jsefa.csv.annotation.CsvField;

@CsvDataType()
public class Data {
    @CsvField(pos = 1, format = "yyyyMMdd")
    private Date date;

    @CsvField(pos = 2)
    private int visits;

    @CsvField(pos = 3)
    private int value;

    @CsvField(pos = 4)
    private String FacetId;

    public Date getDate() {
        return date;
    }

    public int getVisits() {
        return visits;
    }

    public int getValue() {
        return value;
    }

    public String getFacetId() {
        return FacetId;
    }
}