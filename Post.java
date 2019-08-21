package ca.utoronto.utm.mcs;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.DataFormatException;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;

import org.bson.BSONObject;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.inject.Inject;
import dagger.ObjectGraph;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.bson.types.ObjectId;
import static com.mongodb.client.model.Filters.eq;

public class Post implements HttpHandler {
    @Inject MongoClient client;

    @Override
    public void handle(HttpExchange r) throws IOException {
        try {
            if (r.getRequestMethod().equals("GET")) {
                handleGet(r);
            } else if (r.getRequestMethod().equals("PUT")) {
                handlePut(r);
            } else if (r.getRequestMethod().equals("DELETE")) {
                handleDelete(r);
            } else {
                r.sendResponseHeaders(405, -1);
            }
        } catch (IOException e) {
            r.sendResponseHeaders(500, -1);
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void handlePut(HttpExchange r) throws IOException {
    	try {
	    	
	        //Adding to the database
	        String body = Utils.convert(r.getRequestBody());
	        Document deserialized = null;
	        
	        try {
	        	deserialized = Document.parse(body);
	        } catch(Exception e) {
	        	r.sendResponseHeaders(400, -1);
	        	return;
	        }
	
	        // Checks if all required values exists
	        if (deserialized.get("title") == null
	                || deserialized.get("author") == null
	                || deserialized.get("content") == null
	                || deserialized.get("tags") == null ){
	            r.sendResponseHeaders(400,-1);
	            return;
	        }
	        
	        
	        Document doc = new Document()
	                .append("title",deserialized.get("title"))
	                .append("author",deserialized.get("author"))
	                .append("content",deserialized.get("content"))
	                .append("tags", deserialized.get("tags"));
	
	        MongoDatabase db = client.getDatabase("csc301a2");
	
	
	        MongoCollection<Document> collection = db.getCollection("posts");
	        collection.insertOne(doc);
	        //
	
	        //Get id and put it in response body.
	        String response = "{\"_id\": \""+doc.get("_id").toString()+"\"}";
	        
	        
	        r.sendResponseHeaders(200, response.length());
	        OutputStream os = r.getResponseBody();
	        os.write(response.getBytes());
	        os.close();
    	} catch(IOException e) {
    		r.sendResponseHeaders(500, -1);
    		return;
    	}

    }

    public void handleGet(HttpExchange r) throws IOException {
    	try {
    	
	        String body = Utils.convert(r.getRequestBody());
	        
	        Document deserialized = null;
	        
	        try {
	        	deserialized = Document.parse(body);
	        } catch(Exception e) {
	        	r.sendResponseHeaders(400, -1);
	        	return;
	        }
	        
	        // Setup database
	        MongoDatabase db = client.getDatabase("csc301a2");
	        MongoCollection<Document> collection = db.getCollection("posts");
	        
	        // no title and no id
	        if (deserialized.get("title") == null && deserialized.get("_id") == null){
	            r.sendResponseHeaders(400,-1);
	            return;
	        }
	        
	        // request has title but no id
	        if(deserialized.get("title") != null && deserialized.get("_id") == null) {
	        	String response = "{[";
	            FindIterable<Document> cursor = null;
	            
	            // server does not have the requested title
	            try {
	            	cursor = collection.find(Filters.regex("title", deserialized.get("title").toString()));
	            	cursor.first().toString();
	            } catch(NullPointerException e){
	                r.sendResponseHeaders(404,-1);
	                return;
	            }
	            
	            // loops through all documents and concats to string
	            for(Document d: cursor) {
	            	response = response.concat("{\"_id\": {\"$oid\": \"" + d.get("_id").toString() + "\"},"); //id
	            	response = response.concat("\"title\": \"" + d.get("title").toString() + "\","); //title
	            	response = response.concat("\"author\": \"" + d.get("author").toString() + "\","); //author
	            	response = response.concat("\"content\": \"" + d.get("content").toString() + "\","); //content
	            	response = response.concat("\"tags\": [");
	            	String sub = d.get("tags").toString().substring(1, d.get("tags").toString().length()-1); // takes out []
	            	String[] tags = sub.split(",");
	            	
	            	// runs through each tag and quotes them
	            	int i;
	            	for(i = 0; i < tags.length; i++) {
	            		tags[i] = tags[i].trim(); //takes out whitespace 
	            		String str = "\"" + tags[i] + "\",";
	            		response = response.concat(str);
	            		
	            	}
	            	
	            	// takes out last comma
	            	response = response.substring(0, response.length()-1);
	            	response = response.concat("]},");
	            }
	            response = response.substring(0, response.length()-1); // takes out comma at end
	            response = response.concat("]}");
	            
	            r.sendResponseHeaders(200, response.length());
	            OutputStream os = r.getResponseBody();
	            os.write(response.getBytes());
	            os.close();
	        	return;
	        }
	        
	        // request has both title and id OR just id -> similar result b/c id is prioritized
	        if(  (deserialized.get("title") == null && deserialized.get("_id") != null) ||
	        	 (deserialized.get("title") != null && deserialized.get("_id") != null)	) {
	        	String response = "{[";    
	            FindIterable<Document> cursor = null;
	                  
	            // find is null
	            try {
	            	cursor = collection.find(eq("_id", new ObjectId(deserialized.get("_id").toString())));
	            } catch(Exception e){
	                r.sendResponseHeaders(404,-1);
	                return;
	            }
	       
	        	response = response.concat("{\"_id\": {\"$oid\": \"" + cursor.first().get("_id").toString() + "\"},"); //id
	        	response = response.concat("\"title\": \"" + cursor.first().get("title").toString() + "\","); //title
	        	response = response.concat("\"author\": \"" + cursor.first().get("author").toString() + "\","); //author
	        	response = response.concat("\"content\": \"" + cursor.first().get("content").toString() + "\","); //content
	        	response = response.concat("\"tags\": [");
	        	String sub = cursor.first().get("tags").toString().substring(1, cursor.first().get("tags").toString().length()-1); // takes out []
	        	String[] tags = sub.split(",");
	        	
	        	// runs through each tag and quotes them
	        	int i;
	        	for(i = 0; i < tags.length; i++) {
	        		tags[i] = tags[i].trim(); //takes out whitespace 
	        		String str = "\"" + tags[i] + "\",";
	        		response = response.concat(str);
	        		
	        	}
	        	
	        	// takes out last comma
	            response = response.substring(0, response.length()-1); // takes out comma at end
	            response = response.concat("]}");
	            
	            r.sendResponseHeaders(200, response.length());
	            OutputStream os = r.getResponseBody();
	            os.write(response.getBytes());
	            os.close(); 	
	        	return;
	        }
	        
	        // if anything fails then error
	        r.sendResponseHeaders(400,-1);
	        return;
    	} catch (IOException e) {
    		r.sendResponseHeaders(500, -1);
    		return;
    	}
    }
    	


	public void handleDelete(HttpExchange r) throws IOException {
		try {
		
	        String body = Utils.convert(r.getRequestBody());
	        Document deserialized = null;
	        
	        try {
	        	deserialized = Document.parse(body);
	        } catch(Exception e) {
	        	r.sendResponseHeaders(400, -1);
	        	return;
	        }
	
	        //Checks the id from the body
	        if (deserialized.get("_id") == null) {
	            r.sendResponseHeaders(400,-1);
	            return;
	        }
	        //
	
	        //Stores the id in the document
	        Document doc = new Document()
	                .append("_id",deserialized.get("_id"));
	        //
	
	        //Gets database and collection
	        MongoDatabase db = client.getDatabase("csc301a2");
	        MongoCollection<Document> collection = db.getCollection("posts");
	        //
	
	        //Extract id
	        String id = doc.get("_id").toString();
	        //
	        
	        //check if request does not exist
	        FindIterable<Document> cursor = null;
	        try {
	        	cursor = collection.find(eq("_id", new ObjectId(deserialized.get("_id").toString())));
	        	cursor.first().toString();
	        } catch(Exception e) {
	            r.sendResponseHeaders(404, -1);
	            return;
	        }
	        
	        
	        //Delete the record at this id
	        collection.deleteOne(new Document("_id", new ObjectId(id)));
	        //
	
	        //Returns successful.
	        r.sendResponseHeaders(200, -1);
	        return;
        //
		} catch (IOException e) {
			r.sendResponseHeaders(500, -1);
		}
    }
}