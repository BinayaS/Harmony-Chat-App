package wss;

public interface MessageInterface {

	public int getMessageID();
	
	public void setMessageID(int newMessageID);
	
	public int getUserID();
	
	public void setUserID(int newUserID);
	
	public int getChannelID();
	
	public void setChannelID(int newChannelID);
	
	public String getContent();
	
	public void setContent(String newContent);
	
	public String getUserName();
	
}
