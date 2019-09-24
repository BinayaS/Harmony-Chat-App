package wss;

public interface UserInterface {
	   public String getUserName();
	   
	   public void setUserName(String userName);
	    
	   public String getUserPassword();
	    
	   public void setUserPassword(String userPassword);
	    
	   public void addFriend(int friend);

	   public ChannelObject getCurrentChannel();
	    
	   public void setCurrentChannel(ChannelObject currentChannel);
	    
	   public int getUserID();
	    
	   public void setUserID(int userID);
	    
	   public boolean getStatus();
	    
	   public void setStatus(boolean status);
}
