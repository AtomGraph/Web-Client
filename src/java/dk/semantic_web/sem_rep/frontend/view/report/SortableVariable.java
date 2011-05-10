/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dk.semantic_web.sem_rep.frontend.view.report;

/**
 *
 * @author Pumba
 */
public class SortableVariable
{
        private final String name;

        SortableVariable(String name)
        {
            this.name = name;
        }

        public final String getName()
        {
            return name;
        }

        @Override
        public final String toString()
        {
            return getName();
        }

}
