package com.org.agent.Serialization;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class ToJsonSeralizerSettings implements ToJsonSerializer{
	
	@Override
	public String toJsonSerializer(Object object) {
		ObjectMapper Obj = new ObjectMapper();
		String json = null;
		try {
			json = Obj.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
	return json;
	}
    

}
