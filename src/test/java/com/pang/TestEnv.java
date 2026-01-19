package com.pang;

public class TestEnv {
    public static void main(String[] args) {
        System.out.println("OSS_ACCESS_KEY_ID: " + System.getenv("OSS_ACCESS_KEY_ID"));
        System.out.println("OSS_ACCESS_KEY_SECRET: " + System.getenv("OSS_ACCESS_KEY_SECRET"));
    }
}
