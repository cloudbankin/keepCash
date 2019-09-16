package com.org.customer.controller;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.core.UriInfo;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

public class HttpConnectionRestTemplate implements HostnameVerifier {
//	private static final TrustingHostnameVerifier TRUSTING_HOSTNAME_VERIFIER = new TrustingHostnameVerifier();

	
	@Override
    public boolean verify(final String hostname, final SSLSession session) {
        return true;
    }
	
	public static String createSpringUrl(UriInfo uriInfo, String path) {
		URI uri = uriInfo.getRequestUri();
		String bootUrl = "https://" + uri.getHost() + ":8443" + path;
		
		return bootUrl;
	}
	
	
	
	private static final class TrustingHostnameVerifier implements HostnameVerifier {

		@Override
	    public boolean verify(final String hostname, final SSLSession session) {
	        return true;
	    }
	}
	
	public static String connectRestTemplate( String url, String Json, HttpMethod httpMethod) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
	      HttpHeaders headers = new HttpHeaders();
	      
	     // HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
	      
	   /*   TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
	      
	      SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
	                      .loadTrustMaterial(null, acceptingTrustStrategy)
	                      .build();
	   
	      SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
	   
	      CloseableHttpClient httpClient = HttpClients.custom()
	                      .setSSLSocketFactory(csf)
	                      .build();
	   
	      HttpComponentsClientHttpRequestFactory requestFactory =
	                      new HttpComponentsClientHttpRequestFactory();
	   
	      requestFactory.setHttpClient(httpClient);
	      RestTemplate restTemplate = new RestTemplate(requestFactory);*/
	      
	      
	      
	      
	 
	    //  CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
	      
	 
	     RestTemplate restTemplate = new RestTemplate();
	      
	   /*   try {
			SSLUtil.turnOffSslChecking();
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	      
	  

	    

	      headers.setContentType(MediaType.APPLICATION_JSON);
	      HttpEntity<String> requestBody = new HttpEntity<String>(Json, headers);
	        restTemplate.exchange(url, httpMethod, requestBody, String.class);
	      
	    return null;
	}
	

	

	 
}
