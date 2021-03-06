xquery version "3.0";

module namespace aggregate="http://exist-db.org/mongodb/test/aggregate";

import module namespace xqjson = "http://xqilla.sourceforge.net/lib/xqjson";

import module namespace test="http://exist-db.org/xquery/xqsuite" 
                at "resource:org/exist/xquery/lib/xqsuite/xqsuite.xql";

import module namespace mongodb = "http://exist-db.org/xquery/mongodb" 
                at "java:org.exist.mongodb.xquery.MongodbModule";

import module namespace support = "http://exist-db.org/ext/mongodb/test/support"
                at "./support.xqm";
 
(: 
 :  Example test taken from http://docs.mongodb.org/ecosystem/tutorial/use-aggregation-framework-with-java-driver/ 
 :)              

(: Connect to mongodb, store token :)
declare %test:setUp function aggregate:setup()
{
    let $setup := support:setup()
    let $mongodbClientId := support:getToken()
    let $drop := mongodb:drop($mongodbClientId, $support:database, $support:mongoCollection)
    return
        (
            mongodb:insert($mongodbClientId, $support:database, $support:mongoCollection,  
                            '{ "employee" : 1 , "department" : "Sales" , "amount" : 71 , "type" : "airfare"}'),
            mongodb:insert($mongodbClientId, $support:database, $support:mongoCollection,  
                            '{ "employee" : 2 , "department" : "Engineering" , "amount" : 15 , "type" : "airfare"}'),
            mongodb:insert($mongodbClientId, $support:database, $support:mongoCollection,  
                             '{ "employee" : 4 , "department" : "Human Resources" , "amount" : 5 , "type" : "airfare"}'),
            mongodb:insert($mongodbClientId, $support:database, $support:mongoCollection,  
                             '{ "employee" : 42 , "department" : "Sales" , "amount" : 77 , "type" : "airfare"}')
        )
            
};






(: Disconnect from mongodb, cleanup token :)
declare %test:tearDown function aggregate:cleanup()
{   
    support:cleanup()
};


(: ----------------------------
 : Actual tests below this line  
 : ---------------------------- :)

(: 
 : collection#aggregate(query) 
 : 
 : { "_id" : "Human Resources" , "average" : 5.0} 
 : { "_id" : "Engineering" , "average" : 15.0} 
 : { "_id" : "Sales" , "average" : 74.0}
 :)
declare 
    %test:assertEquals("5.0", "15.0", "74.0") 
function aggregate:aggregate_simple() {
    let $mongodbClientId := support:getToken()
    
    let $result := mongodb:aggregate($mongodbClientId, $support:database, $support:mongoCollection,
                   (
                        '{ "$match" : { "type" : "airfare"}}', 
                        '{ "$project" : { "department" : 1 , "amount" : 1 , "_id" : 0}}',
                        '{ "$group" : { "_id" : "$department" , "average" : { "$avg" : "$amount"}}}',
                        '{ "$sort" : { "amount" : -1}}'   )
                    )

    let $formatted := <result>{for $one in $result
    return xqjson:parse-json($one)}</result>
    
    return $formatted//pair[@name eq 'average']/text()
        
};






