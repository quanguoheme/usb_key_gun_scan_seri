package com.chen.scangon.helper;

public class Scanner_Tools {

    private static SderbUtils baseConfig;

    private Scanner_Tools() {

    }

    public static SderbUtils getUtil() {
        if (baseConfig == null) {
            baseConfig = new SderbUtils();
        }
        return baseConfig;
    }
}
