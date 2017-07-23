package utility.net

import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health

/**
 * Created by mknopf on 5/18/2017.
 */
class ServiceHealthIndicator extends AbstractHealthIndicator {

    protected final int timeout
    ServiceClient serviceClient

    ServiceHealthIndicator(final int timeout = 3000) {
        this.timeout = timeout
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {

        def result
        try {
            result = serviceClient.get('/')
            builder.withDetail('serviceUrl', serviceClient.rootUrl)
            builder.up()
        } catch(e) {
            if (serviceClient.rootUrl) {
                builder.down(new Exception("Invalid responseCode '$result.statusCode' from '$serviceClient.rootUrl'."))
            } else {
                builder.down(new Exception("Cannot find service '$serviceClient.serviceId' on Consul."))
            }
        }

    }

}
