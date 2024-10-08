package com.almonium.config.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Custom PasswordEncoder class extending BCryptPasswordEncoder.
 * This class is kept in a separate file to avoid circular dependency problems.
 */
@Component
public class PasswordEncoder extends BCryptPasswordEncoder {}
