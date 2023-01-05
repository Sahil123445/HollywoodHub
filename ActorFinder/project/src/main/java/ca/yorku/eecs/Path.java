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
import org.neo4j.driver.v1.Value;

import com.sun.net.httpserver.HttpExchange;

public class Path {

	private Driver driver;
	
	public Path() {
		driver = GraphDatabase.driver(Utils.uriDb, AuthTokens.basic("neo4j","123456"), Utils.config);
	}
	
	public Record getPath(String id, String otherId) {
		try (Session session = driver.session()) {
        	try (Transaction tx = session.beginTransaction()) {
        		String cql = "MATCH p=shortestPath( (a:actor{id : $id})-[*..10]-(b:actor{id : $otherid}) )\n"
        				+ "RETURN nodes(p)";
        		StatementResult path = tx.run(cql, parameters("id", id, "otherid", otherId));
        		if (!path.hasNext()) {
        			return null;
        		}
        		Record resu = path.single();
        	    tx.success();
				session.close();	
        		return resu;
        	}				
		}
	}
	public int getBaconNumber(String id, String otherId) {
		if (id.equals(otherId)) {
			return 0;
		}
		Record r = getPath(id, otherId);
		if (r == null) {
			return -1;
		}
		Value nodes = r.get("nodes(p)");
		int count = -1;
		for (Value node : nodes.values()) {
			if (node.asNode().hasLabel("actor")) {
				count++;
			}
		}		
		return count;
	}
	public String getBaconNumberResponse(String id, String otherId) throws JSONException {
		int baconNumber = id.equals(otherId)? 0 : getBaconNumber(id, otherId);
		// find all the actors in the shortest path, and then return numberofActors - 1
		if (check(id, otherId) == 404 || baconNumber == -1) {
			return "404";
		}
		JSONObject resu = new JSONObject();
		resu.put("baconNumber", baconNumber);
		return resu.toString();	
	}
	
	public String getPathString(String id, String otherId) throws JSONException {
		// return IDs of all the movies and actors
		if (check(id, otherId) == 404) {
			return "404";
		}
		Record r = getPath(id, otherId);
		if (r==null) {
			return "404";
		}
		Value nodes = r.get("nodes(p)");
		List<String> ids = new ArrayList<>();
		for (Value node : nodes.values()) {
			ids.add(node.get("id").asString());
		}
		JSONObject resu = new JSONObject();
		resu.put("baconPath", ids);
		return resu.toString();
	}
	
	private int check(String id1, String id2) {
		try (Session session = driver.session()) {
        	try (Transaction tx = session.beginTransaction()) {
        		StatementResult checkActor1 = tx.run("MATCH (a:actor)\n"
        				+ "WHERE a.id = $aid\n"
        				+ "RETURN count(a) as count" 
        				, parameters("aid", id1));
        		StatementResult checkActor2 = tx.run("MATCH (a:actor)\n"
        				+ "WHERE a.id = $aid\n"
        				+ "RETURN count(a) as count" 
        				, parameters("aid", id2));
        		if (checkActor1.single().get("count").asInt() == 0 || checkActor2.single().get("count").asInt() == 0) {
        			//if either movie or actor does not exist in the database
        			return 404; 
        		}
        	}
		}
		return 0;
	}
	public void getPathToSigOthers(JSONObject object, HttpExchange r) throws IOException {	//TODO
        try {		
        	String actorId = object.getString("actorId");
        	String otherId = object.getString("SO_ID");
        	Path path = new Path();
        	String resu = path.getPathString(actorId, otherId);
        	path.close();
        	if (resu.equals("404")) {
        		Utils.sendString(r, "", 404);				
        	} else {
        		Utils.sendString(r, resu, 200);				
        	}
        } catch (JSONException e) {
        	e.printStackTrace();
        	Utils.sendString(r,"", 500);			
        } catch (IOException e) {
        	e.printStackTrace();
        	Utils.sendString(r,"", 500);			
        }
	}
	public void computeBaconNumber(JSONObject object, HttpExchange r) throws IOException {
        try {
			String actorId = object.getString("actorId");
			Path path = new Path();
			String resu = path.getBaconNumberResponse(actorId, "nm0000102");
			path.close();
			if (resu.equals("404")) {
				Utils.sendString(r, "", 404);				
			} else {
				Utils.sendString(r, resu, 200);				
			}
		} catch (JSONException e) {
			e.printStackTrace();
        	Utils.sendString(r,"", 400);			
		} catch (IOException e) {
			e.printStackTrace();
        	Utils.sendString(r,"", 500);			
		}		
	}
	public void computeBaconPath(JSONObject object, HttpExchange r) throws IOException {	//TODO
        try {
			String actorId = object.getString("actorId");
			Path path = new Path();
			JSONObject bacon = new JSONObject();
			List<String> arr = new ArrayList<>();
			arr.add("nm0000102");
			bacon.put("baconPath", arr);
			String resu = actorId.equals("nm0000102")? bacon.toString() : path.getPathString(actorId, "nm0000102");
			path.close();
			if (resu.equals("404")) {
				Utils.sendString(r, "", 404);				
			} else {
				Utils.sendString(r, resu, 200);				
			}
		} catch (JSONException e) {
			e.printStackTrace();
        	Utils.sendString(r,"", 400);			
		} catch (IOException e) {
			e.printStackTrace();
        	Utils.sendString(r,"", 500);			
		}
	}
	public void close() {
		driver.close();
	}
}
