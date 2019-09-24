package wss;

import java.util.ArrayList;

//this shows the representation of a voting poll
public class PollObject implements PollInterface{
	//A class that has the information in a vote
	public class Vote {
		public int userID, optionIndex;
		
		public Vote(int userID, int optionIndex) {
			this.userID = userID;
			this.optionIndex = optionIndex;
		}
	}
	
	private String question;
	private ArrayList<String> options;
	private ArrayList<Vote> votes;
	private ChannelObject channel;
	private int pollID;
	
	private static int idCounter = 0;
	//Basic constructor
	public PollObject(ChannelObject channel, String question, ArrayList<String> options) {
		this.question = question;
		this.options = options;
		this.channel = channel;
		this.pollID = ++idCounter;
		this.votes = new ArrayList<>();
	}
	//submit vote to the poll
	public void submitVote(UserObject user, int optionInd) {
		/* check for duplicate votes */
		for (Vote i : votes) {
			if (i.userID == user.getUserID()) return;
		}
		
		/* check for out-of-range options */
		if (optionInd < 0 || optionInd >= options.size()) return;
		
		votes.add(new Vote(user.getUserID(), optionInd));
	}
	//returns poll ID
	public int getID() { return pollID; }
	//returns the question the poll asks
	public String getQuestion() { return question; }
	//returns the options the poll has
	public ArrayList<String> getOptions() { return options; }
	//returns the votes given to the poll
	public ArrayList<Vote> getVotes() { return votes; }
	//returns the vote count
	public int getVoteCount() { return votes.size(); }
	//returns the channel the poll is in
	public ChannelObject getChannel() { return channel; }
}
