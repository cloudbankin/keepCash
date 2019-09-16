package org.apache.fineract.portfolio.springBoot.enumType;

public enum SavingsAccountTypeEnum {

	    SAVINGSACCOUNT(51, "SavingsAccountTypeEnum.savingsAccount"), //
	    ODACCOUNT(52, "SavingsAccountTypeEnum.odAccount");
	    

	    private final Integer value;
	    private final String code;

	    private SavingsAccountTypeEnum(final Integer value, final String code) {
	        this.value = value;
	        this.code = code;
	    }

	    public Integer getValue() {
	        return this.value;
	    }

	    public String getCode() {
	        return this.code;
	    }

	    public static SavingsAccountTypeEnum fromInt(final Integer transactionType) {

	        SavingsAccountTypeEnum savingsAccountTransactionType = SavingsAccountTypeEnum.SAVINGSACCOUNT;
	        switch (transactionType) {
	            case 51:
	                savingsAccountTransactionType = SavingsAccountTypeEnum.SAVINGSACCOUNT;
	            break;
	            case 52:
	                savingsAccountTransactionType = SavingsAccountTypeEnum.ODACCOUNT;
	            break;
	            
	        }
	        return savingsAccountTransactionType;
	    }

	    public boolean isSavingsAccount() {
	        return this.value.equals(SavingsAccountTypeEnum.SAVINGSACCOUNT.getValue());
	    }
	    public boolean isOdAccount() {
	        return this.value.equals(SavingsAccountTypeEnum.ODACCOUNT.getValue());
	    }

	   

}
