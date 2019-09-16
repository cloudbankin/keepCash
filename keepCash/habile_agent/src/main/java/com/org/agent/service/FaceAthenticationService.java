package com.org.agent.service;

import org.springframework.stereotype.Service;

import com.org.agent.data.FaceAthenticationData;

@Service
public interface FaceAthenticationService {

	FaceAthenticationData getFaceIdValues();
}
