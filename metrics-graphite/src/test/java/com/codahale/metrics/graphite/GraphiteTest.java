package com.codahale.metrics.graphite;

import org.junit.Before;
import org.junit.Test;

import javax.net.SocketFactory;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Fail.failBecauseExceptionWasNotThrown;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class GraphiteTest {
    private final SocketFactory socketFactory = mock(SocketFactory.class);
    private final InetSocketAddress address = new InetSocketAddress("example.com", 1234);
    private final Graphite graphite = new Graphite(address, socketFactory);

    private final Socket socket = mock(Socket.class);
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    @Before
    public void setUp() throws Exception {
        when(socket.getOutputStream()).thenReturn(output);
        when(socketFactory.createSocket(anyString(), anyInt())).thenReturn(socket);
    }

    @Test
    public void connectsToGraphite() throws Exception {
        graphite.connect();

        verify(socketFactory).createSocket(address.getHostName(), address.getPort());
    }

    @Test
    public void measuresFailures() throws Exception {
        assertThat(graphite.getFailures())
                .isZero();
    }

    @Test
    public void disconnectsFromGraphite() throws Exception {
        graphite.connect();
        graphite.close();

        verify(socket).close();
    }

    @Test
    public void doesNotAllowDoubleConnections() throws Exception {
        graphite.connect();
        try {
            graphite.connect();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage())
                    .isEqualTo("Already connected");
        }
    }

    @Test
    public void writesValuesToGraphite() throws Exception {
        graphite.connect();
        graphite.send("name", "value", 100);

        assertThat(output.toString())
                .isEqualTo("name value 100\n");
    }

    @Test
    public void sanitizesNames() throws Exception {
        graphite.connect();
        graphite.send("name woo", "value", 100);

        assertThat(output.toString())
                .isEqualTo("name-woo value 100\n");
    }

    @Test
    public void sanitizesValues() throws Exception {
        graphite.connect();
        graphite.send("name", "value woo", 100);

        assertThat(output.toString())
                .isEqualTo("name value-woo 100\n");
    }

    @Test
    public void notifiesIfGraphiteIsUnavailable() throws Exception {
        final String UNKNOWN_HOST = "unknown-host-10el6m7yg56ge7dm.com";
        SocketFactory realSocketFactory = SocketFactory.getDefault();
        InetSocketAddress unknownHostAddress = new InetSocketAddress(UNKNOWN_HOST, 1234);
        Graphite graphiteAtUnknownHost = new Graphite(unknownHostAddress, realSocketFactory);

        try {
            graphiteAtUnknownHost.connect();
            failBecauseExceptionWasNotThrown(UnknownHostException.class);
        } catch (Exception e) {
            assertThat(e.getMessage())
                .isEqualTo(UNKNOWN_HOST);
        }
    }
}
