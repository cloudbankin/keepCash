package org.apache.fineract.portfolio.springBoot;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.UriInfo;

import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.security.exception.ForcePasswordResetException;
import org.apache.fineract.portfolio.springBoot.exception.RestTemplateResponseErrorHandler;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HttpConnectionTemplate {
	
	/*public static String httpUrlConnection(String targetURL, String urlParameters) {
		  HttpURLConnection connection = null;

		  try {
		    //Create connection
		    URL url = new URL("http://localhost:8080/welcome/");
		    connection = (HttpURLConnection) url.openConnection();
		    connection.setRequestMethod("GET");
		    connection.setRequestProperty("Content-Type", 
		        "application/x-www-form-urlencoded");

		   // connection.setRequestProperty("Content-Length", 
		   //     Integer.toString(urlParameters.getBytes().length));
		    connection.setRequestProperty("Content-Language", "en-US");  

		    connection.setUseCaches(false);
		    connection.setDoOutput(true);

		    //Send request
		    DataOutputStream wr = new DataOutputStream (
		        connection.getOutputStream());
		   // wr.writeBytes(urlParameters);
		    wr.close();
		    

		    //Get Response  
		    InputStream is = connection.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
		    String line;
		    while ((line = rd.readLine()) != null) {
		      response.append(line);
		      response.append('\r');
		    }
		    rd.close();
		    return response.toString();
		  } catch (Exception e) {
		    e.printStackTrace();
		    return null;
		  } finally {
		    if (connection != null) {
		      connection.disconnect();
		    }
		  }
		}
	
	
	public static void httpUrlConnectionTemplate()
	{
	    final String uri = "http://localhost:8080/welcome/";

	    RestTemplate restTemplate = new RestTemplate();
	    String result = restTemplate.getForObject(uri, String.class);

	    System.out.println(result);
	}*/
	
	public static String createBootUrl(UriInfo uriInfo, String path) {
		URI uri = uriInfo.getRequestUri();
		//String bootUrl = uri.getScheme() + "://" + uri.getHost() + "8001" + path;
		String bootUrl = "http://" + uri.getHost() + ":8001" + path;
		return bootUrl;
	}
	
	public static String createBootUrlInCustomer(UriInfo uriInfo, String path) {
		URI uri = uriInfo.getRequestUri();		
		String bootUrl = "http://" + uri.getHost() + ":8002" + path;
		
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
	  
	  
	 /* public static void givenAcceptingAllCertificatesUsing( String url, String Json) 
			  throws ClientProtocolException, IOException {
			      CloseableHttpClient httpClient = HttpClients.custom()
			          .setSSLHostnameVerifier(new NoopHostnameVerifier())
			          .build();
			      HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			      requestFactory.setHttpClient(httpClient);
			      
			      HttpHeaders headers = new HttpHeaders();
			      headers.setContentType(MediaType.APPLICATION_JSON);
			 
			      // Data attached to the request.
			      HttpEntity<String> requestBody = new HttpEntity<String>(Json, headers);
			 
			      ResponseEntity<String> response = new RestTemplate(requestFactory).exchange(url, HttpMethod.POST, requestBody, String.class);
			      //assertThat(response.getStatusCode().value(), equalTo(200));
			      response = response; 
			  }*/
	
}
