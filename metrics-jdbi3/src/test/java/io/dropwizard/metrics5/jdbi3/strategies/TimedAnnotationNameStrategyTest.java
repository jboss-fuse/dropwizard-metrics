package io.dropwizard.metrics5.jdbi3.strategies;

import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.annotation.Timed;
import org.jdbi.v3.core.extension.ExtensionMethod;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class TimedAnnotationNameStrategyTest extends AbstractStrategyTest {

    private final TimedAnnotationNameStrategy timedAnnotationNameStrategy = new TimedAnnotationNameStrategy();

    public interface Foo {

        @Timed
        void update();

        @Timed(name = "custom-update")
        void customUpdate();

        @Timed(name = "absolute-update", absolute = true)
        void absoluteUpdate();
    }


    @Timed
    public interface Bar {

        void update();
    }

    @Timed(name = "custom-bar")
    public interface CustomBar {

        @Timed(name = "find-by-id")
        int find(String name);
    }

    public interface Dummy {

        void show();
    }

    @Test
    void testAnnotationOnMethod() throws Exception {
        when(ctx.getExtensionMethod()).thenReturn(new ExtensionMethod(Foo.class, Foo.class.getMethod("update")));
        assertThat(timedAnnotationNameStrategy.getStatementName(ctx))
                .isEqualTo(MetricName.build("io.dropwizard.metrics5.jdbi3.strategies.TimedAnnotationNameStrategyTest$Foo.update"));
    }

    @Test
    void testAnnotationOnMethodWithCustomName() throws Exception {
        when(ctx.getExtensionMethod()).thenReturn(new ExtensionMethod(Foo.class, Foo.class.getMethod("customUpdate")));
        assertThat(timedAnnotationNameStrategy.getStatementName(ctx))
                .isEqualTo(MetricName.build("io.dropwizard.metrics5.jdbi3.strategies.TimedAnnotationNameStrategyTest$Foo.custom-update"));
    }

    @Test
    void testAnnotationOnMethodWithCustomAbsoluteName() throws Exception {
        when(ctx.getExtensionMethod()).thenReturn(new ExtensionMethod(Foo.class, Foo.class.getMethod("absoluteUpdate")));
        assertThat(timedAnnotationNameStrategy.getStatementName(ctx)).isEqualTo(MetricName.build("absolute-update"));
    }

    @Test
    void testAnnotationOnClass() throws Exception {
        when(ctx.getExtensionMethod()).thenReturn(new ExtensionMethod(Bar.class, Bar.class.getMethod("update")));
        assertThat(timedAnnotationNameStrategy.getStatementName(ctx))
                .isEqualTo(MetricName.build("io.dropwizard.metrics5.jdbi3.strategies.TimedAnnotationNameStrategyTest$Bar.update"));
    }

    @Test
    void testAnnotationOnMethodAndClassWithCustomNames() throws Exception {
        when(ctx.getExtensionMethod()).thenReturn(new ExtensionMethod(CustomBar.class, CustomBar.class.getMethod("find", String.class)));
        assertThat(timedAnnotationNameStrategy.getStatementName(ctx)).isEqualTo(MetricName.build("custom-bar.find-by-id"));
    }

    @Test
    void testNoAnnotations() throws Exception {
        when(ctx.getExtensionMethod()).thenReturn(new ExtensionMethod(Dummy.class, Dummy.class.getMethod("show")));
        assertThat(timedAnnotationNameStrategy.getStatementName(ctx)).isNull();
    }

    @Test
    void testNoMethod() {
        assertThat(timedAnnotationNameStrategy.getStatementName(ctx)).isNull();
    }
}