package utility.net

import org.springframework.cloud.client.ServiceInstance

/**
 * Spock is unable to mock the method getUri because
 * its return type is a final class.
 */
class MockServiceInstance implements ServiceInstance {

    URI uri

    MockServiceInstance(URI uri) {
        this.uri = uri
    }

    @Override
    String getServiceId() {
        return null
    }

    @Override
    String getHost() {
        return null
    }

    @Override
    int getPort() {
        return 0
    }

    @Override
    boolean isSecure() {
        return false
    }

    @Override
    URI getUri() {
        return uri
    }

    @Override
    Map<String, String> getMetadata() {
        return null
    }
}
