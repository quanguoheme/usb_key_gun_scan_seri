package com.chen.scangon.helper;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.KeyEvent;

import java.lang.ref.WeakReference;


/**
 * 扫码枪事件解析类 by chen
 */
public class ScanGunKeyEventHelper {


    public ScanGunKeyEventHelper(OnScanSuccessListener onScanSuccessListener) {
        Scanner_Tools.getUtil().onCreate(onScanSuccessListener);

    }



    /**
     * 扫码枪事件解析
     * @param event
     */
    public void analysisKeyEvent(KeyEvent event) {


    }



    public interface OnScanSuccessListener {
        void onScanSuccess(String barcode);
    }


    public void onDestroy() {
        Scanner_Tools.getUtil().onDestroy();

    }


    public boolean isScanGunEvent(KeyEvent event) {
       // Log.d("ca1","name:"+event.getDevice().getName()+",vid:"+Integer.toHexString(event.getDevice().getVendorId())+",pid:"+Integer.toHexString(event.getDevice().getProductId()));
        return true;
       // return event.getDevice().getName().equals(mDeviceName);
    }



}





