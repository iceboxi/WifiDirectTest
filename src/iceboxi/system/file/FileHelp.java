package iceboxi.system.file;

import java.io.File;

import android.content.Context;
import android.os.Environment;

public class FileHelp {
	private Context context;
	
	public FileHelp(Context context) {
		this.context = context;
	}
	
	public static String getSDPath() { 
		File SDRootDir = null; 
		boolean isSDCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); 
		if (isSDCardExist) {                               
			SDRootDir = Environment.getExternalStorageDirectory();
		}   
		return SDRootDir.toString(); 
	}
}
