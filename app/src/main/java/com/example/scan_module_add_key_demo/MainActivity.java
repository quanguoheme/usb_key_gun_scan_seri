package com.example.scan_module_add_key_demo;



import android.app.Activity;

import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class  MainActivity extends Activity {

	EditText editText;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		editText=(EditText)findViewById(R.id.editCode);
		final Button open = (Button)findViewById(R.id.btn_scan);
        open.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				editText.setText("");

			}
		});		
	}


	protected void onDestroy() {


		super.onDestroy();
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
	

}
