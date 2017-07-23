package utility.net

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import groovy.json.JsonBuilder
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType

class EchoHandler implements HttpHandler {

    Logger logger = LogManager.getLogger(this.class.getName())

    @Override
    void handle(HttpExchange exchange) throws IOException {

        // set content type
        exchange.getResponseHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)

        switch(exchange.getRequestMethod().toUpperCase()) {
            case "GET":
                logger.debug("GET request with URI {}", exchange.getRequestURI())
                // convert query string to map
                Map queryMap = HttpUtils.queryStringToMap(exchange.getRequestURI().getQuery())
                // use query map as response body
                String body = new JsonBuilder(queryMap).toString()
                // set headers
                exchange.sendResponseHeaders(200, body.size())
                // write body
                exchange.getResponseBody().write(body.getBytes())
                break
            case "POST":
                // read request body
                String text = exchange.getRequestBody().text
                logger.debug("POST request with body {}", text)
                // set headers
                exchange.sendResponseHeaders(200, text.size())
                // write body
                exchange.getResponseBody().write(text.getBytes())
                break
        }
        exchange.close()
    }

}