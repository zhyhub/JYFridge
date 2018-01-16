package com.joyoungdevlibrary.utils.encryptdecryptutil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESCoder {  
	public static final String TAG = "AESUtils";
	private static final byte[] AES_IV = { 0x15, (byte) 0xFF, 0x01, 0x00, 0x34,  
        (byte) 0xAB, 0x4C, (byte) 0xD3, 0x55, (byte) 0xFE, (byte) 0xA1,  
        0x22, 0x08, 0x4F, 0x13, 0x07 }; 
	
	/**
	 * AES加密
	 * @param seed 加密key
	 * @param clearText 内容
	 * @return String
	 */
    public static String encrypt(String seed, String clearText) {
        // Log.d(TAG, "加密前的seed=" + seed + ",内容为:" + clearText);
        byte[] result = null;
        try {
            //byte[] rawkey = getRawKey(seed.getBytes());
            byte[] rawkey = seed.getBytes();
            int x=clearText.length()+16;
            byte []t=clearText.getBytes();
            byte []src=new byte [clearText.length()+16];
            for(int i=0;i<x;i++){
            	src[i]=0;
            }
            System.arraycopy(t, 0, src, 0, t.length);
            int nSrcLen=(t.length/16)*16;
            int nx=t.length%16;
            if( nx>0){
            	nSrcLen+=16;
            }
            result = encrypt(rawkey, src,0,nSrcLen);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String content = toHex(result);
        // Log.d(TAG, "加密后的内容为:" + content);
        return content;

    }

    public static String decrypt(String seed, String encrypted) {
        // Log.d(TAG, "解密前的seed=" + seed + ",内容为:" + encrypted);
        try {
            //byte[] rawKey = getRawKey(seed.getBytes());
        	byte[] rawKey = seed.getBytes();
            byte[] enc = toByte(encrypted);
            byte[] result = decrypt(rawKey, enc);
            String coentn = new String(result);
            // Log.d(TAG, "解密后的内容为:" + coentn);
            return coentn;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
    public static byte[] initSecretKey() {  
        //返回生成指定算法的秘密密钥的 KeyGenerator 对象  
        KeyGenerator kg = null;
        try {  
            kg = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();  
            return new byte[0];  
        }  
        //初始化此密钥生成器，使其具有确定的密钥大小  
        //AES 要求密钥长度为 128  
        kg.init(128);  
        //生成一个密钥  
        SecretKey secretKey = kg.generateKey();
        return secretKey.getEncoded();  
    } 
    private static byte[] getRawKey2(byte[] seed){      
		try{	
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			 MessageDigest digest = MessageDigest.getInstance("MD5");
	            digest.update( seed);    
	         
		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
		sr.setSeed(seed);  
	      //     AlgorithmParameterSpec s=new AlgorithmParameterSpec();

		kgen.init(128, sr); // 192 and 256 bits may not be available       
		SecretKey skey = kgen.generateKey();
		byte[] raw = skey.getEncoded();      
		return raw;   
		 }catch(Exception ex)
			{
				
			}
			return null;
	}      
    public static String encryptEx(String keys, String data)  {
        try {         
        	 String cipher_algorithm = "AES/ECB/PKCS5Padding";//AES/CBC/NoPadding
        	 String aes_key_algorithm = "AES";
        	 String iv = "fedcba9876543210";
        	 MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update( keys.getBytes("utf-8"));      
            SecretKeySpec keyspec = new SecretKeySpec(getRawKey2(keys.getBytes()), aes_key_algorithm);
            
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            int blockSize = cipher.getBlockSize();                                             
                                                                                               
            byte[] dataBytes = data.getBytes();                                                
            int plaintextLength = dataBytes.length;                                            
            if (plaintextLength % blockSize != 0) {                                            
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }                                                                                  
             
            byte []keybyte=new byte[16];
            for( int i=0;i<16;i++)
            	keybyte[i]=0;
            System.arraycopy(keys.getBytes(), 0, keybyte, 0, keys.length());
            
            SecretKeySpec secretKeySpec = new SecretKeySpec(keybyte, "AES");
         //   IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());  
          
            byte[] plaintext = new byte[plaintextLength];
            for( int i=0;i<plaintextLength;i++)
            	plaintext[i]=0;
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
           
            cipher.init(Cipher.ENCRYPT_MODE, keyspec);
            byte[] encrypted = cipher.doFinal(plaintext);       
            
            String originalString =toHex(encrypted);
            return originalString;                                                                                                 
                                       
                                                                                               
        } catch (Exception e) {
            e.printStackTrace();                                                               
            return null;                                                                       
        }                                                                                      
    }                       
    /*
     * 需要密碼是16個字符
     */
    public static String encryptExOK(String keys, String data)  {
        try {         
        	
            
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            int blockSize = cipher.getBlockSize();                                                                                                                                    
            byte[] dataBytes = data.getBytes();                                                
            int plaintextLength = dataBytes.length;                                            
            if (plaintextLength % blockSize != 0) {                                            
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }                                                                                      
            SecretKeySpec secretKeySpec = new SecretKeySpec(keys.getBytes("utf-8"), "AES");
      //      IvParameterSpec paramSpec = new IvParameterSpec(AES_IV);  
            
            byte[] plaintext = new byte[plaintextLength];
            for( int i=0;i<plaintextLength;i++)
            	plaintext[i]=0;//
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);//, ivspec);
            byte[] encrypted = cipher.doFinal(plaintext);       
            
            String originalString =toHex(encrypted);
            return originalString;                                                                                                 
                                       
                                                                                               
        } catch (Exception e) {
            e.printStackTrace();                                                               
            return null;                                                                       
        }                                                                                      
    }     
    
    /*
     * 需要密碼是16個字符
     */
    public static byte[] encryptExOK(String keys, byte[] dataBytes)  {
        try {         
        	
            
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            int blockSize = cipher.getBlockSize();                                                                                                                                    
            ///byte[] dataBytes = data.getBytes();                                                
            int plaintextLength = dataBytes.length;                                            
            if (plaintextLength % blockSize != 0) {                                            
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }                                                                                      
            SecretKeySpec secretKeySpec = new SecretKeySpec(keys.getBytes("utf-8"), "AES");
      //      IvParameterSpec paramSpec = new IvParameterSpec(AES_IV);  
            
            byte[] plaintext = new byte[plaintextLength];
            for( int i=0;i<plaintextLength;i++)
            	plaintext[i]=0;//
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);//, ivspec);
            byte[] encrypted = cipher.doFinal(plaintext);       
            
            return encrypted;
           // String originalString =toHex(encrypted);                    
           // return originalString;                                                                                                 
                                       
                                                                                               
        } catch (Exception e) {
            e.printStackTrace();                                                               
            return null;                                                                       
        }                                                                                      
    }     
    
    /*
     * 需要密碼是16個字符
     */
    public static byte[] desEncryptOK(String keys, byte[] dataBytes)  {
        try {         
        	
            
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            int blockSize = cipher.getBlockSize();                                                                                                                                    
           // byte[] dataBytes = data.getBytes();                                                
            int plaintextLength = dataBytes.length;                                            
            if (plaintextLength % blockSize != 0) {                                            
                plaintextLength = plaintextLength + (blockSize - (plaintextLength % blockSize));
            }                                                                                      
            SecretKeySpec secretKeySpec = new SecretKeySpec(keys.getBytes("utf-8"), "AES");
      //      IvParameterSpec paramSpec = new IvParameterSpec(AES_IV);  
            
            byte[] plaintext = new byte[plaintextLength];
            for( int i=0;i<plaintextLength;i++)
            	plaintext[i]=0;//
            System.arraycopy(dataBytes, 0, plaintext, 0, dataBytes.length);
            //Cipher.DECRYPT_MODE
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);//, ivspec);
            byte[] encrypted = cipher.doFinal(plaintext);       
            
           // String originalString =toHex(encrypted);                    
           // return new String(encrypted);                                                                                                 
             return     encrypted;                       
                                                                                               
        } catch (Exception e) {
            e.printStackTrace();                                                               
            return null;                                                                       
        }                                                                                      
    }    
    
    public static String desEncrypt(String data) throws Exception {
        try                                                                                    
        {                                                                                      
            String key = "123";
            String iv = "1234567812345678";
          
            
            byte[] encrypted1 =data.getBytes();                      
                                                                                               
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
            IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
                                                                                               
            cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
                                                                                               
            byte[] original = cipher.doFinal(encrypted1);                                      
            String originalString = new String(original);
            return originalString;                                                             
        }                                                                                      
        catch (Exception e) {
            e.printStackTrace();                                                               
            return null;                                                                       
        }                                                                                      
    }                                                                                          

    private static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        //AlgorithmParameterSpe pm=new AlgorithmParameterSpe();
      //  byte ivi[]=new byte[32];
        IvParameterSpec paramSpec = new IvParameterSpec(AES_IV);
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        sr.setSeed(seed);
        //kgen.init(128, pm);
        SecretKey sKey = kgen.generateKey();
        byte[] raw = sKey.getEncoded();

        return raw;
    }
    /*
     * .跟C约定相同的AES算法，AES实现有四种，像CBC/ECB/CFB/OFB
    算法/模式/填充                    16字节加密后数据长度         不满16字节加密后长度
    AES/CBC/NoPadding             16                         不支持
    AES/CBC/PKCS5Padding          32                         16
    AES/CBC/ISO10126Padding       32                          16
    AES/CFB/NoPadding             16                          原始数据长度
    AES/CFB/PKCS5Padding          32                          16
    AES/CFB/ISO10126Padding       32                          16
    AES/ECB/NoPadding             16                          不支持
    AES/ECB/PKCS5Padding          32                          16
    AES/ECB/ISO10126Padding       32                          16
    AES/OFB/NoPadding             16                          原始数据长度
    AES/OFB/PKCS5Padding          32                          16
    AES/OFB/ISO10126Padding       32                          16
    AES/PCBC/NoPadding            16                          不支持
    AES/PCBC/PKCS5Padding         32                          16
    AES/PCBC/ISO10126Padding      32                          16
    */
    private static byte[] encrypt(byte[] raw, byte[] clear,int offset,int len) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        
        //Cipher cipher = Cipher.getInstance("AES");
        //AES/ECB/PKCS5Padding"
    //     Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      //   Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
         Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(
                new byte[cipher.getBlockSize()]));
        byte[] encrypted = cipher.doFinal(clear,offset,len);
        return encrypted;
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted)
            throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
