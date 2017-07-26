package utility.net.servicediscovery

import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.Method
import org.apache.log4j.Logger
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.DiscoveryClient
import utility.net.HttpClient
import utility.net.ServiceClient

/**
 * Manages HTTP requests to a specific service whose root url is
 * configured through service discovery.
 */
class DiscoveredServiceClient implements HttpClient {

    /** serviceClient acts as a delegate, and is configured using information found through discoveryClient. */
    @Delegate(excludes = ["call", "get", "put", "post", "delete"]) ServiceClient serviceClient

    DiscoveryClient discoveryClient
    String serviceId

    DiscoveredServiceClient(String serviceId, String username, String password, DiscoveryClient discoveryClient = null) {
        serviceClient = new ServiceClient(null, username, password)
        logger = Logger.getLogger(DiscoveredServiceClient)
        this.serviceId = serviceId
        this.discoveryClient = discoveryClient
    }

    DiscoveredServiceClient(String serviceId, DiscoveryClient discoveryClient = null) {
        this(serviceId, null, null, discoveryClient)
    }

    /**
     * The strategy used to choose an instance from the list
     * of all discovered instances
     */
    ServiceInstance discoveryStrategy(List<ServiceInstance> instances) {
        return instances[0]
    }

    /**
     * Attempts to discover service and configure rootUrl accordingly.
     */
    void discover() {

        // find all instances with serviceId
        def instances = discoveryClient.getInstances(serviceId)
        if (!instances) {
            def message = "Could not find service $serviceId."
            logger.error(message)
            throw new IllegalArgumentException(message)
        }

        // use discovery strategy to choose an instance
        ServiceInstance service = discoveryStrategy(instances)

        // configure root url of delegate service client
        String url = service.getUri()
        if (!url) {
            def message = "Service $serviceId found, but uri is null."
            logger.error(message)
            throw new Exception(message)
        }
        logger.info("Found service $serviceId at $url.")
        serviceClient.setRootUrl(url)
    }

    /**
     * After attempting to discover (or rediscover) service if necessary,
     * delegates to serviceClient.
     * @param path The path of the URL, excluding the query string
     * @param query A map of query string parameters
     * @param body A map to be converted to a JSON body
     * @param verb The HTTP verb/method to be used
     * @return A map containing keys "statusCode", "json", and "response"
     * (contains is the HttpResponseDecorator instance returned by HttpBuilder)
     */
    HttpResponseDecorator call(String path = "", Map query = null, Map body = null, verb = Method.GET) {

        // if root url is null, attempt to discover
        if (!serviceClient.rootUrl) {
            discover()
        }
        try {
            // attempt to make call
            return serviceClient.call(path, query, body, verb)
        } catch(ConnectException ignore) {
            // attempt to rediscover service
            discover()
            // attempt call again
            return serviceClient.call(path, query, body, verb)
        }
    }

}
