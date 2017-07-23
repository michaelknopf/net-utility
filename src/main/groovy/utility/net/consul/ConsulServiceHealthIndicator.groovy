package utility.net.consul

import utility.net.ServiceHealthIndicator
import org.springframework.boot.actuate.health.Health

/**
 * Created by mknopf on 5/19/2017.
 */
public class ConsulServiceHealthIndicator extends ServiceHealthIndicator {

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {

        builder.withDetail('serviceId', serviceClient.serviceId)
        super.doHealthCheck(builder)

    }

}
