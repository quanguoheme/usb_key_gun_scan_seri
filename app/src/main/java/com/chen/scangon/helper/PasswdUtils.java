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


     //return 返回Base64转码后的加密数据
    public static String encrypt_AES(String data, String secretKey)  {
       return AESEncrypt.encrypt(data,secretKey);
    }

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


    public  static  final  String g_Salt="h6v30LD6UxVE6h8U";
    public static String calc_md5_salt(String src)
    {
        String salt="h6v30LD6UxVE6h8U";
        String ret_str=getMd5(src+salt);
        Log.d(TAG,"calc_md5_salt  : length is  ,"+ret_str.length()+"  , ret_str:"+ret_str+",src:"+src);
        // 如果生成数字未满32位，需要前面补0
        for (int i = 0; i < 32 - ret_str.length(); i++) {
            ret_str += "0";
        }
        if(ret_str.length() != 32)
        {
            Log.e(TAG,"calc_md5_salt error: length is error,"+ret_str.length()+"  , ret_str:"+ret_str+",src:"+src);
        }

        return  ret_str;
    }
    public static String tran_byte_2_string(byte [] buff)
    {
        if(buff ==null || buff.length ==0)
        {
            return "";
        }
        byte[] b = buff;
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
        Log.d(TAG," tran_byte_2_string  out:"+buf.toString());
        return buf.toString();
    }
    //产生有16个字符的随机数
    public static String gen_rand_16_string()
    {

        Random r = new Random();
        StringBuilder sb = new StringBuilder(16);
        sb.append(r.nextInt(99999999)).append(r.nextInt(99999999));
        int len = sb.length();
        if (len < 16) {
            for (int i = 0; i < 16 - len; i++) {
                sb.append("0");
            }
        }

        if (len != 16) {
             Log.e(TAG,"md5sum error");
        }

        return sb.toString();
    }


    /**RSA算法*/
    public static final String RSA = "RSA";
    /**加密方式，android的*/
//  public static final String TRANSFORMATION = "RSA/None/NoPadding";
    /**加密方式，标准jdk的*/
    public static final String TRANSFORMATION = "RSA/None/PKCS1Padding";

    /** 使用公钥加密 */
    public static byte[] encryptByPublicKey(byte[] data, byte[] publicKey) throws Exception {
        // 得到公钥对象
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);
        // 加密数据
        Cipher cp = Cipher.getInstance(TRANSFORMATION);
        cp.init(Cipher.ENCRYPT_MODE, pubKey);
        return cp.doFinal(data);
    }
    public static final String g_public_key_dx="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDP5ahVBYJ7HFF8lMi/zeOshKHKAdK985eovk1EgKkhETbJu+ADgnWGxwIhI5uSNi94KplgTZN26AES0wAQhDGI9T/YmFPLTy9HCHNtbr8DyxstEgP4pcO8OqnA+1x9EoDRgwe7dGtf0YZNlLkRADjskv7UulbE4ODBHiBmMU1ejwIDAQAB";
    public static byte[] RSA_encryptByPublicKey(byte[] data  ) throws Exception {
        byte[] publicKey="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDP5ahVBYJ7HFF8lMi/zeOshKHKAdK985eovk1EgKkhETbJu+ADgnWGxwIhI5uSNi94KplgTZN26AES0wAQhDGI9T/YmFPLTy9HCHNtbr8DyxstEgP4pcO8OqnA+1x9EoDRgwe7dGtf0YZNlLkRADjskv7UulbE4ODBHiBmMU1ejwIDAQAB".getBytes();
        Log.d(TAG,"RSA encryptByPublicKey : key_len:"+publicKey.length);
        // 得到公钥对象
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pubKey = keyFactory.generatePublic(keySpec);

        // 加密数据
        Cipher cp = Cipher.getInstance(TRANSFORMATION);
        cp.init(Cipher.ENCRYPT_MODE, pubKey);
        return cp.doFinal(data);
    }

    /** 使用私钥解密
    public static byte[] decryptByPrivateKey(byte[] encrypted, byte[] privateKey) throws Exception {
        // 得到私钥对象
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);
        KeyFactory kf = KeyFactory.getInstance(RSA);
        PrivateKey keyPrivate = kf.generatePrivate(keySpec);
        // 解密数据
        Cipher cp = Cipher.getInstance(TRANSFORMATION);
        cp.init(Cipher.DECRYPT_MODE, keyPrivate);
        byte[] arr = cp.doFinal(encrypted);
        return arr;
    }
 */



    public static String getFileMD5(File file) {
        final   String TAG = "aa3";
        if (!file.isFile()) {
            Log.e("aaa", "it(!file.isFile())  getFileMD5--> "+file.getPath());
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        //BigInteger bigInt = new BigInteger(1, digest.digest());

        byte[] b = digest.digest();
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

        //  return bigInt.toString(16);
    }
}
