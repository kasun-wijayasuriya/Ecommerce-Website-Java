# V3 - JWT Exposure in Client Storage

## Description
JWT tokens are stored in browser-accessible storage.

## Risk
Tokens can be stolen via XSS or malicious scripts.

## Root Cause
Use of localStorage/sessionStorage for token persistence.

## Fix
- Remove persistent storage
- Use safer in-memory handling

## Status
Fixed