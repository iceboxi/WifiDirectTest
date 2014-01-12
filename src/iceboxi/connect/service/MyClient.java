package iceboxi.connect.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MyClient {
	private final int SOCKET_TIMEOUT = 5000;
	private Socket socket;
    private BufferedReader serverReader;
    private PrintStream serverWriter;
	
	public void connectToServer(InetAddress serverInetAddr, int serverPort) throws IOException {
		socket = new Socket();
		socket.bind(null);
		socket.connect((new InetSocketAddress(serverInetAddr, serverPort)), SOCKET_TIMEOUT);
//		socket = new Socket(serverInetAddr, serverPort);
		
		serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    	serverWriter = new PrintStream(socket.getOutputStream());
	}
	
    public String getServerMessage() throws IOException {
    	return serverReader.readLine();
    }
    
    public void sendMessageToServer(String message) {
    	serverWriter.println(message);
    }
    
    public void closeConnection() {
    	try {
            socket.close();
    	}
    	catch(IOException e) {
    		
    	}    	
    }
}
