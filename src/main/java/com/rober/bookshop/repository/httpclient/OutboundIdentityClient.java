package com.rober.bookshop.repository.httpclient;

import com.rober.bookshop.model.request.ExchangeTokenRequestDTO;
import com.rober.bookshop.model.response.ExchangeTokenResponseDTO;
import feign.QueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "outbound-identity", url = "https://oauth2.googleapis.com")
public interface OutboundIdentityClient {
    @PostMapping(value = "/token", produces = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ExchangeTokenResponseDTO exchangeToken(@QueryMap ExchangeTokenRequestDTO request);
}
