/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.useradministration.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.JdbcSupport;
import org.apache.fineract.infrastructure.core.service.RoutingDataSource;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.springBoot.enumType.AppUserTypes;
import org.apache.fineract.portfolio.springBoot.enumType.AppUserTypesEnumerations;
import org.apache.fineract.useradministration.data.AppUserData;
import org.apache.fineract.useradministration.data.RoleData;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserClientMapping;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.apache.fineract.useradministration.domain.Role;
import org.apache.fineract.useradministration.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;



@Service
public class AppUserReadPlatformServiceImpl implements AppUserReadPlatformService {

    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final RoleReadPlatformService roleReadPlatformService;
    private final AppUserRepository appUserRepository;
    private final StaffReadPlatformService staffReadPlatformService;
    private final CustomerUserMapper customerUserMapper = new CustomerUserMapper();

    @Autowired
    public AppUserReadPlatformServiceImpl(final PlatformSecurityContext context, final RoutingDataSource dataSource,
            final OfficeReadPlatformService officeReadPlatformService, final RoleReadPlatformService roleReadPlatformService,
            final AppUserRepository appUserRepository, final StaffReadPlatformService staffReadPlatformService) {
        this.context = context;
        this.officeReadPlatformService = officeReadPlatformService;
        this.roleReadPlatformService = roleReadPlatformService;
        this.appUserRepository = appUserRepository;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.staffReadPlatformService = staffReadPlatformService;
    }

    /*
     * used for caching in spring expression language.
     */
    public PlatformSecurityContext getContext() {
        return this.context;
    }

    @Override
    @Cacheable(value = "users", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat(#root.target.context.authenticatedUser().getOffice().getHierarchy())")
    public Collection<AppUserData> retrieveAllUsers() {

        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final AppUserNewMapper mapper = new AppUserNewMapper(this.roleReadPlatformService, this.staffReadPlatformService);
        final String sql = "select " + mapper.newSchema();

        return this.jdbcTemplate.query(sql, mapper, new Object[] { hierarchySearchString });
    }

    @Override
    public Collection<AppUserData> retrieveSearchTemplate() {
        final AppUser currentUser = this.context.authenticatedUser();
        final String hierarchy = currentUser.getOffice().getHierarchy();
        final String hierarchySearchString = hierarchy + "%";

        final AppUserLookupMapper mapper = new AppUserLookupMapper();
        final String sql = "select " + mapper.schema();

        return this.jdbcTemplate.query(sql, mapper, new Object[] { hierarchySearchString });
    }

    @Override
    public AppUserData retrieveNewUserDetails() {

        final Collection<OfficeData> offices = this.officeReadPlatformService.retrieveAllOfficesForDropdown();
        final Collection<RoleData> availableRoles = this.roleReadPlatformService.retrieveAllActiveRoles();

        return AppUserData.template(offices, availableRoles);
    }

    @Override
    public AppUserData retrieveUser(final Long userId) {

        this.context.authenticatedUser();

        final AppUser user = this.appUserRepository.findOne(userId);
        if (user == null || user.isDeleted()) { throw new UserNotFoundException(userId); }

        final Collection<RoleData> availableRoles = this.roleReadPlatformService.retrieveAll();

        final Collection<RoleData> selectedUserRoles = new ArrayList<>();
        final Set<Role> userRoles = user.getRoles();
        for (final Role role : userRoles) {
            selectedUserRoles.add(role.toData());
        }

        availableRoles.removeAll(selectedUserRoles);

        final StaffData linkedStaff;
        if (user.getStaff() != null) {
            linkedStaff = this.staffReadPlatformService.retrieveStaff(user.getStaffId());
        } else {
            linkedStaff = null;
        }

        AppUserData retUser = AppUserData.instance(user.getId(), user.getUsername(), user.getEmail(), user.getOffice().getId(),
                user.getOffice().getName(), user.getFirstname(), user.getLastname(), availableRoles, selectedUserRoles, linkedStaff,
                user.getPasswordNeverExpires(), user.isSelfServiceUser(), null);
        
        if(retUser.isSelfServiceUser()){
        	Set<ClientData> clients = new HashSet<>();
        	for(AppUserClientMapping clientMap : user.getAppUserClientMappings()){
        		Client client = clientMap.getClient();
        		clients.add(ClientData.lookup(client.getId(), client.getDisplayName(), 
        				client.getOffice().getId(), client.getOffice().getName()));
        	}
        	retUser.setClients(clients);
        }
        
        return retUser; 
    }

    private static final class AppUserMapper implements RowMapper<AppUserData> {

        private final RoleReadPlatformService roleReadPlatformService;
        private final StaffReadPlatformService staffReadPlatformService;

        public AppUserMapper(final RoleReadPlatformService roleReadPlatformService, final StaffReadPlatformService staffReadPlatformService) {
            this.roleReadPlatformService = roleReadPlatformService;
            this.staffReadPlatformService = staffReadPlatformService;
        }

        @Override
        public AppUserData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String username = rs.getString("username");
            final String firstname = rs.getString("firstname");
            final String lastname = rs.getString("lastname");
            final String email = rs.getString("email");
            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final String officeName = rs.getString("officeName");
            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final Boolean passwordNeverExpire = rs.getBoolean("passwordNeverExpires");
            final Boolean isSelfServiceUser = rs.getBoolean("isSelfServiceUser");
            
            final Collection<RoleData> selectedRoles = this.roleReadPlatformService.retrieveAppUserRoles(id);

            final StaffData linkedStaff;
            if (staffId != null) {
                linkedStaff = this.staffReadPlatformService.retrieveStaff(staffId);
            } else {
                linkedStaff = null;
            }
            return AppUserData.instance(id, username, email, officeId, officeName, firstname, lastname, null, selectedRoles, linkedStaff,
                    passwordNeverExpire, isSelfServiceUser, null);
        }

        public String schema() {
            return " u.id as id, u.username as username, u.firstname as firstname, u.lastname as lastname, u.email as email, u.password_never_expires as passwordNeverExpires, "
                    + " u.office_id as officeId, o.name as officeName, u.staff_id as staffId, u.is_self_service_user as isSelfServiceUser from m_appuser u "
                    + " join m_office o on o.id = u.office_id where o.hierarchy like ? and u.is_deleted=0 order by u.username";
        }
        

    }

    private static final class AppUserLookupMapper implements RowMapper<AppUserData> {

        @Override
        public AppUserData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String username = rs.getString("username");

            return AppUserData.dropdown(id, username);
        }

        public String schema() {
            return " u.id as id, u.username as username from m_appuser u "
                    + " join m_office o on o.id = u.office_id where o.hierarchy like ? and u.is_deleted=0 order by u.username";
        }
    }

