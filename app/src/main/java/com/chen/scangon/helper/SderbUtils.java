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

package com.chen.scangon.helper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.security.InvalidParameterException;

import gidb.com.Sderb;


public   class SderbUtils {
	private WeakReference<ScanGunKeyEventHelper.OnScanSuccessListener> mOnScanSuccessListener;

	protected Sderb mSderb;
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
					byte[] buffer = new byte[1000];
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



	public void onCreate(ScanGunKeyEventHelper.OnScanSuccessListener onScanSuccessListener) {
		mOnScanSuccessListener = new WeakReference<ScanGunKeyEventHelper.OnScanSuccessListener>(onScanSuccessListener);
		this.stop=false;
		try {
			mSderb = getSerialPort();
			mOutputStream = mSderb.getOutputStream();
			mInputStream = mSderb.getInputStream();

			/* Create a receiving thread */
			mReadThread = new ReadThread();
			mReadThread.start();
		} catch (SecurityException e) {
			//DisplayError(R.string.error_security);
		} catch (IOException e) {
			//DisplayError(R.string.error_unknown);
		} catch (Exception e) {
			//DisplayError(R.string.error_configuration);
		}
	}

	public    void onDataReceived(final byte[] buffer, final int size,final int n)
	{
		ScanGunKeyEventHelper.OnScanSuccessListener listener=  mOnScanSuccessListener.get();
		if (listener != null &&  size !=0) {
			try {
				listener.onScanSuccess("acm:"+new String(buffer,0,size,"utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

		}
		//Log.d("ca1","jason2: "+bytesToHexString(buffer,size) +",size:"+size);

	}
/*
	private static String bytesToHexString(byte[] bytes,int size) {
		StringBuilder sb = new StringBuilder(bytes.length);
		byte[] var3 = bytes;
		int var4 = size;

		for(int var5 = 0; var5 < var4; ++var5) {
			byte aByte = var3[var5];
			String temp = Integer.toHexString(255 & aByte);
			if (temp.length() < 2) {
				sb.append(0);
			}

			sb.append(temp);
		}

		return sb.toString();
	}*/
	public void onDestroy() {
		this.stop=true;
		if (mReadThread != null)
			mReadThread.interrupt();
		 closeSerialPort();
		mSderb = null;
		try
		{
			mOutputStream.close();
			mInputStream.close();
		} catch (IOException e) {
		}

	}




	public Sderb getSerialPort() throws SecurityException, IOException, InvalidParameterException {
		if (mSderb == null) {

			mSderb = new Sderb();
		}
		return mSderb;
	}

	public void closeSerialPort() {
		if (mSderb != null) {
			mSderb.close();
			mSderb = null;
		}
	}
}
