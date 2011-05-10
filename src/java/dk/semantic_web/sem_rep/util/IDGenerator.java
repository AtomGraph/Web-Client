/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.util;

import java.util.UUID;

/**
 *
 * @author Pumba
 */
public class IDGenerator
{
    public static String generate()
    {
        return String.valueOf(UUID.randomUUID());
    }
}
