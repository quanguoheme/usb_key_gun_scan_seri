package com.chen.scangon.helper;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Set;


/**
 * 扫码枪事件解析类 by chen
 */
public class ScanGunKeyEventHelper {

    private final static long MESSAGE_DELAY = 500;             //延迟500ms，判断扫码是否完成。
    private StringBuffer mStringBufferResult;                  //扫码内容
    private boolean mCaps;                                     //大小写区分
    private final HandlerThread workThread;
    private Handler mHandler;
    //private final BluetoothAdapter mBluetoothAdapter;
    private final Runnable mScanningFishedRunnable;
   // private OnScanSuccessListener mOnScanSuccessListener;
    private WeakReference<OnScanSuccessListener> mOnScanSuccessListener;
   // private String mDeviceName;

    public ScanGunKeyEventHelper(OnScanSuccessListener onScanSuccessListener) {
        mOnScanSuccessListener = new WeakReference<OnScanSuccessListener>(onScanSuccessListener);
     //   mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mStringBufferResult = new StringBuffer();
        workThread = new HandlerThread("scan_gun");
        workThread.start();
        mHandler=new  Handler(workThread.getLooper());
        mScanningFishedRunnable = new Runnable() {
            @Override
            public void run() {
                performScanSuccess();
            }
        };
    }


    /**
     * 返回扫码成功后的结果
     */
    private void performScanSuccess() {
        String barcode = mStringBufferResult.toString();
        OnScanSuccessListener listener=  mOnScanSuccessListener.get();
        if (listener != null && mStringBufferResult.length() !=0)
            listener.onScanSuccess(barcode);
        mStringBufferResult.setLength(0);
    }


    /**
     * 扫码枪事件解析
     * @param event
     */
    public void analysisKeyEvent(KeyEvent event) {

        int keyCode = event.getKeyCode();

        //字母大小写判断
        checkLetterStatus(event);

        if (event.getAction() == KeyEvent.ACTION_DOWN) {

            char aChar = getInputCode(event);;

            if (aChar != 0) {
                mStringBufferResult.append(aChar);
            }

            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                //若为回车键，直接返回
               // mHandler.removeCallbacks(mScanningFishedRunnable);
                mHandler.removeCallbacksAndMessages(null);
                mHandler.post(mScanningFishedRunnable);
            } else {
                //延迟post，若500ms内，有其他事件
                mHandler.removeCallbacks(mScanningFishedRunnable);
                mHandler.postDelayed(mScanningFishedRunnable, MESSAGE_DELAY);
            }

        }
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


    //获取扫描内容
    private char getInputCode(KeyEvent event) {

        int keyCode = event.getKeyCode();

        char aChar;

        if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
            //字母
            aChar = (char) ((mCaps ? 'A' : 'a') + keyCode - KeyEvent.KEYCODE_A);
        } else if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            //数字
            aChar = (char) ('0' + keyCode - KeyEvent.KEYCODE_0);
        } else {
            //其他符号
            switch (keyCode) {
                case KeyEvent.KEYCODE_PERIOD:
                    aChar = '.';
                    break;
                case KeyEvent.KEYCODE_MINUS:
                    aChar = mCaps ? '_' : '-';
                    break;
                case KeyEvent.KEYCODE_SLASH:
                    aChar = '/';
                    break;
                case KeyEvent.KEYCODE_BACKSLASH:
                    aChar = mCaps ? '|' : '\\';
                    break;
                default:
                    aChar = 0;
                    break;
            }
        }

        return aChar;

    }




    public interface OnScanSuccessListener {
        void onScanSuccess(String barcode);
    }


    public void onDestroy() {
        workThread.quit(); // 退出消息循环.quit(); // 退出消息循环
        //mHandler.removeCallbacks(mScanningFishedRunnable);
        mHandler.removeCallbacksAndMessages(null);
        mOnScanSuccessListener = null;
    }


    //部分手机如三星，无法使用该方法
//    private void hasScanGun() {
//        Configuration cfg = getResources().getConfiguration();
//        return cfg.keyboard != Configuration.KEYBOARD_NOKEYS;
//    }

    /**
     * 扫描枪是否连接
     * @return

    public boolean hasScanGun() {


        return true;

    }
  */

    /**
     * 输入设备是否存在
     * @param deviceName
     * @return

    private boolean isInputDeviceExist(String deviceName) {
        int[] deviceIds = InputDevice.getDeviceIds();

        for (int id : deviceIds) {
            if (InputDevice.getDevice(id).getName().equals(deviceName)) {
                return true;
            }
        }
        return false;
    }
 */

    /**
     * 是否为扫码枪事件(部分机型KeyEvent获取的名字错误)
     * @param event
     * @return

    @Deprecated
    public boolean isScanGunEvent(KeyEvent event) {
        Log.d("ca1",""+event.getDevice().getName());
        return true;
       // return event.getDevice().getName().equals(mDeviceName);
    }
  */


}





