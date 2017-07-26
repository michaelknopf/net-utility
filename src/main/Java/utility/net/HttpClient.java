package utility.net;

import groovyx.net.http.HttpResponseDecorator;

import java.util.Map;

public interface HttpClient {

    HttpResponseDecorator call(String path, Map query, Map body, Object verb);

    default public HttpResponseDecorator get(String path, Map query) {
        return call(path, query, null, "GET");
    }

    default public HttpResponseDecorator get(String path) {
        return call (path, null, null, "GET");
    }

    default HttpResponseDecorator put(String path, Map body) {
        return call(path, null, body, "PUT");
    }

    default HttpResponseDecorator put(String path) {
        return call(path, null, null, "PUT");
    }

    default HttpResponseDecorator post(String path, Map body) {
        return call(path, null, body, "POST");
    }

    default HttpResponseDecorator post(String path) {
        return call(path, null, null, "POST");
    }

    default HttpResponseDecorator delete(String path, Map body) {
        return call(path, null, body, "DELETE");
    }

    default HttpResponseDecorator delete(String path) {
        return call(path, null, null, "DELETE");
    }


}
