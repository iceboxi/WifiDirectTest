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
    public static final String ACTION_SEND_FILE = "iceboxi.wifidirect.SEND_FILE";
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
		System.out.println("333here go");
		Context context = getApplicationContext();
		
		if (intent.getAction().equals(ACTION_SEND_FILE)) {
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
                	System.out.println("here go");
//                    is = cr.openInputStream(Uri.parse(fileUri));
                	is = new ByteArrayInputStream("Hello Java World!".getBytes());
//                } catch (FileNotFoundException e) {
//                    Log.d(WiFiDirectActivity.TAG, e.toString());
                } catch (Exception e) {
					// TODO: handle exception
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
