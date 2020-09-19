package com.github.fmcejudo.tracing.generator.builder;

import java.util.Random;

public final class IdGenerator {


     public static String generateId(final int bits) {
        if (bits % 32 != 0) {
            throw new RuntimeException("Number of bits don't match with valid length");
        }
        int iterations = bits / 32;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < iterations; i++) {
            builder.append(Integer.toHexString(new Random().nextInt(Integer.MAX_VALUE)));
        }
        return builder.toString();
    }
}
