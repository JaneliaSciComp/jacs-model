package org.janelia.rendering.utils;

import java.net.URI;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;

/**
 * This implements a REST client wrapper.
 */
public class ClientProxy implements Client {

    private final Client delegate;
    private final boolean closeable;

    public ClientProxy(Client delegate, boolean closeable) {
        this.delegate = delegate;
        this.closeable = closeable;
    }

    @Override
    public void close() {
        if (closeable) delegate.close();
    }

    @Override
    public WebTarget target(String uri) {
        return delegate.target(uri);
    }

    @Override
    public WebTarget target(URI uri) {
        return delegate.target(uri);
    }

    @Override
    public WebTarget target(UriBuilder uriBuilder) {
        return delegate.target(uriBuilder);
    }

    @Override
    public WebTarget target(Link link) {
        return delegate.target(link);
    }

    @Override
    public Invocation.Builder invocation(Link link) {
        return delegate.invocation(link);
    }

    @Override
    public SSLContext getSslContext() {
        return delegate.getSslContext();
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return delegate.getHostnameVerifier();
    }

    @Override
    public Configuration getConfiguration() {
        return delegate.getConfiguration();
    }

    @Override
    public Client property(String name, Object value) {
        return delegate.property(name, value);
    }

    @Override
    public Client register(Class<?> componentClass) {
        return delegate.register(componentClass);
    }

    @Override
    public Client register(Class<?> componentClass, int priority) {
        return delegate.register(componentClass, priority);
    }

    @Override
    public Client register(Class<?> componentClass, Class<?>... contracts) {
        return delegate.register(componentClass, contracts);
    }

    @Override
    public Client register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        return delegate.register(componentClass, contracts);
    }

    @Override
    public Client register(Object component) {
        return delegate.register(component);
    }

    @Override
    public Client register(Object component, int priority) {
        return delegate.register(component, priority);
    }

    @Override
    public Client register(Object component, Class<?>... contracts) {
        return delegate.register(component, contracts);
    }

    @Override
    public Client register(Object component, Map<Class<?>, Integer> contracts) {
        return delegate.register(component, contracts);
    }
}
