package iceboxi.wifidirect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

public class TransferServerAsyncTask extends AsyncTask<Object, Void, String> {
	private Context context;
    private TextView statusText;
    
    public TransferServerAsyncTask(Context context, View statusText) {
        this.context = context;
        this.statusText = (TextView) statusText;
    }
    
    @Override
    protected void onPreExecute() {
        statusText.setText("Opening a server socket");
    }
    
	@Override
	protected String doInBackground(Object... params) {
		try {
            ServerSocket serverSocket = new ServerSocket((Integer) params[1]);
            Socket client = serverSocket.accept();
            
            if (params[0] == ServiceAction.PostClientIP) {
				DeviceDetailFragment.saveClientInfo(client.getInetAddress().getHostName(), client.getInetAddress().getHostAddress());
				
				return null;
			} else if (params[0] == ServiceAction.TansferFile) {
				final File f = new File(Environment.getExternalStorageDirectory() + "/"
	                    + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
	                    + ".txt");

	            File dirs = new File(f.getParent());
	            if (!dirs.exists())
	                dirs.mkdirs();
	            f.createNewFile();
	            
	            InputStream inputstream = client.getInputStream();
	            DeviceDetailFragment.copyFile(inputstream, new FileOutputStream(f));
	            
	            serverSocket.close();
	            
	            return f.getAbsolutePath();
			}
            
            return null;
        } catch (IOException e) {
            return null;
        }
	}

	 @Override
     protected void onPostExecute(String result) {
         if (result != null) {
        	 statusText.setText("File copied - " + result);
         }
     }
}
