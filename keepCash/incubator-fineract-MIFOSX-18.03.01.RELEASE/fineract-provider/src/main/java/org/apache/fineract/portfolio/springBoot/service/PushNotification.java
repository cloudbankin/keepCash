package org.apache.fineract.portfolio.springBoot.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class PushNotification {

	public static void createPushNotification(String content, String header,  String key, String value) {	
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("app_id", "3092cc98-e67b-467f-bf86-f77f4602b7c7");
		
		JsonObject jsonObject2 = new JsonObject();
		jsonObject2.addProperty("foo", "bar");
		jsonObject.add("data", jsonObject2);
		
		JsonObject jsonObject3 = new JsonObject();
		jsonObject3.addProperty("en", content);
		jsonObject.add("contents", jsonObject3);
		
		JsonObject jsonObject4 = new JsonObject();
		jsonObject4.addProperty("en", header);
		jsonObject.add("headings", jsonObject4);
		
		JsonArray jsonArray = new JsonArray();
		JsonObject jsonObject5 = new JsonObject();
		jsonObject5.addProperty("field", "tag");
		jsonObject5.addProperty("key", key);
		jsonObject5.addProperty("relation", "=");
		jsonObject5.addProperty("value", value);
		jsonArray.add(jsonObject5);
		jsonObject.add("filters", jsonArray);
		
		String url = "https://onesignal.com/api/v1/notifications";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
      	headers.set("Authorization", "Basic OTJmZTRiZWUtNGQyMC00M2RlLWI1NTAtZDdmYjQzMDlhZjkw");
	    
      	RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> requestBody = new HttpEntity<String>(jsonObject.toString(), headers);
		ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, requestBody, String.class);
	    System.out.print(result.getBody());


	}
		 
	 public static void main(String args[]) {
		 createPushNotification("Transaction is successfully", "Transaction", "userEmail", "sakthi.m@habile.in");
	
	  }
		 
	
}



