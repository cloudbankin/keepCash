package com.org.customer.service.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.org.customer.model.AppUser;
import com.org.customer.repository.AppUserRepository;

@Service
public class AppUserServiceImpl {
	
	private final AppUserRepository appUserRepository;
	
	@Autowired
	public AppUserServiceImpl(final AppUserRepository appUserRepository) {
		this.appUserRepository = appUserRepository;
	}

	public AppUser findByIdAppUser(Long appUserId) {
		return appUserRepository.findByid(appUserId);
		
	}
}
