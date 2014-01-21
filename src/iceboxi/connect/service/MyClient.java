package iceboxi.connect.service;

import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class MyClient extends MyService{
	private final int SOCKET_TIMEOUT = 5000;
	private final int MAX_RETRY = 10;
    
    public MyClient(InetAddress serverInetAddr, int serverPort) {
    	try {
    		connectToServer(serverInetAddr, serverPort);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	public void connectToServer(InetAddress serverInetAddr, int serverPort) throws ConnectException {	
		int count = 0;
		while (count < MAX_RETRY) {
			try {
				Socket socket = new Socket();
				socket.bind(null);
				socket.connect((new InetSocketAddress(serverInetAddr, serverPort)), SOCKET_TIMEOUT);
				
				if (socket.isConnected()) {
					setSocket(socket);
					break;
				} else {
					socket.close();
				}
			} catch (Exception e) {
				count++;
				e.printStackTrace();
			}
		}
		
		if (count >= MAX_RETRY) {
			throw new ConnectException("refuse connect");
		}
	}
    
    
	@Override
    public void closeConnection() {
    	try {
    		socket.close();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}    	
    }
}
