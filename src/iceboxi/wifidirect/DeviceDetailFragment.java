package iceboxi.wifidirect;


import iceboxi.connect.service.MyClient;
import iceboxi.connect.service.MyServer;
import iceboxi.connect.service.MyService;
import iceboxi.connect.service.ServiceAction;
import iceboxi.system.file.FileHelp;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

	private View mContentView = null;
	private WifiP2pDevice device;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;
    private MyService chatService;
    private String testFilePath = "/Download/123.mp4";
    private final int CHATPORT = 8998;
    
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
                        ((DeviceActionListener) getActivity()).disconnect();
                    }
                });

        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                    	askFile(testFilePath);
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
        	openChatServer(CHATPORT);
		} else if (info.groupFormed) {
			((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
					.getString(R.string.client_text));
			connectToChatServer(CHATPORT);
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
	
	private void openChatServer(final int port) {
		new Thread(new Runnable(){
			public void run() {
				chatService = new MyServer(port);
				serviceWork();
			}
		}).start();
	}
	
	private void connectToChatServer(final int port) {
		new Thread(new Runnable(){
            public void run() {
            	chatService = new MyClient(info.groupOwnerAddress, port);
            	serviceWork();
            }
        }).start();
	}
	
	private void serviceWork() {
		try {
  	        String chatMsg;
  	        while((chatMsg = chatService.getMessage()) != null) {          
  	        	ServiceAction action = ServiceAction.valueOf(chatMsg);
				switch (action) {
				case AskFile:
					chatMsg = chatService.getMessage();
					checkFile(chatMsg);
					break;
					
				case TransferFile:
					chatMsg = FileHelp.getSDPath() + chatService.getMessage();
					chatService.saveFile(chatMsg);
					disconnect();
					break;

				case Disconnect:
					mainHandler.sendEmptyMessage(0x00);
					break;

				default:
					break;
				}
  	        }
    	} catch (IOException ex) {
    		chatService.closeConnection();
    	}
	}
	
	private void setStatusText(String text) {
		TextView view = (TextView) mContentView.findViewById(R.id.status_text);
		view.setText(text);
	}
	
	private MyHandlerMain mainHandler = new MyHandlerMain(this);
	
	private void askFile(String filePath) {
		try {
			setStatusText("ask file");

			chatService.sendMessage(ServiceAction.AskFile.toString());
			chatService.sendMessage(filePath);		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void checkFile(String filePath) {
		String realPath = FileHelp.getSDPath() + filePath;
		File file = new File(realPath);
		
		if (file.exists()) {
			transferFile(filePath);
		} else {
			disconnect();
		}
	}
	
	private void disconnect() {
		try {
			chatService.sendMessage(ServiceAction.Disconnect.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			chatService.closeConnection();
		}
	}
	
	private void transferFile(final String filePath) {
		try { 
            chatService.sendMessage(ServiceAction.TransferFile.toString());
            chatService.sendMessage(filePath);
            chatService.sendFile(FileHelp.getSDPath() + filePath);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static class MyHandlerMain extends Handler {
		private final WeakReference<DeviceDetailFragment> mFragment;
		
		public MyHandlerMain(DeviceDetailFragment fragment) {
			mFragment = new WeakReference<DeviceDetailFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			final DeviceDetailFragment fragment = mFragment.get();
			if (fragment != null) {
				this.obtainMessage();
				
				switch (msg.what) {
				case 0x00:
					((DeviceActionListener) fragment.getActivity()).disconnect();
					fragment.chatService.closeConnection();
					break;

				default:
					break;
				}
			}
		}
	}
}
