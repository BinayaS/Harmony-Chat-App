package wss;

//class for messages being handled by the database
public class MessageObject implements MessageInterface{

	public int messageID;
	public int userID;
	public String userName;
	public int channelID;
	public String content;
	public boolean pinned;
	
	public MessageObject() {
		//Dummy Constructor
	}

	//basic constructor
	public MessageObject(int messageID, int userID, int channelID, String userName, String content, boolean pinned) {
		this.messageID = messageID;
		this.userID = userID;
		this.channelID = channelID;
		this.content = content;
		this.userName = userName;
		this.pinned = pinned;
	}
	//return message ID
	public int getMessageID() {
		return this.messageID; 
	}
	//set message ID
	public void setMessageID(int newMessageID) {
		this.messageID = newMessageID;
	}
	//returns user ID
	public int getUserID() {
		return this.userID; 
	}
	//sets user ID
	public void setUserID(int newUserID) {
		this.userID = newUserID;
	}
	//returns channel ID
	public int getChannelID() {
		return this.channelID; 
	}
	//sets channel ID
	public void setChannelID(int newChannelID) {
		this.channelID = newChannelID;
	}
	//returns message content
	public String getContent() {
		return this.content;
	}
	//sets message content
	public void setContent(String newContent) {
		this.content = newContent;
	}
	//returns user name that sent message
	public String getUserName() {
		return userName;
	}
}
