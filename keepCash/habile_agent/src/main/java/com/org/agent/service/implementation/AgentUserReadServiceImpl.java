package com.org.agent.service.implementation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collection;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.org.agent.core.data.EnumOptionData;
import com.org.agent.data.AgentUserData;
import com.org.agent.enumType.AgentAccountStatusEnumType;
import com.org.agent.jdbcsupport.jdbcsupport;

@Transactional
@Repository
public class AgentUserReadServiceImpl {

	private static JdbcTemplate jdbcTemplate;

	@Autowired
	public AgentUserReadServiceImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public static AgentUserData retrieveAgentUserEntity(Long UserId) {
		final AgentRetrievalMapper mapper = new AgentRetrievalMapper();
		final String sql = "select " + mapper.agentSchema();

		AgentUserData agentuserentity = jdbcTemplate.queryForObject(sql, mapper, new Object[] { UserId });
		// agentuserentity.setAppUserId(UserId);
		return agentuserentity;

	}

	public static Collection<AgentUserData> getDelegatesByParentId (Long parentUserId) {
		final EmployeeRetrievalMapper mapper = new EmployeeRetrievalMapper();
		final String sql = "select " + mapper.getDelegateByParentIdSchema();
		return jdbcTemplate.query(sql, mapper, new Object[] { parentUserId });
	}
	
	public static Collection<AgentUserData> getAllAgentUser () {
		final AgentRetrievalMapper mapper = new AgentRetrievalMapper();
		final String sql = "select " + mapper.getAllAgentSchema();
		return jdbcTemplate.query(sql, mapper, new Object[] {});
	}
	
	private final static class AgentRetrievalMapper implements RowMapper<AgentUserData> {

		public String agentSchema() {
			return  " m.id as userId, m.office_id, m.staff_id, m.username, m.firstname, m.lastname,  "+ 
					" m.email, m.enabled, m.is_self_service_user,  "+ 
					" au.client_id, au.app_user_type_enum, au.company_name, au.company_address,  "+ 
					" au.date_of_birth, au.mobile_no, au.face_id, au.is_agreement_signup,  "+ 
					" au.is_active, au.auth_mode, au.image, au.image_encryption, au.created_on_date ,au.latitude as latitude,au.longitude as longitude,au.location_name as locationname, "+ 
					" au.location_address as locationaddress,au.ip_address as ipaddress,au.device_id as deviceid,au.employee_id as employeeId  ,au.face_unique_id as agentFaceUniqueId, "+
					" au.status "+ 
					" from m_appuser m left join hab_agent_user au on m.id = au.app_user_id  "+ 
					" where au.app_user_id = ? ";

		}
		
		public String getAllAgentSchema() {
			return " m.id as userId, m.office_id, m.staff_id, m.username, m.firstname, m.lastname, " 
					+" m.email, m.enabled, m.is_self_service_user, " 
					+" au.client_id, au.app_user_type_enum, au.company_name, au.company_address, "
					+" au.date_of_birth, au.mobile_no, au.face_id, au.is_agreement_signup, "
					+" au.is_active, au.auth_mode, au.image, au.image_encryption, au.created_on_date "
					+" from m_appuser m left join hab_agent_user au on m.id = au.app_user_id "
					+" where au.client_id is not null   group by au.client_id ";

		}

		@Override
		public AgentUserData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
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
			//final String faceId = rs.getString("face_id");
			final boolean isAgreementSignUp = rs.getBoolean("is_agreement_signup");
			final boolean isActive = rs.getBoolean("is_active");
			final String authMode = rs.getString("auth_mode");
			final String image = rs.getString("image");
			final String imageEncryption = rs.getString("image_encryption");
			final String createdOnDate = format.format(jdbcsupport.getLocalDate(rs, "created_on_date").toDate());
			final Long employeeId = jdbcsupport.getLong(rs, "employeeId");
			
