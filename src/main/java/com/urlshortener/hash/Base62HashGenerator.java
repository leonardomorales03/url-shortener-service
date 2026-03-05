package com.urlshortener.hash;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Implementation of HashGenerator that uses MD5 hashing with Base62 encoding.
 * Generates 7-character short codes using alphanumeric characters (a-z, A-Z, 0-9).
 */
@Component
public class Base62HashGenerator implements HashGenerator {
    private static final String BASE62_CHARS = 
        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int SHORT_CODE_LENGTH = 7;
    private final SecureRandom random = new SecureRandom();
    
    @Override
    public String generateShortCode(String longUrl) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(longUrl.getBytes(StandardCharsets.UTF_8));
            String encoded = encodeBase62(hash);
            
            // Ensure we have enough characters, pad if necessary
            if (encoded.length() < SHORT_CODE_LENGTH) {
                encoded = padToLength(encoded, SHORT_CODE_LENGTH);
            }
            
            return encoded.substring(0, SHORT_CODE_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            // Fallback to random code if MD5 is not available
            return generateRandomCode();
        }
    }
    
    @Override
    public String generateRandomCode() {
        StringBuilder code = new StringBuilder(SHORT_CODE_LENGTH);
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            code.append(BASE62_CHARS.charAt(random.nextInt(BASE62_CHARS.length())));
        }
        return code.toString();
    }
    
    /**
     * Encodes a byte array to Base62 string.
     * 
     * @param input the byte array to encode
     * @return Base62 encoded string
     */
    private String encodeBase62(byte[] input) {
        BigInteger num = new BigInteger(1, input);
        StringBuilder encoded = new StringBuilder();
        
        while (num.compareTo(BigInteger.ZERO) > 0) {
            int remainder = num.mod(BigInteger.valueOf(62)).intValue();
            encoded.insert(0, BASE62_CHARS.charAt(remainder));
            num = num.divide(BigInteger.valueOf(62));
        }
        
        return encoded.length() > 0 ? encoded.toString() : "0";
    }
    
    /**
     * Pads a string to the specified length using Base62 characters.
     * 
     * @param str the string to pad
     * @param length the target length
     * @return padded string
     */
    private String padToLength(String str, int length) {
        StringBuilder padded = new StringBuilder(str);
        while (padded.length() < length) {
            padded.insert(0, '0');
        }
        return padded.toString();
    }
}
