package ca.yorku.eecs;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.io.UnsupportedEncodingException;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;
import static org.neo4j.driver.v1.Values.parameters;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;

public class Handler implements HttpHandler{
	Path p = new Path();
	ActorServices a = new ActorServices();
	Movie m = new Movie();
	Relationship r = new Relationship();
	@Override
	public void handle(HttpExchange request) throws IOException {
		try {
            if (request.getRequestMethod().equals("GET")) {
                handleGet(request);
            } else if (request.getRequestMethod().equals("PUT")) {
                handlePut(request);
            } else {
            	Utils.sendString(request, "", 501);		
            }
		} catch(Exception e) {
        	e.printStackTrace();
        	Utils.sendString(request,"", 500);			
		}
	}

	private void handleGet(HttpExchange request) throws IOException {
        String endpoint = Utils.getEndpoint(request);
		String body = Utils.getBody(request);
		try {
			JSONObject obj = new JSONObject(body);
			if (endpoint.equals("getCollaborators")) { //getCollaborators
				System.out.print("getCollaborators");
				a.getCollaborators(obj, request);
			} else if (endpoint.equals("getActor")) {	//getActor
				a.getActor(obj, request);
			} else if (endpoint.equals("getMovie")) {	//getMovie
				m.getMovie(obj, request);
			} else if (endpoint.equals("hasRelationship")) {	//hasRelationShip
				r.hasRelationShip(obj, request);
			} else if (endpoint.equals("computeBaconNumber")) {	//computeBaconNumber
				p.computeBaconNumber(obj, request);
			} else if (endpoint.equals("computeBaconPath")) {	//computeBaconPath
				p.computeBaconPath(obj, request);
			} else if (endpoint.equals("getPathToSigOthers")) {	//getPathToSigOthers
				p.getPathToSigOthers(obj, request);
				p.close();
			} else if (endpoint.equals("getPopularActorList")) {	//getPopularActorList
				a.getPopularActorList(obj, request);
			} else {
				Utils.sendString(request, "", 400);			
			}
			p.close();
			a.close();
			m.close();
			r.close();
		} catch(Exception e) {
        	e.printStackTrace();
        	Utils.sendString(request,"", 500);			
		}
	}

	private void handlePut(HttpExchange request) throws IOException {
        String endpoint = Utils.getEndpoint(request);
		String body = Utils.getBody(request);
		try {
			JSONObject obj = new JSONObject(body);
			if (endpoint.equals("addRelationship")) { //getCollaborators
				System.out.print("addRelationship");
				r.addRelationship(obj, request);
			} else if (endpoint.equals("addActor")) {	//addActor
				a.addActor(obj, request);
			} else if (endpoint.equals("addMovie")) {	//addMovie
				System.out.print("addMovie");
				m.addMovie(obj, request);
			} else {
				Utils.sendString(request, "", 400);			
			}
			p.close();
			a.close();
			m.close();
			r.close();
		} catch(Exception e) {
        	e.printStackTrace();
        	Utils.sendString(request, "", 500);			
		}
	}






}
