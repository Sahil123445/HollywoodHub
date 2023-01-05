package ca.yorku.eecs;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;

import com.sun.net.httpserver.HttpExchange;

public class Movie {
	String id;
	String name;
	int revenue;
	private Driver driver;
	
	public Movie() {
		driver = GraphDatabase.driver(Utils.uriDb, AuthTokens.basic("neo4j","123456"), Utils.config);
	}
	
	public int insertMovie(String name, String id) {
		try (Session session = driver.session()) {
        	try (Transaction tx = session.beginTransaction()) {
        		StatementResult node_boolean = tx.run("MERGE (m:movie {Name: $name, id : $id})\n"
						+ "ON MATCH\n" +
						"SET m.found = true\n"
						+ "RETURN m.found"
						, parameters("name", name, "id", id));
        	    tx.success();
				session.close();		
        		if (node_boolean.hasNext()) {
        			// This movie already exists in our database, return 400 status code    
        			Record record = node_boolean.next();
         			if (record.get("m.found").asBoolean(false)) {
            			return 400;         				
         			}
        		}
        		return 200;
        	}				
		}
	}
	
	public String returnMovie(String id) throws JSONException {
		try (Session session = driver.session()) {
        	try (Transaction tx = session.beginTransaction()) {
        		StatementResult checkMovie = tx.run("MATCH (m:movie)\n"
        				+ "WHERE m.id = $mid\n"
        				+ "RETURN count(m) as count" 
        				, parameters("mid", id));
        		if (checkMovie.single().get("count").asInt() == 0) {
        			//if either movie or actor does not exist in the database
        			return "404"; 
        		}       		
        		StatementResult node = tx.run("MATCH (m:movie {id : $id})\n"
        				+ "RETURN m.Name as name, m.id as movieId", parameters("id", id));
        		List<Record> list = node.list();
        		StatementResult actors = tx.run("MATCH (:movie {id: $id})--(a:actor)\n"
        				+ "RETURN a.id", parameters("id", id));
        		JSONObject resu = new JSONObject();
        		resu.put("movieId", list.get(0).get("movieId").asString());
        		resu.put("name", list.get(0).get("name").asString());
        		List<String> actorIdList = new ArrayList<>();
        		while (actors.hasNext()) {
        			actorIdList.add(actors.next().get("a.id").asString());
        		}
        	    tx.success();
				session.close();	
				resu.put("actors", actorIdList);
        		return resu.toString();
        	}	
		}
	}
	public List<Record> moviesOfActor(String actorId) throws JSONException {
        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                List<Record> moviesOfActor = tx.run("MATCH (a:actor {id: $actorId})-[r]->(movie) return movie.id;", parameters("actorId", actorId)).list();
        	    tx.success();
				session.close();	
                return moviesOfActor;
            }                
        }
    }
    
    public List<Record> returnAllActorsFromMovie(String movieId) throws JSONException {
        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                List<Record> allActorsFromMovie = tx.run("MATCH (actor)-[r]->(m:movie {id: $movieId}) return actor.id;", parameters("movieId", movieId)).list();
        	    tx.success();
				session.close();	
                return allActorsFromMovie;
            }                
        }
    }
	public void addMovie(JSONObject object, HttpExchange r) throws IOException {
		try {
			String id = object.getString("movieId");
			String name = object.getString("name");
			Movie movie = new Movie();		
			int code = movie.insertMovie(name, id);
			movie.close();
			Utils.sendString(r, "", code);							
		} catch(Exception e) {
        	e.printStackTrace();
        	Utils.sendString(r,"", 400);			
		}		
	}
	public void getMovie(JSONObject object, HttpExchange r) throws IOException {
		try {
			String id = object.getString("movieId");
			Movie movie = new Movie();		
			String resu = movie.returnMovie(id);
			if (resu.equals("404")) {
				Utils.sendString(r, "", 404);				
			} else {
				Utils.sendString(r, resu, 200);				
			}
			movie.close();
		} catch(Exception e) {
        	e.printStackTrace();
        	Utils.sendString(r,"", 500);			
		}				
	}
	public void close() {
		driver.close();
	}
}
