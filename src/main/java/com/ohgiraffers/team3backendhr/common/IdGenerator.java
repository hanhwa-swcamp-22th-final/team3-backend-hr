package com.ohgiraffers.team3backendhr.common;

import java.util.concurrent.ThreadLocalRandom;

public class IdGenerator {

    private IdGenerator() {}

    public static long generate() {
        long timestamp = System.currentTimeMillis();           // 13자리
        long random = ThreadLocalRandom.current().nextLong(1000); // 0~999
        return timestamp * 1000 + random;
    }
}
