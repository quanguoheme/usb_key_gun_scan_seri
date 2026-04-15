package com.chen.scangon.helper;

public class Scanner_Tools {

    private static UsbCdcScanner baseConfig;

    private Scanner_Tools() {

    }

    public static UsbCdcScanner getUtil() {
        if (baseConfig == null) {
            baseConfig = new UsbCdcScanner();
        }
        return baseConfig;
    }
}
