/*
 * ResourceMapping.java
 *
 * Created on Antradienis, 2007, Balandžio 17, 22.12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package lt.xml.diy.controller;

/**
 *
 * @author Pumba
 */
abstract public class ResourceMapping
{
    abstract public ResourceImpl findByURI(String URI);
}
