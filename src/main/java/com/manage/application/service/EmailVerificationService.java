package com.manage.application.service;

import com.manage.application.utils.AuthInterceptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailVerificationService {
    private final String apiUrl = "http://localhost:8080";  // Replace with your API URL
    RestTemplateBuilder builder = new RestTemplateBuilder();
    AuthenticationService authenticationService = new AuthenticationService();
    RestTemplate restTemplate = builder.additionalInterceptors(new AuthInterceptor(authenticationService)).build();

    public boolean sendOtp(String email, String username) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("email", email);
        body.add("username", username);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        // Make the API call
        ResponseEntity<String> response = restTemplate.postForEntity(
                apiUrl + "/sendOtp",
                requestEntity,
                String.class
        );

        return response.getStatusCode() == HttpStatus.OK;
    }

    public boolean verifyOtp(String email, String otpCode) {
        try {
            // Build the request
            String verifyOtpUrl = apiUrl + "/verifyOtp";  // Replace with your actual verifyOtp API endpoint

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("email", email);
            body.add("otp", otpCode);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            // Make the API call
            ResponseEntity<String> response = restTemplate.postForEntity(verifyOtpUrl, request, String.class);

            // Handle the response
            if (response.getStatusCode() == HttpStatus.OK) {
                return true;
            } else {
                // Log or handle unsuccessful response
                return false;
            }
        } catch (Exception e) {
            // Log or handle the exception
            System.out.println("An error occurred while verifying OTP: " + e.getMessage());
            return false;
        }
    }


}
