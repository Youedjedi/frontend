package com.manage.application.service;

import com.manage.application.domain.request.user.UserRequest;
import com.manage.application.domain.response.common.HttpResponse;
import com.manage.application.domain.response.user.UserListApiResponse;
import com.manage.application.domain.response.user.UserListResponse;
import com.manage.application.domain.response.user.UserSelectListApiResponse;
import com.manage.application.domain.response.user.UserSingleApiResponse;
import com.manage.application.utils.AuthInterceptor;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@Service
public class UserService {
    private final String apiUrl = "http://localhost:8080";  // Replace with your API URL
    RestTemplateBuilder builder = new RestTemplateBuilder();
    AuthenticationService authenticationService = new AuthenticationService();
    RestTemplate restTemplate = builder.additionalInterceptors(new AuthInterceptor(authenticationService)).build();

    public UserListApiResponse getUsers(int page, int size, String sort, String direction, String startDate, String endDate, String username, Set<String> roles) {
        StringBuilder url = new StringBuilder(apiUrl + "/user/list");
        url.append("?page=").append(page);
        url.append("&size=").append(size);
        url.append("&sort=").append(sort);
        url.append("&direction=").append(direction);

        if (username != null) {
            url.append("&userName=").append(username);
        }
        if (startDate != null) {
            url.append("&startDate=").append(startDate);
        }
        if (endDate != null) {
            url.append("&endDate=").append(endDate);
        }
        if (roles != null && !roles.isEmpty()) {
            url.append("&roles=").append(String.join(",", roles));
        }

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<UserListApiResponse> response = restTemplate.exchange(
                url.toString(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<UserListApiResponse>() {
                });

        return response.getBody();
    }

    public UserSelectListApiResponse getUserSelect(String keyword) {
        String urlTemplate = apiUrl + "/user/autocomplete?partialUsername={keyword}";

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<UserSelectListApiResponse> response = restTemplate.exchange(
                urlTemplate,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {},
                keyword
        );

        return response.getBody();
    }

    public boolean addUser(UserRequest user, InputStream profileImageInputStream, String fileName, String fileExtension) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("username", user.getUsername());
        body.add("firstName", user.getFirstName());
        body.add("lastName", user.getLastName());
        body.add("email", user.getEmail());
        body.add("role", user.getRole());
        body.add("isActive", Boolean.toString(user.isActive()));
        body.add("isNonLocked", Boolean.toString(user.isNotLocked()));
        if (profileImageInputStream != null) {
            // Convert InputStream to byte[]
            byte[] imageBytes = IOUtils.toByteArray(profileImageInputStream);

            // Add profile image
            ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return fileName + "." + fileExtension;  // Dynamic file name and extension
                }
            };
            body.add("profileImage", imageResource);
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Make the API call
        ResponseEntity<UserListResponse> response = restTemplate.postForEntity(
                apiUrl + "/user/add",
                requestEntity,
                UserListResponse.class
        );

