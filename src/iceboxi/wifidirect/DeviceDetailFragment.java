package iceboxi.wifidirect;


import iceboxi.connect.service.MyService;
import iceboxi.connect.service.MyClient;
import iceboxi.connect.service.MyServer;
import iceboxi.connect.service.ServiceAction;
import iceboxi.system.file.FileHelp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import org.json.JSONObject;

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
    private MyService chatService;
    private MyService transferFileService;
    private String testFilePath = "/Download/123.pdf";
    private final int CHATPORT = 8998;
    private final int TRANSFERPORT = 8988;
    
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
        	openChatServer(CHATPORT, mainHandler);
		} else if (info.groupFormed) {
			((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
					.getString(R.string.client_text));
			connectToChatServer(CHATPORT, mainHandler);
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
	
	private void openChatServer(final int port, final Handler handler) {
		new Thread(new Runnable(){
			public void run() {
				try {
					Message message = Message.obtain();
					chatService = new MyServer(port);

					String clientMessage;
					while((clientMessage = chatService.getMessage()) != null) {    
						message.obj = clientMessage;
						handler.sendMessage(message);
					}
				} catch (IOException ex) {
					ex.printStackTrace();
					chatService.closeConnection();
				}
			}
		}).start();
	}
	
	private void connectToChatServer(final int port, final Handler handler) {
		new Thread(new Runnable(){
            public void run() {
            	try {
            		Message message = Message.obtain();
            		
            		chatService = new MyClient(info.groupOwnerAddress, port);
          	      
          	        String serverMessage;
          	        while((serverMessage = chatService.getMessage()) != null) {                          	        	
          	        	message.obj = serverMessage;
          	        	handler.sendMessage(message);
          	        }
            	} catch (IOException ex) {
            		chatService.closeConnection();
            	}
            }
        }).start();
	}
	
	private void setStatusText(String text) {
		TextView view = (TextView) mContentView.findViewById(R.id.status_text);
		view.setText(text);
	}
	
	private MyHandlerMain mainHandler = new MyHandlerMain(this);
	
	private void askFile(String filePath) {
		try {
			setStatusText("ask file");
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("action", ServiceAction.AskFile);
			jsonObject.put("filePath", filePath);
			
			chatService.sendMessage(jsonObject.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void checkFile(String filePath) {
		String realPath = FileHelp.getSDPath() + filePath;
		File file = new File(realPath);
		
		isFileExist(file.exists(), filePath);
	}
	
	private void isFileExist(boolean isExist, String filePath) {
		try {
			setStatusText("answer file exist");
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("action", ServiceAction.FileExist);
			jsonObject.put("status", isExist);
			if (isExist) {
				jsonObject.put("filePath", filePath);
			}
			
			chatService.sendMessage(jsonObject.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void giveMeFile(String filePath) {
		try {
			setStatusText("give me file");
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("action", ServiceAction.GiveMeFile);
			jsonObject.put("filePath", filePath);
			
			chatService.sendMessage(jsonObject.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void disconnect() {
		try {
			setStatusText("disconnect");
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("action", ServiceAction.Disconnect);
			
			chatService.sendMessage(jsonObject.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			chatService.closeConnection();
		}
	}
	
	private void transferFile(String filePath) {
		try { 
            new Thread() {
				@Override
				public void run() {
					transferFileService = new MyServer(TRANSFERPORT);
					System.out.println("1");
					transferFileService.sendFile(FileHelp.getSDPath()+testFilePath);
					System.out.println("2");
					transferFileService.closeConnection();
					System.out.println("3");
					fileHandler.sendEmptyMessage(0);
					System.out.println("4");
				}
            }.start();
			
            JSONObject jsonObject = new JSONObject();
			jsonObject.put("action", ServiceAction.TransferFile);
			jsonObject.put("filePath", filePath);
            chatService.sendMessage(jsonObject.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private MyHandler fileHandler = new MyHandler(this);
	
	private static class MyHandler extends Handler {
		private final WeakReference<DeviceDetailFragment> mFragment;
		
		public MyHandler(DeviceDetailFragment fragment) {
			mFragment = new WeakReference<DeviceDetailFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			DeviceDetailFragment fragment = mFragment.get();
			if (fragment != null) {
				fragment.setStatusText("Done");
				fragment.disconnect();
			}
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
				
				try {
					JSONObject jsonObject = new JSONObject(msg.obj.toString());
					ServiceAction action = ServiceAction.valueOf(jsonObject.getString("action"));
					switch (action) {
					case AskFile:
						fragment.setStatusText(jsonObject.getString("filePath"));
						fragment.checkFile(jsonObject.getString("filePath"));
						break;
						
					case FileExist:
						fragment.setStatusText(jsonObject.getString("status"));
						if (jsonObject.getBoolean("status")) {
							fragment.giveMeFile(jsonObject.getString("filePath"));
						} else {
							fragment.disconnect();
						}
						break;
						
					case GiveMeFile:
						fragment.setStatusText("IT SAY GIVE ME FILE!");
						fragment.transferFile(jsonObject.getString("filePath"));
						break;
						
					case TransferFile:
						new Thread() {
							@Override
							public void run() {
								fragment.transferFileService = new MyClient(fragment.chatService.getTargetIP(), fragment.TRANSFERPORT);
								fragment.transferFileService.saveFile(FileHelp.getSDPath() + fragment.testFilePath);
								fragment.transferFileService.closeConnection();
								fragment.fileHandler.sendEmptyMessage(0);
							}
						}.start();
						
						String filePath = jsonObject.getString("filePath");
						fragment.setStatusText("Transfer..." + filePath);
						break;
						
					case Disconnect:
						fragment.setStatusText("disconnect");
						
	                    ((DeviceActionListener) fragment.getActivity()).disconnect();
	                    fragment.chatService.closeConnection();
						break;
					default:
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
