package org.apache.fineract.portfolio.springBoot;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.UriInfo;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class HttpConnectionTemplate {
	
	public static String createBootUrl(UriInfo uriInfo, String path) {
		URI uri = uriInfo.getRequestUri();
		//String bootUrl = uri.getScheme() + "://" + uri.getHost() + "8001" + path;
		String bootUrl = "http://" + uri.getHost() + ":8001" + path;
		return bootUrl;
	}
	
	public static String createBootUrlInCustomer( UriInfo uriInfo, String path) {
		URI uri = uriInfo.getRequestUri();		
		String bootUrl = "http://" + uri.getHost() + ":8002" + path;
		
		return bootUrl;
	}
	
	public static String createDefaultBootUrl(String path) {
		String bootUrl = "http://" + "35.154.152.146" + ":8001" + path;
		//String bootUrl = "http://" + "localhost" + ":8001" + path;
		//String bootUrl = "http://" + "13.234.122.243" + ":8001" + path;
		return bootUrl;
	}
	
	public static String restTemplate( String url, String Json, HttpMethod httpMethod) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		RestTemplate restTemplate = new RestTemplate();
		HttpEntity<String> requestBody = new HttpEntity<String>(Json, headers);
		ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.POST, requestBody, String.class);
  
	    return result.getBody();
	}
      public static String restTemplateForGetMethod( String url, HttpMethod httpMethod) {
	      HttpHeaders headers = new HttpHeaders();
	      headers.setContentType(MediaType.APPLICATION_JSON);
	      RestTemplate restTemplate = new RestTemplate();
	      HttpEntity<String> entity = new HttpEntity<String>(headers);
	      ResponseEntity<String> result = restTemplate.exchange(url, httpMethod, entity, String.class);

	    return result.getBody();
	}
	
      public static String restTemplateForPutMethod( String url, String Json, HttpMethod httpMethod) {
			HttpHeaders headers = new HttpHeaders();
	  		headers.setContentType(MediaType.APPLICATION_JSON);
	  		RestTemplate restTemplate = new RestTemplate();
	  		HttpEntity<String> requestBody = new HttpEntity<String>(Json, headers);
	  		ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.PUT, requestBody, String.class);
		    
  	    return result.getBody();
	}
	
	 public static String fileFormatRestTemplate(String url, String path, MultiValueMap<String , Object> formData) throws IOException {
	      MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
	      bodyMap.add("user-file", getUserFileResource(path));
	      bodyMap.putAll(addFormDataInMap(formData, bodyMap));
	      HttpHeaders headers = new HttpHeaders();
	      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
	      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);

	      RestTemplate restTemplate = new RestTemplate();
	      ResponseEntity<String> response = restTemplate.exchange(url,
	              HttpMethod.POST, requestEntity, String.class);
	      System.out.println("response status: " + response.getStatusCode());
	      System.out.println("response body: " + response.getBody());
	      return response.getBody();
	  }

	  public static Resource getUserFileResource(String path) throws IOException {
	      //todo replace tempFile with a real file
	      Path tempFile = Files.createTempFile(path, path.substring(path.length()-5));
	      Files.write(tempFile, "some test content...\nline1\nline2".getBytes());
	      System.out.println("uploading: " + tempFile);
	      File file = tempFile.toFile();
	      //to upload in-memory bytes use ByteArrayResource instead
	      return new FileSystemResource(file);
	  }
	  
	  public static MultiValueMap<String , Object> addFormDataInMap(MultiValueMap<String, Object> formData, MultiValueMap<String, Object> bodyMap) {
		  Set set = formData.entrySet();
	      Iterator iterator = set.iterator();
	      while(iterator.hasNext()) {
	         Map.Entry mentry = (Map.Entry)iterator.next();
	         bodyMap.add(mentry.getKey().toString(), mentry.getValue());
	      }
		  return bodyMap;
	  }
	  
	
}
