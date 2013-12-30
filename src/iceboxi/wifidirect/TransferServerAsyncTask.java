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

public class TransferServerAsyncTask extends AsyncTask<Void, Void, String> {
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
	protected String doInBackground(Void... params) {
		try {
            ServerSocket serverSocket = new ServerSocket(8988);
            Socket client = serverSocket.accept();
            final File f = new File(Environment.getExternalStorageDirectory() + "/"
                    + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
                    + ".txt");

            File dirs = new File(f.getParent());
            if (!dirs.exists())
                dirs.mkdirs();
            f.createNewFile();

            System.out.println("4444 here go");
            
            InputStream inputstream = client.getInputStream();
            DeviceDetailFragment.copyFile(inputstream, new FileOutputStream(f));
            
            System.out.println("5555 here go");
            serverSocket.close();
            return f.getAbsolutePath();
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
