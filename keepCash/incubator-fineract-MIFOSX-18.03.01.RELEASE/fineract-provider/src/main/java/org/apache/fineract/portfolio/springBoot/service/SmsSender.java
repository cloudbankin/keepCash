package org.apache.fineract.portfolio.springBoot.service;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SmsSender {
	// Username that is to be used for submission
	String username;
	// password that is to be used along with username
	String password;
	// Message content that is to be transmitted
	String message;
	/**
	* What type of the message that is to be sent
	* <ul>
	* <li>0:means plain text</li>
	* <li>1:means flash</li>
	* <li>2:means Unicode (Message content should be in Hex)</li>
	* <li>6:means Unicode Flash (Message content should be in Hex)</li>
	* </ul>
	*/
	String type;
	/**
	* Require DLR or not
	* <ul>
	* <li>0:means DLR is not Required</li>
	* <li>1:means DLR is Required</li>
	* </ul>
	*/
	String dlr;
	/**
	* Destinations to which message is to be sent For submitting more than one
	* destination at once destinations should be comma separated Like
	* 91999000123,91999000124
	*/
	String destination;
	// Sender Id to be used for submitting the message
	String source;
	// To what server you need to connect to for submission
	String server;
	// Port that is to be used like 8080 or 8000
	int port;
	public SmsSender(String server, int port, String username, String password,
	String message, String dlr, String type, String destination,
	String source) {

		this.username = username;
		this.password = password;
		this.message = message;
		this.dlr = dlr;
		this.type = type;
		this.destination = destination;
		this.source = source;
		this.server = server;
		this.port = port;
	}

	private void submitMessage(String message , String mobileNumber) {
		try {
			// Url that will be called to submit the message
			/*URL sendUrl = new URL("http://" + this.server + ":" + this.port
			+ "/bulksms/bulksms");
			*/
						
			URL sendUrl = new URL("http://sms6.rmlconnect.net:8080/bulksms/bulksms?username=BENKEEPD1&password=hGxMPXya&type=0&dlr=1&destination="+mobileNumber+"&source=KEEPIT&message="+message+"");
			HttpURLConnection httpConnection = (HttpURLConnection) sendUrl
			.openConnection();
			// This method sets the method type to POST so that
			// will be send as a POST request
			httpConnection.setRequestMethod("POST");
			// This method is set as true wince we intend to send
			// input to the server
			httpConnection.setDoInput(true);
			// This method implies that we intend to receive data from server.
			httpConnection.setDoOutput(true);
			// Implies do not use cached data
			httpConnection.setUseCaches(false);
			// Data that will be sent over the stream to the server.
			DataOutputStream dataStreamToServer = new DataOutputStream(
			httpConnection.getOutputStream());
			dataStreamToServer.writeBytes("username="
			+ URLEncoder.encode(this.username, "UTF-8") + "&password="
			+ URLEncoder.encode(this.password, "UTF-8") + "&type="
			+ URLEncoder.encode(this.type, "UTF-8") + "&dlr="
			+ URLEncoder.encode(this.dlr, "UTF-8") + "&destination="
			+ URLEncoder.encode(this.destination, "UTF-8") + "&source="
			+ URLEncoder.encode(this.source, "UTF-8") + "&message="
			+ URLEncoder.encode(this.message, "UTF-8"));
			dataStreamToServer.flush();
			dataStreamToServer.close();
			// Here take the output value of the server.
			BufferedReader dataStreamFromUrl = new BufferedReader(
			new InputStreamReader(httpConnection.getInputStream()));
			String dataFromUrl = "", dataBuffer = "";
			// Writing information from the stream to the buffer
			while ((dataBuffer = dataStreamFromUrl.readLine()) != null) {
				dataFromUrl += dataBuffer;
			}
			
			dataStreamFromUrl.close();
			System.out.println("Response: " + dataFromUrl);
		}
		/**
		* Now dataFromUrl variable contains the Response received from the
		* server so we can parse the response and process it accordingly.
		*/
		
		 catch (Exception ex) {
			ex.printStackTrace();
		 }
	}
	
	public static void sendOtp(String OTP, String destination) {
		try {
			String message = OTP+"%20is%20your%20OTP.%20Please%20do%20not%20share%20this%20OTP%20with%20anyone.%20Thank%20you,%20keepCash.";
			
			SmsSender s = new SmsSender("", 8080, "BENKEEPD1",
					"hGxMPXya","", "1", "0","",
					"KEEPIT");
			s.submitMessage(message, destination);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void sendTransactionPIN(String PIN, String destination) {
		try {
			String message = PIN+"%20is%20your%20transaction%20PIN.%20do%20not%20share%20this%20transaction%20PIN%20with%20anyone.%20Thank%20you,%20keepCash.";
			
			SmsSender s = new SmsSender("", 8080, "BENKEEPD1",
					"hGxMPXya","", "1", "0","",
					"KEEPIT");
			s.submitMessage(message, destination);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	
	
	
	
	public static void main(String[] args) {
	try {
	// Below exmaple is for sending Plain text
	SmsSender s = new SmsSender("", 8080, "BENKEEPD1",
	"hGxMPXya", "", "1", "0", "",
	"KEEPIT");
	
		/*SmsSender s = new SmsSender("ems.rmlconnect.net", 8080, "BenKeep",
				"qzNZ95q9", "test for unicode", "1", "0", "9629461303",
				"KEEPIT");
		*/
	//String message = new String ("1235isyourtransactionPINdonotsharethistransactionPINwithanyone.Thankyou,keepCash.");
	//String message = SendRandomOtpMessage.randomAuthorizationTokenGeneration();
	
	String message = "1234%20is%20your%20transaction%20PIN.%20do%20not%20share%20this%20transaction%20PIN%20with%20anyone.%20Thank%20you,%20keepCash.";
	//String message = new String("isyou");
	s.submitMessage(message,"9629461303");
	
	// Below exmaple is for sending unicode
	/*SmsSender s1 = new SmsSender("client-us.rmlconnect.net", 2345, "KeepCash",
	"ZkvAxq77", convertToUnicode("test for unicode").toString(),
	"1", "2", "9629461303", "KEEPIT");
	s1.submitMessage();*/
	} catch (Exception ex) {
	}
	}
	/**
	* Below method converts the unicode to hex value
	* @param regText
	* @return
	*/
	private static StringBuffer convertToUnicode(String regText) {
	char[] chars = regText.toCharArray();
	StringBuffer hexString = new StringBuffer();
	for (int i = 0; i < chars.length; i++) {
	String iniHexString = Integer.toHexString((int) chars[i]);
	if (iniHexString.length() == 1)
		iniHexString = "000" + iniHexString;
		else if (iniHexString.length() == 2)
		iniHexString = "00" + iniHexString;
		else if (iniHexString.length() == 3)
		iniHexString = "0" + iniHexString;
		hexString.append(iniHexString);
		}
		System.out.println(hexString);
		return hexString;
		}
	}