        return response.getStatusCode() == HttpStatus.OK;
    }

    public boolean resetPassword(String token, String newPassword) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Create the payload
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("token", token);
        body.add("new_password", newPassword);

        // Package the payload and headers into HttpEntity
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        // Make the API call
        try {
            ResponseEntity<HttpResponse> response = restTemplate.postForEntity(
                    apiUrl + "/user/reset-password",
                    requestEntity,
                    HttpResponse.class  // Assuming HttpResponse is the class you use for wrapping the response
            );

            return response.getStatusCode() == HttpStatus.OK;

        } catch (HttpClientErrorException e) {
            // Handle client-side errors (4xx)
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                // Token may be invalid or expired
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                // Bad Request: Token or new password is missing
            }

            return false;

        } catch (HttpServerErrorException e) {
            // Handle server-side errors (5xx)
            return false;

        } catch (Exception e) {
            // Handle other exceptions
            return false;
        }
    }

    public void forgotPassword(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Tạo payload
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("email", email);

        // Đóng gói payload và headers vào HttpEntity
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<HttpResponse> response = restTemplate.postForEntity(
                apiUrl + "/user/forgot-password",
                requestEntity,
                HttpResponse.class
        );
        System.out.println(response.getBody());
    }

    public boolean verifyTokenResetPassword(String token) {
        // Instantiate RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Make the GET API call
            ResponseEntity<HttpResponse> response = restTemplate.getForEntity(
                    apiUrl + "/user/verify-reset-password-token/{token}",
                    HttpResponse.class, token // Pass token as a path variable
            );

            return response.getStatusCode() == HttpStatus.OK;

        } catch (HttpClientErrorException e) {
            return false;

        } catch (HttpServerErrorException e) {
            return false;

        } catch (Exception e) {
            return false;
        }
    }

    public boolean updateUser(String currentUsername, UserRequest user, InputStream profileImageInputStream, String fileName, String fileExtension) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("currentUsername", currentUsername);
        body.add("username", user.getUsername());
        body.add("firstName", user.getFirstName());
        body.add("lastName", user.getLastName());
        body.add("email", user.getEmail());
        body.add("role", user.getRole());
        body.add("isActive", Boolean.toString(user.isActive()));
        body.add("isNonLocked", Boolean.toString(user.isNotLocked()));

        // Check if profileImageInputStream is null
        if (profileImageInputStream != null) {
            // Convert InputStream to byte[]
            byte[] imageBytes = IOUtils.toByteArray(profileImageInputStream);

            // Add profile image
            ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return fileName + "." + fileExtension;  // Dynamic file name and extension
                }
            };
            body.add("profileImage", imageResource);
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Make the API call
        ResponseEntity<UserListResponse> response = restTemplate.postForEntity(
                apiUrl + "/user/update",
                requestEntity,
                UserListResponse.class
        );

        return response.getStatusCode() == HttpStatus.OK;
    }

    public boolean updateProfile(String currentUsername, UserRequest user, InputStream profileImageInputStream, String fileName, String fileExtension) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("currentUsername", currentUsername);
        body.add("username", user.getUsername());
        body.add("firstName", user.getFirstName());
        body.add("lastName", user.getLastName());
        body.add("oldPassword", user.getOldPassword());
        body.add("newPassword", user.getNewPassword());

        // Check if profileImageInputStream is null
        if (profileImageInputStream != null) {
            // Convert InputStream to byte[]
            byte[] imageBytes = IOUtils.toByteArray(profileImageInputStream);

            // Add profile image
            ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return fileName + "." + fileExtension;  // Dynamic file name and extension
                }
            };
            body.add("profileImage", imageResource);
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Make the API call
        ResponseEntity<UserListResponse> response = restTemplate.postForEntity(
                apiUrl + "/user/updateProfile",
                requestEntity,
                UserListResponse.class
        );

        return response.getStatusCode() == HttpStatus.OK;
    }


    public void deleteUsers(Long[] idUsers) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Long[]> entity = new HttpEntity<>(idUsers, headers);

        restTemplate.exchange(
                apiUrl + "/user/delete",
                HttpMethod.DELETE,
                entity,
                Void.class
        );
    }

    public void deleteUser(Long idUsers) {
        deleteUsers(new Long[]{idUsers});
    }

    public UserSingleApiResponse getUserByUsername(String username) {
        // Create a URL with the username parameter
        String url = apiUrl + "/user/find/" + username;

        // Create an empty HttpHeaders object
        HttpHeaders headers = new HttpHeaders();

        // Create an HttpEntity object with the headers
        HttpEntity<UserSingleApiResponse> entity = new HttpEntity<>(headers);

        // Use the RestTemplate to make the GET request
        ResponseEntity<UserSingleApiResponse> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );

        // Return the body of the response entity (i.e., the User details)
        return response.getBody();
    }

    public boolean verifyToken(String token) {
        HttpHeaders headers = new HttpHeaders();

        // Assuming your API expects the token as a query parameter.
        String verifyTokenUrl = apiUrl + "/user/verify-token?token=" + token;

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);

        try {
            // Make the API call
            ResponseEntity<String> response = restTemplate.exchange(
                    verifyTokenUrl,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            return response.getStatusCode() == HttpStatus.OK;
        } catch (HttpClientErrorException e) {
            // Handle client errors (e.g., 404 Not Found, 401 Unauthorized)
            System.out.println("Client error: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            // Handle server errors (e.g., 500 Internal Server Error)
            System.out.println("Server error: " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            // Handle other exceptions such as network errors
            System.out.println("An error occurred: " + e.getMessage());
        }

        return false;
    }
}

