package iceboxi.connect.service;

import java.io.IOException;
import java.net.InetAddress;

public interface MyService {
	public String getMessage() throws IOException;
	public void sendMessage(String message);
	public void closeConnection();
	public void sendFile(String filePath);
	public void saveFile(String filePath);
	public InetAddress getTargetIP();
}
