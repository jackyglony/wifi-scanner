package com.shixunaoyou.wifiscanner.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpUtils {
    private static String TAG = "HttpUtils";
    private static HttpClient mHttpClient;

    private static HttpClient getDefaultClient() {
        if (mHttpClient == null) {
            mHttpClient = new DefaultHttpClient();
        }
        return mHttpClient;
    }

    public synchronized static JSONObject sendPostRequest(String url)
            throws JSONException {
        JSONObject result = null;
        URI wrapperUrl;
        Logger.debug(TAG, "Request Url: " + url);
        try {
            wrapperUrl = new URI(url);
            HttpPost post = new HttpPost(wrapperUrl);
            HttpResponse response = getDefaultClient().execute(post);
            HttpParams httpParameters = new BasicHttpParams();
            HttpEntity responseEntity = response.getEntity();
            int timeoutConnection = 5000;
            HttpConnectionParams.setConnectionTimeout(httpParameters,
                    timeoutConnection);
            // Set the default socket timeout (SO_TIMEOUT)
            // in milliseconds which is the timeout for waiting for data.
            int timeoutSocket = 5000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            String jsonResultString = EntityUtils.toString(responseEntity);
            Logger.debug(TAG, "Result: " + jsonResultString);
            responseEntity.consumeContent();
            result = new JSONObject(jsonResultString);
        } catch (URISyntaxException e) {
            Logger.debug(TAG, "Exception: " + e.toString());
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            Logger.debug(TAG, "Exception: " + e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Logger.debug(TAG, "Exception: " + e.toString());
            e.printStackTrace();
        }
        if (result == null) {
            result = new JSONObject(Constants.TOKEN_REQUEST_ERROR);
        }
        return result;
    }
}
