package com.org.customer.service;

import java.util.Collection;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.org.customer.command.CommandProcessingResult;
import com.org.customer.command.api.JsonCommand;
import com.org.customer.data.CustomerUserData;
import com.org.customer.model.AppUser;
import com.org.customer.model.CustomerUserEntity;

@Service
public interface CustomerUserService {
	
	CommandProcessingResult createCustomer(JsonCommand command, Long userId,Long agentId);
	 
	CustomerUserData retrieveCustomer(Long userId);
	
	CommandProcessingResult updateCustomer(Long userId,JsonCommand command);
	
	CustomerUserEntity updateCustomerUserEntity(Map<String, Object> changes,CustomerUserEntity oldData,AppUser appUser,JsonCommand command);
	 
	Collection<CustomerUserData> retrieveAllCustomersUnderAgent(Long userId);
	

	CommandProcessingResult updateClientInCustomerDetails(JsonCommand command, Long agentId);

	CommandProcessingResult updateCompanyDetailsInCustomerDetails(JsonCommand command, Long agentId);

	CommandProcessingResult createCustomerSavingsAccount(JsonCommand command, Long agentId, Integer status);

}
