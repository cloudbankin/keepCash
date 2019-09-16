package com.org.agent.model;

import java.math.BigDecimal;
import java.util.Date;

import org.joda.time.LocalDate;

public class AgentTopupData {

	private Long id;
	private Long agentUserId;
	private LocalDate transactionDate;
	private BigDecimal transactionAmount;
	private Integer paymentTypeId;
	private String accountNumber;
	private String checkNumber;
	private String routingCode;
	private String receiptNumber;
	private String bankNumber;
	private Integer status;
	


	public AgentTopupData(Long id, Long agentUserId, LocalDate transactionDate, BigDecimal transactionAmount,
			Integer paymentTypeId, String accountNumber, String checkNumber, String routingCode, String receiptNumber,
			String bankNumber, Integer status) {
		super();
		this.id = id;
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
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDate getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(LocalDate transactionDate) {
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
