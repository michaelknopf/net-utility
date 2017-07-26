package utility.net.servicediscovery

import org.springframework.boot.actuate.health.Health
import utility.net.ServiceHealthIndicator

/**
 * A Spring Boot health indicator for discovered service clients.
 */
class DiscoveredServiceHealthIndicator extends ServiceHealthIndicator {

    DiscoveredServiceClient discoveredServiceClient

    DiscoveredServiceHealthIndicator(DiscoveredServiceClient discoveredServiceClient, final int timeout = 3000) {
        super(discoveredServiceClient.serviceClient, timeout)
        this.discoveredServiceClient = discoveredServiceClient
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {

        builder.withDetail('serviceId', discoveredServiceClient.serviceId)

        // if service has not been discovered, attempt to discover
        if (!discoveredServiceClient.rootUrl) {
            discoveredServiceClient.discover()

            // if still not found, fail check
            if (!discoveredServiceClient.rootUrl) {
                builder.down(new Exception("failed to discover service"))
                return
            }

        }

        super.doHealthCheck(builder)

    }

}
