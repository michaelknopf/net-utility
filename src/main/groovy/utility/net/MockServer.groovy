package utility.net

import com.sun.net.httpserver.HttpServer

class MockServer {

    @Delegate HttpServer server
    Map<String, MockHandler> handlers = [:]

    MockServer(String hostname, int port) {
        server = HttpServer.create(new InetSocketAddress(hostname, port), 0)
    }

    public void createMockContext(Map options) {
        MockHandler handler = new MockHandler(options)
        handlers[options.path] = handler
        server.createContext(options.path, handler)
    }

    public Map dequeueRequest(String path) {
        return handlers[path].requests.remove()
    }
}
