/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.graphity.analytics.util;

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
