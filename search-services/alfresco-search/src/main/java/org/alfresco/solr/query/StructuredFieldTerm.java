/*
 * #%L
 * Alfresco Search Services
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
 * 
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.solr.query;

import org.apache.lucene.index.Term;

/**
 * @author andyh
 */
public class StructuredFieldTerm
{

    private Term term;

    private StructuredFieldPosition sfp;

    /**
     * 
     */
    public StructuredFieldTerm(Term term, StructuredFieldPosition sfp)
    {
        this.term = term;
        this.sfp = sfp;
    }

    /**
     * @return Returns the sfp.
     */
    public StructuredFieldPosition getSfp()
    {
        return sfp;
    }

    /**
     * @return Returns the term.
     */
    public Term getTerm()
    {
        return term;
    }
}
