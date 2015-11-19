/**   
* Filename: Util.java 
*  
* Description: //TODO
*
* Created Date: Nov 18, 2015 4:25:12 PM 
* 
* Author : <Nannan YU>  
*  
* Copyright (C)    MicroStrategy Incorporated 2015
* All Rights Reserved 
*/


package com.spring.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

  //method thread safe
  private static final Object lock = new Object();
  
  public static void threadSafeWithLockObject(){
    synchronized(lock){
      //init some object;
    }
  }
  
  public static void threadSafeWithClass(){
    synchronized(Util.class){
      //lock this class, do something.
    }
  }
  
  // http://www.runoob.com/java/java-regular-expressions.html
  public static void playWithRegEx() {
    String pattern = "^/image/user.*"
            + "|^/image/file.*"
            + "|^/user/image.*"
            + "|^/badgephotos/org.*"
            + "|^/badges/org.*"
            + "|^/orguser/image.*"
            + "|^/user/get_public_image.*"
            + "|^/user/get_badge_photo.*"
            + "|^/user/get_public_image.*";
    
    String[] strings = {"/image/user/1234/type",
                        "/image/file",
                        "/image/fil",
                        "/user/get_public_image",
                        "testNN/user/get_public_image",
                        "/user/get_badge_phot",
                        "/user/"
                        };
    
    Pattern pr = Pattern.compile(pattern);
    
    for (String s: strings){
      Matcher m = pr.matcher(s);
      if (m.find()){
        System.out.println(s+ ": Matched! ^_^");
      }else {
        System.out.println(s+": Not matched! T_T");
      }
    }
  }

  public static String readerToString(Reader reader) {
    return readerToString(new BufferedReader(reader));
  }

  public static String streamToString(InputStream stream) {
        return readerToString(new InputStreamReader(stream));
  }
  
  public static String readerToString(BufferedReader reader) {
    String line = null;
    StringBuilder sb = new StringBuilder();
    try {
      while ((line = reader.readLine()) != null) {
        sb.append(line + "\n");
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return sb.toString();
  }
  
  public String hashAlg(String source, String alg /*"SHA-1", "MD5"*/) {
    

        String sha1 = "";
        try
        {
            MessageDigest crypt = MessageDigest.getInstance(alg);
            crypt.reset();
            crypt.update(source.getBytes("UTF-8"));
            sha1 = byteToHex(crypt.digest());
        }
        catch(NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch(UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return sha1;
  }
  
  private static String byteToHex(final byte[] hash)
  {
      Formatter formatter = new Formatter();
      for (byte b : hash)
      {
          formatter.format("%02x", b);
      }
      String result = formatter.toString();
      formatter.close();
      return result;
  }
}