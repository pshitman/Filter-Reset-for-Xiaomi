package com.example.filterreset;

import java.security.MessageDigest;

/** Port of getpwd() from xiaomi-air-purifier-filter-reset (src/index.ts). */
public final class PwdCalc {
    private PwdCalc() {}

    public static byte[] getPwd(byte[] uid) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-1").digest(uid);
            int b0 = h[0] & 0xff;
            return new byte[] {
                h[b0 % 20], h[(b0 + 5) % 20], h[(b0 + 13) % 20], h[(b0 + 17) % 20]
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02X", x & 0xff));
        return sb.toString();
    }

    public static byte[] fromHex(String s) {
        s = s.replace(":", "");
        byte[] r = new byte[s.length() / 2];
        for (int i = 0; i < r.length; i++) r[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
        return r;
    }
}
