package com.joyoungdevlibrary.utils;


import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

import javax.crypto.Cipher;

public class RSAUtil {


	public static RSAPublicKey getPublicKey(String modulus, String exponent) {
		try {
			BigInteger b1 = new BigInteger(modulus,16);
			BigInteger b2 = new BigInteger(exponent,16);
			Security.addProvider(new BouncyCastleProvider());
			KeyFactory keyFactory = KeyFactory.getInstance("RSA","BC");
			RSAPublicKeySpec keySpec = new RSAPublicKeySpec(b1, b2);
			return (RSAPublicKey) keyFactory.generatePublic(keySpec);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static RSAPrivateKey getPrivateKey(String modulus, String exponent) {
		try {
			BigInteger b1 = new BigInteger(modulus,16);
			BigInteger b2 = new BigInteger(exponent,16);
			Security.addProvider(new BouncyCastleProvider());  
			KeyFactory keyFactory = KeyFactory.getInstance("RSA","BC");
			RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(b1, b2);
			return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	public static String encryptByPublicKey(String data, RSAPublicKey publicKey)
			throws Exception {
		Security.addProvider(new BouncyCastleProvider());  
		Cipher cipher = Cipher.getInstance("RSA","BC");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		
		int key_len = publicKey.getModulus().bitLength() / 8;
		
		String[] datas = splitString(data, key_len - 11);
		String mi = "";
		
		for (String s : datas) {
			mi += bcd2Str(cipher.doFinal(s.getBytes()));
		}
		return mi;
	}


	public static String decryptByPrivateKey(String data,
			RSAPrivateKey privateKey) throws Exception {
		
		Cipher cipher = Cipher.getInstance("RSA","BC");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		
		int key_len = privateKey.getModulus().bitLength() / 8;
		byte[] bytes = data.getBytes();
		byte[] bcd = ASCII_To_BCD(bytes, bytes.length);
		System.err.println(bcd.length);
		
		String ming = "";
		byte[][] arrays = splitArray(bcd, key_len);
		for (byte[] arr : arrays) {
			ming += new String(cipher.doFinal(arr));
		}
		return ming.trim();
	}


	public static byte[] ASCII_To_BCD(byte[] ascii, int asc_len) {
		byte[] bcd = new byte[asc_len / 2];
		int j = 0;
		for (int i = 0; i < (asc_len + 1) / 2; i++) {
			bcd[i] = asc_to_bcd(ascii[j++]);
			bcd[i] = (byte) (((j >= asc_len) ? 0x00 : asc_to_bcd(ascii[j++])) + (bcd[i] << 4));
		}
		return bcd;
	}

	public static byte asc_to_bcd(byte asc) {
		byte bcd;

		if ((asc >= '0') && (asc <= '9'))
			bcd = (byte) (asc - '0');
		else if ((asc >= 'A') && (asc <= 'F'))
			bcd = (byte) (asc - 'A' + 10);
		else if ((asc >= 'a') && (asc <= 'f'))
			bcd = (byte) (asc - 'a' + 10);
		else
			bcd = (byte) (asc - 48);
		return bcd;
	}

	public static String bcd2Str(byte[] bytes) {
		char temp[] = new char[bytes.length * 2], val;

		for (int i = 0; i < bytes.length; i++) {
			val = (char) (((bytes[i] & 0xf0) >> 4) & 0x0f);
			temp[i * 2] = (char) (val > 9 ? val + 'A' - 10 : val + '0');

			val = (char) (bytes[i] & 0x0f);
			temp[i * 2 + 1] = (char) (val > 9 ? val + 'A' - 10 : val + '0');
		}
		return new String(temp);
	}


	public static String[] splitString(String string, int len) {
		int x = string.length() / len;
		int y = string.length() % len;
		int z = 0;
		if (y != 0) {
			z = 1;
		}
		String[] strings = new String[x + z];
		String str = "";
		for (int i = 0; i < x + z; i++) {
			if (i == x + z - 1 && y != 0) {
				str = string.substring(i * len, i * len + y);
			} else {
				str = string.substring(i * len, i * len + len);
			}
			strings[i] = str;
		}
		return strings;
	}


	public static byte[][] splitArray(byte[] data, int len) {
		int x = data.length / len;
		int y = data.length % len;
		int z = 0;
		if (y != 0) {
			z = 1;
		}
		byte[][] arrays = new byte[x + z][];
		byte[] arr;
		for (int i = 0; i < x + z; i++) {
			arr = new byte[len];
			if (i == x + z - 1 && y != 0) {
				System.arraycopy(data, i * len, arr, 0, y);
			} else {
				System.arraycopy(data, i * len, arr, 0, len);
			}
			arrays[i] = arr;
		}
		return arrays;
	}


	public static void main(String[] args) throws Exception {

		String N = "8A63B6EE62EF2C5EADF62E5E75F5C24C11A7E267308747F26A74DF5712299F984E045AD160858788CCFF9672493AB9E1379875781BB25C54EA44CB4031D95A223718E19A6E180AA929CBF5CA7E835BA9C9ED89F74EA05240E0DBC5C5EEDD22DEEAC5520829C081A34191A5619B08B57F18578D285B516CD97E2C658E72A1044D";

		String D = "1F5CA1A43C7F7F4AA599D8C047733E6906C74923A177C6F244F5DF7758025ECCA141936347C07B0AA18A7A89C3D6AF313EEC2A12E213F5A0B8C6865931F2D3DDFDCE2A5FA327D30BBACA37A02D123C103714361C6E49912DE663F3FBBF47A7071EABE64DA43415AC5C09E074343418B7AE5BCCB626D23353F5F7D2507709A181";
		String E = "10001";

		RSAPrivateKey privateKey = RSAUtil.getPrivateKey(N, E);
		RSAPublicKey publicKey = RSAUtil.getPublicKey(N, D);

		 System.out.println("N:"+N.length()+":"+N);
		 System.out.println("D:"+D.length()+":"+D);
		 System.out.println("E:"+E);



		String test = "xxteakey";

		String encode = RSAUtil.encryptByPublicKey(test, publicKey);

		System.out.println("公钥加密："+encode);

		String decode = RSAUtil.decryptByPrivateKey(encode, privateKey);
		System.out.println("私钥解密："+decode);
		
		//公钥私钥生成
//		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
//		keyPairGen.initialize(1024);
//		KeyPair keyPair = keyPairGen.generateKeyPair();
//		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
//		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
//		
//		System.out.println(publicKey.getModulus().toString(16).toUpperCase());
//		System.out.println(privateKey.getPrivateExponent().toString(16).toUpperCase());
//		
//		String E = "10001";
	}
}