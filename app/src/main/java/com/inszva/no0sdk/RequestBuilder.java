package com.inszva.no0sdk;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by InsZVA on 2017/7/10.
 */

public class RequestBuilder {
    static HttpURLConnection newRequest(String url, String method, byte[] body) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(url).openConnection();
        httpURLConnection.setRequestMethod(method);
        if (body != null) {
            OutputStream outputStream = httpURLConnection.getOutputStream();
            outputStream.write(body);
            outputStream.flush();
        }
        return httpURLConnection;
    }
}
