package iceboxi.wifidirect;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

public class TransferService extends IntentService {
	private static final int SOCKET_TIMEOUT = 5000;
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
    
    public TransferService(String name) {
		super(name);
	}
    
    public TransferService() {
        super("TransferService");
    }
    
	@Override
	protected void onHandleIntent(Intent intent) {
		Context context = getApplicationContext();
		
		if (intent.getAction().equals(ServiceAction.TansferFile.toString())) {
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

            try {
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;
                try {
                	is = new ByteArrayInputStream("Hello Java World!".getBytes());
                } catch (Exception e) {
                	
				}
                DeviceDetailFragment.copyFile(is, stream);
            } catch (IOException e) {
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }

        } else if (intent.getAction().equals(ServiceAction.PostClientIP.toString())) {
        	String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            
            try {
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;
                try {
                	is = new ByteArrayInputStream("".getBytes());
                } catch (Exception e) {
                	
				}
                DeviceDetailFragment.copyFile(is, stream);
            } catch (IOException e) {
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }
		}
	}

}
