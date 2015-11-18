/**   
* Filename: HttpClientWrapper.java 
*  
* Description: //TODO
*
* Created Date: Nov 18, 2015 4:48:47 PM 
* 
* Author : <Nannan YU>  
*  
* Copyright (C)    MicroStrategy Incorporated 2015
* All Rights Reserved 
*/


package com.spring.util;

import java.io.FileInputStream;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.util.EntityUtils;


import javax.net.ssl.*;


public class HttpClientWrapper {


    private static Integer connTimeout = -1;
    private static Integer soTimeout = -1;

    public static void setConfig(String name, String value){
        if (name==null || value==null){
            System.out.println("Failed to set http client config: null pointer parameters.");
            return;
        }
        Integer v = 0;
        try{
            v = Integer.parseInt(value);
        }catch (Exception e){
          System.out.println("Failed to set http client config: " + e.getMessage());
            return;
        }
        if (name.compareToIgnoreCase("connection_timeout") == 0){
            connTimeout = v;
        }else if (name.compareToIgnoreCase("socket_timeout") == 0){
            soTimeout = v;
        }else{
          System.out.println("Unknown http client config: " + name);
            return;
        }
        System.out.println("Set http client config: " + name + "(" + value + ")");
    }


    private static RequestConfig createRequestConfig(Integer nTimeout){

        RequestConfig.Builder builder= RequestConfig.custom();
        if (connTimeout>=0) {
            builder.setConnectTimeout(connTimeout);
        }
        int lTimeout = 0;
        if (nTimeout != null)
            lTimeout = nTimeout.intValue();

        lTimeout = (soTimeout>lTimeout)?soTimeout:lTimeout;
        if (lTimeout>=0){
            builder.setSocketTimeout(lTimeout);
        }
        RequestConfig requestConfig= builder .build();
        return requestConfig;
    }

