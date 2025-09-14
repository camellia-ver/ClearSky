package com.portfolio.clearSky.model.enums;

public enum RegionCategory {
    KOR("kor"),
    JPN("jpn"),
    CHN("chn");

    private final String code;

    RegionCategory(String code){
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static RegionCategory fromCode(String code){
        for (RegionCategory category : values()){
            if (category.code.equalsIgnoreCase(code)){
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
