package utility.net.consul

import groovyx.net.http.Method
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.client.discovery.DiscoveryClient
import utility.net.ServiceClient

/**
 * Created by mknopf on 5/18/2017.
 */
class ConsulServiceClient extends ServiceClient {

    @Autowired
    DiscoveryClient discoveryClient

    String serviceId

    ConsulServiceClient(String serviceId, String username, String password) {
        super(null, username, password)
        logger = Logger.getLogger(ConsulServiceClient)
        this.serviceId = serviceId
    }

    ConsulServiceClient(String serviceId) {
        super(null)
        logger = Logger.getLogger(ConsulServiceClient)
        this.serviceId = serviceId
    }

    void refreshRootUrl() {
        def service = discoveryClient.getInstances(serviceId)[0]
        if (!service) {
            def message = "Could not find service $serviceId."
            logger.error(message)
            throw new IllegalArgumentException(message)
        }
        String url = service.getUri()
        if (!url) {
            def message = "Service $serviceId found, but uri is null."
            logger.error(message)
            throw new Exception(message)
        }
        logger.info("Found service $serviceId at $url.")
        setRootUrl(url)
    }

    def call(path, query = null, body = null, verb = Method.GET) {
        if (!rootUrl) {
            refreshRootUrl()
        }
        try {
            return super.call(path, query, body, verb)
        } catch(e) {
            refreshRootUrl()
            return super.call(path, query, body, verb)
        }
    }

}