package iceboxi.connect.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

public abstract class MyService {
	protected Socket socket;
	protected BufferedReader reader;
	protected PrintStream writer;
	
	public abstract void closeConnection();
	
	public void setSocket(Socket socket) throws IOException {
		this.socket = socket;
		
		initialIO();
	}
	
	private void initialIO() throws IOException {
		reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		writer = new PrintStream(socket.getOutputStream());
	}
	
	public String getMessage() throws IOException {
    	return reader.readLine();
    }
    
    public void sendMessage(String message) {
    	writer.println(message);
    }
	
	public void sendFile(String filePath) {
		try {
			File file = new File(filePath);
			DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
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
	
	public void saveFile(String filePath) {
		try {
			BufferedInputStream inputStream = new BufferedInputStream(socket.getInputStream());
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
	
	public InetAddress getTargetIP() {
		return socket.getInetAddress();
	}
}
