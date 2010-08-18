/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model;

/**
 *
 * @author Pumba
 */
public class PagePeer
{
    public static Page doSelectByName(String name)
    {
        Page page = null;

        if (name.equals("contacts")) page = new Page(name);

        return page;
    }
}
