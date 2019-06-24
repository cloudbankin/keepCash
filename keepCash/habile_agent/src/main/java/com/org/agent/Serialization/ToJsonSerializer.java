package com.org.agent.Serialization;

import org.springframework.stereotype.Service;

@Service
public interface ToJsonSerializer {
	
	String toJsonSerializer(Object object);
}
