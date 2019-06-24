package com.org.agent.service.implementation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.org.agent.model.AppUser;
import com.org.agent.repository.AppUserRepository;

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
