package com.portfolio.clearSky.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BodyDto {
    private String dataType;
    private ItemsDto items;
    private int pageNo;
    private int numOfRows;
    private int totalCount;
}
