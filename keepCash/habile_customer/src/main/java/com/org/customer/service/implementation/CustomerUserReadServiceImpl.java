package com.org.customer.service.implementation;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.org.customer.core.Utils.ApiParameterHelper;
import com.org.customer.core.Utils.ColumnValidator;
import com.org.customer.core.data.EnumOptionData;
import com.org.customer.core.service.Page;
import com.org.customer.core.service.PaginationHelper;
import com.org.customer.core.service.SearchParameters;
import com.org.customer.data.CustomerUserData;
import com.org.customer.jdbcsupport.jdbcsupport;
import com.org.customer.service.CustomerUserReadService;
import com.org.customer.service.CustomerUserService;

@Transactional
@Repository
public class CustomerUserReadServiceImpl implements CustomerUserReadService{

	private static JdbcTemplate jdbcTemplate;
	private final PaginationHelper<CustomerUserData> paginationHelper = new PaginationHelper<>();
	private final CustomerMembersOfGroupMapper membersOfGroupMapper = new CustomerMembersOfGroupMapper();
	private final ColumnValidator columnValidator;

	@Autowired
	public CustomerUserReadServiceImpl(JdbcTemplate jdbcTemplate, final ColumnValidator columnValidator) {
		this.jdbcTemplate = jdbcTemplate;
		this.columnValidator = columnValidator;
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
					+" cu.is_active, cu.auth_mode, cu.image, cu.image_encryption, cu.created_on_date, cu.parent_user_id, "
					+ "cu.goal_id, cu.goal_name, cu.goal_amount, cu.goal_start_date, cu.goal_end_date,cu.latitude as latitude,cu.longitude as longitude,cu.location_name as locationname,"
					+" cu.location_address as locationaddress,cu.ip_address as ipaddress,cu.device_id as deviceid, cu.face_unique_id as customerFaceUniqueId  "
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
			//final String faceId = rs.getString("face_id");
			final boolean isAgreementSignUp = rs.getBoolean("is_agreement_signup");
			final boolean isActive = rs.getBoolean("is_active");
			final String authMode = rs.getString("auth_mode");
			final String image = rs.getString("image");
			final String imageEncryption = rs.getString("image_encryption");
			final String createdOnDate = format.format(jdbcsupport.getLocalDate(rs, "created_on_date").toDate());
			final Long parentUserId = jdbcsupport.getLong(rs, "parent_user_id");
			
			final Long goalId = jdbcsupport.getLong(rs, "goal_id");
			final String goalName = rs.getString("goal_name");
			final BigDecimal goalAmount = rs.getBigDecimal("goal_amount");
			LocalDate startDate = jdbcsupport.getLocalDate(rs, "goal_start_date");
			String goalStartDate = null;
			if(startDate != null) {
				goalStartDate = format.format(startDate.toDate());
			}
			LocalDate endDate = jdbcsupport.getLocalDate(rs, "goal_end_date");
			String goalendDate = null;
			if(endDate != null) {
				goalendDate = format.format(endDate.toDate());
			}
                        final String latitude = rs.getString("latitude");
			final String longitude = rs.getString("longitude");
			final String locationName = rs.getString("locationname");
			final String locationAddress = rs.getString("locationaddress");
			final String ipAddress = rs.getString("ipaddress");
			final String deviceId = rs.getString("deviceid");
			final String customerFcaeUniqueId = rs.getString("customerFaceUniqueId");
			
			final CustomerUserData customerdetails = new CustomerUserData(userId, office, staff, emailId, userName, firstName, lastName,
					enabled, isSelfServiceUser, clientId, appUserTypeEnum, companyName, companyAddress, dateOfBirth, mobileNo, 
					isAgreementSignUp, isActive, authMode, image, imageEncryption, createdOnDate,parentUserId,
					goalId, goalName, goalAmount, goalStartDate, goalendDate, latitude,
			          longitude,  locationName,  locationAddress,
			          ipAddress,  deviceId, customerFcaeUniqueId);
			
			return customerdetails;
		}

	}
	public static Collection<CustomerUserData> retrieveCustomerUsersUnderAgent(Long agentId) {
		final customerRetrievalMapper mapper = new customerRetrievalMapper();
		final String sql = "select " + mapper.customersUnderAgentSchema();

		return jdbcTemplate.query(sql, mapper, new Object[] { agentId });
		

	}
	

	
	public String customersMapperSchema() {
		return " m.id as userId, m.office_id, m.staff_id, m.username, m.firstname, m.lastname, " 
				+" m.email, m.enabled, m.is_self_service_user, " 
				+" cu.client_id, cu.app_user_type_enum, cu.company_name, cu.company_address, "
				+" cu.date_of_birth, cu.mobile_no, cu.face_id, cu.is_agreement_signup, "
				+" cu.is_active, cu.auth_mode, cu.image, cu.image_encryption, cu.created_on_date, cu.parent_user_id "
				+" from m_appuser m join hab_customer_user cu on m.id = cu.app_user_id ";
				
	}
	

	@Override
    public Page<CustomerUserData> retrieveAllCustomers(final SearchParameters searchParameters) {

        final StringBuilder sqlBuilder = new StringBuilder(10);
        sqlBuilder.append("select SQL_CALC_FOUND_ROWS ");
        sqlBuilder.append(customersMapperSchema());
        
        List<Object> paramList = new ArrayList<>(Arrays.asList());

        if(searchParameters!=null) {
            final String extraCriteria = buildSqlStringFromCustomerCriteria(customersMapperSchema(), searchParameters, paramList);
            if (StringUtils.isNotBlank(extraCriteria)) {
                sqlBuilder.append(" and (").append(extraCriteria).append(")");
            }
           
            if (searchParameters.isOrderByRequested()) {
                sqlBuilder.append(" order by ").append(searchParameters.getOrderBy());
                this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getOrderBy());
                if (searchParameters.isSortOrderProvided()) {
                    sqlBuilder.append(' ').append(searchParameters.getSortOrder());
                    this.columnValidator.validateSqlInjection(sqlBuilder.toString(), searchParameters.getSortOrder());
                }
            }

            if (searchParameters.isLimited()) {
                sqlBuilder.append(" limit ").append(searchParameters.getLimit());
                if (searchParameters.isOffset()) {
                    sqlBuilder.append(" offset ").append(searchParameters.getOffset());
                }
            }
        }
        final String sqlCountRows = "SELECT FOUND_ROWS()";
        return this.paginationHelper.fetchPage(this.jdbcTemplate, sqlCountRows, sqlBuilder.toString(), paramList.toArray(), this.membersOfGroupMapper);
    }

    private String buildSqlStringFromCustomerCriteria(String schemaSql, final SearchParameters searchParameters, List<Object> paramList) {

        String sqlSearch = searchParameters.getSqlSearch();
        final Long userId = searchParameters.getUserId();
        final String mobileNo = searchParameters.getMobileNo();

        String extraCriteria = "";
        if (sqlSearch != null) {
            sqlSearch = sqlSearch.replaceAll(" display_name ", " c.display_name ");
            sqlSearch = sqlSearch.replaceAll("display_name ", "c.display_name ");
            extraCriteria = " and (" + sqlSearch + ")";
            this.columnValidator.validateSqlInjection(schemaSql, sqlSearch);
        }

        if (userId != null) {
            extraCriteria += " and cu.app_user_id = ? ";
            paramList.add(userId);
        }

        if (mobileNo != null) {
        	extraCriteria += " and cu.mobile_no = ? ";
        	paramList.add(mobileNo);
        }

        if (searchParameters.isScopedByOfficeHierarchy()) {
        	paramList.add(ApiParameterHelper.sqlEncodeString(searchParameters.getHierarchy() + "%"));
            extraCriteria += " and o.hierarchy like ? ";
        }
        
        if(searchParameters.isOrphansOnly()){
        	extraCriteria += " and c.id NOT IN (select client_id from m_group_client) ";
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }
        return extraCriteria;
    }
    
    private static final class CustomerMembersOfGroupMapper implements RowMapper<CustomerUserData> {

        private final String schema;

        public CustomerMembersOfGroupMapper() {
            final StringBuilder sqlBuilder = new StringBuilder(200);

            sqlBuilder.append(" m.id as userId, m.office_id, m.staff_id, m.username, m.firstname, m.lastname, m.email, m.enabled, m.is_self_service_user, ");
            sqlBuilder.append(" cu.client_id, cu.app_user_type_enum, cu.company_name, cu.company_address, cu.date_of_birth, cu.mobile_no, cu.face_id, cu.is_agreement_signup, cu.is_active, cu.auth_mode, cu.image, cu.image_encryption, cu.created_on_date, cu.parent_user_id ");
            sqlBuilder.append(" from m_appuser m join hab_customer_user cu on m.id = cu.app_user_id ");

            this.schema = sqlBuilder.toString();
        }

        public String schema() {
            return this.schema;
        }

    	@Override
    	public CustomerUserData mapRow(ResultSet rs, int rowNum) throws SQLException {
            final Long appId = jdbcsupport.getLong(rs, "userId");

            return CustomerUserData.instance(appId);
    	}
    }
   
}


