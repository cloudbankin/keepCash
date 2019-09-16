package org.apache.fineract.portfolio.springBoot.enumType;

public enum AgentAccountStatusEnumType {

	ACTIVE(1, "legalFormType.active"),
	INACTIVE(2, "legalFormType.inactive"),
	TOPUP(3, "legalFormType.topup");
	
	
	private final Integer value;
    private final String code;
	
    private AgentAccountStatusEnumType(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }
    
    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }
	
    public static AgentAccountStatusEnumType fromInt(final Integer type) {

    	AgentAccountStatusEnumType appUserType = null;
        switch (type) {
            case 1:
            	appUserType = AgentAccountStatusEnumType.ACTIVE;
            break;
            case 2:
            	appUserType = AgentAccountStatusEnumType.INACTIVE;
            break;
            case 3:
            	appUserType = AgentAccountStatusEnumType.TOPUP;
            break;
           
        }
        return appUserType;
    }
    
    public boolean isActive() {
        return this.value.equals(AgentAccountStatusEnumType.ACTIVE.getValue());
    }
    
    public boolean isInactive() {
        return this.value.equals(AgentAccountStatusEnumType.INACTIVE.getValue());
    }
    
    public boolean isTopup() {
        return this.value.equals(AgentAccountStatusEnumType.TOPUP.getValue());
    }
    

}
