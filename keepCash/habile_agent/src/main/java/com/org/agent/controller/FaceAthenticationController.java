package com.org.agent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.org.agent.command.FromJsonHelper;
import com.org.agent.service.FaceAthenticationService;

@RestController
@RequestMapping("/face")
public class FaceAthenticationController {

	@Autowired
	private final FaceAthenticationService faceAthenticationService;
	private final FromJsonHelper fromJsonHelper;
	
	@Autowired
	public FaceAthenticationController(final FaceAthenticationService faceAthenticationService, final FromJsonHelper fromJsonHelper) {
		this.faceAthenticationService = faceAthenticationService;
		this.fromJsonHelper = fromJsonHelper;
	}
	
	@GetMapping(value = "/faceId")
	public String getFaceAthentication() {
		return fromJsonHelper.toJson(faceAthenticationService.getFaceIdValues());
	}
	
}
