package wss;

//this represents the data in a channel
public class ChannelObject implements ChannelInterface {

	public int channelID;
	public String name;
	public int adminID;
	
	public ChannelObject() {
		//Dummy Constructor
	}
	
	//basic constructor
	public ChannelObject(int givenChannelID, String givenName, int adminID) {
		channelID = givenChannelID;
		name = givenName;
		this.adminID = adminID;
	}
	//returns channel name
	public String getName() {
		return name;
	}
	//sets channel name
	public void setName(String newName) {
		name = newName;
	}
	//returns channel admin's ID
	public int getAdminID() {
		return adminID; 
	}
	//return channel ID
	public int getID() {
		return channelID;
	}
}
