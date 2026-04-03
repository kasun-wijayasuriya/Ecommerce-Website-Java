package com.ecommerce.service;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Service;

@Service
public class HtmlSanitizerService {

    private static final PolicyFactory POLICY = Sanitizers.FORMATTING
            .and(Sanitizers.LINKS)
            .and(Sanitizers.BLOCKS)
            .and(Sanitizers.IMAGES);

    public String sanitize(String input) {
        if (input == null) return null;
        return POLICY.sanitize(input);
    }
}
