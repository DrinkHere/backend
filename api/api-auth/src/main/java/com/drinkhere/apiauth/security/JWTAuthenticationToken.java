package com.drinkhere.apiauth.security;

public class JWTAuthenticationToken extends AbstractAuthenticationToken {
    private Long userId;
    public JWTAuthenticationToken(Long userId) {
        super(null);
        this.userId = userId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }
}
