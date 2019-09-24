package wss;

//this is the basic representation of what makes up a chat object
public class ChatObject implements ChatInterface {

	private String userName;
	private String message;
	private int channelID;

    public ChatObject() {
    }

    //Basic constructor
    public ChatObject(String userName, String message, int channelID) {
        super();
        this.userName = userName;
        this.message = message;
        this.channelID = channelID;
    }

    //returns user name
    public String getUserName() {
        return userName;
    }
    //sets user name
    public void setUserName(String userName) {
        this.userName = userName;
    }
    //returns channel ID
    public int getChannelID() {
    	return channelID;
    }
    //returns message body
    public String getMessage() {
        return message;
    }
    //sets message body
    public void setMessage(String message) {
        this.message = message;
    }

}
