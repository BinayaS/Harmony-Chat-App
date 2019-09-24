package wss;

import java.util.ArrayList;


//Represents the data that users are associated with
public class UserObject implements UserInterface{

    private String userName;
    private String userPassword;
    private ArrayList<Integer> listFriends;
    private ChannelObject currentChannel;
    private int userID;
    private boolean status;
    
    public UserObject() {
    }
    
    //Basic constructor
    public UserObject(String userName, String userPassword, int userID) {
        super();
        this.userName = userName;
        this.userPassword = userPassword;
        this.listFriends = new ArrayList<Integer>();
		this.currentChannel = null;
		this.userID = userID;
        this.status = false;
    }

    //returns user name
    public String getUserName() {
        return userName;
    }
    //sets user name
    public void setUserName(String userName) {
        this.userName = userName;
    }
    //returns user password
    public String getUserPassword() {
    	return userPassword;
    }
    //sets user password
    public void setUserPassword(String userPassword) {
    	this.userPassword = userPassword;
    }
    //adds a friend
	public void addFriend(int friend) {
		listFriends.add(friend);			
	}
	//returns friends list
	public ArrayList<Integer> getFriends(){
		return listFriends;
	}
    //returns current channel
    public ChannelObject getCurrentChannel() {
    	return currentChannel;
    }
    //sets current channel
    public void setCurrentChannel(ChannelObject currentChannel) {
    	this.currentChannel = currentChannel;
    }

    //removes channel from user
    public void removeCurrentChannel() {
    	this.currentChannel = null;
    }
    
    //returns user ID

    public int getUserID() {
        return userID;
    }
    //set user ID
    public void setUserID(int userID) {
        this.userID = userID;
    }
    //returns user status
    public boolean getStatus() {
    	return status;
    }
    //set user status
    public void setStatus(boolean status) {
    	this.status = status;
    }
    

}
