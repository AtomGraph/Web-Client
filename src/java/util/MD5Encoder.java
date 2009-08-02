/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Pumba
 */
public class MD5Encoder {

    public static String encode(String string) throws UnsupportedEncodingException, NoSuchAlgorithmException
    {
	byte[] bytes = string.getBytes("UTF-8");
	MessageDigest digest = MessageDigest.getInstance("MD5");
	digest.update(bytes);
	String result = new BigInteger(1, digest.digest()).toString(16);
	while(result.length() < 32) result = "0" + result;

	return result;
    }
}
