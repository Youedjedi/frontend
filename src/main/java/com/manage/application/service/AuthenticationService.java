package com.manage.application.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.manage.application.domain.request.user.UserRequest;
import com.manage.application.domain.response.user.UserListResponse;
import com.manage.application.domain.response.user.UserSingleApiResponse;
import com.manage.application.domain.response.user.UserSingleResponse;
import com.manage.application.utils.AuthInterceptor;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;
import org.json.JSONObject;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthenticationService {
    private static final String API_URL = "http://localhost:8080"; // Replace with your API URL
    private String token;
    private String loggedInUsername;
    RestTemplateBuilder builder = new RestTemplateBuilder();
    RestTemplate restTemplate = builder.additionalInterceptors(new AuthInterceptor(this)).build();


    public ResponseEntity<UserSingleApiResponse> login(UserRequest user) {
        String url = API_URL + "/user/login";
        return restTemplate.postForEntity(url, user, UserSingleApiResponse.class);
    }

    public boolean register(UserRequest user) {
        String url = API_URL + "/user/register";
        ResponseEntity<UserListResponse> response = restTemplate.postForEntity(url, user, UserListResponse.class);
        return (response.getStatusCode().is2xxSuccessful());
    }

    public void logOut() {
        this.token = null;
        this.loggedInUsername = null;
        getSession().removeAttribute("user");
        getSession().removeAttribute("token");
    }

    public void saveToken(String token) {
        this.token = token;
        getSession().setAttribute("token", token);
    }

    public void addUserToLocalCache(UserSingleResponse user) {
        String userJson = new JSONObject(user).toString(); // Assuming User object can be converted to JSON directly
        getSession().setAttribute("user", userJson);
    }

    public UserSingleResponse getUserFromLocalCache() {
        String userJson = (String) getSession().getAttribute("user");
        if (userJson != null && !userJson.isEmpty()) {
            // Convert the JSON string back to a User object
            JSONObject jsonObject = new JSONObject(userJson);
            UserSingleResponse user = new UserSingleResponse();
            // Assuming User class has setters for all properties
            user.setUsername(jsonObject.getString("username"));
            // Add other fields similarly
            return user;
        }
        return null;
    }

    public void loadToken() {
        this.token = (String) getSession().getAttribute("token");
    }

    private WrappedSession getSession() {
        return VaadinSession.getCurrent().getSession();
    }

    public String getToken() {
        return this.token;
    }

    public boolean isUserLoggedIn() {
        loadToken();
        if (this.token != null && !this.token.isEmpty()) {
            DecodedJWT jwt = JWT.decode(this.token);
            if (jwt.getSubject() != null && !jwt.getSubject().isEmpty()) {
                try {
                    // Sử dụng thuật toán HMAC512
                    Algorithm algorithm = Algorithm.HMAC512("[a-zA-Z0-9._]^+$Guidelines89797987forAlphabeticalArraNumeralsandOtherSymbo$");
                    JWTVerifier verifier = JWT.require(algorithm)
                            .withSubject(jwt.getSubject())
                            .build();
                    verifier.verify(this.token);
                    this.loggedInUsername = jwt.getSubject();
                    return true;
                } catch (JWTVerificationException exception) {
                    return false;
                }
            }
        }
        this.logOut();
        return false;
    }



}
