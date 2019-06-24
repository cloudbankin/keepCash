package com.org.customer.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.org.customer.command.api.JsonCommand;
import com.org.customer.core.AbstractPersistableCustom;

@Entity
@Table(name = "hab_cashin_cashout_transaction")
public class CashInCashOutTransaction extends AbstractPersistableCustom<Long>{
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "agent_user_id")
	private AppUser agentUserId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "customer_user_id")
	private AppUser customerUserId;
	
	@Column(name = "transaction_amount")
	private BigDecimal transactionAmount;
	
	@Column(name = "transaction_date")
	private Date transactionDate;

	
	
	public AppUser getAgentUserId() {
		return agentUserId;
	}

	public void setAgentUserId(AppUser agentUserId) {
		this.agentUserId = agentUserId;
	}

	public AppUser getCustomerUserId() {
		return customerUserId;
	}

	public void setCustomerUserId(AppUser customerUserId) {
		this.customerUserId = customerUserId;
	}

	public BigDecimal getTransactionAmount() {
		return transactionAmount;
	}

	public void setTransactionAmount(BigDecimal transactionAmount) {
		this.transactionAmount = transactionAmount;
	}

	public Date getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}
	
	
	public CashInCashOutTransaction(AppUser agentUserId, AppUser customerUserId, BigDecimal transactionAmount,
			Date transactionDate) {
		super();
		this.agentUserId = agentUserId;
		this.customerUserId = customerUserId;
		this.transactionAmount = transactionAmount;
		this.transactionDate = transactionDate;
	}

	public static CashInCashOutTransaction createNew(AppUser agentUserId, AppUser customerUserId, JsonCommand command) {
		BigDecimal transactionAmount = BigDecimal.valueOf(Long.parseLong(command.stringValueOfParameterNamed("transactionAmount")));
		Date transactionDate = command.DateValueOfParameterNamed("transactionDate");
		return new CashInCashOutTransaction(agentUserId, customerUserId, transactionAmount, transactionDate);
	}
	
	
	
}
