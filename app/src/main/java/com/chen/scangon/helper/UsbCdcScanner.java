package com.chen.scangon.helper;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class UsbCdcScanner {
    private static final String TAG = "UsbCdcScanner";
    private static final String ACTION_USB_PERMISSION =
            "com.example.scan_module_add_key_demo.USB_CDC_PERMISSION";

    private static final int TARGET_VENDOR_ID = 0x152A;
    private static final int TARGET_PRODUCT_ID = 0x880F;

    private static final int CDC_SET_LINE_CODING = 0x20;
    private static final int CDC_SET_CONTROL_LINE_STATE = 0x22;
    private static final int USB_RECIP_INTERFACE = 0x01;
    private static final int CDC_CONTROL_REQUEST_TYPE =
            UsbConstants.USB_TYPE_CLASS | USB_RECIP_INTERFACE | UsbConstants.USB_DIR_OUT;
    private static final int CDC_CONTROL_LINE_DTR_RTS = 0x03;
    private static final int BULK_READ_TIMEOUT_MS = 200;

    private final Object lock = new Object();

    private WeakReference<ScanGunKeyEventHelper.OnScanSuccessListener> listenerRef;
    private Context context;
    private UsbManager usbManager;
    private PendingIntent permissionIntent;
    private boolean receiverRegistered;
    private volatile boolean stop;

    private UsbDevice usbDevice;
    private UsbInterface controlInterface;
    private UsbInterface dataInterface;
    private UsbEndpoint bulkInEndpoint;
    private UsbEndpoint bulkOutEndpoint;
    private UsbDeviceConnection connection;
    private ReadThread readThread;

    public void onCreate(ScanGunKeyEventHelper.OnScanSuccessListener onScanSuccessListener) {
        listenerRef = new WeakReference<ScanGunKeyEventHelper.OnScanSuccessListener>(onScanSuccessListener);
        stop = false;

        if (!(onScanSuccessListener instanceof Context)) {
            Log.e(TAG, "listener is not a Context, cannot access UsbManager");
            return;
        }

        context = ((Context) onScanSuccessListener).getApplicationContext();
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (usbManager == null) {
            Log.e(TAG, "UsbManager is null");
            return;
        }

        registerReceiverIfNeeded();
        enumerateAndOpen();
    }

    public void onDestroy() {
        stop = true;
        closeDevice();
        unregisterReceiverIfNeeded();
        listenerRef = null;
        context = null;
        usbManager = null;
    }

    private void registerReceiverIfNeeded() {
        if (receiverRegistered || context == null) {
            return;
        }

        Intent permissionAction = new Intent(ACTION_USB_PERMISSION);
        permissionAction.setPackage(context.getPackageName());
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_MUTABLE;
        }
        permissionIntent = PendingIntent.getBroadcast(
                context,
                0,
                permissionAction,
                flags);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(usbReceiver, filter);
        }
        receiverRegistered = true;
    }

    private void unregisterReceiverIfNeeded() {
        if (!receiverRegistered || context == null) {
            return;
        }

        try {
            context.unregisterReceiver(usbReceiver);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "usb receiver already unregistered", e);
        }
        receiverRegistered = false;
    }

    private void enumerateAndOpen() {
        if (usbManager == null) {
            return;
        }

        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        if (deviceList == null || deviceList.isEmpty()) {
            Log.i(TAG, "no usb devices found");
            return;
        }

        for (UsbDevice device : deviceList.values()) {
            Log.i(TAG, "usb device name=" + device.getDeviceName()
                    + ", vid=0x" + Integer.toHexString(device.getVendorId())
                    + ", pid=0x" + Integer.toHexString(device.getProductId())
                    + ", class=" + device.getDeviceClass()
                    + ", interfaceCount=" + device.getInterfaceCount());

            if (isTargetDevice(device)) {
                if (usbManager.hasPermission(device)) {
                    openDevice(device);
                } else {
                    Log.i(TAG, "request usb permission for target device");
                    usbManager.requestPermission(device, permissionIntent);
                }
                return;
            }
        }

        Log.i(TAG, "target usb cdc device not found");
    }

    private boolean isTargetDevice(UsbDevice device) {
        return device != null
                && device.getVendorId() == TARGET_VENDOR_ID
                && device.getProductId() == TARGET_PRODUCT_ID;
    }

    private void openDevice(UsbDevice device) {
        synchronized (lock) {
            closeDeviceLocked();

            usbDevice = device;
            if (!findInterfacesAndEndpoints(device)) {
                Log.e(TAG, "cannot find cdc data interface or bulk in endpoint");
                clearUsbStateLocked();
                return;
            }

            UsbDeviceConnection newConnection = usbManager.openDevice(device);
            if (newConnection == null) {
                Log.e(TAG, "openDevice failed");
                clearUsbStateLocked();
                return;
            }

            if (controlInterface != null && !newConnection.claimInterface(controlInterface, true)) {
                Log.e(TAG, "claim control interface failed");
                newConnection.close();
                clearUsbStateLocked();
                return;
            }

            if (!newConnection.claimInterface(dataInterface, true)) {
                Log.e(TAG, "claim data interface failed");
                if (controlInterface != null) {
                    newConnection.releaseInterface(controlInterface);
                }
                newConnection.close();
                clearUsbStateLocked();
                return;
            }

            connection = newConnection;
            configureCdcAcmLocked();
            startReadThreadLocked();
            Log.i(TAG, "usb cdc device opened");
        }
    }

    private boolean findInterfacesAndEndpoints(UsbDevice device) {
        UsbInterface firstBulkDataInterface = null;
        UsbEndpoint firstBulkIn = null;
        UsbEndpoint firstBulkOut = null;

        for (int i = 0; i < device.getInterfaceCount(); i++) {
            UsbInterface usbInterface = device.getInterface(i);
            Log.i(TAG, "interface index=" + i
                    + ", id=" + usbInterface.getId()
                    + ", class=" + usbInterface.getInterfaceClass()
                    + ", subclass=" + usbInterface.getInterfaceSubclass()
                    + ", protocol=" + usbInterface.getInterfaceProtocol()
                    + ", endpointCount=" + usbInterface.getEndpointCount());

            if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_COMM
                    && controlInterface == null) {
                controlInterface = usbInterface;
            }

            UsbEndpoint inEndpoint = null;
            UsbEndpoint outEndpoint = null;
            for (int j = 0; j < usbInterface.getEndpointCount(); j++) {
                UsbEndpoint endpoint = usbInterface.getEndpoint(j);
                Log.i(TAG, "endpoint index=" + j
                        + ", address=0x" + Integer.toHexString(endpoint.getAddress())
                        + ", type=" + endpoint.getType()
                        + ", direction=" + endpoint.getDirection()
                        + ", maxPacketSize=" + endpoint.getMaxPacketSize());

                if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                    if (endpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                        inEndpoint = endpoint;
                    } else if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                        outEndpoint = endpoint;
                    }
                }
            }

            if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_CDC_DATA
                    && inEndpoint != null) {
                dataInterface = usbInterface;
                bulkInEndpoint = inEndpoint;
                bulkOutEndpoint = outEndpoint;
                return true;
            }

            if (firstBulkDataInterface == null && inEndpoint != null) {
                firstBulkDataInterface = usbInterface;
                firstBulkIn = inEndpoint;
                firstBulkOut = outEndpoint;
            }
        }

        if (firstBulkDataInterface != null) {
            dataInterface = firstBulkDataInterface;
            bulkInEndpoint = firstBulkIn;
            bulkOutEndpoint = firstBulkOut;
            Log.w(TAG, "use first bulk interface as cdc data fallback");
            return true;
        }

        return false;
    }

    private void configureCdcAcmLocked() {
        if (connection == null || controlInterface == null) {
            Log.w(TAG, "skip cdc control setup, control interface missing");
            return;
        }

        byte[] lineCoding = new byte[] {
                0x00, (byte) 0xC2, 0x01, 0x00,
                0x00,
                0x00,
                0x08
        };
        int interfaceId = controlInterface.getId();
        int lineResult = connection.controlTransfer(
                CDC_CONTROL_REQUEST_TYPE,
                CDC_SET_LINE_CODING,
                0,
                interfaceId,
                lineCoding,
                lineCoding.length,
                1000);
        Log.i(TAG, "SET_LINE_CODING result=" + lineResult);

        int stateResult = connection.controlTransfer(
                CDC_CONTROL_REQUEST_TYPE,
                CDC_SET_CONTROL_LINE_STATE,
                CDC_CONTROL_LINE_DTR_RTS,
                interfaceId,
                null,
                0,
                1000);
        Log.i(TAG, "SET_CONTROL_LINE_STATE result=" + stateResult);
    }

    private void startReadThreadLocked() {
        if (readThread != null) {
            readThread.requestStop();
            readThread.interrupt();
        }
        readThread = new ReadThread();
        readThread.start();
    }

    private void closeDevice() {
        synchronized (lock) {
            closeDeviceLocked();
        }
    }

    private void closeDeviceLocked() {
        stop = true;

        if (readThread != null) {
            readThread.requestStop();
            readThread.interrupt();
            readThread = null;
        }

        if (connection != null) {
            try {
                if (dataInterface != null) {
                    connection.releaseInterface(dataInterface);
                }
                if (controlInterface != null) {
                    connection.releaseInterface(controlInterface);
                }
            } catch (Exception e) {
                Log.w(TAG, "release usb interface failed", e);
            }
            connection.close();
        }

        clearUsbStateLocked();
    }

    private void clearUsbStateLocked() {
        connection = null;
        usbDevice = null;
        controlInterface = null;
        dataInterface = null;
        bulkInEndpoint = null;
        bulkOutEndpoint = null;
    }

    private void onDataReceived(byte[] buffer, int size) {
        WeakReference<ScanGunKeyEventHelper.OnScanSuccessListener> ref = listenerRef;
        if (ref == null || size <= 0) {
            return;
        }

        ScanGunKeyEventHelper.OnScanSuccessListener listener = ref.get();
        if (listener == null) {
            return;
        }

        try {
            listener.onScanSuccess("acm:" + new String(buffer, 0, size, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UTF-8 unsupported", e);
        }
    }

    private class ReadThread extends Thread {
        private volatile boolean threadStop;

        void requestStop() {
            threadStop = true;
        }

        @Override
        public void run() {
            stop = false;
            while (!stop && !threadStop) {
                UsbDeviceConnection currentConnection;
                UsbEndpoint currentEndpoint;
                synchronized (lock) {
                    currentConnection = connection;
                    currentEndpoint = bulkInEndpoint;
                }

                if (currentConnection == null || currentEndpoint == null) {
                    return;
                }

                int bufferSize = Math.max(currentEndpoint.getMaxPacketSize(), 64);
                byte[] buffer = new byte[bufferSize];
                int size = currentConnection.bulkTransfer(
                        currentEndpoint,
                        buffer,
                        buffer.length,
                        BULK_READ_TIMEOUT_MS);
                if (size > 0) {
                    Log.i(TAG, "bulk read size=" + size);
                    onDataReceived(buffer, size);
                } else if (size < 0) {
                    Log.w(TAG, "bulk read failed, size=" + size);
                }
            }
        }
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context receiverContext, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                Log.i(TAG, "usb permission result granted=" + granted);
                if (granted && isTargetDevice(device)) {
                    stop = false;
                    openDevice(device);
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (isTargetDevice(device)) {
                    Log.i(TAG, "target usb cdc device detached");
                    closeDevice();
                }
            }
        }
    };
}
