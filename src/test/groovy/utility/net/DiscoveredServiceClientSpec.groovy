package utility.net

import groovyx.net.http.HttpResponseDecorator
import org.mockserver.integration.ClientAndServer
import org.mockserver.socket.PortFactory
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.DiscoveryClient
import spock.lang.Specification
import utility.net.servicediscovery.DiscoveredServiceClient

import static org.mockserver.integration.ClientAndServer.startClientAndServer
import static org.mockserver.model.HttpRequest.request
import static org.mockserver.model.HttpResponse.response

class DiscoveredServiceClientSpec extends Specification {

    String SERVICE_ID = "some-service"
    int N_SERVICES = 3

    def "can discover service and choose instance"() {

        setup: "initialize objects"

        // initialize mock service instances
        List<ServiceInstance> services = []
        for (int i = 1; i < N_SERVICES; i++) {
            services << new MockServiceInstance(new URI("http://localhost:$i"))
        }

        // initialize mock discovery client
        DiscoveryClient discoveryClient = Stub(DiscoveryClient)
        discoveryClient.getInstances(SERVICE_ID) >> services

        // initialize discoveredServiceClient
        DiscoveredServiceClient discoveredServiceClient = new DiscoveredServiceClient(SERVICE_ID, null, null, discoveryClient)

        when: "discover service"
        discoveredServiceClient.discover()

        then: "chosen service client is first in list"
        discoveredServiceClient.rootUrl == services[0].getUri() as String

    }

    def "can make get call"() {

        setup:
        // setup mock server
        int port = PortFactory.findFreePort()
        ClientAndServer mockServer = startClientAndServer(port)

        // initialize discovery service client
        DiscoveredServiceClient discoveredServiceClient = new DiscoveredServiceClient(SERVICE_ID, null, null, null)
        // set rootUrl manually, circumventing discovery step
        discoveredServiceClient.rootUrl = "http://localhost:$port"

        // respond 200 to requests to root
        mockServer
        .when(
                request()
                .withPath("/")
        )
        .respond(
                response()
                .withStatusCode(200)
        )

        when: "make get call to path"
        HttpResponseDecorator resp = discoveredServiceClient.get(path)

        then: "get expected status code"
        resp.statusLine.statusCode == statusCode

        where:
        path << ["/", "/fail"]
        statusCode << [200, 404]

    }

}
