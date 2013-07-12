import java.io.*;
import java.net.*;
import java.util.*;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

final class HttpRequest implements Runnable {
    
    final static String CRLF = "\r\n";
    Socket socket;

    // Constructor
    public HttpRequest(Socket socket) throws Exception {
        this.socket = socket;
    }

    // Implement the run() method of the Runnable interface.
    public void run() {
        try {
            disableCertificateValidation();
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    private static void sendBytes(FileInputStream fis, OutputStream os) 
    throws Exception {
    // Construct a 1K buffer to hold bytes on their way to the socket.
    byte[] buffer = new byte[1024];
    int bytes = 0;

    // Copy requested file into the socket's output stream.
    while((bytes = fis.read(buffer)) != -1 ) {
        os.write(buffer, 0, bytes);
        }
    }

    private static String contentType(String fileName) {
    if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
        return "text/html";
    }
    if(fileName.endsWith(".jpeg") || fileName.endsWith(".jpg")) {
    return "image/jpeg";
    }
    if(fileName.endsWith(".gif")) {
    return "image/gif";
    }
    return "application/json";
    }

    public static void disableCertificateValidation() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }};
        
        // Ignore differences between given hostname and certificate hostname
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) { return true; }
        };
        
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
        } catch (Exception e) {}
    }

    private void processRequest() throws Exception {
        // Get a reference to the socket's input and output streams.
        InputStream is = socket.getInputStream();
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());

        // Set up input stream filters.
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        // Get the request line of the HTTP request message.
        String requestLine = new String(br.readLine());

        // Display the request line.
        System.out.println();
        System.out.println(requestLine);

        // Get and display the header lines.
        String headerLine = null;
        while ((headerLine = br.readLine()).length() != 0) {
            System.out.println(headerLine);
        }
        
            // Extract the filename from the request line.
    StringTokenizer tokens = new StringTokenizer(requestLine);
    tokens.nextToken(); // skip over the method, which should be "GET"
    String fileName = tokens.nextToken();
    // Prepend a "." so that file request is within the current directory.
    fileName = "." + fileName;

    // Open the requested file.
    FileInputStream fis = null;
    boolean fileExists = true;
    try {
    fis = new FileInputStream(fileName);
    } catch (FileNotFoundException e) {
    fileExists = false;
    }

    // Construct the response message.
    String statusLine = null;
    String contentTypeLine = null;
    String connClose = null;
        String entityBody = null;
    if (fileExists) {
    statusLine = "200 OK" + CRLF;
    contentTypeLine = "Content-type: " + 
        contentType( fileName ) + CRLF;
    } else {
    statusLine = "HTTP/1.1 200 OK" + CRLF;
        connClose = "Connection:close" + CRLF;
    contentTypeLine = "Content-type: application/json" + CRLF;

    }
        // Send the status line.
          os.writeBytes(statusLine);
          os.writeBytes(connClose);
        
        // Send the content type line.
         os.writeBytes(contentTypeLine);
        
        // Send a blank line to indicate the end of the header lines.
         os.writeBytes(CRLF);
        
        HttpsURLConnection httpCon = null;

try
		{
		    //Use this line to get Inbox for the User whose credentials are passed as the request header.....
		    URL url = new URL("https://192.168.1.226:8443/vmrest/user");
		    //example: https://sea-vm-ks-50.cisco.com:8443/vmrest/mailbox/folders/inbox
		 
		    //Use this line to get Inbox for any other User other than the User whose credentials are passed...
		    //it has a query parameter named userobjectid
			//URL url = new URL("https://"+APIConfig.Server+"/vmrest/mailbox/folders/inbox?userobjectid=fbe5d01b-e568-4729-9d34-9b964f567d29");
		     //example: https://sea-vm-ks-50.cisco.com:8443/vmrest/mailbox/folders/inbox?userobjectid=fbe5d01b-e568-4729-9d34-9b964f567d29
		    
		    
		    //This code is for passing the Username password in the header parameters - start
		     String userPassword = "cstachowicz:C1sco12345";
		     String encoding = new sun.misc.BASE64Encoder().encode (userPassword.getBytes());
		    //This code is for passing the Username password in the header parameters - end		
		    
		     
		    //This line opens an HTTP Connection on the URL 
			httpCon = (HttpsURLConnection) url.openConnection();
			
			//This line sets up the authentication information in the request header 
			httpCon.setRequestProperty ("Authorization", "Basic " + encoding);
			
			//The line setup the request method...
			httpCon.setRequestMethod("GET");  
			
			//This code is for reading the response from the URL and printing it on console - start
			InputStream fin = httpCon.getInputStream();
			int i = fin.read();
			String xml = "";
			while(i != -1)
			{
				char ch = (char) i;
				xml = xml + ch;
				i = fin.read();
			}
			fin.close();
			fin = null;
			//This code is for reading the response from the URL and printing it on console - end
			
			httpCon.disconnect();
			httpCon = null;
            System.out.println("API Output::" + xml);
            String xmlNoHead = xml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", "");
			//	String messageCount = getMessageCount(xml);
			//	System.out.println("Message Count::" + messageCount);
			os.writeBytes("callback({\"xml\": \""+  xmlNoHead + "\"})");
			
		}
		catch(Exception ex)
		{
		    	System.out.println("There is an error::" + ex);
		}
		finally
		{
			try
			{
				if(httpCon != null)  {httpCon.disconnect();  httpCon = null;}
			}
			catch(Exception e){}
		}






    // Send the entity body.
    if (fileExists) {
    sendBytes(fis, os);
    fis.close();
    } else {
  //  os.writeBytes("File DNE: Content Not Found!");
    }

        // Close streams and socket.
        os.close();
        br.close();
        socket.close();
    }
    public static void main(String[] args) throws Exception {
        final ServerSocket ss = new ServerSocket(10080);
        while (true)
            new HttpRequest(ss.accept()).run();
    }
}
