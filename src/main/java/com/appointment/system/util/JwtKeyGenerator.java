package com.appointment.system.util;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Base64;

public class JwtKeyGenerator {
    public static void main(String[] args) {
        // Generate secure key for HS512
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());

        System.out.println("Generated JWT Secret Key (Base64):");
        System.out.println(base64Key);
        System.out.println("\nKey length: " + key.getEncoded().length * 8 + " bits");
    }
}
