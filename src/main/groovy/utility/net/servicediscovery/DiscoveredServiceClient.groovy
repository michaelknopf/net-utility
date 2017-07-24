package utility.net.servicediscovery

import groovyx.net.http.Method
import org.apache.log4j.Logger
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.DiscoveryClient
import utility.net.ServiceClient

/**
 * Manages HTTP requests to a specific service whose root url is
 * configured through service discovery.
 */
class DiscoveredServiceClient {

    /**
     * serviceClient is configured using information found through discoveryClient.
     */
    @Delegate(excludes=['call']) ServiceClient serviceClient

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
    List<ServiceInstance> discoveryStrategy(instances) {
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
        def service = discoveryStrategy(instances)

        // configure root url of delegate service client
        String url = service.getUri()
        if (!url) {
            def message = "Service $serviceId found, but uri is null."
            logger.error(message)
            throw new Exception(message)
        }
        logger.info("Found service $serviceId at $url.")
        setRootUrl(url)
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
    def call(path, query = null, body = null, verb = Method.GET) {

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
