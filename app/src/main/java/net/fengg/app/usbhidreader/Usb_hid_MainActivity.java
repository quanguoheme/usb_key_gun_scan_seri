package net.fengg.app.usbhidreader;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.scan_module_add_key_demo.R;


public class Usb_hid_MainActivity extends Activity {
    String TAG = "MainActivity";
    USBHIDReader usb;
    TextView txt_log;
    StringBuilder sb = new StringBuilder();
     Button open;
    Button btn_close;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main33);
        txt_log = (TextView) findViewById(R.id.txt_log);

        open = (Button)findViewById(R.id.btn_open);
        open.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                open_DD(null);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                      //  open_DD(null);
                    }
                }).start();
            }
        });

        btn_close = (Button)findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                close_DD(null);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                      //  close_DD(null);
                    }
                }).start();
            }
        });
    }

    protected void open_DD(View view) {
        usb = new USBHIDReader(this);
       boolean is_ok= usb.open();
        if(!is_ok)
        {
            Log.e("aa1","usb.open(); error");
            return;
        }
        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                sb.append("\nstart read...");
                txt_log.setText(sb.toString());
            }

            @Override
            protected String doInBackground(Void... voids) {
                String result = read(50000);
                return result;
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                sb.append("\nget result:");
                sb.append(result);
                txt_log.setText(sb.toString());
            }
        }.execute();


    }

    protected void close_DD(View view) {
        if(null != usb) {
            usb.setiRlistener(null);
            usb.stopRead();
            usb.close();
            sb.append("\nclose");
            txt_log.setText(sb.toString());
        }
    }

    private String read(int timeout) {
        final String[] code = {null};
        usb.setiRlistener(new USBHIDReader.IReceiveDataListener() {

            @Override
            public void onReceiveData(String data) {
                code[0] = data;
            }
        });
        usb.setUsbThreadDataReceiver();
        byte[] bytes = {27,49};
        //send data
        int res = usb.sendData(new String(bytes), false);
        if(res != -1) {
            final int SLEEPTIME = 100;
            int i = timeout / SLEEPTIME;
            while (null == code[0] || "".equals(code[0].trim())) {
                if (timeout >= 0 && i <= 0) {
                    //send data close
                    res = usb.sendData("0x1b 0x30", true);
                    if (-1 == res) {
                        Log.i(TAG, "send fail");
                    }
                    code[0] = "timeout";
                    break;
                }
                i--;
                try {
                    Thread.sleep(SLEEPTIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            usb.stopRead();
        }
        return code[0];
    }
}
