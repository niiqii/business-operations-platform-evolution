package com.bop.workorder.config;

import com.bop.workorder.common.TraceIdFilter;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(ExternalServiceProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeoutMs());
        factory.setReadTimeout(properties.getReadTimeoutMs());

        RestTemplate restTemplate = new RestTemplate(factory);
        ClientHttpRequestInterceptor traceInterceptor = (request, body, execution) -> {
            String traceId = MDC.get(TraceIdFilter.TRACE_ID_KEY);
            if (StringUtils.hasText(traceId)) {
                request.getHeaders().set(TraceIdFilter.TRACE_ID_KEY, traceId);
            }
            return execution.execute(request, body);
        };
        restTemplate.setInterceptors(Collections.singletonList(traceInterceptor));
        return restTemplate;
    }
}
