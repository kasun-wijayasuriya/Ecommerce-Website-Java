package com.ecommerce.util;

import com.ecommerce.exception.BadRequestException;

import java.util.Set;

public class SortValidator {

    public static void validateSortField(String sortBy, Set<String> allowedFields) {
        if (!allowedFields.contains(sortBy)) {
            throw new BadRequestException("Invalid sort field: " + sortBy + ". Allowed: " + allowedFields);
        }
    }

    public static int enforceMaxPageSize(int requestedSize, int maxSize) {
        return Math.min(requestedSize, maxSize);
    }
}
