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

public class Util {

  public Util() {
    // TODO Auto-generated constructor stub
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