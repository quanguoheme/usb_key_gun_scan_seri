package com.example.scan_module_add_key_demo;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;

public class Scanner_Tools {

    private static SerialPortUtils baseConfig;

    private Scanner_Tools() {

    }

    public static SerialPortUtils getUtil() {
        if (baseConfig == null) {
            baseConfig = new SerialPortUtils();
        }
        return baseConfig;
    }
}
