package iceboxi.connect.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServer implements MyService {
	private int port;
	private ServerSocket serverSkt;
	private InputStream clientInputStream;
	private OutputStream clientOutputStream;
	private InetAddress clientIP;
	
	public MyServer(int port) {
		this.port = port;
		
		try {
			waitForClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void waitForClient() throws IOException{
		if (serverSkt == null) {
			serverSkt = new ServerSocket();
			serverSkt.setReuseAddress(true);
			serverSkt.bind(new InetSocketAddress(port));
		}
    	Socket clientSkt = serverSkt.accept();
    	
    	clientInputStream = clientSkt.getInputStream();
    	clientOutputStream = clientSkt.getOutputStream();
    	
    	clientIP = clientSkt.getInetAddress();
	}
	
	@Override
	public void sendFile(String filePath) {
		try {
			File file = new File(filePath);
			DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(clientOutputStream));
			BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
			
			int readin;
            while((readin = inputStream.read()) != -1) { 
            	dataOut.write(readin); 
            }
            
            dataOut.close();
            inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void saveFile(String filePath) {
		try {
			BufferedInputStream inputStream = new BufferedInputStream(clientInputStream);
			BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath));
			
			int readin; 
	        while((readin = inputStream.read()) != -1) { 
	            outputStream.write(readin);
	        }
	        
	        inputStream.close();
	        outputStream.close();  
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public InetAddress getTargetIP() {
		return clientIP;
	}
	
	@Override
	public String getMessage() throws IOException {
		BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientInputStream));
    	return clientReader.readLine();
    }
    
	@Override
    public void sendMessage(String message) {
		PrintStream clientWriter = new PrintStream(clientOutputStream);
    	clientWriter.println(message);
    }
    
	@Override
    public void closeConnection() {
    	try {
            serverSkt.close();
    	} catch (IOException e) {	
    	}
    }
}
