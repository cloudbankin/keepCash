package com.org.agent.service.implementation;

import java.util.ArrayList;
import java.util.List;

import com.org.agent.core.data.EnumOptionData;
import com.org.agent.model.AppUserTypes;

public class AppUserTypesEnumerations {

	public static EnumOptionData appUserType(final Integer statusId) {
        return appUserType(AppUserTypes.fromInt(statusId));
    }
    
    public static EnumOptionData appUserType(final AppUserTypes appUserType) {
    	final EnumOptionData optionData = new EnumOptionData(appUserType.getValue().longValue(), appUserType.getCode(),
    			appUserType.toString());
        return optionData;
    }
    
    public static List<EnumOptionData> appUserType(final AppUserTypes[] appUserTypes) {
        final List<EnumOptionData> optionDatas = new ArrayList<>();
        for (final AppUserTypes appUserType : appUserTypes) {
            optionDatas.add(appUserType(appUserType));
        }
        return optionDatas;
    }
}
