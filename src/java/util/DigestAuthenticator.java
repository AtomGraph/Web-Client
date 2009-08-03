/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 *
 * @author Pumba
 */
public class DigestAuthenticator extends Authenticator {

    @Override
    public PasswordAuthentication getPasswordAuthentication()
    {
	System.err.println("Feeding username and password for " + getRequestingScheme());
	return (new PasswordAuthentication("mjusevicius", "n7dx2grc".toCharArray()));
    }
    
}
