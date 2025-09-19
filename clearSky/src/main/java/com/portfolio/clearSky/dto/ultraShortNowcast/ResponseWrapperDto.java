package com.portfolio.clearSky.dto.ultraShortNowcast;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseWrapperDto {
    private HeaderDto header;
    private BodyDto body;
}
