package com.example.scan_module_add_key_demo;



import android.app.Activity;

import android.os.Bundle;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.chen.scangon.helper.ScanGunKeyEventHelper;

import java.util.HashMap;
import java.util.Map;


public class  MainActivity extends Activity implements ScanGunKeyEventHelper.OnScanSuccessListener {

	EditText editText;
    TextView TextViewCode;
    private ScanGunKeyEventHelper mScanGunKeyEventHelper;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		editText=(EditText)findViewById(R.id.editCode);
        TextViewCode =findViewById(R.id.TextViewCode);
		final Button open = (Button)findViewById(R.id.btn_scan);

        mScanGunKeyEventHelper = new ScanGunKeyEventHelper(this);
        open.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				editText.setText("");
                TextViewCode.setText("");
			}
		});		
	}


	protected void onDestroy() {

        mScanGunKeyEventHelper.onDestroy();
		super.onDestroy();
	}


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

       //  if (mScanGunKeyEventHelper.isScanGunEvent(event)) {
        mScanGunKeyEventHelper.analysisKeyEvent(event);
      //  return true;
       // }
        return true;
        //  return super.dispatchKeyEvent(event);
    }


    @Override
    public void onScanSuccess(String barcode) {
        Log.d("ca1","jason: "+barcode);
    }

/*
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        checkLetterStatus(event);
        keyCodeToNum(keyCode);
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            Log.e("ca1","键盘事件"+ buffer.toString());
            buffer.delete(0, buffer.length());
            return true;
        }

        return false;
    }


    //检查shift键
    private void checkLetterStatus(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT || keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                //按着shift键，表示大写
                mCaps = true;
            } else {
                //松开shift键，表示小写
                mCaps = false;
            }
        }
    }
    //根据keycode得到对应的字母和数字
    private void keyCodeToNum(int keycode) {
        if (keycode >= KeyEvent.KEYCODE_A && keycode <= KeyEvent.KEYCODE_Z) {
            if (mCaps) {
                buffer.append(map.get(keycode).toUpperCase());
            } else {
                buffer.append(map.get(keycode));
            }

        } else if ((keycode >= KeyEvent.KEYCODE_0 && keycode <= KeyEvent.KEYCODE_9)) {
            buffer.append(keycode - KeyEvent.KEYCODE_0);
        } else {
            //暂不处理特殊符号
        }

    }


    static Map<Integer, String> map = new HashMap<>();
   static {

       map.put(29, "a");
       map.put(30, "b");
       map.put(31, "c");
       map.put(32, "d");
       map.put(33, "e");
       map.put(34, "f");
       map.put(35, "g");
       map.put(36, "h");
       map.put(37, "i");
       map.put(38, "g");
       map.put(39, "k");
       map.put(40, "l");
       map.put(41, "m");
       map.put(42, "n");
       map.put(43, "0");
       map.put(44, "p");
       map.put(45, "q");
       map.put(46, "r");
       map.put(47, "s");
       map.put(48, "t");
       map.put(49, "u");
       map.put(50, "v");
       map.put(51, "w");
       map.put(52, "x");
       map.put(53, "y");
       map.put(54, "z");
    }*/
}
