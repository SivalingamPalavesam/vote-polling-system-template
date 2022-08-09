package com.System.payload;

public class SignInResponse {


    private String jwtToken;
    private String tokenType="Bearer";


    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public SignInResponse(String jwtToken) {
        this.jwtToken = jwtToken;
    }

}
