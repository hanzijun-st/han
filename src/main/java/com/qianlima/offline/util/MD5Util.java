package com.qianlima.offline.util;


import lombok.extern.slf4j.Slf4j;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class MD5Util {

    /**利用MD5进行加密*/
    public static String encoderByMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException{
        //确定计算方法
        MessageDigest md5=MessageDigest.getInstance("MD5");
        BASE64Encoder base64en = new BASE64Encoder();
        //加密后的字符串
        String newstr=base64en.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }

    /**判断用户密码是否正确
     *newpasswd 用户输入的密码
     *oldpasswd 正确密码*/
    public static boolean checkpassword(String newpasswd,String oldpasswd) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        if(encoderByMd5(newpasswd).equals(oldpasswd)) {
            return true;
        }else {
            return false;
        }
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, UnsupportedEncodingException{
        String s = encoderByMd5("123456");

        boolean checkpassword = checkpassword("123456", s);
        System.out.println(s);
        System.out.println(checkpassword);
    }
}