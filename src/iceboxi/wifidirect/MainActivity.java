package iceboxi.wifidirect;

import iceboxi.wifidirect.R;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity implements DeviceActionListener{

	public static final String TAG = "wifidirecttest";
	private WifiP2pManager manager;
	private boolean isWifiP2pEnabled = false;
	
	private final IntentFilter intentFilter = new IntentFilter();
	private Channel channel;
	private BroadcastReceiver receiver = null;
	
	public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// add necessary intent values to be matched.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}

	public void resetData() {
		DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
				.findFragmentById(R.id.frag_list);
		DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
				.findFragmentById(R.id.frag_detail);
		if (fragmentList != null) {
			fragmentList.clearPeers();
		}
		if (fragmentDetails != null) {
			fragmentDetails.resetViews();
		}
	}
	
	public boolean searchPeers(View v) {
		if (!isWifiP2pEnabled) {
            Toast.makeText(MainActivity.this, "Wifi-direct is not available", Toast.LENGTH_SHORT).show();
            return true;
        }
		
		DeviceListFragment listFragment = (DeviceListFragment) getFragmentManager()
				.findFragmentById(R.id.frag_list);
		listFragment.onInitiateDiscovery();
		
		manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(MainActivity.this, "Discovery Initiated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(MainActivity.this, "Discovery Failed : " + reasonCode, Toast.LENGTH_SHORT).show();
            }
        });
		return true;
	}
	
	@Override
	public void showDetails(WifiP2pDevice device) {
		DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.showDetails(device);
	}

	@Override
	public void cancelDisconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connect(WifiP2pConfig config) {
		manager.connect(channel, config, new ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
	}

	@Override
	public void disconnect() {
		final DeviceDetailFragment detailFragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
		detailFragment.resetViews();
        manager.removeGroup(channel, new ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);

            }

            @Override
            public void onSuccess() {
            	detailFragment.getView().setVisibility(View.GONE);
            }

        });
	}

}
