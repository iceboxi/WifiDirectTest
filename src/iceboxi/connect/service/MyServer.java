package iceboxi.connect.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServer {
	private int port;
	private ServerSocket serverSkt;
	private BufferedReader clientReader;
	private PrintStream clientWriter;
	
	public MyServer(int port) {
		this.port = port;
	}
	
	public String waitForClient() throws IOException{
		serverSkt = new ServerSocket(port);
    	Socket clientSkt = serverSkt.accept();
    	
    	clientReader = new BufferedReader(new InputStreamReader(clientSkt.getInputStream()));
    	clientWriter = new PrintStream(clientSkt.getOutputStream());
    	
    	return clientSkt.getInetAddress().getHostAddress();
	}
	
	public String getClientMessage() throws IOException {
    	return clientReader.readLine();
    }
    
    public void sendMessageToClient(String message) {
    	clientWriter.println(message);
    }
    
    public void closeConnection() {
    	try {
            serverSkt.close();
    	}
    	catch(IOException e) {	
    	}
    }
}