			final String latitude = rs.getString("latitude");
			final String longitude = rs.getString("longitude");
			final String locationName = rs.getString("locationname");
			final String locationAddress = rs.getString("locationaddress");
			final String ipAddress = rs.getString("ipaddress");
			final String deviceId = rs.getString("deviceid");
			final String agentFaceUniqueId = rs.getString("agentFaceUniqueId");
			final Integer status = jdbcsupport.getInteger(rs, "status");
			
			AgentAccountStatusEnumType agentAccountStatusEnumType = AgentAccountStatusEnumType.fromInt(status);
			final EnumOptionData statusEnum = new EnumOptionData(agentAccountStatusEnumType.getValue().longValue(), agentAccountStatusEnumType.getCode(),
					agentAccountStatusEnumType.toString());

			final AgentUserData agentdetails = new AgentUserData(userId, office, staff, emailId, userName, firstName, lastName,
					enabled, isSelfServiceUser, clientId, appUserTypeEnum, companyName, companyAddress, dateOfBirth, mobileNo, 
					isAgreementSignUp, isActive, authMode, image, imageEncryption, createdOnDate, latitude,
			          longitude,  locationName,  locationAddress,
			          ipAddress,  deviceId,employeeId, agentFaceUniqueId,statusEnum);
			
			return agentdetails;
		}
		
		
	}
	
	
	private final static class EmployeeRetrievalMapper implements RowMapper<AgentUserData> {
		public String getDelegateByParentIdSchema() {
			return " m.id as userId, m.office_id, m.staff_id, m.username, m.firstname, m.lastname, " 
					+" m.email, m.enabled, m.is_self_service_user, " 
					+" au.client_id, au.app_user_type_enum, au.company_name, au.company_address, "
					+" au.date_of_birth, au.mobile_no, au.face_id, au.is_agreement_signup, "
					+" au.is_active, au.auth_mode, au.image, au.image_encryption, "
					+" au.created_on_date ,au.latitude as latitude,au.longitude as longitude,au.location_name as locationname, "
					+" au.location_address as locationaddress,au.ip_address as ipaddress,au.device_id as deviceid,au.employee_id as employeeId  ,"
					+" au.face_unique_id as agentFaceUniqueId "
					+" from m_appuser m left join hab_agent_user au on m.id = au.app_user_id "
					+" where au.parent_user_id = ? ";

		}
		
		@Override
		public AgentUserData mapRow(ResultSet rs, @SuppressWarnings("unused") int rowNum) throws SQLException {
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
			final boolean isAgreementSignUp = rs.getBoolean("is_agreement_signup");
			final boolean isActive = rs.getBoolean("is_active");
			final String authMode = rs.getString("auth_mode");
			final String image = rs.getString("image");
			final String imageEncryption = rs.getString("image_encryption");
			final String createdOnDate = format.format(jdbcsupport.getLocalDate(rs, "created_on_date").toDate());
			final Long employeeId = jdbcsupport.getLong(rs, "employeeId");
			
			final String latitude = rs.getString("latitude");
			final String longitude = rs.getString("longitude");
			final String locationName = rs.getString("locationname");
			final String locationAddress = rs.getString("locationaddress");
			final String ipAddress = rs.getString("ipaddress");
			final String deviceId = rs.getString("deviceid");
			final String agentFaceUniqueId = rs.getString("agentFaceUniqueId");

			
			final AgentUserData agentdetails = new AgentUserData(userId, office, staff, emailId, userName, firstName, lastName,
					enabled, isSelfServiceUser, clientId, appUserTypeEnum, companyName, companyAddress, dateOfBirth, mobileNo, 
					isAgreementSignUp, isActive, authMode, image, imageEncryption, createdOnDate, latitude,
			          longitude,  locationName,  locationAddress,
			          ipAddress,  deviceId,employeeId, agentFaceUniqueId, null);
			
			return agentdetails;
		}

	}
}
