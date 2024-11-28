package com.manage.application.service;

import com.manage.application.domain.request.post.PostRequest;
import com.manage.application.domain.response.post.PostSingleApiResponse;
import com.manage.application.domain.response.post.PostListApiResponse;
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
import java.util.stream.Collectors;

@Service
public class PostService {
    private final String apiUrl = "http://localhost:8080";
    RestTemplateBuilder builder = new RestTemplateBuilder();
    AuthenticationService authenticationService = new AuthenticationService();
    RestTemplate restTemplate = builder.additionalInterceptors(new AuthInterceptor(authenticationService)).build();


    public PostListApiResponse getPosts(String keyword, int page, int size) {
        String urlTemplate = apiUrl + "/post/list?keyword={keyword}&page={page}&size={size}";

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<PostListApiResponse> response = restTemplate.exchange(
                urlTemplate,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {},
                keyword,
                page,
                size
        );

        return response.getBody();
    }

    public PostSingleApiResponse addPost(PostRequest post, InputStream profileImageInputStream, String fileName, String fileExtension) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("title", post.getTitle());
        body.add("excerpt", post.getExcerpt());
        body.add("content", post.getContent());
        String editorIdsAsString = post.getEditorIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        body.add("editorIds", editorIdsAsString);

        if (profileImageInputStream != null) {
            // Convert InputStream to byte[]
            byte[] imageBytes = IOUtils.toByteArray(profileImageInputStream);

            // Add profile image
            ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return fileName + "." + fileExtension;
                }
            };
            body.add("thumbnailUrl", imageResource);
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Make the API call
        ResponseEntity<PostSingleApiResponse> response = restTemplate.postForEntity(
                apiUrl + "/post/create",
                requestEntity,
                PostSingleApiResponse.class
        );

        return response.getBody();
    }


}
