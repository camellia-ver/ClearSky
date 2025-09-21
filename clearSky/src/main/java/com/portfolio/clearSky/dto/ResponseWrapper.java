package com.portfolio.clearSky.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseWrapper {
    private Body body;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private Items items;

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Items {
            @JacksonXmlElementWrapper(useWrapping = true) // <items> 감싸고 있음
            @JacksonXmlProperty(localName = "item")
            private List<ItemDto> item;

            public void setItem(List<ItemDto> item) { this.item = item; }
        }

        public void setItems(Items items) { this.items = items; }
    }

    public void setBody(Body body) { this.body = body; }
}
