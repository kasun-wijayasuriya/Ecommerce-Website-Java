# V2 - No Brute-Force Protection

## Description
Login endpoint allows unlimited failed attempts.

## Risk
Enables brute-force and credential stuffing attacks.

## Root Cause
No rate limiting or login attempt tracking.

## Fix
- Implement login attempt counter
- Add temporary lockout after threshold

## Status
Open