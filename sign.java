package service.wxcamera;

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.Formatter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;  

import com.alibaba.fastjson.JSONObject;

class Sign {
    public static void main(String[] args) {
    	 
        try {
			String accessToken=AccessToken.getAccessToken();
		String Url="http://api.weixin.qq.com/cgi-bin/ticket/getticket?type=jsapi&access_token="+accessToken	;
		 URL url = new URL(Url);
	     HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 
	     connection.setRequestMethod("GET");
	     connection.setDoOutput(true);
	     connection.setDoInput(true);
	     connection.connect();
	     //获取返回的字符
	     InputStream inputStream = connection.getInputStream();
	     int size =inputStream.available();
	     byte[] bs =new byte[size];
	     inputStream.read(bs);
	     String message=new String(bs,"UTF-8");
	     JSONObject jsonObject = JSONObject.parseObject(message);
	     String jsapi_ticket = jsonObject.getString("ticket");
	    // System.out.println(jsapi_ticket);
	     // 注意 URL 一定要动态获取，不能 hardcode
	        String url1 = "http://example.com";
	        Map<String, String> ret = sign(jsapi_ticket, url1);
	        for (Map.Entry entry : ret.entrySet()) {
	            System.out.println(entry.getKey() + ", " + entry.getValue());
	        }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     
    };

    public static Map<String, String> sign(String jsapi_ticket, String url) {
        Map<String, String> ret = new HashMap<String, String>();
        String nonce_str = create_nonce_str();
        String timestamp = create_timestamp();
        String string1;
        String signature = "";

        //注意这里参数名必须全部小写，且必须有序
        string1 = "jsapi_ticket=" + jsapi_ticket +
                  "&noncestr=" + nonce_str +
                  "&timestamp=" + timestamp +
                  "&url=" + url;
       // System.out.println(string1);

        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(string1.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        ret.put("url", url);
        ret.put("jsapi_ticket", jsapi_ticket);
        ret.put("nonceStr", nonce_str);
        ret.put("timestamp", timestamp);
        ret.put("signature", signature);

        return ret;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private static String create_nonce_str() {
        return UUID.randomUUID().toString();
    }

    private static String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }
}
