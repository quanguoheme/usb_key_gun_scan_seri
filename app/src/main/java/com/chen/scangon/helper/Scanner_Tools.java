package com.chen.scangon.helper;

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
