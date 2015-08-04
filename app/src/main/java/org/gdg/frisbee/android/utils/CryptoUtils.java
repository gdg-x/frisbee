/*
 * Copyright 2014 The GDG Frisbee Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gdg.frisbee.android.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created with IntelliJ IDEA.
 * User: maui
 * Date: 02.04.14
 * Time: 21:00
 * To change this template use File | Settings | File Templates.
 */
public class CryptoUtils {

    private CryptoUtils() {

    }
    
    public static String encrypt(@NonNull String seed, @NonNull String cleartext) throws Exception {
        byte[] rawKey = getRawKey(seed.getBytes());
        byte[] result = encrypt(rawKey, cleartext.getBytes());
        return toHex(result);
    }

    @NonNull
    public static String decrypt(@NonNull String seed, @NonNull String encrypted) throws Exception {
        byte[] rawKey = getRawKey(seed.getBytes());
        byte[] enc = toByte(encrypted);
        byte[] result = decrypt(rawKey, enc);
        return new String(result);
    }

    private static byte[] getRawKey(@NonNull byte[] key) throws Exception {
        SecretKey skey = new SecretKeySpec(key, "AES");
        return skey.getEncoded();
    }


    private static byte[] encrypt(@NonNull byte[] raw, @NonNull byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        return cipher.doFinal(clear);
    }

    private static byte[] decrypt(@NonNull byte[] raw, @NonNull byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        return cipher.doFinal(encrypted);
    }

    public static String toHex(@NonNull String txt) {
        return toHex(txt.getBytes());
    }
    @NonNull
    public static String fromHex(@NonNull String hex) {
        return new String(toByte(hex));
    }

    @NonNull
    public static byte[] toByte(@NonNull String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        }
        return result;
    }

    public static String toHex(@Nullable byte[] buf) {
        if (buf == null) {
            return "";
        }
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }
    private static final String HEX = "0123456789ABCDEF";
    private static void appendHex(@NonNull StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }
}
