package com.org.customer.service.implementation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import javax.sql.DataSource;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.org.customer.jdbcsupport.jdbcsupport;
import com.org.customer.core.data.EnumOptionData;
import com.org.customer.data.CustomerUserData;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.org.customer.model.CustomerUserEntity;
import com.org.customer.repository.CustomerUserRepository;
import com.org.customer.web.DBConfig;
import javax.sql.DataSource;

@Transactional
@Repository
public class CustomerUserReadServiceImpl {

	private static JdbcTemplate jdbcTemplate;

	@Autowired
	public CustomerUserReadServiceImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public static CustomerUserData retrieveCustomerUserEntity(Long UserId) {
		final customerRetrievalMapper mapper = new customerRetrievalMapper();
		final String sql = "select " + mapper.customerSchema();

		CustomerUserData customeruserentity = jdbcTemplate.queryForObject(sql, mapper, new Object[] { UserId });
		// customeruserentity.setAppUserId(UserId);
		return customeruserentity;

	}

	private final static class customerRetrievalMapper implements RowMapper<CustomerUserData> {

		public String customerSchema() {
			return " m.id as userId, m.office_id, m.staff_id, m.username, m.firstname, m.lastname, " 
					+" m.email, m.enabled, m.is_self_service_user, " 
					+" cu.client_id, cu.app_user_type_enum, cu.company_name, cu.company_address, "
					+" cu.date_of_birth, cu.mobile_no, cu.face_id, cu.is_agreement_signup, "
					+" cu.is_active, cu.auth_mode, cu.image, cu.image_encryption, cu.created_on_date, cu.parent_user_id "
					+" from m_appuser m left join hab_customer_user cu on m.id = cu.app_user_id "
					+" where cu.app_user_id = ? ";

		}

		public String customersUnderAgentSchema() {
			return " m.id as userId, m.office_id, m.staff_id, m.username, m.firstname, m.lastname, " 
					+" m.email, m.enabled, m.is_self_service_user, " 
					+" cu.client_id, cu.app_user_type_enum, cu.company_name, cu.company_address, "
					+" cu.date_of_birth, cu.mobile_no, cu.face_id, cu.is_agreement_signup, "
					+" cu.is_active, cu.auth_mode, cu.image, cu.image_encryption, cu.created_on_date, cu.parent_user_id "
					+" from m_appuser m left join hab_customer_user cu on m.id = cu.app_user_id "
					+" where cu.parent_user_id = ? ";

		}

		@Override
		public CustomerUserData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
			final Long userId = jdbcsupport.getLong(rs, "userId");
			final Long office = jdbcsupport.getLong(rs, "office_id");
			final Long staff = jdbcsupport.getLong(rs, "staff_id");
			final String emailId = rs.getString("email");
			final String userName = rs.getString("username");
			final String firstName = rs.getString("firstname");
			final String lastName = rs.getString("lastname");
			final boolean enabled = rs.getBoolean("enabled");
			final boolean isSelfServiceUser = rs.getBoolean("is_self_service_user");
			
			final Long clientId = jdbcsupport.getLong(rs, "client_id");
			final Integer appUserTypeEnumValue = jdbcsupport.getInteger(rs, "app_user_type_enum");
			EnumOptionData appUserTypeEnum = AppUserTypesEnumerations.appUserType(appUserTypeEnumValue);
			final String companyName = rs.getString("company_name");
			final String companyAddress = rs.getString("company_address");
			SimpleDateFormat format = new SimpleDateFormat("dd MMMM yyyy");
			LocalDate date = jdbcsupport.getLocalDate(rs, "date_of_birth");
			String dateOfBirth = null;
			if(date != null) {
			dateOfBirth = format.format(date.toDate());
			}
			final String mobileNo = rs.getString("mobile_no");
			final String faceId = rs.getString("face_id");
			final boolean isAgreementSignUp = rs.getBoolean("is_agreement_signup");
			final boolean isActive = rs.getBoolean("is_active");
			final String authMode = rs.getString("auth_mode");
			final String image = rs.getString("image");
			final String imageEncryption = rs.getString("image_encryption");
			final String createdOnDate = format.format(jdbcsupport.getLocalDate(rs, "created_on_date").toDate());
			final Long parentUserId = jdbcsupport.getLong(rs, "parent_user_id");
			
			final CustomerUserData customerdetails = new CustomerUserData(userId, office, staff, emailId, userName, firstName, lastName,
					enabled, isSelfServiceUser, clientId, appUserTypeEnum, companyName, companyAddress, dateOfBirth, mobileNo, faceId, 
					isAgreementSignUp, isActive, authMode, image, imageEncryption, createdOnDate,parentUserId);
			
			return customerdetails;
		}

	}
	public static Collection<CustomerUserData> retrieveCustomerUsersUnderAgent(Long agentId) {
		final customerRetrievalMapper mapper = new customerRetrievalMapper();
		final String sql = "select " + mapper.customersUnderAgentSchema();

		return jdbcTemplate.query(sql, mapper, new Object[] { agentId });
		

	}
	
}
