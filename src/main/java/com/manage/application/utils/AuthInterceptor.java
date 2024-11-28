package com.manage.application.utils;

import com.manage.application.service.AuthenticationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class AuthInterceptor implements ClientHttpRequestInterceptor {
    private static final String API_URL = "http://localhost:8080";
    private AuthenticationService authenticationService;
    public AuthInterceptor(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        if (request.getURI().toString().contains(API_URL + "/user/login") ||
                request.getURI().toString().contains(API_URL + "/user/verify-token")||
                request.getURI().toString().contains(API_URL + "/user/forgot-password")||
                request.getURI().toString().contains(API_URL + "/user/reset-password")||
                request.getURI().toString().contains(API_URL + "/user/verify-reset-password-token")||
                request.getURI().toString().contains(API_URL + "/user/register")) {
            return execution.execute(request, body);
        }

        authenticationService.loadToken();
        String token = authenticationService.getToken();

        HttpHeaders headers = request.getHeaders();
        headers.add("Authorization", "Bearer " + token);

        return execution.execute(request, body);
    }
}
