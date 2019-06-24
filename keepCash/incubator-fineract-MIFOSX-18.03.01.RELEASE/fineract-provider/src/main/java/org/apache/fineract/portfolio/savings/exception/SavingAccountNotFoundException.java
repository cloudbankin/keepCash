package org.apache.fineract.portfolio.savings.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;
import org.apache.fineract.portfolio.savings.DepositAccountType;

public class SavingAccountNotFoundException extends AbstractPlatformResourceNotFoundException{

	public SavingAccountNotFoundException(final DepositAccountType accountType) {
		  super("error.msg.savingsaccount does not exist", "Savings account does not exist.");
	}
}
