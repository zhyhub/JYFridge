package com.joyoungdevlibrary.utils.encryptdecryptutil;


import org.apaches.commons.codec.binary.Hex;

/* XXTea.java
* Author:       Ma Bingyao < andot@ujn.edu.cn > 
* Copyright:    CoolCode.CN 
* Version:      1.0 
* LastModified: 2006-05-11 
* This library is free.  You can redistribute it and/or modify it. 
* http://www.coolcode.cn/?p=169 
*/
public class XXTea {
    public static void main(String[] args) {
        try {
            //模拟服务器生成accessToken
            String userName = "15213603672";
            String passwd = "111111";
            //xxteaKey
            String xxteaKey = Encrypt.MD5(userName + passwd).substring(0, 16);
            //sessionKey
            //String sessionId = MakeUUID.getMyUUID();
            String sessionId = "5cbb51269a5843c29bfa517e450492d8";
            String sessionKey = Encrypt.MD5(sessionId);
            System.out.println("sessionKey=" + sessionKey);
            //accessToken
            //String accessToken = Hex.encodeHexString(XXTea.encrypt(sessionKey.getBytes(), xxteaKey.getBytes()));
            String accessToken = "243fe68343a7193c86ad1cd14d835021cc9b5975aff48ee175f4a75a8da65281abfc830b";
            System.out.println("accessToken=" + accessToken);

            //模拟客户端解密
            //accessToken2
            byte[] accessToken2 = Hex.decodeHex(accessToken.toCharArray());
            //解密后的sessionKey
            String sessionKey2 = new String((XXTea.decrypt(accessToken2, xxteaKey.getBytes())));
            System.out.println("解密后的sessionKey=" + sessionKey2);
            //dataKey
            String dataKey = "";
            for (int i = 0; i < sessionKey2.length(); i++) {
                if (i % 2 == 0) {
                    dataKey += sessionKey2.substring(i, i + 1);
                }
            }
            System.out.println("dataKey=" + dataKey);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Encrypt data with key.
     *
     * @param data
     * @param key
     * @return byte[]
     */
    public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        if (data.length == 0) {
            return data;
        }
        return toByteArray(encrypt(toIntArray(data, true), toIntArray(key, false)), false);
    }

    /**
     * Decrypt data with key.
     *
     * @param data
     * @param key
     * @return byte[]
     */
    public static byte[] decrypt(byte[] data, byte[] key) {
        if (data.length == 0) {
            return data;
        }
        return toByteArray(decrypt(toIntArray(data, false), toIntArray(key, false)), true);
    }

    /**
     * Encrypt data with key.
     *
     * @param v
     * @param k
     * @return
     */
    private static int[] encrypt(int[] v, int[] k) {
        int n = v.length - 1;
        if (n < 1) {
            return v;
        }
        if (k.length < 4) {
            int[] key = new int[4];
            System.arraycopy(k, 0, key, 0, k.length);
            k = key;
        }
        int z = v[n], y = v[0], delta = 0x9E3779B9, sum = 0, e;
        int p, q = 6 + 52 / (n + 1);
        while (q-- > 0) {
            sum = sum + delta;
            e = sum >>> 2 & 3;
            for (p = 0; p < n; p++) {
                y = v[p + 1];
                z = v[p] += (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (k[p & 3 ^ e] ^ z);
            }
            y = v[0];
            z = v[n] += (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (k[p & 3 ^ e] ^ z);
        }
        return v;
    }

    /**
     * Decrypt data with key.
     *
     * @param v
     * @param k
     * @return
     */
    private static int[] decrypt(int[] v, int[] k) {
        int n = v.length - 1;
        if (n < 1) {
            return v;
        }
        if (k.length < 4) {
            int[] key = new int[4];
            System.arraycopy(k, 0, key, 0, k.length);
            k = key;
        }
        int z = v[n], y = v[0], delta = 0x9E3779B9, sum, e;
        int p, q = 6 + 52 / (n + 1);
        sum = q * delta;
        while (sum != 0) {
            e = sum >>> 2 & 3;
            for (p = n; p > 0; p--) {
                z = v[p - 1];
                y = v[p] -= (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (k[p & 3 ^ e] ^ z);
            }
            z = v[n];
            y = v[0] -= (z >>> 5 ^ y << 2) + (y >>> 3 ^ z << 4) ^ (sum ^ y) + (k[p & 3 ^ e] ^ z);
            sum = sum - delta;
        }
        return v;
    }

    /**
     * Convert byte array to int array.
     *
     * @param data
     * @param includeLength
     * @return
     */
    private static int[] toIntArray(byte[] data, boolean includeLength) {
        int n = (((data.length & 3) == 0) ? (data.length >>> 2)
                : ((data.length >>> 2) + 1));
        int[] result;
        if (includeLength) {
            result = new int[n + 1];
            result[n] = data.length;
        } else {
            result = new int[n];
        }
        n = data.length;
        for (int i = 0; i < n; i++) {
            result[i >>> 2] |= (0x000000ff & data[i]) << ((i & 3) << 3);
        }
        return result;
    }

    /**
     * Convert int array to byte array.
     *
     * @param data
     * @param includeLength
     * @return
     */
    private static byte[] toByteArray(int[] data, boolean includeLength) {
        int n;
        if (includeLength) {
            n = data[data.length - 1];
        } else {
            n = data.length << 2;
        }

        byte[] result = new byte[n];
        for (int i = 0; i < n; i++) {
            result[i] = (byte) (data[i >>> 2] >>> ((i & 3) << 3));
        }
        return result;
    }
}