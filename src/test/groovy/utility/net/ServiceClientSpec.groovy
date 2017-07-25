package utility.net

import groovy.json.JsonBuilder
import groovyx.net.http.HttpResponseDecorator
import org.mockserver.integration.ClientAndServer
import org.mockserver.model.Cookie
import org.mockserver.model.Header
import org.mockserver.socket.PortFactory
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType

import static org.mockserver.integration.ClientAndServer.startClientAndServer
import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.HttpResponse.response


class ServiceClientSpec extends Specification {

    ClientAndServer mockServer
    ServiceClient serviceClient

    @Shared Map data = [
            path: ["/", "/some/long/path"],
            body:  [[someKey: "someValue"]] * 2
    ]

    def setup() {

        // initialize mock server
        int port = PortFactory.findFreePort()
        mockServer = startClientAndServer(port)
        String rootUrl = "http://localhost:$port"

        // initialize service client
        serviceClient = new ServiceClient(rootUrl)

    }

    @Unroll
    def "GET request to #path with query params #requestParams returns 200"() {

        setup: "setup URI that responds 200 for GET requests"
        mockServer
        .when(
                request()
                        .withMethod("GET")
                        .withPath(path)
                        .withQueryStringParameters(*requestParams.collect{ k, v -> new org.mockserver.model.Parameter(k, v) })
        )
        .respond(
                response()
                        .withStatusCode(200)
        )

        when: "make GET call"
        HttpResponseDecorator result = serviceClient.get(path, requestParams)

        then: "response is 200"
        200 == result.statusLine.statusCode

        where:
        path << data.path
        requestParams << data.body

    }

    @Unroll
    def "POST request to #path with body #requestBody returns 200"() {

        setup: "setup URI that responds 200 for POST requests"
        mockServer
        .when(
                request()
                        .withMethod("POST")
                        .withPath(path)
                        .withBody(new JsonBuilder(requestBody).toString())
        )
        .respond(
                response()
                        .withStatusCode(200)
        )

        when: "make POST call"
        HttpResponseDecorator result = serviceClient.post(path, requestBody)

        then: "response is 200"
        200 == result.statusLine.statusCode

        where:
        path << data.path
        requestBody << data.body

    }

    @Unroll
    def "GET request to #path returns expected body #responseBody"() {

        setup: "setup URI that responds 200 with JSON body for GET requests"
        mockServer
        .when(
                request()
                        .withMethod("GET")
                        .withPath(path)
        )
        .respond(
                response()
                        .withStatusCode(200)
                        .withBody(new JsonBuilder(responseBody).toString())
                        .withHeader(new Header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON))
        )

        when: "make GET call"
        HttpResponseDecorator result = serviceClient.get(path)

        then: "response is 200"
        responseBody == result.getData()

        where:
        path << data.path
        responseBody << data.body

    }

    @Unroll
    def "can get and use cookie #cookie"() {

        setup: "setup URI that responds with cookie"
        mockServer
        .when(
                request()
                        .withMethod("GET")
                        .withPath("/start-session")
        )
        .respond(
                response()
                        .withStatusCode(200)
                        .withCookie(new Cookie(cookie.name, cookie.value))
        )

        and: "setup URI that requires cookie"
        mockServer
        .when(
                request()
                        .withMethod("GET")
                        .withPath("/do-something")
                        .withCookie(new Cookie(cookie.name, cookie.value))
        )
        .respond(
                response()
                        .withStatusCode(200)
        )

        when: "obtain and store cookie"
        serviceClient.get("/start-session")

        and: "use cookie"
        HttpResponseDecorator result = serviceClient.get("/do-something")

        then: "response is 200 and stored cookies are as expected"
        result.statusLine.statusCode == 200
        serviceClient.cookies == [cookie]

        where:
        cookie << [new HttpCookie("sessionId", "2By8LOhBmaW5nZXJwcmludCIlMDAzMW")]

    }

}