    @Override
    public boolean isUsernameExist(String username) {
        String sql = "select count(*) from m_appuser where username = ?";
        Object[] params = new Object[] { username };
        Integer count = this.jdbcTemplate.queryForObject(sql, params, Integer.class);
        if (count == 0) { return false; }
        return true;
    }
    
    private static final class AppUserNewMapper implements RowMapper<AppUserData> {

        private final RoleReadPlatformService roleReadPlatformService;
        private final StaffReadPlatformService staffReadPlatformService;

        public AppUserNewMapper(final RoleReadPlatformService roleReadPlatformService, final StaffReadPlatformService staffReadPlatformService) {
            this.roleReadPlatformService = roleReadPlatformService;
            this.staffReadPlatformService = staffReadPlatformService;
        }

        @Override
        public AppUserData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String username = rs.getString("username");
            final String firstname = rs.getString("firstname");
            final String lastname = rs.getString("lastname");
            final String email = rs.getString("email");
            final Long officeId = JdbcSupport.getLong(rs, "officeId");
            final String officeName = rs.getString("officeName");
            final Long staffId = JdbcSupport.getLong(rs, "staffId");
            final Boolean passwordNeverExpire = rs.getBoolean("passwordNeverExpires");
            final Boolean isSelfServiceUser = rs.getBoolean("isSelfServiceUser");
            final Integer userTypeId = JdbcSupport.getInteger(rs, "appUserType");
            EnumOptionData userType = null;
            if(userTypeId != null) {
            	userType = AppUserTypesEnumerations.appUserType(userTypeId);
            }else {
            	userType = AppUserTypesEnumerations.appUserType(AppUserTypes.ADMINISTRATOR.getValue());
            }

