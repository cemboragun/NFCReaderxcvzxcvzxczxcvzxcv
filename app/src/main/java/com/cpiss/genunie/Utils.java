package com.cpiss.genunie;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;

public class
Utils {
    public static String getStringFromConnection(String url, List<NameValuePair> nameValuePairs) {
        Log.d("CON", "Connection url>" + url);
        Log.d("CON", "-----POST VALUES-----");
        if (nameValuePairs != null) {
            for (int i = 0; i < nameValuePairs.size(); i++) {
                Log.d("CON", nameValuePairs.get(i).getName() + " > " + nameValuePairs.get(i).getValue());
            }
        }
        Log.d("CON", "------------------");

        HttpPost httpMethod = null;
        httpMethod = new HttpPost(url);
        DefaultHttpClient clients = new DefaultHttpClient();
        try {
            httpMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = clients.execute(httpMethod);
            if (response == null) {
                return null;
            }

            HttpEntity entitty = response.getEntity();
            String returnString = EntityUtils.toString(entitty);
            Log.d("CON", "ReturnString> " + returnString);
            return returnString;

        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
