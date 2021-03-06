/*
 *  eXist Open Source Native XML Database
 *  Copyright (C) 2014 The eXist Project
 *  http://exist-db.org
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.exist.mongodb.xquery.mongodb.client;

import com.mongodb.MongoException;
import java.util.Set;
import org.exist.dom.QName;
import org.exist.mongodb.shared.Constants;
import static org.exist.mongodb.shared.FunctionDefinitions.DESCR_MONGODB_CLIENT_ID;
import org.exist.mongodb.shared.MongodbClientStore;
import org.exist.mongodb.xquery.MongodbModule;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.FunctionReturnSequenceType;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.StringValue;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;

/**
 * Function to list all GridFS buckets
 *
 * @author Dannes Wessels
 */
public class ListMongdbClientIds extends BasicFunction {

    private static final String LIST_CLIENT_IDS = "list-mongodb-clientids";

    public final static FunctionSignature signatures[] = {
        new FunctionSignature(
            new QName(LIST_CLIENT_IDS, MongodbModule.NAMESPACE_URI, MongodbModule.PREFIX),
            "Get all MongoDB client ids",
            new SequenceType[]{ /* No Parameters */ },
            new FunctionReturnSequenceType(Type.STRING, Cardinality.ZERO_OR_MORE, String.format("Sequence of %ss", DESCR_MONGODB_CLIENT_ID))
        ),
    };

    public ListMongdbClientIds(XQueryContext context, FunctionSignature signature) {
        super(context, signature);
    }

    @Override
    public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException {

        // User must either be DBA or in the JMS group
        if (!context.getSubject().hasDbaRole() && !context.getSubject().hasGroup(Constants.MONGODB_GROUP)) {
            String txt = String.format("Permission denied, user '%s' must be a DBA or be in group '%s'",
                    context.getSubject().getName(), Constants.MONGODB_GROUP);
            LOG.error(txt);
            throw new XPathException(this, txt);
        }

        try {
            Set<String> clientIds = MongodbClientStore.getInstance().list();
            
            ValueSequence valueSequence = new ValueSequence();
           
            for (String mongodbClientId : clientIds) {
                valueSequence.add(new StringValue(mongodbClientId));
            }

            return valueSequence;


//        } catch (XPathException ex) {
//            LOG.error(ex.getMessage(), ex);
//            throw new XPathException(this, ex.getMessage(), ex);

        } catch (MongoException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new XPathException(this, MongodbModule.MONG0002, ex.getMessage());

        } catch (Throwable ex) {
            LOG.error(ex.getMessage(), ex);
            throw new XPathException(this, MongodbModule.MONG0003, ex.getMessage());
        }


    }

}
