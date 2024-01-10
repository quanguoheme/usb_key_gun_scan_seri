package com.example.scan_module_add_key_demo;



import android.app.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;

import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chen.scangon.helper.PasswdUtils;
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
        // 获取当前设备的电源管理器
       reg_Receiver_for_BatteryCharging(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        mScanGunKeyEventHelper = new ScanGunKeyEventHelper(this);
        open.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				editText.setText("");
                TextViewCode.setText("");
			}
		});
        init_view();

	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }
    Button btn_pd3;
    void init_view()
    {
            btn_pd3 = (Button)findViewById(R.id.btn_pd3);
        final Button open = (Button)findViewById(R.id.btn_pd2);
        open.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                    }
                }).start();
            }
        });
    }

	protected void onDestroy() {

        mScanGunKeyEventHelper.onDestroy();
        unReg_Receiver_for_BatteryCharging(this);

		super.onDestroy();
	}
    long begin_time=0;
    boolean is_begin_time=false;
    boolean from_hid=false;
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        long current=System.currentTimeMillis();
        if(current -begin_time > 1_000)
        {
            begin_time=System.currentTimeMillis();
            Log.d("ca1","name:"+event.getDevice().getName()+",vid:"+Integer.toHexString(event.getDevice().getVendorId())+",pid:"+Integer.toHexString(event.getDevice().getProductId()));

        }

         if (mScanGunKeyEventHelper.isScanGunEvent(event)) {

             from_hid=true;
         }
         mScanGunKeyEventHelper.analysisKeyEvent(event);
        return true;
        //  return super.dispatchKeyEvent(event);
    }

    String m_str="";
    int count_line=0;
    @Override
    public void onScanSuccess(final String barcode) {
        long current=System.currentTimeMillis();
        float ddd=(float)(current -begin_time);
        ddd=ddd/1000;
        current=(long )(ddd*100);
        String str="["+((current) )+"]from_ser:";
        begin_time=0;
        if(from_hid)
        {
            str="["+(current -begin_time )+"]from_ser:";
        }
       final String stdd=str+PasswdUtils.getMd5(barcode);
        count_line++;
        if( count_line >9)
        {
            count_line=0;
            m_str="";
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(MainActivity.this, "barcode length: " +barcode.length()+",code:"+ barcode, Toast.LENGTH_SHORT).show();
                m_str="\n"+count_line+" barcode length: "+barcode.length()+",md5sum:" +stdd+m_str;
                editText.setText(m_str );
            }
        });

        Log.d("ca1","jason: "+barcode.length()+"," +PasswdUtils.getMd5(barcode)+",code:"+barcode);
    }


    public     BatteryReceiver   mBatteryReceiver2=new BatteryReceiver();
    public   class BatteryReceiver extends BroadcastReceiver {
        public static final String TAG = "batt2";

        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取广播事件
            String action = intent.getAction();

            if(!TextUtils.equals("android.intent.action.BATTERY_CHANGED", action)){
                    return;
            }

            Log.e(TAG, "  jason2 :  554335level:"+
                    ", intent.getAction():"+ intent.getAction());
            Log.e(TAG, " jason2  " + intent.getIntExtra("level", -1));
            Log.e(TAG, " jason2  " + intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -2));
            final int levde=intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -2);
            //BatteryManager.BATTERY_STATUS_CHARGING 表示是充电状态
// BatteryManager.BATTERY_STATUS_DISCHARGING 放电中
// BatteryManager.BATTERY_STATUS_NOT_CHARGING 未充电
// BatteryManager.BATTERY_STATUS_FULL 电池满
            if(BatteryManager.BATTERY_STATUS_CHARGING  == intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN))
            {
                Log.e(TAG, " jason2  BATTERY_STATUS_CHARGING " + intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(levde ==100)
                        {
                            btn_pd3.setText("BATTERY_STATUS_is full 充电满 :"+levde);
                        }
                        else
                        btn_pd3.setText("BATTERY_STATUS_CHARGING 充电中 :"+levde);
                    }
                });
            }
            else
            {
                Log.e(TAG, " jason2 ,battery is not at charging, " + intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_UNKNOWN));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btn_pd3.setText("battery is not at charging(未充电) :"+levde);
                    }
                });
            }



        }
    }

    public   void unReg_Receiver_for_BatteryCharging(Context context){
        context.unregisterReceiver(mBatteryReceiver2);
    }

    /**
     * 主动获取当前电池是否在充电 , 即数据线是否插在手机上
     * @return
     */
    public   void reg_Receiver_for_BatteryCharging(Context context){
        boolean isBatteryCharging = false;

        // 主动发送包含是否正在充电状态的广播 , 该广播会持续发送
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        // 注册广播接受者
        Intent intent = context.registerReceiver(mBatteryReceiver2, intentFilter);


    }
}
