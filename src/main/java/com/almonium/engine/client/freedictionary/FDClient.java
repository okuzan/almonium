package com.almonium.engine.client.freedictionary;

import static lombok.AccessLevel.PRIVATE;

import com.almonium.engine.client.AbstractClient;
import com.almonium.engine.client.Client;
import com.almonium.engine.client.freedictionary.dto.FDEntry;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Client
@AllArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
@Slf4j
public class FDClient extends AbstractClient {
    static String BASE_URL = "https://api.dictionaryapi.dev/api/v2";
    static String ENDPOINT = "/entries";
    static String LANG_CODE = "/en/";
    RestTemplate restTemplate;

    public ResponseEntity<List<FDEntry>> request(String word) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return restTemplate.exchange(
                BASE_URL + ENDPOINT + LANG_CODE + word,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {});
    }
}
