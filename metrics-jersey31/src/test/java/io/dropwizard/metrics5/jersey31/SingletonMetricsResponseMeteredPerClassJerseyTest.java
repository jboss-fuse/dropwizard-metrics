package io.dropwizard.metrics5.jersey31;

import io.dropwizard.metrics5.Meter;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.jersey31.exception.mapper.TestExceptionMapper;
import io.dropwizard.metrics5.jersey31.resources.InstrumentedResourceResponseMeteredPerClass;
import io.dropwizard.metrics5.jersey31.resources.InstrumentedSubResourceResponseMeteredPerClass;
import jakarta.ws.rs.core.Application;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static io.dropwizard.metrics5.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests registering {@link InstrumentedResourceMethodApplicationListener} as a singleton
 * in a Jersey {@link ResourceConfig}
 */
class SingletonMetricsResponseMeteredPerClassJerseyTest extends JerseyTest {
    static {
        Logger.getLogger("org.glassfish.jersey").setLevel(Level.OFF);
    }

    private MetricRegistry registry;

    @Override
    protected Application configure() {
        this.registry = new MetricRegistry();


        ResourceConfig config = new ResourceConfig();

        config = config.register(new MetricsFeature(this.registry));
        config = config.register(InstrumentedResourceResponseMeteredPerClass.class);
        config = config.register(new TestExceptionMapper());

        return config;
    }

    @Test
    void responseMetered2xxPerClassMethodsAreMetered() {
        assertThat(target("responseMetered2xxPerClass")
        .request()
        .get().getStatus())
        .isEqualTo(200);

        final Meter meter2xx = registry.meter(name(InstrumentedResourceResponseMeteredPerClass.class,
        "responseMetered2xxPerClass",
        "2xx-responses"));

        assertThat(meter2xx.getCount()).isEqualTo(1);
    }

    @Test
    void responseMetered4xxPerClassMethodsAreMetered() {
        assertThat(target("responseMetered4xxPerClass")
        .request()
        .get().getStatus())
        .isEqualTo(400);
        assertThat(target("responseMeteredBadRequestPerClass")
        .request()
        .get().getStatus())
        .isEqualTo(400);

        final Meter meter4xx = registry.meter(name(InstrumentedResourceResponseMeteredPerClass.class,
        "responseMetered4xxPerClass",
        "4xx-responses"));
        final Meter meterException4xx = registry.meter(name(InstrumentedResourceResponseMeteredPerClass.class,
        "responseMeteredBadRequestPerClass",
        "4xx-responses"));

        assertThat(meter4xx.getCount()).isEqualTo(1);
        assertThat(meterException4xx.getCount()).isEqualTo(1);
    }

    @Test
    void responseMetered5xxPerClassMethodsAreMetered() {
        assertThat(target("responseMetered5xxPerClass")
        .request()
        .get().getStatus())
        .isEqualTo(500);

        final Meter meter5xx = registry.meter(name(InstrumentedResourceResponseMeteredPerClass.class,
        "responseMetered5xxPerClass",
        "5xx-responses"));

        assertThat(meter5xx.getCount()).isEqualTo(1);
    }

    @Test
    void responseMeteredMappedExceptionPerClassMethodsAreMetered() {
        assertThat(target("responseMeteredTestExceptionPerClass")
        .request()
        .get().getStatus())
        .isEqualTo(500);

        final Meter meterTestException = registry.meter(name(InstrumentedResourceResponseMeteredPerClass.class,
        "responseMeteredTestExceptionPerClass",
        "5xx-responses"));

        assertThat(meterTestException.getCount()).isEqualTo(1);
    }

    @Test
    void responseMeteredUnmappedExceptionPerClassMethodsAreMetered() {
        try {
            target("responseMeteredRuntimeExceptionPerClass")
            .request()
            .get();
            fail("expected RuntimeException");
        } catch (Exception e) {
            assertThat(e.getCause()).isInstanceOf(RuntimeException.class);
        }

        final Meter meterException5xx = registry.meter(name(InstrumentedResourceResponseMeteredPerClass.class,
        "responseMeteredRuntimeExceptionPerClass",
        "5xx-responses"));

        assertThat(meterException5xx.getCount()).isEqualTo(1);
    }

    @Test
    void subresourcesFromLocatorsRegisterMetrics() {
        final Meter meter2xx = registry.meter(name(InstrumentedSubResourceResponseMeteredPerClass.class,
                "responseMeteredPerClass",
                "2xx-responses"));
        final Meter meter200 = registry.meter(name(InstrumentedSubResourceResponseMeteredPerClass.class,
                "responseMeteredPerClass",
                "200-responses"));

        assertThat(meter2xx.getCount()).isZero();
        assertThat(meter200.getCount()).isZero();

        assertThat(target("subresource/responseMeteredPerClass")
        .request()
        .get().getStatus())
        .isEqualTo(200);

        assertThat(meter2xx.getCount()).isOne();
        assertThat(meter200.getCount()).isOne();
    }
}
