package com.example.scan_module_add_key_demo;



import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android_serialport_api.SerialPort;
import hdx.HdxUtil;

public class  MainActivity extends Activity {
	private static final String TAG = "scan";
	private static final int BUZZER_ON=1;
	private static final int BUZZER_OFF=2;
	private TTT   app;
	EditText editText;
	protected SerialPort mSerialPort;
	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread; 
	MyHandler handler;
	DecodeString decode;
    protected static MessageDigest messagedigest = null;  
    boolean opened=false;
    private String code="";
	BroadcastReceiver mIntentReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		handler = new MyHandler();
		decode = new DecodeString(500);
		
 /*       long begin = System.currentTimeMillis();  
        
        File file = new File("/system/lib/libandroid_runtime.so");  
        try {
			String md5 = getFileMD5String(file);
			Log.d(TAG,"md5 "+md5);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
  
        long end = System.currentTimeMillis(); 
        
        Log.d(TAG,"time "+ (end-begin));*/
		app = new TTT();
		editText=(EditText)findViewById(R.id.editCode);  
        HdxUtil.SwitchSerialFunction(HdxUtil.SERIAL_FUNCTION_IDCARD);
		open();
		
        mIntentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                	Log.d(TAG,"screen on");
                	open();
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                	//WakeLock lock;
                   // PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
                    //lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
                    //lock.acquire();
                	Log.d(TAG,"screen off");
                	close();
                	//lock.release();
               	
                } else {
                    Log.w(TAG, "unexpected Intent: " + intent.getAction());
                }
            }

        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        getBaseContext().registerReceiver(mIntentReceiver, filter);		
        
		final Button open = (Button)findViewById(R.id.btn_scan);
        open.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				editText.setText("");
				code = "";
				opened=true;
				////Trig();
				HdxUtil.TriggerScan();	
			}
		});		
	}


	protected void onDestroy() {
		close();
		getBaseContext().unregisterReceiver(mIntentReceiver);
		super.onDestroy();
	}
	protected void onDataReceived(final String  id) {
		runOnUiThread(new Runnable() {
			public void run() {

				String str;
				
				str = editText.getText().toString();
//				Log.d(TAG,"onDataReceived1 "+id+" "+str);
//				if(str.length()>2 && str.charAt(str.length()-2)==13&& str.charAt(str.length()-1)==10)
//					str = id;
//				else 
				str += id;
				editText.setText(str);
				code=str;
				Log.d(TAG,"onDataReceived2 "+id+" "+str);
				//if(str.length()>2 && str.charAt(str.length()-2)==13&& str.charAt(str.length()-1)==10){
					handler.removeMessages(BUZZER_ON);
//					HdxUtil.TriggerScan();	
					handler.sendMessageDelayed(handler.obtainMessage(BUZZER_ON, 1, 0,null),100);
				//	editText.setText(str);
				//}
					
			}
		});
	}		
    boolean open()
    {
    	boolean enable=true;
		try {
			mSerialPort = app.getSerialPort();
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();
			/* Create a receiving thread */
			
			mReadThread = new ReadThread();
			mReadThread.start();

			enable = false;
		} catch (Exception e) {
		}

    	return true;
    }
    boolean close()
    {
		if (mReadThread != null)
		{
			mReadThread.interrupt();
		}

		app.closeSerialPort();
		mSerialPort = null;
		mOutputStream=null;
		mInputStream=null;
		
		return true;
    }    
	private class ReadThread extends Thread {

		private byte[] response=new byte[1024];
		
		private void porcessPacket(byte buffer[],int size)
		{
		}
		@Override
		public void run() {
			super.run();
			int resp_size=0;
			while(!isInterrupted()) {
				int size;
				Log.d(TAG,"resp_size1 "+resp_size);
				try {
					byte[] buffer = new byte[128];
					if (mInputStream == null) return;
					size = mInputStream.read(buffer);
					int i;
					
					Log.d(TAG,"size "+size);
					if(size>0){
						String resp=new String();
						byte arr[]=new byte[size];
						for(i=0;i<size;i++)
						{
							Log.d(TAG,"rece "+Byte.toString(buffer[i]));
							arr[i]= buffer[i];
						}
						onDataReceived(new String(arr));
					}
					
					
				} catch (IOException e) {
					e.printStackTrace();
					Log.d(TAG,"read exception");
					return;
				}
			}
			Log.d(TAG,"quit readthrad");
		}
	}    
    
	private class TTT
	{
//		public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
		private SerialPort mSerialPort = null;
	
		public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
			if (mSerialPort == null) {
				String path = "/dev/ttyS1";  //053
				int baudrate = 9600;
	
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
	
	private class DecodeString{
		private int interval;
		private long lastTime;
		private String code="";
		public DecodeString(int inter){
			interval=inter;
			lastTime = System.currentTimeMillis();
		}
		
		public String decode(String str){
			long time = System.currentTimeMillis();
			if(time-lastTime>=interval)
				code = str;
			else
				code += str;
			lastTime = time;
			return code;
		}
	}
	
	private class MyHandler extends Handler{
        public void handleMessage(Message msg) {
             switch (msg.what) {
                case BUZZER_ON:
                	//beep
                	this.removeMessages(BUZZER_OFF);
                	HdxUtil.EnableBuzze(1);
                	HdxUtil.PowerOffScan();
                	sendMessageDelayed(obtainMessage(BUZZER_OFF, 1, 0,null),500);
                	
                    break;
                case BUZZER_OFF:
                	HdxUtil.EnableBuzze(0);
               default:
                  	break;
            }
        }		
	}
	

	
    private static String bufferToHex(byte bytes[]) {  
        return bufferToHex(bytes, 0, bytes.length);  
    }  
    protected static char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6',  
        '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };  
  
    private static String bufferToHex(byte bytes[], int m, int n) {  
        StringBuffer stringbuffer = new StringBuffer(2 * n);  
        int k = m + n;  
        for (int l = m; l < k; l++) {  
            appendHexPair(bytes[l], stringbuffer);  
        }  
        return stringbuffer.toString();  
    }  
  
    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {  
        char c0 = hexDigits[(bt & 0xf0) >> 4];// ȡ�ֽ��и� 4 λ������ת��, >>> Ϊ�߼����ƣ�������λһ������,�˴�δ�������ַ����кβ�ͬ   
        char c1 = hexDigits[bt & 0xf];// ȡ�ֽ��е� 4 λ������ת��   
        stringbuffer.append(c0);  
        stringbuffer.append(c1);  
    }  	
	

    public static String getFileMD5String(File file) throws IOException {         
        InputStream fis;  
        fis = new FileInputStream(file);  
        byte[] buffer = new byte[1024];  
        int numRead = 0;  
        while ((numRead = fis.read(buffer)) > 0) {  
            messagedigest.update(buffer, 0, numRead);  
        }  
        fis.close();  
        return bufferToHex(messagedigest.digest());  
    }  	
    static {  
        try {  
            messagedigest = MessageDigest.getInstance("MD5");  
        } catch (NoSuchAlgorithmException nsaex) {  
            nsaex.printStackTrace();  
        } 
    }
}
