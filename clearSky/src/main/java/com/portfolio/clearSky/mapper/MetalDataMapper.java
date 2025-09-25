package com.portfolio.clearSky.mapper;

import com.portfolio.clearSky.dto.ItemDto;
import com.portfolio.clearSky.dto.MetalDataResponse;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MetalDataMapper {
    public static final Map<String, String> STATION_NAME_MAP;
    public static final Map<String, String> ITEM_NAME_MAP;
    static{
        Map<String, String> stationNameMap = new HashMap<>();
        Map<String, String> itemNameMap = new HashMap<>();

        stationNameMap.put("1","수도권");
        stationNameMap.put("2","백령도");
        stationNameMap.put("3","호남권");
        stationNameMap.put("4","중부권");
        stationNameMap.put("5","제주도");
        stationNameMap.put("6","영남권");
        stationNameMap.put("7","경기권");
        stationNameMap.put("8","충청권");
        stationNameMap.put("9","전북권");
        stationNameMap.put("10","강원권");
        stationNameMap.put("11","충북권");

        itemNameMap.put("90303","납");
        itemNameMap.put("90304","니켈");
        itemNameMap.put("90305","망간");
        itemNameMap.put("90314","아연");
        itemNameMap.put("90319","칼슙");
        itemNameMap.put("90318","칼륨");
        itemNameMap.put("90325","황");

        STATION_NAME_MAP = stationNameMap;
        ITEM_NAME_MAP = itemNameMap;
    }

    public List<MetalDataResponse> map(List<ItemDto> items){
        if (items == null || items.isEmpty()){
            return List.of();
        }

        return items.stream()
                .map(this::mapSingleItem)
                .collect(Collectors.toList());
    }

    private MetalDataResponse mapSingleItem(ItemDto item){
        String stationName = convertStationCodeToName(item.getStationcode());
        String itemName = convertItemCodeToName(item.getItemcode());

        return new MetalDataResponse(
                item.getSdate(),
                stationName,
                itemName,
                item.getValue()
        );
    }

    private String convertStationCodeToName(String code) {
        return STATION_NAME_MAP.getOrDefault(code, "알 수 없는 연구소");
    }
    private String convertItemCodeToName(String code) {
        return ITEM_NAME_MAP.getOrDefault(code, "알 수 없는 항목");
    }
}
