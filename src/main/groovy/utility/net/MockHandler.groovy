package utility.net

import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType

class MockHandler implements HttpHandler {

    Logger logger = LogManager.getLogger(this.class.getName())

    LinkedList<Map> requests = new LinkedList<Map>()

    String body
    int statusCode
    Map<String, String> headers

    MockHandler(Map options) {
        this.body = options.body ?: [:]
        this.statusCode = options.statusCode ?: 200
        this.headers = options.headers
    }

    @Override
    void handle(HttpExchange exchange) {

        logger.info("{} request to {}", exchange.getRequestMethod().toUpperCase(), exchange.getRequestURI())

        // read request body
        String text = exchange.getRequestBody().text

        // get cookie
        requests.add([
                method: exchange.getRequestMethod(),
                URI: exchange.getRequestURI(),
                queryMap: HttpUtils.queryStringToMap(exchange.getRequestURI().getQuery()),
                body: text,
                headers: exchange.getRequestHeaders()
        ])

        // set headers
        Headers responseHeaders = exchange.getResponseHeaders()
        for (def header : headers.entrySet()) {
            responseHeaders.set(header.getKey(), header.getValue())
        }
        responseHeaders.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)

        // send headers
        exchange.sendResponseHeaders(statusCode, body.size())

        // write body
        exchange.getResponseBody().write(body.getBytes())
    }

}