    private static KeyStore createTrustKeyStore(String trustStoreFile, String trustStorePass) {
        if (trustStoreFile == null)
            return null;
        String keyStoreType = "jks";

        try {
            KeyStore trustStore = KeyStore.getInstance(keyStoreType);

            FileInputStream inStream = new FileInputStream(trustStoreFile);
            if (trustStorePass == null) {
                trustStore.load(inStream, null);
            } else {
                trustStore.load(inStream, trustStorePass.toCharArray());
            }
            inStream.close();
            return trustStore;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static KeyStore createClientKeyStore(String keyStoreFile, String keyStorePass) {
        if (keyStoreFile == null)
            return null;

        try {
            KeyStore keyStore = KeyStore.getInstance("pkcs12");

            FileInputStream instream = new FileInputStream(keyStoreFile);
            //Logger.info("+ client key store password: " + keyStorePass);
            keyStore.load(instream, keyStorePass.toCharArray());
            instream.close();
            return keyStore;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static SSLConnectionSocketFactory installKeyStores(String keyStoreFile, String keyStorePass, String trustStoreFile, String trustStorePass, HostnameVerifier hostNameVerifier)
    {
        try {

            KeyStore clientStore = createClientKeyStore(keyStoreFile, keyStorePass);
            KeyManager[] keymanagers = null;
            if (null != clientStore) {
                KeyManagerFactory kmfactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                kmfactory.init(clientStore, keyStorePass != null ? keyStorePass.toCharArray() : null);
                keymanagers = kmfactory.getKeyManagers();
            }

            KeyStore trustStore = createTrustKeyStore(trustStoreFile, trustStorePass);
            TrustManager[] trustmanagers = null;
            if (null != trustStore) {
                TrustManagerFactory tmfactory=TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmfactory.init(trustStore);
                trustmanagers = tmfactory.getTrustManagers();
            }

            SSLContext sslcontext=SSLContext.getInstance("TLSv1.2");
            sslcontext.init(keymanagers,trustmanagers,null);

            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
                    sslcontext,
                    hostNameVerifier);

            return socketFactory;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
      
    }

    public static String getUrl(String url, String keyStoreFile, String keyStorePass, String trustStoreFile, String trustStorePass,Integer nTimeout)  throws Exception {
        return getUrl(url, keyStoreFile, keyStorePass, trustStoreFile, trustStorePass, null,nTimeout);
    }

    public static String getUrl(String url, String keyStoreFile, String keyStorePass, String trustStoreFile, String trustStorePass)  throws Exception {
        return getUrl(url, keyStoreFile, keyStorePass, trustStoreFile, trustStorePass, null,null);
    }

    private static void installHeaders(AbstractHttpMessage req, Map<String, String> headers) {
        if (headers == null)
            return;
        for (String key : headers.keySet()) {
            String val = headers.get(key);
            req.addHeader(key, val);
        }
    }


    public static String getUrl(String url, String keyStoreFile, String keyStorePass, String trustStoreFile, String trustStorePass, Map<String, String> headers) throws Exception  {
        return getUrl(url,keyStoreFile,keyStorePass,trustStoreFile,trustStorePass,headers,null);

    }
    public static String getUrl(String url, String keyStoreFile, String keyStorePass, String trustStoreFile, String trustStorePass, Map<String, String> headers,Integer nTimeout) throws Exception {
        CloseableHttpClient client= null;     
        String body = null;
        try {
          RequestConfig requestConfig = createRequestConfig(nTimeout);
          SSLConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSystemSocketFactory();
          
          if (url.startsWith("https")) {
              sslsf = installKeyStores( keyStoreFile, keyStorePass, trustStoreFile, trustStorePass, null);
          }

          client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).setSSLSocketFactory(sslsf).build();

            HttpGet getReq = new HttpGet(url);
            if (getReq == null)
                return body;

            getReq.setConfig(requestConfig);
            installHeaders(getReq, headers);


            HttpResponse resp = client.execute(getReq);
            HttpEntity entity = resp.getEntity();
            body = Util.streamToString(entity.getContent());
            EntityUtils.consume(entity);
            //HttpHeaders headers = resp.getHeaders("X-RateLimit-Remaining");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
              if (null != client)
                client.close();
            }catch(Exception e){
              throw new RuntimeException(e);
            }
        }
        return body;
    }


    public static String postUrl(String url, String keyStoreFile, String keyStorePass, String trustStoreFile, String trustStorePass, Map<String, String> headers, Map<String, String> paramMap, HostnameVerifier hostNameVerifier)  throws Exception {
        return postUrl(url,keyStoreFile,keyStorePass,trustStoreFile,trustStorePass,headers,paramMap,hostNameVerifier,null);
    }
    public static String postUrl(String url, String keyStoreFile, String keyStorePass, String trustStoreFile, String trustStorePass, Map<String, String> headers, Map<String, String> paramMap, HostnameVerifier hostNameVerifier,Integer nTimeout)  throws Exception {
        CloseableHttpClient client =  null;
        
        String body = null;
        try {
          RequestConfig requestConfig = createRequestConfig(nTimeout);
          SSLConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSystemSocketFactory();
          if (url.startsWith("https")) {
              sslsf = installKeyStores( keyStoreFile, keyStorePass, trustStoreFile, trustStorePass, hostNameVerifier);
          }

          client= HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).setSSLSocketFactory(sslsf).build();

            HttpPost postReq = new HttpPost(url);
            if (postReq == null)
                return body;

            postReq.setConfig(requestConfig);
            if (paramMap != null) {
                HttpEntity reqEntity = new ByteArrayEntity(map2qstr(paramMap, true, false).getBytes());
                postReq.setEntity(reqEntity);
                postReq.setHeader("Content-Type", "application/x-www-form-urlencoded");
            }

            installHeaders(postReq, headers);


            HttpResponse resp = client.execute(postReq);
            HttpEntity entity = resp.getEntity();
            body = Util.streamToString(entity.getContent());
            EntityUtils.consume(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
          try {
            if (null != client)
              client.close();
          }catch(Exception e){
            throw new RuntimeException(e);
          }
        }
        return body;
    }

    //nyu@2015-10-09: Just add this function to pass the json_body into the POST api.
    public static String postUrl_jsonBody(String url, String keyStoreFile, String keyStorePass,
                                          String trustStoreFile, String trustStorePass, Map<String, String> headers,
                                          String jsonBody, HostnameVerifier hostNameVerifier,Integer nTimeout)  throws Exception {
        CloseableHttpClient client=null;
        
        String body = null;
        try {
          RequestConfig requestConfig = createRequestConfig(nTimeout);
          SSLConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSystemSocketFactory();
          if (url.startsWith("https")) {
              sslsf = installKeyStores( keyStoreFile, keyStorePass, trustStoreFile, trustStorePass, hostNameVerifier);
          }

          
          client =HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).setSSLSocketFactory(sslsf).build();

            HttpPost postReq = new HttpPost(url);
            if (postReq == null)
                return body;
            postReq.setConfig(requestConfig);
            if (jsonBody != null) {
                HttpEntity reqEntity = new ByteArrayEntity(jsonBody.getBytes());
                postReq.setEntity(reqEntity);
                postReq.setHeader("Content-Type", "application/json");
            }
            installHeaders(postReq, headers);


            HttpResponse resp = client.execute(postReq);
            HttpEntity entity = resp.getEntity();
            body = Util.streamToString(entity.getContent());
            EntityUtils.consume(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
              if (null != client)
                client.close();
            }catch(Exception e){
              throw new RuntimeException(e);
            }
        }
        return body;
    }

    public static String putUrl(String url, String keyStoreFile, String keyStorePass, String trustStoreFile,String trustStorePass, Map<String, String> headers, Map<String, String> paramMap)  throws Exception {
        return putUrl(url,keyStoreFile,keyStorePass,trustStoreFile,trustStorePass,headers,paramMap,null);
    }

    public static String putUrl(String url, String keyStoreFile, String keyStorePass, String trustStoreFile,String trustStorePass, Map<String, String> headers, Map<String, String> paramMap,Integer nTimeout) throws Exception {
        HttpEntity reqEntity = null;
        if (paramMap != null) {
            reqEntity = new ByteArrayEntity(map2qstr(paramMap, true, false).getBytes());
            // kewang 2014-04-15
            // For TQMS 877951. the reasons are listed bellow in the other putUrl method.
            // move the content-type to headers argument.
            headers.put("Content-Type", "application/x-www-form-urlencoded");
        }
        return putUrl(url, keyStoreFile, keyStorePass, trustStoreFile,trustStorePass, headers, reqEntity, null,nTimeout);
    }

    //nyu@2015-09-29: Add a new function to deal with the json body in the PUT-API.
    public static String putUrl_jsonBody(String url, String keyStoreFile, String keyStorePass, String trustStoreFile,
                                         String trustStorePass, Map<String, String> headers, String body,Integer nTimeout) throws Exception {

        HttpEntity reqEntity = null;
        if (body != null) {
            reqEntity = new ByteArrayEntity(body.getBytes());
            headers.put("Content-Type", "application/json");
        }
        return putUrl(url, keyStoreFile, keyStorePass, trustStoreFile,trustStorePass, headers, reqEntity, null,nTimeout);
   }

    public static String putUrl(String url, String keyStoreFile, String keyStorePass, String trustStoreFile,String trustStorePass, Map<String, String> headers, HttpEntity putEntity, HostnameVerifier hostNameVerifier)  throws Exception {
        return putUrl(url,keyStoreFile,keyStorePass,trustStoreFile,trustStorePass,headers,putEntity,hostNameVerifier,null);

    }

    public static String putUrl(String url, String keyStoreFile, String keyStorePass, String trustStoreFile,String trustStorePass, Map<String, String> headers, HttpEntity putEntity, HostnameVerifier hostNameVerifier,Integer nTimeout)  throws Exception {
        CloseableHttpClient client= null;
    
        String body = null;
        try {
          RequestConfig requestConfig = createRequestConfig(nTimeout);
          SSLConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSystemSocketFactory();
          if (url.startsWith("https")) {
              sslsf = installKeyStores( keyStoreFile, keyStorePass, trustStoreFile, trustStorePass, hostNameVerifier);
          }

          client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).setSSLSocketFactory(sslsf).build();

            HttpPut putReq = new HttpPut(url);
           if (putReq == null)
                return body;
            putReq.setConfig(requestConfig);
            if (putEntity != null) {
                // kewang 2014-04-15
                // Somebody added this line to force the content-type to "application/x-www-form-urlencoded"
                // and this will cause an issue TQMS 877951 for upload image feature.
                // For most kinds of requests, this content-type is fine; but it will cause problems
                // if we the entity is MultipartEntity, the content-type should be "multipart/form-data"
                // by default HttpClient will set this content-type for us.
                // If one really want to set content-type, please set in "headers" argument.
                //putReq.setHeader("Content-Type", "application/x-www-form-urlencoded");
                putReq.setEntity(putEntity);
            }

            installHeaders(putReq, headers);

            HttpResponse resp = client.execute(putReq);

            HttpEntity entity = resp.getEntity();
            body = Util.streamToString(entity.getContent());
            EntityUtils.consume(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
              if (null != client)
                client.close();
            }catch(Exception e){
              throw new RuntimeException(e);
            }
        }
        return body;
    }

