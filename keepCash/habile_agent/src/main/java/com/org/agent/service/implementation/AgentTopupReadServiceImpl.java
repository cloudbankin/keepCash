package com.org.agent.service.implementation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.org.agent.jdbcsupport.jdbcsupport;
import com.org.agent.model.AgentTopupData;
import com.org.agent.service.AgentTopupReadService;

@Component
public class AgentTopupReadServiceImpl implements AgentTopupReadService {

	@Autowired
	private static JdbcTemplate jdbcTemplate; 
	
	@Autowired
	public AgentTopupReadServiceImpl(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
	@Override
	public AgentTopupData getAgentTopupTotalAmount(Long agentUserId) {
		AgentTopupData agentTopupData = retrieveAgentTotalAmount(agentUserId);
		BigDecimal totalTopupAmount = agentTopupData.getTransactionAmount();
		BigDecimal loanAmount = totalTopupAmount.divide(BigDecimal.TEN).setScale(0, RoundingMode.HALF_EVEN);
		agentTopupData.setTransactionAmount(loanAmount);
		agentTopupData.setTransactionDate(null);
		agentTopupData.setReceiptNumber(null);
		
		return agentTopupData;
	
	}
	
	
	public static AgentTopupData retrieveAgentTotalAmount (Long agentUserId) {
		final AgentRetrieveTopupMapper mapper = new AgentRetrieveTopupMapper();
		final String sql = "select " + mapper.retriveLoanEligibilityAmount();
		return jdbcTemplate.queryForObject(sql, mapper, new Object[] { agentUserId });	
	}
	
	
	private static class AgentRetrieveTopupMapper implements RowMapper<AgentTopupData>{
		public String retriveLoanEligibilityAmount() {
			return " "+Schema()
					+ "where hasa.agent_user_id = ?";
		}
		
		public String Schema() {
			return " hasa.id, hasa.agent_user_id as agentUserId, hasa.transaction_date as transactionDate, sum(hasa.transaction_amount) as totalTransactionAmount, "
					+ "hasa.receipt_number as receiptNumber, hasa.payment_type_id as paymentTypeId, hasa.account_number as accountNumber, "
					+ "hasa.check_number as checkNumber, hasa.routing_code as routingCode, hasa.bank_number as bankNumber , hasa.status"
					+ " from hab_agent_savings_account hasa ";

		}
		
		
		@Override
		public AgentTopupData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
			final Long id = jdbcsupport.getLong(rs, "id");
			final Long agentUserId = jdbcsupport.getLong(rs, "agentUserId");
			SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
			LocalDate transactionDate = jdbcsupport.getLocalDate(rs, "transactionDate");
			BigDecimal transactionAmount = rs.getBigDecimal("totalTransactionAmount");
			Integer paymentTypeId = rs.getInt("paymentTypeId") ;
			String accountNumber = rs.getString("accountNumber");
			String checkNumber = rs.getString("checkNumber");
			String routingCode = rs.getString("routingCode");
			String receiptNumber = rs.getString("receiptNumber");
			String bankNumber = rs.getString("bankNumber");
			Integer status = rs.getInt("status");

			final AgentTopupData agentTopupData = new AgentTopupData(id, agentUserId, transactionDate, transactionAmount, 
					paymentTypeId, accountNumber, checkNumber, routingCode, receiptNumber, bankNumber, status);
			

			return agentTopupData;
		}

	}
}
