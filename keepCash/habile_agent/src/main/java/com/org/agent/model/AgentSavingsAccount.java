package com.org.agent.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.org.agent.command.api.JsonCommand;
import com.org.agent.core.AbstractPersistableCustom;

@Entity
@Table(name = "hab_agent_savings_account")
public class AgentSavingsAccount extends AbstractPersistableCustom<Long>{
	
	@Column(name = "transaction_date")
	private Date transactionDate;
	
	@Column(name = "transaction_amount", precision=19, scale=2)
	private BigDecimal transactionAmount;
	
	@Column(name = "payment_type_id", length = 20)
	private Integer paymentTypeId;
	
	@Column(name = "account_number", length = 255)
	private String accountNumber;
	
	@Column(name = "check_number", length = 255)
	private String checkNumber;
	
	@Column(name = "routing_code", length = 255)
	private String routingCode;
	
	@Column(name = "receipt_number", length = 255)
	private String receiptNumber;
	
	@Column(name = "bank_number", length = 255)
	private String bankNumber;
	
	@Column(name = "status", length = 20)
	private Integer status;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "agent_user_id")
	private AppUser agentUserId;
	
	public AgentSavingsAccount(final AppUser agentUserId, final Date transactionDate,
			final BigDecimal transactionAmount, final Integer paymentTypeId,
			final String accountNumber, final String checkNumber, final String routingCode,
			final String receiptNumber, final String bankNumber, final Integer status) {
		this.agentUserId = agentUserId;
		this.transactionDate = transactionDate;
		this.transactionAmount = transactionAmount;
		this.paymentTypeId = paymentTypeId;
		this.accountNumber = accountNumber;
		this.checkNumber = checkNumber;
		this.routingCode = routingCode;
		this.receiptNumber = receiptNumber;
		this.bankNumber = bankNumber;
		this.status = status;
	}
	
	public static AgentSavingsAccount createNew(JsonCommand command, AppUser appUser, final Integer agentSavingsAccountStatus) {
		Date transactionDate = command.DateValueOfParameterNamed("transactionDate");
		BigDecimal transactionAmount = BigDecimal.valueOf(Long.parseLong(command.stringValueOfParameterNamed("transactionAmount")));
		Integer paymentTypeId = command.integerValueOfParameterNamed("paymentTypeId");
		String accountNumber = command.stringValueOfParameterNamed("accountNumber");
		String checkNumber = command.stringValueOfParameterNamed("checkNumber");
		String routingCode = command.stringValueOfParameterNamed("routingCode");
		String receiptNumber = command.stringValueOfParameterNamed("receiptNumber");
		String bankNumber = command.stringValueOfParameterNamed("bankNumber");
		Integer status = agentSavingsAccountStatus;
		
		return new AgentSavingsAccount(appUser, transactionDate, transactionAmount, paymentTypeId,
				accountNumber, checkNumber, routingCode, receiptNumber, bankNumber, status);
		
	}


	public AppUser getAgentUserId() {
		return agentUserId;
	}


	public void setAgentUserId(AppUser agentUserId) {
		this.agentUserId = agentUserId;
	}


	public Date getTransactionDate() {
		return transactionDate;
	}


	public void setTransactionDate(Date transactionDate) {
		this.transactionDate = transactionDate;
	}


	public BigDecimal getTransactionAmount() {
		return transactionAmount;
	}


	public void setTransactionAmount(BigDecimal transactionAmount) {
		this.transactionAmount = transactionAmount;
	}


	public Integer getPaymentTypeId() {
		return paymentTypeId;
	}


	public void setPaymentTypeId(Integer paymentTypeId) {
		this.paymentTypeId = paymentTypeId;
	}


	public String getAccountNumber() {
		return accountNumber;
	}


	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}


	public String getCheckNumber() {
		return checkNumber;
	}


	public void setCheckNumber(String checkNumber) {
		this.checkNumber = checkNumber;
	}


	public String getRoutingCode() {
		return routingCode;
	}


	public void setRoutingCode(String routingCode) {
		this.routingCode = routingCode;
	}


	public String getReceiptNumber() {
		return receiptNumber;
	}


	public void setReceiptNumber(String receiptNumber) {
		this.receiptNumber = receiptNumber;
	}


	public String getBankNumber() {
		return bankNumber;
	}


	public void setBankNumber(String bankNumber) {
		this.bankNumber = bankNumber;
	}


	public Integer getStatus() {
		return status;
	}


	public void setStatus(Integer status) {
		this.status = status;
	}
	
	
	
}