    public static String deleteUrl(String url, String keyStoreFile, String keyStorePass,
                                   String trustStoreFile, String trustStorePass, Map<String, String> headers)  throws Exception  {
        return deleteUrl(url, keyStoreFile, keyStorePass, trustStoreFile, trustStorePass, headers, null);
    }

    public static String deleteUrl(String url, String keyStoreFile, String keyStorePass, String trustStoreFile,
                                   String trustStorePass, Map<String, String> headers, HostnameVerifier hostNameVerifier) throws Exception  {
        CloseableHttpClient client= null;      
        String body = null;
        try {
          RequestConfig requestConfig = createRequestConfig(null);
          SSLConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSystemSocketFactory();
          if (url.startsWith("https")) {
              sslsf = installKeyStores( keyStoreFile, keyStorePass, trustStoreFile, trustStorePass, hostNameVerifier);
          }
          
          client = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).setSSLSocketFactory(sslsf).build();
           
          HttpDelete request = new HttpDelete(url);
            if (request == null)
                return body;
            request.setConfig(requestConfig);
            installHeaders(request, headers);

            HttpResponse resp = client.execute(request);
            HttpEntity entity = resp.getEntity();
            body = Util.streamToString(entity.getContent());
            EntityUtils.consume(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
              if (null != client)
                client.close();
            }catch(Exception e){
              throw new RuntimeException(e);
            }
        }
        return body;
    }

    public static String postUrl(String url, String keyStoreFile, String keyStorePass, String trustStoreFile, String trustStorePass, Map<String, String> headers, HttpEntity postEntity, HostnameVerifier hostNameVerifier)  throws Exception {
        return postUrl(url,keyStoreFile,keyStorePass,trustStoreFile,trustStorePass,headers,postEntity,hostNameVerifier,null);
    }

    public static String postUrl(String url, String keyStoreFile, String keyStorePass, String trustStoreFile, String trustStorePass, Map<String, String> headers, HttpEntity postEntity, HostnameVerifier hostNameVerifier,Integer nTimeout)  throws Exception {
        CloseableHttpClient client= null;
        
        String body = null;
        try {
          RequestConfig requestConfig = createRequestConfig(nTimeout);
          SSLConnectionSocketFactory sslsf = SSLConnectionSocketFactory.getSystemSocketFactory();
          if (url.startsWith("https")) {
              sslsf = installKeyStores( keyStoreFile, keyStorePass, trustStoreFile, trustStorePass, hostNameVerifier);
          }
        
          HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).setSSLSocketFactory(sslsf).build();

            HttpPost postReq = new HttpPost(url);
            if (postReq == null)
                return body;
            postReq.setConfig(requestConfig);
            postReq.setEntity(postEntity);

            installHeaders(postReq, headers);


            HttpResponse resp = client.execute(postReq);
            HttpEntity entity = resp.getEntity();
            body = Util.streamToString(entity.getContent());
            EntityUtils.consume(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
              if (null != client)
                client.close();
            }catch(Exception e){
              throw new RuntimeException(e);
            }
        }
        return body;
    }
    
    /* convert a map to query string */
    public static String map2qstr(Map<String, String> map, boolean doEncode, boolean needQuestionMark) {
        String  s = "";
        boolean first = true;
        for (Map.Entry<String, String> entry : map.entrySet()) { 
            String key = entry.getKey();
            String value = entry.getValue();
            if (doEncode && value != null) {
        try {
                  value = URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
            }
            s += first ? (needQuestionMark ? "?" : "") : "&";
            s += key + "=" + value;
            first = false;
        }
        return s;
    }
}
