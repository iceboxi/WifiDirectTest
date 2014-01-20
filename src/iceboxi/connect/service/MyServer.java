package iceboxi.connect.service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class MyServer extends MyService {
	private int port;
	private ServerSocket serverSkt;
	
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
		
		setSocket(serverSkt.accept());
	}
    
	@Override
    public void closeConnection() {
    	try {
            serverSkt.close();
    	} catch (IOException e) {	
    	}
    }
}
