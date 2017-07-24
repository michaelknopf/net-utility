package utility.net.servicediscovery

import utility.net.ServiceHealthIndicator
import org.springframework.boot.actuate.health.Health


class DiscoveredServiceHealthIndicator extends ServiceHealthIndicator {

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {

        builder.withDetail('serviceId', serviceClient.serviceId)
        super.doHealthCheck(builder)

    }

}
