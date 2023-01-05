package ca.yorku.eecs;

public class Actor implements Comparable<Actor>{
	String name;
	String id;
	double revenue;
	public Actor() {
		
	}
	public Actor(String name, String id, double revenue) {
		this.name = name;
		this.id = id;
		this.revenue = revenue;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public double getRevenue() {
		return revenue;
	}
	public void setRevenue(double revenue) {
		this.revenue = revenue;
	}
	@Override
	public int compareTo(Actor other) {
		return Double.compare(this.getRevenue(), other.getRevenue());
	}
}
