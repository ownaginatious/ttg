package com.timetablegenerator.scraper.utility.network;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public final class RestResponse {

    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<String, String> headerValues = new TreeMap<>();
    private final Map<String, String> cookieValues = new TreeMap<>();

    private byte[] responseBytes = null;
    private int responseCode;

    private HttpClient httpClient;
    private HttpContext httpContext;

    public final Set<String> getCookies() {
        return cookieValues.keySet();
    }

    public final String getCookieValue(String cookie) {
        return cookieValues.get(cookie);
    }

    public final Set<String> getHeaders() {
        return headerValues.keySet();
    }

    public final String getHeaderValue(String header) {
        return headerValues.get(header);
    }

    public final int getResponseCode() {
        return this.responseCode;
    }

    public final String getResponseString() {
        return new String(this.responseBytes, StandardCharsets.UTF_8);
    }

    public final String getResponseString(Charset charset) {
        return new String(this.responseBytes, charset);
    }

    public final byte[] getResponseBytes() {
        return this.responseBytes;
    }

    public final InputStream getResponseStream() {

        if (this.responseBytes == null)
            return null;

        return new ByteArrayInputStream(this.responseBytes);
    }

    RestResponse(HttpClient client, HttpContext context, HttpRequestBase request, final int retryCount) throws IOException {

        this.httpClient = client;
        this.httpContext = context;

        HttpResponse response = null;

        int attempts = 0;

        while (attempts <= retryCount) {
            try {
                response = client.execute(request, context);
                break;
            } catch (SocketTimeoutException | ConnectTimeoutException | NoHttpResponseException e) {
                LOGGER.warn("Server failed to respond [attempt " + ++attempts + " of " + retryCount + "]");
            }
        }

        if (response == null) {
            throw new IOException("Failed to yield a response from the server after "
                    + attempts + " attempt" + (retryCount > 0 ? "s" : ""));
        }

        // Get the HTTP status code.
        this.responseCode = response.getStatusLine().getStatusCode();

        // Read in the headers.
        for (Header h : response.getAllHeaders())
            headerValues.put(h.getName(), h.getValue());

        // Read in the cookies.
        for (Cookie c : ((BasicCookieStore) context.getAttribute(HttpClientContext.COOKIE_STORE)).getCookies())
            cookieValues.put(c.getName(), c.getValue());

        HttpEntity responseEntity = response.getEntity();

        if (responseEntity == null)
            return;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        InputStream responseStream = responseEntity.getContent();

        int b;
        while ((b = responseStream.read()) != -1)
            bos.write(b);

        responseStream.close();

        this.responseBytes = bos.toByteArray();
    }

    private RestRequest load(RestRequest req) {
        return req.recycleConnection(this.httpClient, this.httpContext);
    }

    public RestRequest nextDelete(String url) {
        return this.load(RestRequest.delete(url));
    }

    public RestRequest nextGet(String url) {
        return this.load(RestRequest.get(url));
    }

    public RestRequest nextPost(String url) {
        return this.load(RestRequest.post(url));
    }

    public RestRequest nextPut(String url) {
        return this.load(RestRequest.put(url));
    }

    public RestRequest nextHead(String url) {
        return this.load(RestRequest.head(url));
    }

    @Override
    public final String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("------------------\n");
        sb.append("  RESPONSE START  \n");
        sb.append("------------------\n\n");

        sb.append("Response code: ").append(this.getResponseCode()).append("\n");

        if (!this.headerValues.isEmpty()) {

            sb.append("\nHeaders:\n\n");

            for (String header : this.getHeaders())
                sb.append('\t').append(header).append(" : ").append(this.getHeaderValue(header)).append('\n');
        }

        if (!this.cookieValues.isEmpty()) {

            sb.append("\nCookies:\n\n");

            for (String cookie : this.getCookies())
                sb.append('\t').append(cookie).append(" : ").append(this.getCookieValue(cookie)).append('\n');
        }

        sb.append("\nBody:\n");
        sb.append("----\n");

        sb.append(this.getResponseString(Charset.defaultCharset()));

        sb.append("\n\n------------------\n");
        sb.append("  RESPONSE ENDS  \n");
        sb.append("------------------\n");

        return sb.toString();
    }
}
