package iceboxi.connect.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MyClient extends MyService{
	private final int SOCKET_TIMEOUT = 5000;
    
    public MyClient(InetAddress serverInetAddr, int serverPort) {
    	try {
    		connectToServer(serverInetAddr, serverPort);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	public void connectToServer(InetAddress serverInetAddr, int serverPort) throws IOException {
		Socket socket = new Socket();
		socket.bind(null);
		socket.connect((new InetSocketAddress(serverInetAddr, serverPort)), SOCKET_TIMEOUT);
		
		setSocket(socket);
	}
    
    
	@Override
    public void closeConnection() {
    	try {
            socket.close();
    	} catch (IOException e) {
    	}    	
    }
}
