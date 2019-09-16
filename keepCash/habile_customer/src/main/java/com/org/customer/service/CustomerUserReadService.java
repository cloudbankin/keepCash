package com.org.customer.service;

import java.util.Collection;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.org.customer.command.CommandProcessingResult;
import com.org.customer.command.api.JsonCommand;
import com.org.customer.core.service.Page;
import com.org.customer.core.service.SearchParameters;
import com.org.customer.data.CustomerUserData;
import com.org.customer.model.AppUser;
import com.org.customer.model.CustomerUserEntity;

@Service
public interface CustomerUserReadService {	
	Page<CustomerUserData> retrieveAllCustomers(final SearchParameters searchParameters);
	


}
