package utility.net

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.commons.lang.StringUtils
import org.apache.log4j.Logger

import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType

/**
 * Created by mknopf on 5/18/2017.
 */
class ServiceClient {

    String rootUrl
    String username
    String password
    List<HttpCookie> cookies = []

    Logger logger

    ServiceClient(String rootUrl, String username, String password) {
        logger = Logger.getLogger(ServiceClient)
        setRootUrl(rootUrl)
        this.username = username
        this.password = password
    }

    ServiceClient(String rootUrl) {
        this(rootUrl, null, null)
    }

    public void setRootUrl(String rootUrl) {
        if (rootUrl) {
            this.rootUrl = StringUtils.stripEnd(rootUrl, '/')
        }
    }

    def call(path, query = null, body = null, verb = Method.GET) {

        if (!rootUrl) {
            def message = "rootUrl is null, cannot make call to $path."
            logger.error(message)
            throw new IllegalStateException(message)
        }

        verb = verb as Method
        path = StringUtils.stripStart(path, '/')
        String fullPath = "${rootUrl}/${path}"

        HTTPBuilder http = new HTTPBuilder(fullPath)
        if (username) {
            http.auth.basic(username, password)
        }

        def result = [:]
        try {
            http.request(verb, ContentType.JSON) { req ->
                headers.Accept = MediaType.APPLICATION_JSON
                headers.Cookie = cookies.join(';')

                uri.query = query
                if (body != null) {
                    delegate.body = body
                }

                response.success = { resp, json ->

                    result.response = resp
                    result.statusCode = resp.statusLine.statusCode
                    result.json = json

                    for (String header : result.response.getHeaders(HttpHeaders.SET_COOKIE)) {
                        cookies += HttpCookie.parse(header.toString())
                    }
                }
                response.failure = response.success
            }
        } catch(e) {
            logger.error("'$verb' call on '$fullPath' failed with exception: $e")
            throw e
        }

        return result
    }

    def get(path, query = null) {
        call(path, query, null, Method.GET)
    }

    def put(path, body = null) {
        call(path, null, body, Method.PUT)
    }

    def post(path, body = null) {
        call(path, null, body, Method.POST)
    }

    def delete(path, body = null) {
        call(path, null, body, Method.DELETE)
    }

}
