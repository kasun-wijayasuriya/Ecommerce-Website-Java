package com.ecommerce.service;

import org.passay.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PasswordValidatorService {

    private final org.passay.PasswordValidator validator;

    public PasswordValidatorService() {
        validator = new org.passay.PasswordValidator(
                new LengthRule(8, 128),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1),
                new WhitespaceRule()
        );
    }

    public RuleResult validate(String password) {
        return validator.validate(new PasswordData(password));
    }

    public List<String> getErrors(RuleResult result) {
        return validator.getMessages(result);
    }
}
