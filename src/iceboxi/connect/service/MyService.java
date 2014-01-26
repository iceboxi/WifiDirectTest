package iceboxi.connect.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public abstract class MyService {
	protected Socket socket;
	protected DataInputStream reader;
	protected DataOutputStream writer;
	
	public abstract void closeConnection();
	
	public void setSocket(Socket socket) throws IOException {
		this.socket = socket;
		
		initialIO();
	}
	
	private void initialIO() throws IOException {
		reader = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
		writer = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
	}
	
	public String getMessage() throws IOException {
		return reader.readUTF();
    }
    
    public void sendMessage(String message) throws IOException {
    	writer.writeUTF(message);
    	writer.flush();
    }
	
	public void sendFile(String filePath) {
		try {
			File file = new File(filePath);
			BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
			
			writer.writeLong(file.length());
			writer.flush();
			
			int readin;
            while((readin = inputStream.read()) != -1) { 
            	writer.write(readin); 
            }
            writer.flush();
            
            inputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void saveFile(String filePath) {
		try {
			BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath));
			
			long readin = reader.readLong();
	        while((readin = readin-1) >= 0) { 
	            outputStream.write(reader.read());
	        }

	        outputStream.close();  
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public InetAddress getTargetIP() {
		return socket.getInetAddress();
	}
}
