package com.org.agent.model;


public enum AppUserTypes {

	AGENT(1, "legalFormType.agent"),
	DELEGATE(2, "legalFormType.delegate"),
	CUSTOMER(3, "legalFormType.customer");
	
	private final Integer value;
    private final String code;
	
    private AppUserTypes(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }
    
    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }
	
    public static AppUserTypes fromInt(final Integer type) {

    	AppUserTypes appUserType = null;
        switch (type) {
            case 1:
            	appUserType = AppUserTypes.AGENT;
            break;
            case 2:
            	appUserType = AppUserTypes.DELEGATE;
            break;
            case 3:
            	appUserType = AppUserTypes.CUSTOMER;
            break;
        }
        return appUserType;
    }
    
    public boolean isAgent() {
        return this.value.equals(AppUserTypes.AGENT.getValue());
    }
    
    public boolean isDelegate() {
        return this.value.equals(AppUserTypes.DELEGATE.getValue());
    }
    
    public boolean isCustomer() {
        return this.value.equals(AppUserTypes.CUSTOMER.getValue());
    }
}
