package org.janelia.model.security;

public class AppAuthorization {
    private final Subject authenticatedSubject;
    private final String authenticationToken;
    private final Subject proxiedSubject;

    public AppAuthorization(Subject authenticatedSubject, String authenticationToken, Subject proxiedSubject) {
        this.authenticatedSubject = authenticatedSubject;
        this.authenticationToken = authenticationToken;
        this.proxiedSubject = proxiedSubject;
    }

    public Subject getAuthenticatedSubject() {
        return authenticatedSubject;
    }

    public String getAuthenticationToken() {
        return authenticationToken;
    }

    public Subject getProxiedSubject() {
        return proxiedSubject;
    }
}
