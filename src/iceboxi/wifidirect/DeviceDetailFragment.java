package iceboxi.wifidirect;


import iceboxi.connect.service.MyClient;
import iceboxi.connect.service.MyServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpPost;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

	private View mContentView = null;
	private WifiP2pDevice device;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;
    MyServer myServer;
    MyClient myClient;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_detail, null);
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            	dismissDialog();
            	
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true
                        );
                ((DeviceActionListener) getActivity()).connect(config);

            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                    	if (myClient != null) {
                    		myClient.closeConnection();
						}
                    	if (myServer != null) {
                    		myServer.closeConnection();
						}
                        ((DeviceActionListener) getActivity()).disconnect();
                    }
                });

        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                    	//暫時只是轉過去直接傳送固定字串
                    	if (myClient != null) {
                    		myClient.sendMessageToServer("clinet sent something");
						}
                    	if (myServer != null) {
                    		myServer.sendMessageToClient("server sent something");
						}
                    }
                });

        return mContentView;
    }
    
	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		dismissDialog();
		this.info = info;
        this.getView().setVisibility(View.VISIBLE);
        
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                        : getResources().getString(R.string.no)));
        
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());
        
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
        
        if (info.groupFormed && info.isGroupOwner) {
        	// TODO 有人要開socket，先考慮one to one
        	openServer(8988, uiHandler);
		} else if (info.groupFormed) {
			((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
					.getString(R.string.client_text));
			connectToServer(8988, uiHandler);
		}
        
	}
	
	public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());
    }
	
	private void dismissDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
	}
	
	public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }
	
	public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(MainActivity.TAG, e.toString());
            return false;
        }
        return true;
    }
	
	private void openServer(final int port, final Handler handler) {
		new Thread(new Runnable(){
			public void run() {
				try {
					Message message = Message.obtain();
					myServer = new MyServer(port);
					String ipString = myServer.waitForClient();
					System.out.println("ip = " + ipString);

					String clientMessage;
					while((clientMessage = myServer.getClientMessage()) != null) {    
						message.obj = clientMessage;
						handler.sendMessage(message);
					}
				} catch (IOException ex) {
					myServer.closeConnection();
				}
			}
		}).start();
	}
	
	private void connectToServer(final int port, final Handler handler) {
		new Thread(new Runnable(){
            public void run() {
            	try {
            		Message message = Message.obtain();
            		
            		myClient = new MyClient();
          	        myClient.connectToServer(info.groupOwnerAddress, port);
          	      
          	        String serverMessage;
          	        while((serverMessage = myClient.getServerMessage()) != null) {                          	        	
          	        	message.obj = serverMessage;
          	        	handler.sendMessage(message);
          	        }
            	} catch (IOException ex) {
            		myClient.closeConnection();
            	}
            }
        }).start();
	}
	
	Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			TextView view = (TextView) mContentView.findViewById(R.id.status_text);
			if (info.groupFormed && info.isGroupOwner) {
				view.setText("I am ower, " + msg.obj.toString());
			} else if (info.groupFormed) {
				view.setText("I am client, " + msg.obj.toString());
			}
		}
	};
}
