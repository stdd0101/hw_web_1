package ru.netology.server.request;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final InputStream in;

    private Request(String method, String path, Map<String, String> headers, InputStream in) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.in = in;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public InputStream getIn() {
        return in;
    }

    public static List<NameValuePair> getQueryParams(String url) throws URISyntaxException {

        List<NameValuePair> params = URLEncodedUtils.parse(new URI(url), StandardCharsets.UTF_8);

        for (NameValuePair param : params) {
            System.out.println(param.getName() + " : " + param.getValue());
        }

        return params;
    }

    public static String getQueryParam(List<NameValuePair> params, String name) throws UnsupportedEncodingException
    {
        return params
                .stream()
                .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue)).get(name);
    }

    public static Request fromInputStream(InputStream inputStream) throws IOException, URISyntaxException {
        var in = new BufferedReader(new InputStreamReader(inputStream));

        // read only request line for simplicity
        // must be in form GET /path HTTP/1.1
        final var requestLine = in.readLine();
        final var parts = requestLine.split(" ");

        if (parts.length != 3) {
            // just close socket
            throw new IOException("Invalid request");
        }
        var method = parts[0];
        var path = parts[1];

        var queryList = Request.getQueryParams(path);
        System.out.println(Request.getQueryParam(queryList, "params"));

        var headers = new HashMap<String, String>();
        String line;
        while(!(line = in.readLine()).isEmpty()) {
            //Content-Type: application/form-data
            var index = line.indexOf(":");
            var headerName = line.substring(0, index);
            var headerValue = line.substring(index + 2);

            headers.put(headerName, headerValue);
        }
        return new Request(method, path, headers, inputStream);
    }
}
