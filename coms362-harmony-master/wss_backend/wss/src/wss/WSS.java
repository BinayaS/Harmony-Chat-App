package wss;

import java.sql.*;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.*;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
public class WSS {
	static ArrayList<ChannelObject> channelList = new ArrayList<ChannelObject>();

	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost:3306/?useSSL=false";
	// Database credentials
	static String USER = "root";
	static String PASS = "root";
	static Connection conn = null;


	static ArrayList<UserObject> userList = new ArrayList<>();
	static ArrayList<UserObject> friendList = new ArrayList<>();
	static ArrayList<File> filesList = new ArrayList<File>();
    static ArrayList<MessageObject> messageList = new ArrayList<>();
    static ArrayList<PollObject> pollList = new ArrayList<>();

    public static void main(String[] args) throws InterruptedException {
        final Configuration server_config = new Configuration();
        final int PORT = 3301;
        server_config.setPort(PORT);

        SocketConfig sconf = new SocketConfig();
        sconf.setReuseAddress(true);
        server_config.setSocketConfig(sconf);

        final SocketIOServer sv = new SocketIOServer(server_config);
        int userID;
        String userName = null;
        String userPassword = null;

        BadWords.loadConfigs();

        initializeDatabase();
        initializeUsers();
        initializeChannels();
        initializeFiles();
        
        initializeMessages();

        addConnectListeners(sv);
        addEventListeners(sv);
        createAndSendUserList(sv, userList);

        sv.start();

        System.out.println("Started socket.io server on port " + PORT);
        Thread.sleep(Integer.MAX_VALUE);
        sv.stop();
    }

