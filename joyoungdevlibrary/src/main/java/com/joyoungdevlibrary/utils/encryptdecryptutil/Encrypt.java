package com.joyoungdevlibrary.utils.encryptdecryptutil;


import org.apaches.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Encrypt {
	public static String MD5(byte[] value) {
		try {
			byte[] encs;
			encs = Md5Coder.encrypt(value);
			return Hex.encodeHexString(encs).toUpperCase();
		} catch (Exception ex) {
			return "";
		}
	}

	public static String MD5(String data) {
		try {
			return MD5(data.getBytes("utf-8"))
					.toUpperCase();
		} catch (Exception ex) {
			return "";
		}
	}
	
	public static String MD54AfterSale(String data) {
		try {
			return MD5(data.getBytes())
					.toUpperCase();
		} catch (Exception ex) {
			return "";
		}
	}

	public static String MD5(String data, String salt) {
		try {
			byte[] dataBuffer = data.getBytes("utf-8");
			byte[] saltBuffer = salt.getBytes("utf-8");
			return MD5(dataBuffer, saltBuffer);
		} catch (Exception ex) {
			return "";
		}
	}

	public static String MD5(byte[] dataBuffer, String salt) {
		try {
			byte[] saltBuffer = salt.getBytes("utf-8");
			return MD5(dataBuffer, saltBuffer);
		} catch (Exception ex) {
			return "";
		}
	}

	public static String MD5(byte[] dataBuffer, byte[] saltBuffer) {
		try {
			byte[] encs;
			if ((saltBuffer == null) || saltBuffer.length == 0) {
				encs = Md5Coder.encrypt(saltBuffer);
			} else {
				encs = Md5Coder.encrypt(dataBuffer, saltBuffer, true, 0,
						dataBuffer.length);
			}
			return Hex.encodeHexString(encs).toUpperCase();
		} catch (Exception ex) {
			return "";
		}
	}

	public static String encryptExOK(String keys, String data) {
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
			int blockSize = cipher.getBlockSize();
			byte[] dataBytes = data.getBytes();
			int plaintextLength = dataBytes.length;
			if (plaintextLength % blockSize != 0) {
				plaintextLength = plaintextLength
						+ (blockSize - (plaintextLength % blockSize));
			}
			SecretKeySpec secretKeySpec = new SecretKeySpec(
					keys.getBytes("utf-8"), "AES");
			// IvParameterSpec paramSpec = new IvParameterSpec(AES_IV);

			byte[] plaintext = new byte[plaintextLength];
			for (int i = 0; i < plaintextLength; i++)
				plaintext[i] = 0;//
			System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);// , ivspec);
			byte[] encrypted = cipher.doFinal(plaintext);

			String originalString = toHex(encrypted);
			return originalString;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }
    
    private static void appendHex(StringBuffer sb, byte b) {
        final String HEX = "0123456789ABCDEF";
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }
    
	public static String getDataKey(String key)
	{
		String str = "";
		for(int i=0,length=key.length();i<length;i+=2)
		{
			str+=key.substring(i,i+1);
		}
		return str;
	}
}
