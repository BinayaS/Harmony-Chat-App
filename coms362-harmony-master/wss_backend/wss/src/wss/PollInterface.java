package wss;

import java.util.ArrayList;

import wss.PollObject.Vote;

public interface PollInterface {

	public void submitVote(UserObject user, int optionInd);
	
	public int getID();
	public String getQuestion();
	public ArrayList<String> getOptions();
	public ArrayList<Vote> getVotes();
	public int getVoteCount();
	public ChannelObject getChannel();
	
}
