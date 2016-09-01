/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.atomgraph.client.exception;

/**
 *
 * @author Martynas Jusevičius <martynas@atomgraph.com>
 */
public class OntClassNotFoundException extends RuntimeException
{

    public OntClassNotFoundException()
    {
    }

    public OntClassNotFoundException(String string) 
    {
        super(string);
    }

    public OntClassNotFoundException(String string, Throwable thrwbl)
    {
        super(string, thrwbl);
    }

    public OntClassNotFoundException(Throwable thrwbl)
    {
        super(thrwbl);
    }

    public OntClassNotFoundException(String string, Throwable thrwbl, boolean bln, boolean bln1)
    {
        super(string, thrwbl, bln, bln1);
    }

    
}
