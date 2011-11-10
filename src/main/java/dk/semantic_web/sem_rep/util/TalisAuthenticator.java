/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.util;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 *
 * @author Pumba
 */
public class TalisAuthenticator extends Authenticator {

    @Override
    public PasswordAuthentication getPasswordAuthentication()
    {
	System.err.println("Feeding username and password for " + getRequestingScheme());
	return (new PasswordAuthentication("mjusevicius", "n7dx2grc".toCharArray()));
    }
    
}
