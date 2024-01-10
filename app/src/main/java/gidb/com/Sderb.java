/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package gidb.com;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class Sderb {

	private static final String TAG = "Sderb";

	/*
	 * Do not remove or rename the field mFd: it is used by native method close();
	 */
	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;

	public static String execCommand(String cmd){
		Process process = null;
		DataOutputStream os = null;
		String msg="";
		try{
			process = Runtime.getRuntime().exec("hdxsu");
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd+"\n");
			os.flush();
			os.writeBytes("exit\n");
			os.flush();

			String line;

			//Log.i(TAG, "execCommand: " + "BufferedReader");
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));

			//Log.i(TAG, "bufferedReader: " + "BufferedReader readLine");
			while ((line = bufferedReader.readLine()) != null) {
				msg+=line+"\n";
				Log.i(TAG, "bufferedReader read: " + line);
			}
			// Log.i(TAG, "waitFor");
			process.waitFor();
			// Log.i(TAG, "waitFor END");
		} catch (Exception e) {
			e.printStackTrace();
			return msg;
		} finally {
			try {
				if (os != null)   {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return msg;
	}
	public static int execShellCmdForStatue(String command) {
		int status = -1;
		try {
			Process process = null;
			BufferedReader error = null;
			BufferedReader reader = null;
			BufferedWriter writer = null;
			process = Runtime.getRuntime().exec("hdxsu");
			writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
			error = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			//writer.write("wm overscan 0,0,0,-210 \n");
			writer.write(command);
			writer.flush();


			Log.d(TAG, " _________ddd----- command: " + command + "    status = " + status);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return status;

	}
	public Sderb(File device, int baudrate, int flags) throws SecurityException, IOException {

		/* Check access permission */
		if (!device.canRead() || !device.canWrite()) {
			
			try {
				Log.d(TAG, "1============================");
				// Missing read/write permission, trying to chmod the file 
				//Process su;
				//su = Runtime.getRuntime().exec("/system/xbin/su");
				//String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
						//+ "exit\n";
				String cmd = " chmod 666 " + device.getAbsolutePath() ;
				Log.d(TAG, "cmd====================="+cmd);
				//Runtime.getRuntime().exec(cmd);
				execCommand(cmd);
				//su.getOutputStream().write(cmd.getBytes());
				if (/*(su.waitFor() != 0) || */!device.canRead()
						|| !device.canWrite()) {
					Log.d(TAG, "2=====================");
					throw new SecurityException();
				}
			} catch (Exception e) {
				Log.d(TAG, "3=================");
				e.printStackTrace();
				throw new SecurityException();
			}
			
			//do_exec("su root\n");
			//Log.e(TAG, "=============cmd : "+device.getAbsolutePath());
			//do_exec("chmod 755 " + device.getAbsolutePath() + "\n");
			//do_exec("rm /sdcard/123");
		//Runtime.getRuntime().exec("/system/xbin/su");
		//Runtime.getRuntime().exec("chmod 755 /dev/ttyS1");

		}

		mFd = getdesc(device.getAbsolutePath(), baudrate, flags);
		if (mFd == null) {
			Log.d(TAG, "native open returns null");
			throw new IOException();
		}
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
	}

	// Getters and setters
	public InputStream getInputStream() {
		return mFileInputStream;
	}

	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}
	
	String do_exec(String cmd) {
		  String s = "/n";
		  try {  
			  Process p = Runtime.getRuntime().exec(cmd);
			  BufferedReader in = new BufferedReader(
								  new InputStreamReader(p.getInputStream()));
			  String line = null;
			  while ((line = in.readLine()) != null) {	
				  s += line + "/n"; 				
			  }  
		  } catch (IOException e) {
			  // TODO Auto-generated catch block  
			  e.printStackTrace();	
		  }  
	
		  return cmd;		
	}  

	// JNI 
	private native static FileDescriptor getdesc(String path, int baudrate, int flags);
	public native void close();
	static {
		System.loadLibrary("serial_port_d");
	}
}
