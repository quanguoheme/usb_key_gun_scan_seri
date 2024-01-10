package com.chen.scangon.helper;

import android.os.Build;
import android.util.Log;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;

import javax.crypto.Cipher;

public class PasswdUtils {
    final static String TAG="pass";


    public static String getMd5(String plainText) {
        //Log.d(TAG,"getMd5 in:"+plainText);
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                md.update(plainText.getBytes(StandardCharsets.UTF_8));
            }
            else
            {
                Log.e(TAG," if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {");
            }

            byte[] b = md.digest();
            int i;
            StringBuilder buf = new StringBuilder();
            for (byte b1 : b) {
                i = b1;
                if (i < 0){
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            Log.d(TAG,"getMd5 out:"+buf.toString());
            return buf.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return " ";
        }
    }


}
