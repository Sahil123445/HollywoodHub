package ca.yorku.eecs;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.TreeMap;
import java.util.Map.Entry;

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
import org.neo4j.driver.v1.types.Node;

import com.sun.net.httpserver.HttpExchange;

public class ActorServices {
	private Driver driver;
	
	public ActorServices() {
		driver = GraphDatabase.driver(Utils.uriDb, AuthTokens.basic("neo4j","123456"), Utils.config);
	}
	
	public int insertActor(String name, String id) {
		try (Session session = driver.session()) {
        	try (Transaction tx = session.beginTransaction()) {
        		Random rand = new Random();
        		StatementResult checkActor = tx.run("MATCH (a:actor)\n"
        				+ "WHERE a.id = $aid\n"
        				+ "RETURN count(a) as count" 
        				, parameters("aid", id));
        		if (checkActor.single().get("count").asInt() == 1) {
        			return 400;
        		}
        		double revenue = rand.nextDouble() * 100000000;
        		StatementResult node_boolean = tx.run("MERGE (a:actor {Name: $name, id : $id, revenue : $r})\n"
						, parameters("name", name, "id", id, "r", revenue));
        	    tx.success();
				session.close();		
        		return 200;
        	}				
		}
	}
	
	public String returnActor(String id) throws JSONException {
		try (Session session = driver.session()) {
        	try (Transaction tx = session.beginTransaction()) {
        		StatementResult checkActor = tx.run("MATCH (a:actor)\n"
        				+ "WHERE a.id = $aid\n"
        				+ "RETURN count(a) as count" 
        				, parameters("aid", id));
        		if (checkActor.single().get("count").asInt() == 0) {
        			// if there is no actor in the database that exists with that actorId.
        			return "404";
        		}
        		StatementResult node = tx.run("MATCH (a:actor {id : $id})\n"
        				+ "RETURN a.Name as name, a.id as actorId", parameters("id", id));
        		List<Record> list = node.list();
        		StatementResult movies = tx.run("MATCH (:actor {id: $id})--(m:movie)\n"
        				+ "RETURN m.id", parameters("id", id));
        		JSONObject resu = new JSONObject();
        		resu.put("actorId", list.get(0).get("actorId").asString());
        		resu.put("name", list.get(0).get("name").asString());
        		List<String> movieIdList = new ArrayList<>();
        		while (movies.hasNext()) {
        			movieIdList.add(movies.next().get("m.id").asString());
        		}
        	    tx.success();
				session.close();	
				resu.put("movies", movieIdList);
        		return resu.toString();
        	}				
		}
	}
	public List<Record> returnAllActors() throws JSONException {
        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                List<Record> allActors = tx.run("match (a:actor) return a.id").list();
        	    tx.success();
				session.close();	
                return allActors;
            }                
        }
    }
	public List<Record> getAllActorNodes() throws JSONException {
        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                List<Record> allActors = tx.run("match (a:actor) return a").list();
        	    tx.success();
				session.close();	
                return allActors;
            }                
        }
	}
	public String getActorList(int n) throws JSONException {
		List<Record> allActorNodes = getAllActorNodes();
		if (allActorNodes.size() == 0) {// if the database does not have any actors
			return "404";
		}
		List<Actor> allActors = new ArrayList<>();
		for (Record node : allActorNodes) {
			Value val = node.get(0);
			allActors.add(new Actor(val.get("Name").asString(), val.get("id").asString(), val.get("revenue").asDouble()));
		}
		if (allActors.size() > n) {
			PriorityQueue<Actor> topNActors = new PriorityQueue<>(Collections.reverseOrder());	// min heap of size n 
			//keep n actors with the highest avenues
			for (Actor actor : allActors) {
				Actor temp = new Actor("", "", 0);
				if (topNActors.size() >= n) {
					temp = topNActors.poll();					
				}
				topNActors.offer(Collections.max(Arrays.asList(temp, actor)));
			}	
			allActors = new ArrayList<>(topNActors);
		}
		JSONObject resu = new JSONObject();
		JSONArray arr = new JSONArray();
		for (Actor actor : allActors) {
			JSONObject cur = new JSONObject();
			Movie m = new Movie();
			Path p = new Path();
			cur.put("nameOfActor", actor.getName());
			cur.put("ActorID", actor.getId());
			cur.put("Revenue", actor.getRevenue());
			cur.put("Total_movies", m.moviesOfActor(actor.getId()).size());
			cur.put("Bacon_number", p.getBaconNumber(actor.getId(), "nm0000102"));
			arr.put(cur);
		}
		resu.put("actorList", arr);
		System.out.println(allActors.toString());
		return resu.toString();
		}
	public void addActor(JSONObject object, HttpExchange r) throws IOException {	//TODO
		try {
			String id = object.getString("actorId");
			String name = object.getString("name");
			ActorServices actor = new ActorServices();		
			int code = actor.insertActor(name, id);
			actor.close();
			Utils.sendString(r, "", code);							
		} catch(Exception e) {
        	e.printStackTrace();
        	Utils.sendString(r,"", 400);			
		}

	}
	public void getActor(JSONObject object, HttpExchange r) throws IOException {	//TODO
		try {
			String id = object.getString("actorId");
			ActorServices actor = new ActorServices();		
			String resu = actor.returnActor(id);
			if (resu.equals("404")) {
				Utils.sendString(r, "", 404);				
			} else {
				Utils.sendString(r, resu, 200);				
			}
			actor.close();
		} catch(Exception e) {
        	e.printStackTrace();
        	Utils.sendString(r,"", 400);			
		}		
	}
	public void getPopularActorList(JSONObject object, HttpExchange r) throws IOException {
        try {		
        	int actorId = object.getInt("numberOfActors");
        	if (actorId < 1) {
            	Utils.sendString(r,"", 400);			
        	}
        	ActorServices actor = new ActorServices();
        	String resu = actor.getActorList(actorId);
        	actor.close();
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
	public void getCollaborators(JSONObject object, HttpExchange r) throws IOException {	
		try {
			HashMap<String, Integer> noOfCollaborators = new HashMap<String, Integer>();	
			String actorId;
			String otherActorId;
			String movieId;
			Movie m = new Movie();
			if (!object.has("actorId") || !object.has("howMany")) {
	        	Utils.sendString(r,"", 404);			
			}
			actorId = object.getString("actorId");
			int counter = Integer.parseInt(object.getString("howMany"));
			for (Record movieR: m.moviesOfActor(actorId)) {
				movieId = movieR.get(0).toString();
				for (Record actorR: m.returnAllActorsFromMovie(movieId.substring(1, movieId.length()-1))) {
					otherActorId = actorR.get(0).toString();
					if(!otherActorId.equals('"' + actorId + '"')) {
						if(noOfCollaborators.containsKey(otherActorId)) {
							noOfCollaborators.put(otherActorId, noOfCollaborators.get(otherActorId)+1);
						}
						else {
							noOfCollaborators.put(otherActorId,1);
						}
					}
				}
			}
			TreeMap<Integer, ArrayList<String>> noOfCollaboratorsReverse = new TreeMap<Integer, ArrayList<String>>(Collections.reverseOrder());
			int collaborations;
			ArrayList<String> tempArray = new ArrayList<String>(); 
			for(String key: noOfCollaborators.keySet()) {
				collaborations = noOfCollaborators.get(key); 
				if(!noOfCollaboratorsReverse.containsKey(collaborations)) {
					noOfCollaboratorsReverse.put(collaborations, (new ArrayList<String>()));
				}
				tempArray = noOfCollaboratorsReverse.get(collaborations);
				tempArray.add(key);
				noOfCollaboratorsReverse.put(collaborations, tempArray);
			}
			ArrayList<String> actors = new ArrayList<String>();
			for(Entry<Integer, ArrayList<String>> entry: noOfCollaboratorsReverse.entrySet()) {
				for(String actor: entry.getValue()) {
					actors.add(actor);
					counter -= 1;
					if(counter == 0) {break;}
				}
				if(counter == 0) {break;}
			}
		Utils.sendString(r, actors.toString(), 200);
		} catch (JSONException e) {
			e.printStackTrace();
        	Utils.sendString(r,"", 500);			
		} catch (IOException e) {
			e.printStackTrace();
        	Utils.sendString(r,"", 500);			
		}
	}
	public void close() {
		driver.close();
	}

}
