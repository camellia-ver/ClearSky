package com.portfolio.clearSky.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemDto {
    // getter / setter
    private String baseDate;
    private String baseTime;
    private String category;
    private int nx;
    private int ny;
    private String obsrValue;

    public void setBaseDate(String baseDate) { this.baseDate = baseDate; }

    public void setBaseTime(String baseTime) { this.baseTime = baseTime; }

    public void setCategory(String category) { this.category = category; }

    public void setNx(int nx) { this.nx = nx; }

    public void setNy(int ny) { this.ny = ny; }

    public void setObsrValue(String obsrValue) { this.obsrValue = obsrValue; }
}