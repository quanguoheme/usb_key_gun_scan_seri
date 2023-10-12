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

package com.example.scan_module_add_key_demo;

import android.util.Log;

import com.chen.scangon.helper.ScanGunKeyEventHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;


public   class SerialPortUtils {
	private WeakReference<ScanGunKeyEventHelper.OnScanSuccessListener> mOnScanSuccessListener;

	protected SerialPort mSerialPort;
	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	protected ReadThread mReadThread;
	//private static final String TAG = "SerialPortActivity";
	private int n = 0;
	public boolean stop = false; 
	class ReadThread extends Thread {
			String TAG="ddd";
		@Override
		public void run() {
			super.run();
			while(!stop) {
				int size;
				try {
					byte[] buffer = new byte[512];
					//Log.d(TAG,"*");

					if (mInputStream == null) return;
					if(mInputStream.available()>0)
					{
						size = mInputStream.read(buffer);
						//buffer[size]=0;
						if (size > 0) {
							onDataReceived(buffer, size,n);
						}
					}
					else
					{
							try {
							sleep(300);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}					
						
					}

					
					
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
			//Log.d(TAG,"quck");
		}
	}



	protected void onCreate(ScanGunKeyEventHelper.OnScanSuccessListener onScanSuccessListener) {
		mOnScanSuccessListener = new WeakReference<ScanGunKeyEventHelper.OnScanSuccessListener>(onScanSuccessListener);
		this.stop=false;
		try {
			mSerialPort = getSerialPort();
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread */
			mReadThread = new ReadThread();
			mReadThread.start();
		} catch (SecurityException e) {
			//DisplayError(R.string.error_security);
		} catch (IOException e) {
			//DisplayError(R.string.error_unknown);
		} catch (InvalidParameterException e) {
			//DisplayError(R.string.error_configuration);
		}
	}

	public    void onDataReceived(final byte[] buffer, final int size,final int n)
	{
		ScanGunKeyEventHelper.OnScanSuccessListener listener=  mOnScanSuccessListener.get();
		if (listener != null &&  size !=0) {
			try {
				listener.onScanSuccess(new String(buffer,0,size,"utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}


	}


	protected void onDestroy() {
		this.stop=true;
		if (mReadThread != null)
			mReadThread.interrupt();
		 closeSerialPort();
		mSerialPort = null;
		try
		{
			mOutputStream.close();
			mInputStream.close();
		} catch (IOException e) {
		}

	}




	public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
		if (mSerialPort == null) {
			/* Read serial port parameters */
			//SharedPreferences sp = getSharedPreferences("android_serialport_api.sample_preferences", MODE_PRIVATE);
			String path = "/dev/ttyACM0";
			//String path = "/dev/ttyS3";

			int baudrate = 9600;//Integer.decode(sp.getString("BAUDRATE", "-1"));

			/* Open the serial port */
			mSerialPort = new SerialPort(new File(path), baudrate, 0);
		}
		return mSerialPort;
	}

	public void closeSerialPort() {
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
	}
}