            final Collection<RoleData> selectedRoles = this.roleReadPlatformService.retrieveAppUserRoles(id);

            final StaffData linkedStaff;
            if (staffId != null) {
                linkedStaff = this.staffReadPlatformService.retrieveStaff(staffId);
            } else {
                linkedStaff = null;
            }
            return AppUserData.instance(id, username, email, officeId, officeName, firstname, lastname, null, selectedRoles, linkedStaff,
                    passwordNeverExpire, isSelfServiceUser, userType);
        }

        public String newSchema() {
            return " u.id as id, ifNull(hau.app_user_id,  hcu.app_user_id)as appUserId ,ifNull(hau.app_user_type_enum, hcu.app_user_type_enum )as appUserType , u.username as username, u.firstname as firstname, u.lastname as lastname, u.email as email, u.password_never_expires as passwordNeverExpires, "
                    + " u.office_id as officeId, o.name as officeName, u.staff_id as staffId, u.is_self_service_user as isSelfServiceUser from m_appuser u "
                    + " join m_office o on o.id = u.office_id left join hab_agent_user hau ON hau.app_user_id = u.id left join hab_customer_user hcu ON hcu.app_user_id = u.id where o.hierarchy like ? and u.is_deleted=0 order by u.username";
        }

    }
    
    
    public Collection<AppUserData> getUserMobileNo(final String mobileNo) {         
    	 final CustomerUserMapper mapper = new CustomerUserMapper();
         final String sql = "select " + mapper.schema();
         return this.jdbcTemplate.query(sql, mapper, new Object[] { mobileNo , mobileNo});
     
    }
    
    private static final class CustomerUserMapper implements RowMapper<AppUserData> {

        @Override
        public AppUserData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
            final Long id = rs.getLong("id");
            final String username = rs.getString("username");
            return AppUserData.mobileAppUserData(id, username);
        }

        public String schema() {
        	return " au.id, au.username from m_appuser au " + 
        			" left join hab_customer_user  hcu  ON au.id = hcu.app_user_id " + 
        			" left join hab_agent_user hau ON hau.app_user_id = au.id "
        			+ "where hcu.mobile_no = ? or hau.mobile_no = ?";
        }
        
    }
    
    @Override
    public Collection<AppUserData> getAgnetUserByClientId(final Long clientId) {         
   	 final AgentUserMapper mapper = new AgentUserMapper();
        final String sql = "select " + mapper.schema();
        return this.jdbcTemplate.query(sql, mapper, new Object[] { clientId });
    
   }
   
   private static final class AgentUserMapper implements RowMapper<AppUserData> {

       @Override
       public AppUserData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
           final Long id = rs.getLong("id");
           final String username = rs.getString("username");
           final Integer userTypeId = rs.getInt("appUserTypeId");
           
           EnumOptionData userType = null;
           if(userTypeId != null){
        	   Long userTypeLongId = new Long(userTypeId);
        	   AppUserTypes appUserTypes = AppUserTypes.fromInt(userTypeId);
        	   userType = new EnumOptionData(userTypeLongId, appUserTypes.getCode(), appUserTypes.getValue().toString());
           }
           return AppUserData.getAgentAppUserData(id, username, userType);
       }

       public String schema() {
           return " au.id, au.username, hau.app_user_type_enum as appUserTypeId from hab_agent_user hau left join m_appuser au ON au.id = hau.app_user_id "
           		+ " where hau.client_id = ? and  hau.parent_user_id is null ";
       }
       
   }
   
   
  public Collection<AppUserData> getAgentUserByMobileNoAndPin(final String mobileNo) {         
  	 final AgentUserMapperLogin mapper = new AgentUserMapperLogin();
     final String sql = "select " + mapper.schema();
     return this.jdbcTemplate.query(sql, mapper, new Object[] { mobileNo});
  }
  
  private static final class AgentUserMapperLogin implements RowMapper<AppUserData> {
      @Override
      public AppUserData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
          final Long id = rs.getLong("id");
          final String username = rs.getString("username");
          return AppUserData.mobileAppUserData(id, username);
      }

      public String schema() {
      	return  " au.id, au.username from m_appuser au " + 
      			" left join hab_agent_user hau ON hau.app_user_id = au.id "+
      			" where hau.mobile_no = ? ";
      }
      
  }
  
  public Collection<AppUserData> getCustomerUserByMobileNoAndPin(final String mobileNo, final String transactionPin) {         
	  	 final customerUserMapperLogin mapper = new customerUserMapperLogin();
	     final String sql = "select " + mapper.schema();
	     return this.jdbcTemplate.query(sql, mapper, new Object[] { mobileNo});
	  }
	  
	  private static final class customerUserMapperLogin implements RowMapper<AppUserData> {
	      @Override
	      public AppUserData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
	          final Long id = rs.getLong("id");
	          final String username = rs.getString("username");
	          return AppUserData.mobileAppUserData(id, username);
	      }

	      public String schema() {
	      	return  " au.id, au.username from m_appuser au " + 
	      			" left join hab_customer_user  hcu  ON au.id = hcu.app_user_id " + 
	      			" where hcu.mobile_no = ? ";
	      }
	      
	  }
  
	  public Collection<AppUserData> transactionPinVerificationByUser(final Long id, final String transactionPin) {         
	  	 final transactionPinVerification mapper = new transactionPinVerification();
	     final String sql = "select " + mapper.schema();
	     return this.jdbcTemplate.query(sql, mapper, new Object[] { id, transactionPin, transactionPin});
	 }
		  
	  private static final class transactionPinVerification implements RowMapper<AppUserData> {
	      @Override
	      public AppUserData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
	          final Long id = rs.getLong("id");
	          final String username = rs.getString("username");
	          return AppUserData.mobileAppUserData(id, username);
	      }

	      public String schema() {
	      	return  " au.id, au.username ,hau.transaction_pin, hcu.transaction_pin "
	      			+" from m_appuser au left join hab_agent_user hau ON au.id = hau.app_user_id "
	      			+" left join hab_customer_user hcu ON au.id = hcu.app_user_id "
	      			+" where au.id = ? and (hau.transaction_pin = ? or hcu.transaction_pin = ?) ";
	      }
	      
	  }
	  
  	public Collection<AppUserData> checkFaceUniqueId(final Integer faceUniqueId) {         
	  	 final faceUniqueIdByUser mapper = new faceUniqueIdByUser();
	     final String sql = "select " + mapper.schema();
	     return this.jdbcTemplate.query(sql, mapper, new Object[] {faceUniqueId, faceUniqueId});
	 }
		  
	  private static final class faceUniqueIdByUser implements RowMapper<AppUserData> {
	      @Override
	      public AppUserData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
	          final Long id = rs.getLong("id");
	          final String username = rs.getString("username");
	          return AppUserData.mobileAppUserData(id, username);
	      }

	      public String schema() {
	      	return  " au.id, au.username ,hau.face_unique_id, hcu.face_unique_id "
	      			+" from m_appuser au left join hab_agent_user hau ON au.id = hau.app_user_id "
	      			+" left join hab_customer_user hcu ON au.id = hcu.app_user_id "
	      			+" where hau.face_unique_id = ? or hcu.face_unique_id = ? ";
	      }
	      
	  }
	  
	  private static final class UpdateAppUserMapper implements RowMapper<AppUserData> {

	        @Override
	        public AppUserData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {
	            final Long id = rs.getLong("id");
	            final String username = rs.getString("username");
	            return AppUserData.mobileAppUserData(id, username);
	        }

	        public String schema() {
	        	return " au.id, au.username from m_appuser au " + 
	        			" left join hab_customer_user  hcu  ON au.id = hcu.app_user_id " + 
	        			" left join hab_agent_user hau ON hau.app_user_id = au.id "
	        			+ "where (hcu.mobile_no = ? or hau.mobile_no = ?) and au.id != ?";
	        }
	        
	    }
	    
	    public Collection<AppUserData> getCheckAlreadyExistedMobileNoByUserId(final Long userId, final String mobileNO) {         
	   	 final UpdateAppUserMapper mapper = new UpdateAppUserMapper();
	        final String sql = "select " + mapper.schema();
	        return this.jdbcTemplate.query(sql, mapper, new Object[] { mobileNO, mobileNO, userId });
	    
	   }
}