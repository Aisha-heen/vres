package com.vres.generator;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.UUID;

@Service
public class CodeGeneratorService {

    private static final String CHARACTERS = "23456789ABCDEFGHJKMNPQRSTUVWXYZ";
    private static final int CODE_LENGTH = 4;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateUniqueCode() {
        long seed = System.nanoTime() ^ UUID.randomUUID().getMostSignificantBits();
        secureRandom.setSeed(seed);

        StringBuilder codeBuilder = new StringBuilder("VRES-");

        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = secureRandom.nextInt(CHARACTERS.length());
            codeBuilder.append(CHARACTERS.charAt(randomIndex));
        }

        return codeBuilder.toString(); 
    }
}