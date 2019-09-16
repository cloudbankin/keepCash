package org.apache.fineract.portfolio.springBoot.enumType;

public enum SavingsTransactionDetailsTypeEnum {

	TOPUP(10, "SavingsTransactionDetailsTypeEnum.topup"),
	CASHIN(20, "SavingsTransactionDetailsTypeEnum.cashIn"),
	CASHOUT(30, "SavingsTransactionDetailsTypeEnum.cashOut"),
	ODTOPUP(40, "SavingsTransactionDetailsTypeEnum.odTopup"),
	ODDEPOSIT(50, "SavingsTransactionDetailsTypeEnum.odDeposit"),
	ODWITHDRAWAL(60, "SavingsTransactionDetailsTypeEnum.odWithtrawal"),
	INVALID(70, "SavingsTransactionDetailsTypeEnum.invalid");
    
    

    private final Integer value;
    private final String code;

    private SavingsTransactionDetailsTypeEnum(final Integer value, final String code) {
        this.value = value;
        this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

    public String getCode() {
        return this.code;
    }

    public static SavingsTransactionDetailsTypeEnum fromInt(final Integer transactionType) {

        SavingsTransactionDetailsTypeEnum savingsAccountTransactionType = SavingsTransactionDetailsTypeEnum.INVALID;
        switch (transactionType) {
            case 10:
                savingsAccountTransactionType = SavingsTransactionDetailsTypeEnum.TOPUP;
            break;
            case 20:
                savingsAccountTransactionType = SavingsTransactionDetailsTypeEnum.CASHIN;
            break;
            case 30:
                savingsAccountTransactionType = SavingsTransactionDetailsTypeEnum.CASHOUT;
            break;
            case 40:
                savingsAccountTransactionType = SavingsTransactionDetailsTypeEnum.ODTOPUP;
            break;
            case 50:
                savingsAccountTransactionType = SavingsTransactionDetailsTypeEnum.ODDEPOSIT;
            break;
            case 60:
                savingsAccountTransactionType = SavingsTransactionDetailsTypeEnum.ODWITHDRAWAL;
            break;
            
        }
        return savingsAccountTransactionType;
    }

    public boolean isTopup() {
        return this.value.equals(SavingsTransactionDetailsTypeEnum.TOPUP.getValue());
    }
    public boolean isCashIn() {
        return this.value.equals(SavingsTransactionDetailsTypeEnum.CASHIN.getValue());
    }
    public boolean isCashOut() {
        return this.value.equals(SavingsTransactionDetailsTypeEnum.CASHOUT.getValue());
    }
    public boolean isOdTopup() {
        return this.value.equals(SavingsTransactionDetailsTypeEnum.ODTOPUP.getValue());
    }
    public boolean isOdDeposit() {
        return this.value.equals(SavingsTransactionDetailsTypeEnum.ODDEPOSIT.getValue());
    }
    public boolean isOdWithtrawal() {
        return this.value.equals(SavingsTransactionDetailsTypeEnum.ODWITHDRAWAL.getValue());
    }

}
