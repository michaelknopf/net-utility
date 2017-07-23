package utility.net

import groovy.json.JsonBuilder
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.junit.Test

import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType

class MockServerTest {

    MockServer mockServer
    Logger logger = LogManager.getLogger(this.class.getName())

    @Test
    void testGet() {

        // define request
        Map query = [someKey: "someValue"]
        Map requestBody = [anotherKey: "anotherValue"]

        // define response
        String path = "/test"
        int statusCode = 202
        Map responseBody = ["foo": "bar"]
        Map<String, String> headers = [
                (HttpHeaders.SET_COOKIE): "some cookie"
        ]

        // start mock server
        mockServer = new MockServer("", 0)
        mockServer.createMockContext(
                path: "$path",
                body: new JsonBuilder(responseBody).toString(),
                statusCode: statusCode,
                headers: headers
        )
        mockServer.start()

        // make POST request
        new HTTPBuilder("http:/${mockServer.address}$path").request(Method.POST, ContentType.JSON) { req ->

            // set headers
            headers.SomeHeader = "SomeHeaderValue"
            headers.Accept = MediaType.APPLICATION_JSON

            // set query string
            uri.query = query

            // set body
            if (requestBody != null) {
                delegate.body = requestBody
            }

            // validate response
            response.success = { resp, json ->
                assert resp.statusLine.statusCode == statusCode
                assert json == responseBody
                assert resp.headers[HttpHeaders.CONTENT_TYPE].value == MediaType.APPLICATION_JSON
                assert headers.every { k, v -> resp.headers[k].value == v }
            }
        }

        // validate request
        Map request = mockServer.dequeueRequest(path)
        assert request.queryMap == query
        assert request.body == new JsonBuilder(requestBody).toString()
        assert request.method.toUpperCase() == Method.POST.toString()
    }

}