//        Cipher cipher = Cipher.getInstance("AES");
         Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(
                new byte[cipher.getBlockSize()]));
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    public static String toHex(String txt) {
        return toHex(txt.getBytes());
    }

    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }

    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        return result;
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
	
	
	/** 
	 * 加密 
	 *  
	 * @param content 需要加密的内容 
	 * @param password  加密密码 
	 * @return 
	 */  
	
	/*
	public static byte[] encrypt(String content, String password) {  
	        try {             
	                KeyGenerator kgen = KeyGenerator.getInstance("AES");  
	                kgen.init(128, new SecureRandom(password.getBytes()));  
	                SecretKey secretKey = kgen.generateKey();  
	                byte[] enCodeFormat = secretKey.getEncoded();  
	                SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");  
	                Cipher cipher = Cipher.getInstance("AES");// 创建密码器   
	                byte[] byteContent = content.getBytes("utf-8");  
	                cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化   
	                byte[] result = cipher.doFinal(byteContent);  
	                return result; // 加密   
	        } catch (NoSuchAlgorithmException e) {  
	                e.printStackTrace();  
	        } catch (NoSuchPaddingException e) {  
	                e.printStackTrace();  
	        } catch (InvalidKeyException e) {  
	                e.printStackTrace();  
	        } catch (UnsupportedEncodingException e) {  
	                e.printStackTrace();  
	        } catch (IllegalBlockSizeException e) {  
	                e.printStackTrace();  
	        } catch (BadPaddingException e) {  
	                e.printStackTrace();  
	        }  
	        return null;  
	} 
	*/
    /**解密 
     * @param content  待解密内容 
     * @param password 解密密钥 
     * @return 
     */
	/*
    public static byte[] decrypt(byte[] content, String password) {  
            try {  
                     KeyGenerator kgen = KeyGenerator.getInstance("AES");  
                     kgen.init(128, new SecureRandom(password.getBytes()));  
                     SecretKey secretKey = kgen.generateKey();  
                     byte[] enCodeFormat = secretKey.getEncoded();  
                     SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");              
                     Cipher cipher = Cipher.getInstance("AES");// 创建密码器   
                    cipher.init(Cipher.DECRYPT_MODE, key);// 初始化   
                    byte[] result = cipher.doFinal(content);  
                    return result; // 加密   
            } catch (NoSuchAlgorithmException e) {  
                    e.printStackTrace();  
            } catch (NoSuchPaddingException e) {  
                    e.printStackTrace();  
            } catch (InvalidKeyException e) {  
                    e.printStackTrace();  
            } catch (IllegalBlockSizeException e) {  
                    e.printStackTrace();  
            } catch (BadPaddingException e) {  
                    e.printStackTrace();  
            }  
            return null;  
    }  
    */
}
