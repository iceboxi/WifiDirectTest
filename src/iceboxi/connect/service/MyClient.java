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
import java.net.Socket;

public class MyClient implements MyService{
	private final int SOCKET_TIMEOUT = 5000;
	private Socket socket;
    private InputStream serveriInputStream;
    private OutputStream serverOutputStream;
    
    public MyClient(InetAddress serverInetAddr, int serverPort) {
    	try {
    		connectToServer(serverInetAddr, serverPort);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	public void connectToServer(InetAddress serverInetAddr, int serverPort) throws IOException {
		socket = new Socket();
		socket.bind(null);
		socket.connect((new InetSocketAddress(serverInetAddr, serverPort)), SOCKET_TIMEOUT);
		
		serveriInputStream = socket.getInputStream();
		serverOutputStream = socket.getOutputStream();
	}
	
	public void sendFile(String filePath) {
		try {
			File file = new File(filePath);
			DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(serverOutputStream));
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
			BufferedInputStream inputStream = new BufferedInputStream(serveriInputStream);
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
		return socket.getInetAddress();
	}
	
	@Override
    public String getMessage() throws IOException {
		BufferedReader serverReader = new BufferedReader(new InputStreamReader(serveriInputStream));
    	return serverReader.readLine();
    }
    
	@Override
    public void sendMessage(String message) {
		PrintStream serverWriter = new PrintStream(serverOutputStream);
    	serverWriter.println(message);
    }
    
	@Override
    public void closeConnection() {
    	try {
            socket.close();
    	} catch (IOException e) {
    	}    	
    }
}
