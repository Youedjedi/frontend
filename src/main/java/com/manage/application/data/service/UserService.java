package com.manage.application.data.service;

import com.manage.application.data.model.PagedResponse;
import com.manage.application.data.model.User;
import com.manage.application.utils.AuthInterceptor;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

@Service
public class UserService {
    private final String apiUrl = "http://localhost:8082";  // Replace with your API URL
    RestTemplateBuilder builder = new RestTemplateBuilder();
    AuthenticationService authenticationService = new AuthenticationService();
    RestTemplate restTemplate = builder.additionalInterceptors(new AuthInterceptor(authenticationService)).build();

    public PagedResponse<User> getUsers(int page, int size, String sort, String direction, String startDate, String endDate, String username, Set<String> roles) {
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

        ResponseEntity<PagedResponse<User>> response = restTemplate.exchange(
                url.toString(),
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<PagedResponse<User>>() {});

        return response.getBody();
    }

    public boolean addUser(User user, InputStream profileImageInputStream, String fileName, String fileExtension) throws IOException {
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
        ResponseEntity<User> response = restTemplate.postForEntity(
                apiUrl + "/user/add",
                requestEntity,
                User.class
        );

        return response.getStatusCode() == HttpStatus.OK;
    }


    public boolean updateUser(String currentUsername, User user, InputStream profileImageInputStream, String fileName, String fileExtension) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("currentUsername", currentUsername);
        body.add("username", user.getUsername());
        body.add("firstName", user.getFirstName());
        body.add("lastName", user.getLastName());
        body.add("email", user.getEmail());
        body.add("role", user.getRole().getValue());
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
        ResponseEntity<User> response = restTemplate.postForEntity(
                apiUrl + "/user/update",
                requestEntity,
                User.class
        );

        return response.getStatusCode() == HttpStatus.OK;
    }

    public boolean updateProfile(String currentUsername, User user, InputStream profileImageInputStream, String fileName, String fileExtension) throws IOException {
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
        ResponseEntity<User> response = restTemplate.postForEntity(
                apiUrl + "/user/updateProfile",
                requestEntity,
                User.class
        );

        return response.getStatusCode() == HttpStatus.OK;
    }


    public void deleteUsers(String[] usernames) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String[]> entity = new HttpEntity<>(usernames, headers);

        restTemplate.exchange(
                apiUrl + "/user/delete",
                HttpMethod.DELETE,
                entity,
                Void.class
        );
    }

    public void deleteUser(String username) {
        deleteUsers(new String[]{username});
    }

    public User getUserByUsername(String username) {
        // Create a URL with the username parameter
        String url = apiUrl + "/user/find/" + username;

        // Create an empty HttpHeaders object
        HttpHeaders headers = new HttpHeaders();

        // Create an HttpEntity object with the headers
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // Use the RestTemplate to make the GET request
        ResponseEntity<User> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<User>() {}
        );

        // Return the body of the response entity (i.e., the User details)
        return response.getBody();
    }
}

