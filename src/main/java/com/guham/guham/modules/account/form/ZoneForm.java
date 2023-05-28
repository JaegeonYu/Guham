package com.guham.guham.modules.account.form;

import com.guham.guham.modules.zone.Zone;
import lombok.Data;

@Data
public class ZoneForm {
    private String zoneName;

    public String getCityName() {
        return zoneName.substring(0, zoneName.indexOf("("));
    }

    public String getLocalNameOfCity() {
        return zoneName.substring(zoneName.indexOf("(") + 1, zoneName.indexOf(")"));
    }

    public String getProvince() {
        return zoneName.substring(zoneName.indexOf("/") + 1);
    }

    public Zone getZone(){
        return Zone.builder()
                .city(getCityName())
                .localNameOfCity(getLocalNameOfCity())
                .province(getProvince())
                .build();
    }
}
