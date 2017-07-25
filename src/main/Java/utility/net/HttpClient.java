package utility.net;

import java.util.Map;

public interface HttpClient {

    Object call(String path, Map query, Map body, Object verb);

    default public Object get(String path, Map query) {
        return call(path, query, null, "GET");
    }

    default public Object get(String path) {
        return call (path, null, null, "GET");
    }

    default Object put(String path, Map body) {
        return call(path, null, body, "PUT");
    }

    default Object put(String path) {
        return call(path, null, null, "PUT");
    }

    default Object post(String path, Map body) {
        return call(path, null, body, "POST");
    }

    default Object post(String path) {
        return call(path, null, null, "POST");
    }

    default Object delete(String path, Map body) {
        return call(path, null, body, "DELETE");
    }

    default Object delete(String path) {
        return call(path, null, null, "DELETE");
    }


}
