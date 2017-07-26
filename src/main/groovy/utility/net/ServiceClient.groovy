package utility.net

import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.Method
import org.apache.commons.lang.StringUtils
import org.apache.log4j.Logger

import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType
/**
 * Manages HTTP requests to a specific domain.
 */
class ServiceClient implements HttpClient {

    String rootUrl
    String username
    String password

    // store cookies and use them in subsequent requests
    Map<String, HttpCookie> cookies = [:]

    Logger logger = Logger.getLogger(ServiceClient)

    ServiceClient(String rootUrl, String username, String password) {
        setRootUrl(rootUrl)
        this.username = username
        this.password = password
    }

    ServiceClient(String rootUrl) {
        this(rootUrl, null, null)
    }

    void setRootUrl(String rootUrl) {
        if (rootUrl) {
            this.rootUrl = StringUtils.stripEnd(rootUrl, '/')
        }
    }

    /**
     * Makes an HTTP request to rootUrl.  If the username field is non-null,
     * uses basic authentication.  Uses any cookies found in the cookies field.
     * @param path The path of the URL, excluding the query string
     * @param query A map of query string parameters
     * @param body A map to be converted to a JSON body
     * @param verb The HTTP verb/method to be used
     * @return A map containing keys "statusCode", "json", and "response"
     * (contains is the HttpResponseDecorator instance returned by HttpBuilder)
     */
    HttpResponseDecorator call(String path = "", Map query = null, Map body = null, verb = Method.GET) {

        // ensure that rootUrl is not null
        if (!rootUrl) {
            def message = "rootUrl is null, cannot make call to $path."
            logger.error(message)
            throw new IllegalStateException(message)
        }

        // convert to type Method (if necessary)
        verb = verb as Method

        // allow paths to contain or omit the leading slash
        path = StringUtils.stripStart(path, '/')
        String fullPath = "${rootUrl}/${path}"

        HTTPBuilder http = new HTTPBuilder(fullPath)

        // if username is non-null, use basic authentication
        if (username) {
            http.auth.basic(username, password)
        }

        try {
            // make request
            (HttpResponseDecorator) http.request(verb, ContentType.JSON) { req ->

                // set headers
                headers.Accept = MediaType.APPLICATION_JSON
                headers.Cookie = cookies.values().join(';')

                // set query string
                uri.query = query

                // set request body
                if (body != null) {
                    delegate.body = body
                }

                // handle response
                response.success = { HttpResponseDecorator resp, json ->

                    // add json to response decorator
                    resp.responseData = json

                    // store cookies
                    for (String header : resp.getHeaders(HttpHeaders.SET_COOKIE)) {
                        for (HttpCookie cookie : HttpCookie.parse(header.toString())) {
                            cookies[cookie.name] = cookie
                        }
                    }

                    // return response decorator
                    return resp
                }

                // for status codes >= 400, use same handler and allow exception if one occurs
                response.failure = response.success
            }
        } catch(e) {
            logger.error("'$verb' call on '$fullPath' failed with exception: $e")
            throw e
        }
    }

}
