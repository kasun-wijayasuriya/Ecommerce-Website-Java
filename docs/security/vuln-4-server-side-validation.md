# V4 - Missing Server-Side Validation

## Description
Profile update endpoint accepts invalid input.

## Risk
Allows malicious or malformed data to be stored.

## Root Cause
Missing validation annotations and enforcement.

## Fix
- Add validation annotations
- Enforce validation in controllers

## Status
Open