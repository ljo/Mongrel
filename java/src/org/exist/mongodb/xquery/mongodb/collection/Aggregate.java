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
package org.exist.mongodb.xquery.mongodb.collection;

import com.mongodb.AggregationOutput;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.util.JSONParseException;
import java.util.List;
import org.exist.dom.QName;
import org.exist.mongodb.shared.ConversionTools;
import static org.exist.mongodb.shared.FunctionDefinitions.PARAMETER_COLLECTION;
import static org.exist.mongodb.shared.FunctionDefinitions.PARAMETER_DATABASE;
import static org.exist.mongodb.shared.FunctionDefinitions.PARAMETER_MONGODB_CLIENT;
import org.exist.mongodb.shared.MongodbClientStore;
import org.exist.mongodb.xquery.MongodbModule;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.FunctionParameterSequenceType;
import org.exist.xquery.value.FunctionReturnSequenceType;
import org.exist.xquery.value.Sequence;
import org.exist.xquery.value.SequenceType;
import org.exist.xquery.value.StringValue;
import org.exist.xquery.value.Type;
import org.exist.xquery.value.ValueSequence;

/**
 * Function to remove document from mongodb
 *
 * @author Dannes Wessels
 */
public class Aggregate extends BasicFunction {

    private static final String AGGREGATE = "aggregate";
    
    public static final String PARAM_PIPELINE = "pipeline";
    public static final String DESCR_PIPELINE= "Operations to be performed in the aggregation pipeline, JSON formatted";

    public static final FunctionParameterSequenceType PARAMETER_PIPELINE
            = new FunctionParameterSequenceType(PARAM_PIPELINE, Type.STRING, Cardinality.ZERO_OR_MORE, DESCR_PIPELINE);
    
  
    public final static FunctionSignature signatures[] = {
        
        new FunctionSignature(
        new QName(AGGREGATE, MongodbModule.NAMESPACE_URI, MongodbModule.PREFIX), "Atomically modify and return a single document.",
        new SequenceType[]{
            PARAMETER_MONGODB_CLIENT, PARAMETER_DATABASE, PARAMETER_COLLECTION, PARAMETER_PIPELINE},
        new FunctionReturnSequenceType(Type.STRING, Cardinality.ZERO_OR_ONE, "The document as it was before it was removed formatted as JSON")
        ),
        
    };

    public Aggregate(XQueryContext context, FunctionSignature signature) {
        super(context, signature);
    }

    @Override
    public Sequence eval(Sequence[] args, Sequence contextSequence) throws XPathException {

        try {
            // Verify clientid and get client
            String mongodbClientId = args[0].itemAt(0).getStringValue();                  
            MongodbClientStore.getInstance().validate(mongodbClientId);
            MongoClient client = MongodbClientStore.getInstance().get(mongodbClientId);
            
            // Get parameters
            String dbname = args[1].itemAt(0).getStringValue();
            String collection = args[2].itemAt(0).getStringValue();
            List<DBObject> pipeline = ConversionTools.convertPipeline(args[3]);

            // Get collection in database
            DB db = client.getDB(dbname);
            DBCollection dbcol = db.getCollection(collection);
            
            // Execute query      
            AggregationOutput aggrOutput = dbcol.aggregate(pipeline);
            
            // Parse results
            Sequence retVal = new ValueSequence();
            
            for (DBObject result : aggrOutput.results()) {
                retVal.add(new StringValue(result.toString()));
            }
            
            return retVal;
            
        } catch(JSONParseException ex){
            LOG.error(ex.getMessage());
            throw new XPathException(this, MongodbModule.MONG0004, ex.getMessage());

        } catch (XPathException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new XPathException(this, ex.getMessage(), ex);

        } catch (MongoException ex) {
            LOG.error(ex.getMessage(), ex);
            throw new XPathException(this, MongodbModule.MONG0002, ex.getMessage());

        } catch (Throwable ex) {
            LOG.error(ex.getMessage(), ex);
            throw new XPathException(this, MongodbModule.MONG0003, ex.getMessage());
        }

    }
    

    

}
