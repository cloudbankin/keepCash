package com.org.agent.data;

public class FaceAthenticationData {

	private String licenseId;
	private String publicId;
	
	
	public FaceAthenticationData(String licenseId, String publicId) {
		super();
		this.licenseId = licenseId;
		this.publicId = publicId;
	}


	public String getLicencseId() {
		return licenseId;
	}


	public void setLicencseId(String licenseId) {
		this.licenseId = licenseId;
	}


	public String getPublicId() {
		return publicId;
	}


	public void setPublicId(String publicId) {
		this.publicId = publicId;
	}
	
	
}
