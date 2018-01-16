package com.joyoungdevlibrary.utils.encryptdecryptutil;

import java.security.MessageDigest;

/**
 * Md5加密算法
 * 说明：包含直接md5加密，salt值加密，二次加密。
 *
 */
public abstract class Md5Coder {

	/**
	 * MD5加密：直接加密
	 *
	 * @param data
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data) throws Exception {
		return encrypt(data, null, false);
	}

	/**
	 * MD5加密：定位直接加密
	 *
	 * @param data
	 * @param offset
	 * @param length
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data,int offset,int length) throws Exception {
		return encrypt(data, null, false,offset,length);
	}

	/**
	 * MD5加密：是否做偏移加密
	 * @param data
	 * @param isTwice
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, boolean isTwice) throws Exception {
		return encrypt(data, null, isTwice);
	}


	/**
	 * MD5加密：salt加密
	 * @param data
	 * @param salt
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, byte[] salt) throws Exception {
		return encrypt(data, salt, false);
	}

	/**
	 * MD5加密：是否做偏移salt加密
	 * @param data
	 * @param salt
	 * @param isTwice
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, byte[] salt, boolean isTwice)
			throws Exception {
		return encrypt(data, salt, isTwice,0,data.length);
	}

	/**
	 * @param data
	 * @param salt
	 * @param isTwice
	 * @param offset
	 * @param length
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, byte[] salt, boolean isTwice,int offset,int length)
			throws Exception {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(data,offset,length);
		if ((salt != null) && (salt.length != 0)) {
			md5.update(salt);
		}
		byte[] md5Bytes = md5.digest();
		if (isTwice) {
			md5.update(md5Bytes, 8, 8);
			byte[] md52 = md5.digest();
			System.arraycopy(md52, 8, md5Bytes, 8, 8);
		}
		return md5Bytes;
	}
}
