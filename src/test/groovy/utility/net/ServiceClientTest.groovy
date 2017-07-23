package utility.net

import com.sun.net.httpserver.HttpServer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Created by mknopf on 7/22/2017.
 */
class ServiceClientTest {

    ServiceClient serviceClient
    HttpServer echoServer
    Logger logger = LogManager.getLogger(this.class.getName())

    @Before
    void setup() {
        echoServer = HttpServer.create(new InetSocketAddress("", 0), 0)
        echoServer.createContext("/echo", new EchoHandler())
        echoServer.start()
        final String address = "http:/${echoServer.address}"
        serviceClient = new ServiceClient("http:/${echoServer.address}")
        logger.info("Listening on ${address}")
    }

    @Test
    void testPost() {
        Map body = ["foo": "bar"]
        def response = serviceClient.post("echo", body)
        assert response.json == body
    }

    @Test
    void testGet() {
        Map query = ["foo": "bar", "wham": "bam"]
        def response = serviceClient.get("echo", query)
        assert response.json.every{ k, v -> v == query[k] }
    }

    @After
    void cleanup() {

    }

}
