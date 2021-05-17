package com.deco.utils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;

@Service("request")
public class RequestUtils {

	/*
     * ����https GET/POST����
     * �����ַ�����󷽷�������
     */
    public static JSONObject httpsRequest(String requestUrl,String requestMethod,String outputStr){
        StringBuffer buffer=null;
        try{
        //����SSLContext
        SSLContext sslContext=SSLContext.getInstance("SSL");
        TrustManager[] tm={(TrustManager) new MyX509TrustManager()};
        //��ʼ��
        sslContext.init(null, tm, new java.security.SecureRandom());;
        //��ȡSSLSocketFactory����
        SSLSocketFactory ssf=sslContext.getSocketFactory();
        URL url=new URL(requestUrl);
        HttpsURLConnection conn=(HttpsURLConnection)url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setUseCaches(false);
        conn.setRequestMethod(requestMethod);
        //���õ�ǰʵ��ʹ�õ�SSLSoctetFactory
        conn.setSSLSocketFactory(ssf);
        System.out.println(conn.toString()); 
        conn.connect();
        //����������д����
        if(null!=outputStr){
            OutputStream os=conn.getOutputStream();
            os.write(outputStr.getBytes("utf-8"));
            System.out.println(os.toString());
            os.close();
        }
        
        //��ȡ�������˷��ص�����
        InputStream is=conn.getInputStream();
        InputStreamReader isr=new InputStreamReader(is,"utf-8");
        BufferedReader br=new BufferedReader(isr);
        buffer=new StringBuffer();
        String line=null;
        while((line=br.readLine())!=null){
            buffer.append(line);
        }
        }catch(Exception e){
            e.printStackTrace();
        }
        return JSONObject.parseObject(buffer.toString());
    }
    
    public static String post(JSONObject json,String path) {
        String result="";
        try {
            HttpClient client=new DefaultHttpClient();
            HttpPost post=new HttpPost(path);
            post.setHeader("Content-Type", "appliction/json");
            post.addHeader("X-APP-Id", "pp8t336vCK9");//  这几个是设置header头的
            post.addHeader("X-APP-Key", "Cn0PboLmab");
            post.addHeader("X-CTG-Request-Id", "123");
            StringEntity s=new StringEntity(json.toString(), "utf-8");
            s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "appliction/json"));
            post.setEntity(s);
            HttpResponse httpResponse=client.execute(post);
            InputStream in=httpResponse.getEntity().getContent();
            BufferedReader br=new BufferedReader(new InputStreamReader(in, "utf-8"));
            StringBuilder strber=new StringBuilder();
            String line=null;
            while ((line=br.readLine())!=null) {
                strber.append(line+"\n");

            }
            in.close();
            result=strber.toString();
            if(httpResponse.getStatusLine().getStatusCode()!= HttpStatus.SC_OK){
                result="服务器异常";
            }
        } catch (Exception e) {
            System.out.println("请求异常");
            throw new RuntimeException(e);
        }
        System.out.println("result=="+result);//请求返回
        return result;
    }
}


/*@Configuration
public class RequestUtils {
    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}*/