package com.rober.bookshop.repository.httpclient;

import com.rober.bookshop.model.response.OutboundUserResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "outbound-user-client", url = "https://www.googleapis.com")
public interface OutboundUserClient {
    @GetMapping(value = "/oauth2/v1/userinfo")
    OutboundUserResponseDTO getUserInfo(@RequestParam("alt") String alt,
                                        @RequestParam("access_token") String accessToken);
}