    private static void initializeDatabase() {
        /* connect to the database and initialize the required structure */

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            /* ensure correct database structure */
            Statement s = conn.createStatement();

            /* initialize schemas */
            s.execute("create schema if not exists harmony");

            /* initialize tables */
            s.execute("create table if not exists harmony.users (userID int auto_increment, userName varchar(45), userPassword varchar(45), primary key (userID), unique key (userName))");
            s.execute("create table if not exists harmony.channels (channelID int auto_increment, channelName varchar(45), ownerID int, channelMessage varchar(1000), primary key (channelID), unique key (channelName))");
            s.execute("create table if not exists harmony.privileges (userID int, channelID int)");
            s.execute("create table if not exists harmony.mutes (userID int, channelID int)");
            s.execute("create table if not exists harmony.bans (userID int, channelID int)");
            s.execute("create table if not exists harmony.messages (messageID int auto_increment, userID int, channelID int, content varchar(1000), pinned boolean, timestamp datetime, primary key (messageID))");
            s.execute("create table if not exists harmony.files (fileName varchar(100), filePath varchar(150))");
        } catch (SQLException e) {
            e.printStackTrace(); /* on connection/query failure */
        } catch (ClassNotFoundException e) {
            e.printStackTrace(); /* on driver registration failure */
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {}
        }
    }

    private static void initializeUsers() {
        /* synchronize users from database */

        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            /* query user table */
            Statement s = conn.createStatement();
            ResultSet res = s.executeQuery("select * from harmony.users");

            userList = new ArrayList<>();
            while (res.next()) {
                userList.add(new UserObject(res.getString("userName"),
                                            res.getString("userPassword"),
                                            res.getInt("userID")));
            }

            System.out.println("Synchronized " + userList.size() + " users");
        } catch (SQLException e) {
            e.printStackTrace(); /* on connection/query failure */
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {}
        }
    }

    private static void initializeChannels() {
        /* synchronize channels from database */
        channelList = new ArrayList<>();

        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            /* query channel table */
            Statement s = conn.createStatement();
            ResultSet res = s.executeQuery("select * from harmony.channels");

            channelList = new ArrayList<>();
            while (res.next()) {
                channelList.add(new ChannelObject(res.getInt("channelID"),
                                                  res.getString("channelName"),
                                                  res.getInt("ownerID")));
            }

            System.out.println("Synchronized " + channelList.size() + " channels");
        } catch (SQLException e) {
            e.printStackTrace(); /* on connection/query failure */
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {}
        }
    }
    
    private static void initializeMessages() {
        /* synchronize messages from database */
        messageList = new ArrayList<>();

        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            /* query channel table */
            Statement s = conn.createStatement();
            ResultSet res = s.executeQuery("select * from harmony.messages");

            messageList = new ArrayList<>();
            while (res.next()) {
            	UserObject user = getUser(res.getInt("userID"), userList);
            	
                messageList.add(new MessageObject(res.getInt("messageID"), 
                								  res.getInt("userID"),
                								  res.getInt("channelID"),
                								  user.getUserName(),
                                                  res.getString("content"),
                                                  res.getBoolean("pinned")));
            }

            System.out.println("Synchronized " + messageList.size() + " messages");
        } catch (SQLException e) {
            e.printStackTrace(); /* on connection/query failure */
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {}
        }
    }

	private static void initializeFiles() {
		/* synchronize files from database */
		filesList = new ArrayList<File>();

		try {
			conn = DriverManager.getConnection(DB_URL, USER, PASS);

			/* query channel table */
			Statement s = conn.createStatement();
			ResultSet res = s.executeQuery("select * from harmony.files");

			while (res.next()) {
				filesList.add(new File("../upload/" + res.getString("fileName")));
				// fileNameList.add(res.getString("fileName"));
			}

			System.out.println("Synchronized " + filesList.size() + " files");
		} catch (SQLException e) {
			e.printStackTrace(); /* on connection/query failure */
		} finally {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
			}
		}
	}
	
	private static void addFiles() {

		File folder = new File("../upload");
		File[] filesArr = folder.listFiles();

		
		try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);

            Statement stmt = conn.createStatement();
    		
    		for(int i = 0; i<filesArr.length; i++) {
    			if(!filesList.contains(filesArr[i])) {
    	            stmt.execute("insert into harmony.files(fileName,filePath) values ('"+filesArr[i].getName()+"','"+filesArr[i].getAbsolutePath()+"')");
    			}
    		}            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initializeFiles();
    }


	// Method for handling user list
	private static void createAndSendUserList(SocketIOServer sv, ArrayList<UserObject> list) {
		sv.getBroadcastOperations().sendEvent("returnUsers", writeObjectToJSON(list));
	}

	// Method for handling poll list
	private static void createAndSendPollList(SocketIOServer sv, ArrayList<PollObject> list) {
		sv.getBroadcastOperations().sendEvent("sendPolls", writeObjectToJSON(list));
	}

	// Method that contains all event listeners
	private static void addEventListeners(SocketIOServer sv) {

		// Event listener for clients asking for files
		sv.addEventListener("askForFiles", String.class, new DataListener<String>() {
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
				addFiles();
				sv.getBroadcastOperations().sendEvent("getFiles", writeObjectToJSON(filesList));

			}
		});

		// Event listener for handling messages
		sv.addEventListener("message", ChatObject.class, new DataListener<ChatObject>() {
			@Override
			public void onData(SocketIOClient client, ChatObject data, AckRequest ackRequest) throws SQLException {
				// Profanity Filter
				String originalMsg = data.getMessage();
				boolean containsBadWords = BadWords.badWordsFound(originalMsg);
				if (containsBadWords)
					data.setMessage("This message contains a bad word :(");

				// broadcast messages to all clients
				if (data.getChannelID() != -1) {

					/* get sending user */
					UserObject user = getUserID(data.getUserName(), userList);

					/* before sending, verify the user is not muted */
					try {
						conn = DriverManager.getConnection(DB_URL, USER, PASS);

						Statement stmt = conn.createStatement();
						if (stmt.executeQuery("select * from harmony.mutes where channelID=" + data.getChannelID()
								+ " and userID=" + user.getUserID()).next()) {
							return;
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}

					try {
						conn = DriverManager.getConnection(DB_URL, USER, PASS);

						Statement stmt = conn.createStatement();
						stmt.execute("insert into harmony.messages values (0, " + user.getUserID() + ", "
								+ data.getChannelID() + ", '" + data.getMessage() + "',false, NOW())");
					} catch (SQLException e) {
						e.printStackTrace();
					}

					initializeMessages();
					sv.getBroadcastOperations().sendEvent("receive", writeObjectToJSON(messageList));
				}
			}
		});

		// Event listener for handling messages
		sv.addEventListener("askForMessages", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
				// broadcast messages to all clients

				sv.getBroadcastOperations().sendEvent("returnMessages", writeObjectToJSON(messageList));
			}
		});

		// Event listener for handling when user tries to join a channel
		sv.addEventListener("joinChannel", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
				Object obj = null;
				try {
					obj = new JSONParser().parse(data);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				JSONObject jo = (JSONObject) obj;
				for (UserObject user : userList) {
					Long lid = (Long) jo.get("userID");
					if (user.getUserID() == Integer.parseInt(lid.toString())) {
						for (ChannelObject channel : channelList) {
							Long lid2 = (Long) jo.get("channelID");
							if (channel.getID() == Integer.parseInt(lid2.toString())) {

								/* before sending, verify the user is not muted */
								try {
									conn = DriverManager.getConnection(DB_URL, USER, PASS);

									Statement stmt = conn.createStatement();
									if (stmt.executeQuery("select * from harmony.bans where channelID="
											+ channel.getID() + " and userID=" + user.getUserID()).next()) {
										client.sendEvent("checkIfBanned", true);
										return;
									} else {
										client.sendEvent("checkIfBanned", false);
										user.setCurrentChannel(channel);
										client.sendEvent("receive", writeObjectToJSON(messageList));
									}
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		});

		// Event listener for creating new channels
		sv.addEventListener("newChannelID", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
				Object obj = null;

				try {
					obj = new JSONParser().parse(data);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				JSONObject jo = (JSONObject) obj;

				/* try and insert channel entry in db */
				try {
					String name = (String) jo.get("channelName");
					Long ownerID = (Long) jo.get("userID");

					conn = DriverManager.getConnection(DB_URL, USER, PASS);

					Statement stmt = conn.createStatement();
					stmt.execute("insert into harmony.channels values (0, '" + name + "', " + ownerID + ", '')");
					System.out.println("User " + ownerID + " created channel " + name);
				} catch (SQLException e) {
					e.printStackTrace();
				}

				/* sync channels from db before sending back */
				initializeChannels();
				sv.getBroadcastOperations().sendEvent("returnChannels", writeObjectToJSON(channelList));
			}
		});

		// Event listener for client asking for list of channels
		sv.addEventListener("askForChannels", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
				// broadcast messages to all clients
				sv.getBroadcastOperations().sendEvent("returnChannels", writeObjectToJSON(channelList));
			}
		});

		// Event listener for deleting channels
		sv.addEventListener("deleteChannelID", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
				/* delete a channel if the user has rights */
				Object obj = null;

				try {
					obj = new JSONParser().parse(data);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				JSONObject jo = (JSONObject) obj;

				Long callerID = (Long) jo.get("userID");
				Long channelID = (Long) jo.get("channelID");

				UserObject caller = getUser(callerID.intValue(), userList);
				ChannelObject chan = getChannel(channelID.intValue());

				if (!getUserPrivilege(caller, chan))
					return;

				try {
					conn = DriverManager.getConnection(DB_URL, USER, PASS);

					Statement stmt = conn.createStatement();
					stmt.execute("delete from harmony.channels where channelID=" + chan.getID());
					System.out.println("deleted channel " + chan.getName());
				} catch (SQLException e) {
					e.printStackTrace();
				}

				initializeChannels();
				sv.getBroadcastOperations().sendEvent("returnChannels", writeObjectToJSON(channelList));
			}
		});

		// Event listener for when users want the user list
		sv.addEventListener("searchUserID", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
				if (getUser(Integer.parseInt(data), userList) != null) {
					userList.add(getUser(Integer.parseInt(data), userList));
				}
				sv.getBroadcastOperations().sendEvent("returnUsers", writeObjectToJSON(userList));
			}
		});

		// Event listener for getting ID of specific user
		sv.addEventListener("askForID", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
				UserObject u = getUserID(data, userList);
				sv.getBroadcastOperations().sendEvent("returnID", u.getUserID());
			}
		});

		// Event listener for setting a user online
		sv.addEventListener("setOnline", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {

				UserObject u = getUser(Integer.parseInt(data), userList);
				if (u != null) {
					u.setStatus(true);
					System.out.println(u.getUserName() + " status set to " + u.getStatus());
				}
			}
		});

		// Event listener for setting a user offline
		sv.addEventListener("setOffline", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {

				UserObject u = getUser(Integer.parseInt(data), userList);
				if (u != null) {
					u.setStatus(false);
					System.out.println(u.getUserName() + " status set to " + u.getStatus());
				}
			}
		});

		// Event listener for getting user list
		sv.addEventListener("askForUsers", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {

				sv.getBroadcastOperations().sendEvent("returnUsers", writeObjectToJSON(userList));
			}
		});

		// Event listener for when someone adds a friend
		sv.addEventListener("addFriend", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
				Object obj = null;
				try {
					obj = new JSONParser().parse(data);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				JSONObject jo = (JSONObject) obj;
				UserObject u = getUserID((String) jo.get("userName"), userList);
				System.out.println(jo);
				Long result = (Long) jo.get("friendID");
				u.addFriend(Integer.parseInt(result.toString()));

				class returnClass {
					public int requestUserID;
					public ArrayList<Integer> friendsList;
				}

				ObjectMapper mapper = new ObjectMapper();

				returnClass returnObj = new returnClass();
				returnObj.requestUserID = u.getUserID();
				returnObj.friendsList = u.getFriends();

				sv.getBroadcastOperations().sendEvent("returnFriends", writeObjectToJSON(returnObj));
			}
		});

		// Event listener for returning the friends of a specific user
		sv.addEventListener("askForFriends", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
				// broadcast messages to all clients
				UserObject u = getUserID(data, userList);
				System.out.println(u.getFriends().toString());

				class returnClass {
					public int requestUserID;
					public ArrayList<Integer> friendsList;
				}

				ObjectMapper mapper = new ObjectMapper();

				returnClass returnObj = new returnClass();
				returnObj.requestUserID = u.getUserID();
				returnObj.friendsList = u.getFriends();

				sv.getBroadcastOperations().sendEvent("returnFriends", writeObjectToJSON(returnObj));
			}
		});

		// Event listener for handling direct messages
		sv.addEventListener("directMessage", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
				System.out.println(data);
				sv.getBroadcastOperations().sendEvent("sendDM", data);
			}
		});

		// Event listener for inviting user to a channel
		sv.addEventListener("invite", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
				Object obj = null;
				try {
					obj = new JSONParser().parse(data);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				sv.getBroadcastOperations().sendEvent("sendInvite", data);
			}
		});

		// Event listener for checking if user exists
		sv.addEventListener("existUser", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
				ObjectMapper mapper = new ObjectMapper();
				UserObject newUser = null;
				try {
					newUser = mapper.readValue(data, UserObject.class);
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (!(userList.isEmpty())) {
					for (UserObject user : userList) {
						String name = user.getUserName();
						if (name.equals(newUser.getUserName())) {
							System.out.println("User Exist!");
							sv.getBroadcastOperations().sendEvent("accounts", writeObjectToJSON(true));
							return;
						}
					}
				}
				sv.getBroadcastOperations().sendEvent("accounts", writeObjectToJSON(false));
			}
		});

		//Event listener for handling the creation of users
		sv.addEventListener("createUser", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
				ObjectMapper mapper = new ObjectMapper();
				UserObject newUser = null;
				try {
					newUser = mapper.readValue(data, UserObject.class);

					newUser.setUserID(userList.size());
					System.out.println(newUser);
					userList.add(newUser);

					try {
						conn = DriverManager.getConnection(DB_URL, USER, PASS);

						Statement statement = conn.createStatement();
						int rs;
						int ID = newUser.getUserID() + 1;
						String sql = "INSERT INTO harmony.users" + " VALUES (0, '" + newUser.getUserName() + "', '"
								+ newUser.getUserPassword() + "')";
						System.out.println(sql);
						rs = statement.executeUpdate(sql);

						// Close all statements and connections
						statement.close();

					} catch (SQLException e) {
						e.printStackTrace();
					} finally {
						try {
							if (conn != null)
								conn.close();
						} catch (SQLException e) {
						}
					}

					initializeUsers();
					sv.getBroadcastOperations().sendEvent("userCreated", writeObjectToJSON(true));
				} catch (JsonParseException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		//Event listener for setting a user to admin level
		sv.addEventListener("setAdmin", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
				/* set user admin privileges for channel */

				/*
				 * NOTE: we depend on the user to be honest about who they are. this allows
				 * arbitrary privilege escalation if the channel's owner is known
				 * 
				 * to mitigate this we need to associate connections with users.
				 */

				Object obj = null;

				try {
					obj = new JSONParser().parse(data);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				JSONObject jo = (JSONObject) obj;

				Long callerID = (Long) jo.get("userID");
				String operandName = (String) jo.get("operandName");
				Long channelID = (Long) jo.get("channelID");

				UserObject user = getUser(callerID.intValue(), userList);
				UserObject destuser = getUserID(operandName, userList);
				ChannelObject chan = getChannel(channelID.intValue());

				if (!getUserPrivilege(user, chan)) {
					System.out.println("Denied setAdmin request from " + user.getUserName());
					return;
				}

				if (getUserPrivilege(destuser, chan))
					return;

				try {
					conn = DriverManager.getConnection(DB_URL, USER, PASS);

					Statement stmt = conn.createStatement();
					stmt.execute("insert into harmony.privileges values (" + destuser.getUserID() + ", " + chan.getID()
							+ ")");
					System.out.println(user.getUserName() + " granted rights on channel " + chan.getName() + " to "
							+ destuser.getUserName());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});

		//Event listener for muting users
		sv.addEventListener("muteUser", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
				Object obj = null;

				try {
					obj = new JSONParser().parse(data);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				JSONObject jo = (JSONObject) obj;

				Long callerID = (Long) jo.get("userID");
				String operandName = (String) jo.get("operandName");
				Long channelID = (Long) jo.get("channelID");

				UserObject user = getUser(callerID.intValue(), userList);
				UserObject destuser = getUserID(operandName, userList);
				ChannelObject chan = getChannel(channelID.intValue());

				if (!getUserPrivilege(user, chan)) {
					System.out.println("Denied muteUser request from " + user.getUserName());
					return;
				}

				try {
					conn = DriverManager.getConnection(DB_URL, USER, PASS);

					Statement stmt = conn.createStatement();
					stmt.execute(
							"insert into harmony.mutes values (" + destuser.getUserID() + ", " + chan.getID() + ")");
					System.out
							.println(user.getUserName() + " muted " + destuser.getUserName() + " in " + chan.getName());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});

		//Event listener for banning users
		sv.addEventListener("banUser", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
				Object obj = null;

				try {
					obj = new JSONParser().parse(data);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				JSONObject jo = (JSONObject) obj;

				Long callerID = (Long) jo.get("userID");
				String operandName = (String) jo.get("operandName");
				Long channelID = (Long) jo.get("channelID");

				UserObject user = getUser(callerID.intValue(), userList);
				UserObject destuser = getUserID(operandName, userList);
				ChannelObject chan = getChannel(channelID.intValue());

				if (!getUserPrivilege(user, chan)) {
					System.out.println("Denied banUser request from " + user.getUserName());
					return;
				}

				try {
					conn = DriverManager.getConnection(DB_URL, USER, PASS);

					Statement stmt = conn.createStatement();
					stmt.execute(
							"insert into harmony.bans values (" + destuser.getUserID() + ", " + chan.getID() + ")");
					System.out.println(
							user.getUserName() + " banned " + destuser.getUserName() + " in " + chan.getName());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});

		//Event listener for the creation of polls
		sv.addEventListener("createPoll", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
				/* create a new poll and broadcast it to the other users in the channel */

				System.out.println("createPoll: " + data);
				Object obj = null;

				try {
					obj = new JSONParser().parse(data);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				JSONObject jo = (JSONObject) obj;

				ArrayList<String> options = (ArrayList<String>) jo.get("options");
				String question = (String) jo.get("question");
				Long callerID = (Long) jo.get("userID");
				Long channelID = (Long) jo.get("channelID");

				UserObject user = getUser(callerID.intValue(), userList);
				ChannelObject chan = getChannel(channelID.intValue());

				/* test that the user is not muted on the channel */
				try {
					conn = DriverManager.getConnection(DB_URL, USER, PASS);

					Statement stmt = conn.createStatement();
					if (stmt.executeQuery("select * from harmony.mutes where channelID=" + chan.getID() + " and userID="
							+ user.getUserID()).next()) {
						return;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}

				/* create the poll object and store it */
				pollList.add(new PollObject(chan, question, options));

				/* broadcast polls to clients */
				createAndSendPollList(sv, pollList);
			}
		});

		//Event listener for handling voting for polls
		sv.addEventListener("votePoll", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
				/* create a new poll and broadcast it to the other users in the channel */

				System.out.println("votePoll: " + data);
				Object obj = null;

				try {
					obj = new JSONParser().parse(data);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				JSONObject jo = (JSONObject) obj;

				Long callerID = (Long) jo.get("userID");
				Long pollID = (Long) jo.get("pollID");
				int optionInd = ((Long) jo.get("optionInd")).intValue();

				UserObject user = getUser(callerID.intValue(), userList);

				/* find poll and submit vote */
				for (PollObject p : pollList) {
					if (p.getID() == pollID.intValue()) {
						p.submitVote(user, optionInd);
					}
				}

				/* broadcast polls to clients */
				createAndSendPollList(sv, pollList);
			}
		});

		//Event listener for handling the deletion of messages
		sv.addEventListener("deleteMessage", String.class, new DataListener<String>() {
			@Override
			public void onData(SocketIOClient client, String data, AckRequest ackRequest) {
				/* delete a message if the caller owns it */

				System.out.println("deleteMessage: " + data);
				Object obj = null;

				try {
					obj = new JSONParser().parse(data);
				} catch (ParseException e) {
					e.printStackTrace();
				}

				JSONObject jo = (JSONObject) obj;

				Long callerID = (Long) jo.get("userID");
				Long messageID = (Long) jo.get("messageID");

				/* format the query to check for ownership */
				try {
					conn = DriverManager.getConnection(DB_URL, USER, PASS);

					Statement stmt = conn.createStatement();
					stmt.executeQuery(
							"delete from harmony.messages where messageID=" + messageID + " and userID=" + callerID);
				} catch (SQLException e) {
					e.printStackTrace();
				}

				/* broadcast new message table to clients */
				initializeMessages();
				sv.getBroadcastOperations().sendEvent("receive", writeObjectToJSON(messageList));
			}
		});
	}

	//Returns the channel object related to the channel ID
	private static ChannelObject getChannel(int ID) {
		ChannelObject channel = null;
		for (ChannelObject chan : channelList) {
			if (chan.getID() == ID) {
				channel = chan;
			}
		}
		return channel;
	}

	//Returns user object related to the user ID
	private static UserObject getUser(int UserID, ArrayList<UserObject> userList) {
		UserObject user = null;
		for (UserObject usr : userList) {
			if (usr.getUserID() == UserID) {
				user = usr;
			}
		}
		return user;
	}

	//Returns the user object with given userName
	private static UserObject getUserID(String userName, ArrayList<UserObject> userList) {
		UserObject user = null;
		for (UserObject usr : userList) {
			if (usr.getUserName().equals(userName)) {
				user = usr;
			}
		}
		return user;
	}

	//Checks if a user is an admin for a channel
	private static boolean getUserPrivilege(UserObject user, ChannelObject chan) {
		/* determine whether a user has administrator status on a channel */
		System.out.println("Checking if " + user.getUserName() + " has privileges on " + chan.getName());

		if (chan.getAdminID() == user.getUserID()) {
			return true; /* user owns channel */
		}

		try {
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			Statement stmt = conn.createStatement();

			if (stmt.executeQuery("select * from harmony.channels where channelID=" + chan.getID() + " and ownerID="
					+ user.getUserID()).next()) {
				return true; /* user owns channel */
			}

			if (stmt.executeQuery("select * from harmony.privileges where userID=" + user.getUserID()
					+ " and channelID=" + chan.getID()).next()) {
				return true; /* user has assigned rights from setAdmin */
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("no");
		return false;
	}

	//Tells server a client is connected
	private static void addConnectListeners(SocketIOServer sv) {
		sv.addConnectListener(new ConnectListener() {
			public void onConnect(SocketIOClient cl) {
				System.out.println("Accepted socket.io client " + cl.toString());
			}
		});
	}
	//Writes objects for client into JSON format
	private static String writeObjectToJSON(Object obj) {
		ObjectMapper om = new ObjectMapper();
		StringWriter sw = new StringWriter();

		try {
			om.writeValue(sw, obj);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sw.toString();
	}

	//Work around since default JSON numbers being longs
	private static int longToInt(Object object) {
		return Integer.parseInt(object.toString());
	}
}
