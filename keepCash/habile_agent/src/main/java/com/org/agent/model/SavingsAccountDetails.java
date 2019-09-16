package com.org.agent.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.org.agent.command.api.JsonCommand;
import com.org.agent.core.AbstractPersistableCustom;
import com.org.agent.enumType.SavingsAccountTypeEnum;

@Entity
@Table(name = "hab_savings_account_details")
public class SavingsAccountDetails extends AbstractPersistableCustom<Long> {

	@Column(name = "savings_id" ,length=20, nullable = false)
	private Long savingsId;
	
	@Column(name = "account_type", length = 20, nullable = false)
	private Integer accountType;
	
	@Column(name = "client_id", length = 20, nullable = false)
	private Long clientId;

	
	public SavingsAccountDetails() {
		
	}
	
	public SavingsAccountDetails(Long savingsId, Integer accountType, Long clientId) {
		this.savingsId = savingsId;
		this.accountType = accountType;
		this.clientId = clientId;
	}
	
	public static SavingsAccountDetails newSavingsAccountDetails(JsonCommand command) {
		Long savingsId = command.longValueOfParameterNamed("savingsId");
		Integer accountType = Integer.valueOf(SavingsAccountTypeEnum.fromInt(command.integerValueOfParameterNamed("accountType")).getValue());
		Long clientId = command.longValueOfParameterNamed("clientId");
		
		return new SavingsAccountDetails(savingsId, accountType, clientId);
		
	}
	

	public Long getSavingsId() {
		return savingsId;
	}

	public void setSavingsId(Long savingsId) {
		this.savingsId = savingsId;
	}

	public Integer getAccounttype() {
		return accountType;
	}

	public void setAccounttype(Integer accounttype) {
		this.accountType = accounttype;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}
	
	
	
	
}
