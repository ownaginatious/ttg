package com.timetablegenerator.scraper.utility.network;

import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class RestRequest {

    private enum Operation {GET, POST, PUT, DELETE, HEAD}

    private static final int DEFAULT_RETRY_COUNT = 5;

    private final String url;
    private final Operation operation;

    private final Map<String, String> headerValues = new TreeMap<>();
    private final Map<String, Set<String>> formValues = new TreeMap<>();
    private final Map<String, String> queryValues = new TreeMap<>();

    private CookieStore cookieValues = new BasicCookieStore();

    private boolean followRedirects = true;
    private boolean allowInvalidCertificates = false;

    private static final RequestConfig redirectsConfig = RequestConfig.custom()
            .setRedirectsEnabled(true)
            .setSocketTimeout(10000).setConnectTimeout(10000).setConnectionRequestTimeout(10000).build();

    private static final RequestConfig noRedirectsConfig = RequestConfig.custom()
            .setRedirectsEnabled(false)
            .setSocketTimeout(10000).setConnectTimeout(10000).setConnectionRequestTimeout(10000).build();

    private int retryCount = DEFAULT_RETRY_COUNT;

    private HttpClient httpClient;
    private HttpContext httpContext;

    private HttpClient buildHttpClient(boolean secure) {

        int ten_seconds = 10000;
        RequestConfig rcb = RequestConfig.custom()
                .setConnectTimeout(ten_seconds)
                .setSocketTimeout(ten_seconds).build();

        HttpClientBuilder htb = HttpClients.custom();
        htb.setDefaultRequestConfig(rcb);

        if (!secure) {
            try {

                // Build an HttpClient that accepts self-signed certificates as valid.
                htb.setSSLSocketFactory(
                        new SSLConnectionSocketFactory(
                                new SSLContextBuilder()
                                        .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                                        .build()
                        )
                );

            } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
                throw new IllegalStateException("Problem creating certificate-ignoring HTTP client: "
                        + e.getMessage());
            }
        }

        return htb.setRedirectStrategy(new LaxRedirectStrategy()).build();
    }

    private RestRequest(String url, Operation operation) {

        this.url = url;
        this.operation = operation;
    }

    public static RestRequest get(String url) {
        return new RestRequest(url, Operation.GET);
    }

    public static RestRequest post(String url) {
        return new RestRequest(url, Operation.POST);
    }

    public static RestRequest put(String url) {
        return new RestRequest(url, Operation.PUT);
    }

    public static RestRequest delete(String url) {
        return new RestRequest(url, Operation.DELETE);
    }

    public static RestRequest head(String url) {
        return new RestRequest(url, Operation.HEAD);
    }

    public RestRequest setCookie(String name, String value) {

        cookieValues.addCookie(new BasicClientCookie(name, value));
        return this;
    }

    public RestRequest setHeader(String header, String value) {

        if (value != null)
            headerValues.put(header, value);
        else
            headerValues.remove(header);

        return this;
    }

    public RestRequest setQueryParameter(String param, String value) {

        if (value != null)
            queryValues.put(param, value);
        else
            queryValues.remove(param);

        return this;
    }

    public RestRequest setFormParameter(String param, String value) {

        if (this.operation != Operation.POST)
            throw new IllegalArgumentException("Tried to insert form parameter '" + param
                    + "' into a " + this.operation.name() + " request.");

        Set<String> values = formValues.get(param);

        if (values == null)
            values = new HashSet<>();

        if (value != null)
            values.add(value);

        formValues.put(param, values);
        return this;
    }

    public RestRequest allowInvalidCertificates(boolean allowInvalidCertificates) {
        this.allowInvalidCertificates = allowInvalidCertificates;
        this.httpClient = null;
        return this;
    }

    public RestRequest followRedirects(boolean followRedirects) {

        this.followRedirects = followRedirects;
        return this;
    }

    public RestRequest setRetryCount(int retryCount) {

        this.retryCount = retryCount;
        return this;
    }

    RestRequest recycleConnection(HttpClient httpClient, HttpContext httpContext) {

        this.httpClient = httpClient;
        this.httpContext = httpContext;
        this.cookieValues = (CookieStore) httpContext.getAttribute(HttpClientContext.COOKIE_STORE);

        return this;
    }

    /**
     * Performs a call to a REST interface and returns a response object.
     *
     * @return A response object.
     */
    public RestResponse run() throws IOException {

        RequestConfig currentRedirectsConfig = (followRedirects) ? redirectsConfig : noRedirectsConfig;

        CookieStore cookieStore;

        if (this.httpContext == null) {

            cookieStore = new BasicCookieStore();

            this.httpContext = new BasicHttpContext();
            this.httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);

        }

        StringBuilder queryUrl = new StringBuilder(this.url).append('?');

        // Add query parameters.
        for (String key : queryValues.keySet())
            queryUrl.append(key).append('=')
                    .append(URLEncoder.encode(queryValues.get(key), "UTF-8")).append('&');

        queryUrl.setLength(queryUrl.length() - 1);
        String loadedUrl = queryUrl.toString();

        HttpRequestBase request;

        // Set the type of method call.
        switch (this.operation) {

            case GET:
                request = new HttpGet(loadedUrl);
                break;

            case POST:
                request = new HttpPost(loadedUrl);
                break;

            case HEAD:
                request = new HttpHead(loadedUrl);
                break;

            default:
                throw new UnsupportedOperationException("Support for the interface type '" +
                        operation.name() + "' has not been implemented.");
        }

        request.setConfig(currentRedirectsConfig);

        // Add headers.
        for (String h : headerValues.keySet())
            request.addHeader(new BasicHeader(h, headerValues.get(h)));

        // Add form values.
        if (request instanceof HttpPost && formValues.size() > 0) {

            List<BasicNameValuePair> formParameters = new ArrayList<>();

            for (String f : formValues.keySet())
                formParameters.addAll(formValues.get(f).stream()
                        .map(g -> new BasicNameValuePair(f, g)).collect(Collectors.toList()));

            ((HttpPost) request).setEntity(new UrlEncodedFormEntity(formParameters));
        }

        if (this.httpClient == null)
            this.httpClient = buildHttpClient(this.allowInvalidCertificates);

        return new RestResponse(this.httpClient, this.httpContext, request, retryCount);
    }

    @Override
    public final String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append("-------------------\n");
        sb.append("   REQUEST START   \n");
        sb.append("-------------------\n\n");

        sb.append("Target: ").append(this.url).append("\n");

        if (!this.headerValues.keySet().isEmpty()) {

            sb.append("\nHeaders:\n\n");

            for (String header : this.headerValues.keySet())
                sb.append('\t').append(header).append(" : ").append(this.headerValues.get(header)).append('\n');
        }

        if (!this.cookieValues.getCookies().isEmpty()) {

            sb.append("\nCookies:\n\n");

            for (Cookie cookie : this.cookieValues.getCookies())
                sb.append('\t').append(cookie.getName()).append(" : ").append(cookie.getValue()).append('\n');
        }

        if (!this.queryValues.isEmpty()) {
            sb.append("\nQuery parameters:\n\n");

            for (String value : this.queryValues.keySet())
                sb.append('\t').append(value).append(" : ").append(this.queryValues.get(value)).append('\n');
        }

        if (!this.formValues.isEmpty()) {
            sb.append("\nForm parameters:\n\n");

            for (String value : this.formValues.keySet())
                sb.append('\t').append(value).append(" : ").append(this.formValues.get(value)).append('\n');
        }

        sb.append("\n------------------\n");
        sb.append("   REQUEST ENDS   \n");
        sb.append("------------------\n");

        return sb.toString();
    }
}
