package utility.net

import groovyx.net.http.HttpResponseDecorator
import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health

/**
 * A Spring Boot health indicator for service clients.
 */
class ServiceHealthIndicator extends AbstractHealthIndicator {

    protected final int timeout
    ServiceClient serviceClient

    ServiceHealthIndicator(ServiceClient serviceClient, final int timeout = 3000) {
        this.serviceClient = serviceClient
        this.timeout = timeout
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {

        HttpResponseDecorator response = serviceClient.get('/')
        if (response.statusLine.statusCode >= 400) {
            builder.down(new Exception("Request to '$serviceClient.rootUrl' received ${response.statusLine.statusCode} response."))
        } else {
            builder.withDetail('serviceUrl', serviceClient.rootUrl)
            builder.up()
        }

    }

}
