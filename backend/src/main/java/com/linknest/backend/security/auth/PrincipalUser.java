package com.linknest.backend.security.auth;

import java.io.Serializable;
import java.util.List;

public record PrincipalUser(Long id, String username, List<String> roles) implements Serializable {}
