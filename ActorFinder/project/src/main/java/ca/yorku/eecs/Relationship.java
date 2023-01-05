package ca.yorku.eecs;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;

import com.sun.net.httpserver.HttpExchange;

public class Relationship {

	private Driver driver;
	
	public Relationship() {
		driver = GraphDatabase.driver(Utils.uriDb, AuthTokens.basic("neo4j","123456"), Utils.config);
	}
	
	public int add(String actorId, String movieId) {
		try (Session session = driver.session()) {
        	try (Transaction tx = session.beginTransaction()) {
        		StatementResult checkActor = tx.run("MATCH (a:actor)\n"
        				+ "WHERE a.id = $aid\n"
        				+ "RETURN count(a) as count" 
        				, parameters("aid", actorId));
        		StatementResult checkMovie = tx.run("MATCH (m:movie)\n"
        				+ "WHERE m.id = $mid\n"
        				+ "RETURN count(m) as count" 
        				, parameters("mid", movieId));
        		if (checkActor.single().get("count").asInt() == 0 || checkMovie.single().get("count").asInt() == 0) {
        			//if either movie or actor does not exist in the database
        			return 404; 
        		}
        		StatementResult matchRelationship = tx.run(
						"MATCH (a : actor {id : $aid})-[r:ACTED_IN]->(m :movie {id : $mid})\n"
						+ "RETURN count(r) as count"
						, parameters("aid", actorId, "mid", movieId));
        		if (matchRelationship.single().get("count").asInt() == 1) {
        			//if either movie or actor does not exist in the database
        			return 400; 
        		}
        		StatementResult createRelationship = tx.run(
        				"MATCH (a:actor {id : $aid}),\n"
        				+"(m:movie {id : $mid})"
						+"MERGE (a)-[r:ACTED_IN]->(m)"
						, parameters("aid", actorId, "mid", movieId));
        	    tx.success();
				session.close();		
        	}	
        	return 200;
		}
	}

	public String getRelationship(String actorId, String movieId) throws JSONException {
		try (Session session = driver.session()) {
        	try (Transaction tx = session.beginTransaction()) {
        		StatementResult checkActor = tx.run("MATCH (a:actor)\n"
        				+ "WHERE a.id = $aid\n"
        				+ "RETURN count(a) as count" 
        				, parameters("aid", actorId));
        		StatementResult checkMovie = tx.run("MATCH (m:movie)\n"
        				+ "WHERE m.id = $mid\n"
        				+ "RETURN count(m) as count" 
        				, parameters("mid", movieId));
        		if (checkActor.single().get("count").asInt() == 0 || checkMovie.single().get("count").asInt() == 0) {
        			//if either movie or actor does not exist in the database
        			return "404"; 
        		}
        		StatementResult matchRelationship = tx.run(
						"MATCH (a : actor {id : $aid})-[r:ACTED_IN]->(m :movie {id : $mid})\n"
						+ "RETURN count(r) as count"
						, parameters("aid", actorId, "mid", movieId));
        		boolean hasRelationship = false;
        		if (matchRelationship.single().get("count").asInt() == 1) {
        			hasRelationship = true;
        		}
        	    tx.success();
				session.close();		
        		JSONObject resu = new JSONObject();
        		resu.put("actorId", actorId);
        		resu.put("movieId", movieId);
				resu.put("hasRelationship", hasRelationship);
            	return resu.toString();
        	}	
		}
	}
	public void addRelationship(JSONObject object, HttpExchange r) throws IOException {
		try {
			String actorId = object.getString("actorId");
			String movieId = object.getString("movieId");
			Relationship relation = new Relationship();
			int errorCode = relation.add(actorId, movieId);
			relation.close();
			Utils.sendString(r, "", errorCode);							
		} catch(Exception e) {
        	e.printStackTrace();
        	Utils.sendString(r,"", 400);			
		}		
	}


    public void hasRelationShip(JSONObject object, HttpExchange r) throws IOException { 
        try {
            String actorId = object.getString("actorId");
            String movieId = object.getString("movieId");
            Relationship relation = new Relationship();
            String ret = relation.getRelationship(actorId, movieId);
            relation.close();
            if (ret.equals("404")) {
                Utils.sendString(r,"", 404);
            } else {
                Utils.sendString(r,ret, 200);           	
            }
        } catch (Exception e) {
            e.printStackTrace();
        	Utils.sendString(r,"", 500);			
        }
    }
	public void close() {
		driver.close();
	}
}
