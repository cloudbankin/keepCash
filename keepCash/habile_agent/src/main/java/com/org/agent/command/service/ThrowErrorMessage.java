package com.org.agent.command.service;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.org.agent.command.exception.ApiErrorMessageArg;
import com.org.agent.command.exception.ApiParameterError;

public class ThrowErrorMessage {

	 public static String errorMessageHandler(List<ApiParameterError> dataValidationErrors) {
	    	String errorMessage = null;
	    	JsonObject json = null;
	    	if(!dataValidationErrors.isEmpty()) {
	    		json = new JsonObject();
	    		JsonArray jsonArray = new JsonArray();
	    		
	    		for(ApiParameterError dataValidationError: dataValidationErrors) {
	    			JsonObject jsonObject = new JsonObject();
	    			
	        		jsonObject.addProperty("developerMessage", dataValidationError.getDeveloperMessage());
	        		jsonObject.addProperty("defaultUserMessage", dataValidationError.getDefaultUserMessage());
	        		jsonObject.addProperty("userMessageGlobalisationCode", dataValidationError.getUserMessageGlobalisationCode());
	        		jsonObject.addProperty("parameterName", dataValidationError.getParameterName());
	        		jsonObject.add("value", null);
	        		if(!dataValidationError.getArgs().isEmpty()) {
	        			JsonArray array = new JsonArray();
	        			for(ApiErrorMessageArg arg : dataValidationError.getArgs()) {
	        				JsonObject jsonArgs = new JsonObject();
	        				jsonArgs.addProperty("value", arg.getValue().toString());
	        				array.add(jsonArgs);
	            		}
	        			jsonObject.add("args", array);
	        		}
	        		
	        		jsonArray.add(jsonObject);
	        	}
	    		json.add("errors", jsonArray);
	    	}

	    	if(json != null) {
	    		errorMessage = json.toString();
	    	}
	    	return errorMessage;
	    }
}
