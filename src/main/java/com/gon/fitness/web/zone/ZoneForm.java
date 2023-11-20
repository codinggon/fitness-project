package com.gon.fitness.web.zone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneForm {

    private String zoneValue;

    private String city;

    private String localNameOfCity;

    private String province; //경기도 전라남도 등




}
