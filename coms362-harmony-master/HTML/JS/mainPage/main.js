var noteBar = $('#NotificationsBar');
var noteText = document.getElementById("NotificationsText");
var socket = io.connect('http://localhost:3301');
var message = document.getElementById("msg");
var output = document.getElementById("output");
var btn = document.getElementById("button-addon1");
var userName = '';
var channelList = [];
var intendedUser;
var userList = [];
var friendList = [];
var messageList;
var userStatus;
var currentChannel = -1;
var userID = -1;
var messages = [];
var files = [];
var filesQuery = "";

var queryString = decodeURIComponent(window.location.search);
queryString = queryString.substring(1);
var queries = queryString.split("&");
userName = queries[0];

$('#inviteChannelModal').modal({ show: false });

/*
 * Send message to server
 */
btn.addEventListener('click', function(){
	socket.emit('chat', {
		message: message.value,
	});
	message.value = "";
});

/*
 * Acts as Enter button
 */
message.addEventListener("keydown", function(e) {
	if(e.keyCode == 13) {
		$('#button-addon1').click();
	}
});

/*
 * Set up channels, id, users, and friends
 */
socket.on('connect', function () {
    socket.emit('askForChannels');
    socket.emit('askForID', userName);
    socket.emit('askForMessages');
    socket.on("returnID", function (data) {
        var id = JSON.parse(data);
        if (userID == -1) {
            userID = id;
        }
    });
    socket.emit('askForUsers');
    socket.emit('askForFriends', userName);
    socket.emit("askForFiles");
});

socket.on("returnID", function (data) {
    if (userID == -1) {
        userID = data;
    }
    var obj = { userName: userName, selfID: userID };
    socket.emit('askForFriends', JSON.stringify(obj));});

/*
 * Populate channels list
 */
socket.on('returnChannels', function (data) {
	channelList = JSON.parse(data);
});

/*
 * Populate messages list
 */
socket.on('returnMessages', function (data) {
	messageList = JSON.parse(data);
});

/*
 * Populate users
 */
socket.on('returnUsers', function (data) {
	userList = JSON.parse(data);
});

/*
 * Populate friends
 */
socket.on('returnFriends', function (data) {
    console.log(data);
    var jData = JSON.parse(data);
    console.log(jData.selfID + ":" + userID);
    if (jData.selfID == userID) {
        $("#FriendText").html('<font class="text-primary" size="4">Friends</font> <br>');
        for (var i = 0; i < jData.friendList.length; i++) {
            friendList[i] = jData.friendList[i];
            $("#FriendText").append('<p class="text-primary">' + friendList[i] + " : " + jData.friendChannelList[i] + '</p>');
        }
        console.log("CHANNEL LIST: " + jData.friendChannelList);
        console.log("LIST:" + friendList);
    }
});

/*
 * Show stored message in chat
 */
socket.on('receive', function (data) {
    /* clear all messages */
    output.innerHTML = "";

	var obj = JSON.parse(data);
	for(var i = 0; i < obj.length; i += 1){
        var msg = obj[i];
        console.log(msg);
		if (msg.channelID == currentChannel) {
            console.log('appending ' + msg.userName + ': ' + msg.content);
			output.innerHTML += '<p><strong>' + msg.userName + ': </strong>' + msg.content + '</p>';
		}
	}
	console.log(obj);
});

/*
 * Send message to server
 */
function sendMessage() {
	var message = $('#msg').val();
	$('#msg').val('');
	var jsonMsg = { userName: this.userName, message: message, channelID: currentChannel };
	console.log(jsonMsg);
	socket.emit('message', jsonMsg);
}

/*
 * Create a channel
 */
function createChannel() {
    var name = $('#createChannelInput').val();

    var json = {channelName: name, userID: userID};
    socket.emit('newChannelID', JSON.stringify(json));
    socket.emit('askForChannels',"");
    socket.on('returnChannels', function (data) {
        channelList = JSON.parse(data);
    });
    name.value = "";
}

/*
 * Set status of user online
 */
function setOnline() {
	socket.emit('setOnline', userID);
	askForUsers();
}

/*
 * Set status of user offline
 */
function setOffline(){
	socket.emit('setOffline', userID);
	askForUsers();
}

/*
 * Request users
 */
function askForUsers() {
	socket.emit('askForUsers', -1);
}

/*
 * Delete channel
 */
function deleteChannel(theChannelID) {
    socket.emit('deleteChannelID', JSON.stringify({channelID: theChannelID, userID: userID}));
}

function setAdmin() {
    var destname = $('#setAdminInput').val();
    socket.emit('setAdmin', JSON.stringify({channelID: currentChannel, userID: userID, operandName: destname}));
}

function muteUser() {
    var destname = $('#muteUserInput').val();
    socket.emit('muteUser', JSON.stringify({channelID: currentChannel, userID: userID, operandName: destname}));
}

function banUser() {
    var destname = $('#banUserInput').val();
    socket.emit('banUser', JSON.stringify({channelID: currentChannel, userID: userID, operandName: destname}));
}

/*
 * Delete Channel Modal
 */
function deleteChannelModal() {
	for (var i = 0; i < channelList.length; i++) {
		$('#deleteChannelBody').append('<button type="button" onclick=deleteChannel(' + channelList[i].channelID + ')> Delete ' + channelList[i].name + '</button>');
	}
}

/*
 * Search User Modal
 */
function searchUserModal() {
	askForUsers();
	for (var j = 0; j < userList.length; j++) {
		console.log(userList[j].userName);
		if(userList[j].status) {
			userStatus = "success";
		} else {
			userStatus = "secondary";
		}

		$('#searchUsersBody').append( '<div class="dropdown">' +
			'<button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenu1" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" onclick=""> ' + userList[j].userName + '</button>' +
			'<span class="badge badge-' + userStatus + ' m-2" style="width: 10px; height: 10px;"> </span>' +
			'<div class="dropdown-menu" aria-labelledby="dropdownMenu1">' +
			'<a class="dropdown-item" onclick="messageUser('+userList[j].userID+')" data-toggle="modal" data-target="#directMessageModal"> Message User </a>' +
			'<a class="dropdown-item" onclick="addFriend('+userList[j].userID+')"> Add Friend </a>' +
			'<a class="dropdown-item" onclick="banUser('+userList[j].userID+')"> Ban User </a>' + 
			'<a class="dropdown-item" onclick="kickUser('+userList[j].userID+')"> Kick User </a>' +
			'<a class="dropdown-item" onclick="inviteUser('+userList[j].userID+')"> Invite User to Channel </a>' +
			'</div> </div> <br>');
	
	}
}

/*
 * Join a channel
 */
function joinChannel(theChannelID) {
	socket.emit('askForMessages');
	var channelIDtoJoin = theChannelID;
	var obj = { channelID: channelIDtoJoin, userID: userID };

	socket.emit('joinChannel', JSON.stringify(obj));

	socket.on('checkIfBanned', function (data) {
			if(!data){
					currentChannel = theChannelID;
					console.log('Current channel is ' + currentChannel);
					$('#chanTitle').html(currentChannel);
					/* clear all messages */
					output.innerHTML = "";
			}else{
				$('#chanTitle').html("Failed to join channel");
				/* clear all messages */
				output.innerHTML = "You are probably banned";
			}
	});
}

/*
 * Create a join channel modal
 */
function joinChannelModal() {
	for (var i = 0; i < channelList.length; i++) {
		$('#joinChannelBody').append('<button type="button" onclick=joinChannel(' + channelList[i].channelID + ')> Join ' + channelList[i].name + '</button>');
	}
}

/*
 * Change the channel status to none
 */
function leaveChannel() {
	currentChannel = -1;
	$('#chanTitle').html("Currently not in a Channel");
	/* clear all messages */
	output.innerHTML = "";
}

/*
 * Show a pane with a list of users
 */
$("#searchUserModal").on("hidden.bs.modal", function () {
	$("#searchUsersBody").html('Users: ');
});

/*
 * Show a pane with a list of channels to delete
 */
$("#deleteChannelModal").on("hidden.bs.modal", function () {
	$("#deleteChannelBody").html('Channels to Delete: ');
});

/*
 * Show a pane with a list of channels to
 */
$("#joinChannelModal").on("hidden.bs.modal", function () {
	$("#joinChannelBody").html('Channels to Join: ');
});

/*
 * Show a notification
 */
function showNotification(m) {
	noteText.innerHTML = m;
	noteBar.collapse("show");
	setTimeout(hideNotification, 3000)
}

/*
 * Hide the notification
 */
function hideNotification() {
	noteBar.collapse("hide");
}

/*
 * Start a new friendship <3
 */
function addFriend(friendID) {
  var obj = { friendID: friendID, userName: userName, selfID: userID };
  console.log(friendID);
  socket.emit('addFriend', JSON.stringify(obj));
}

/*
 * Asks to be friends with someone
 */
function askForFriends() {
	socket.emit('askForFriends', -1);
}

/*
 * Check friends channels
 */
function checkFriendChannel() {

}

/*
 * Message a user
 */
function messageUser(userID) {
	intendedUser = userID;
}

/*
 * Send a direct message to a user
 */
function sendDirectMessage() {
	var dm = $('#directMessageInput').val();
	$('#directMessageInput').val('');
	var obj = { message: dm, from: userID, to: intendedUser };
	socket.emit('directMessage',JSON.stringify(obj));

}

socket.on("sendDM", function (data) {
	var obj = JSON.parse(data);
	if (obj.to == userID) {
		showNotification(obj.message);
	}
});

/*
 * Invite user to current channel
 */
function inviteUser(invitedID) {
	var obj = { userID: userID, channelID: currentChannel, inviteID: invitedID };
	socket.emit("invite", JSON.stringify(obj));
}

/*
 * Kick User from current channel
 */
function kickUser(kickedID){
	var obj = { userID: userID, channelID: currentChannel, kickedID: kickedID };
	socket.emit("kick", JSON.stringify(obj));
}

/*
 * Ban User from current channel
 */
function banUser(bannedID){
	var obj = { userID: userID, channelID: currentChannel, bannedID: bannedID};
	socket.emit("ban", JSON.stringify(obj));
}

/*
 * Send invitation for channel to server
 */
socket.on("sendInvite", function (data) {
	var obj = JSON.parse(data);
	if (obj.userID == userID) {
		$('#inviteChannelBody').html('Someone invited you!<br><button type="button" onclick=joinChannel(' + obj.channelID + ') > Join?</button >');
		$('#inviteChannelBody').append('<br><button type="button" onclick="hideInvite()"> Decline</button >')
		$('#inviteChannelModal').modal('show');
	}
});

/*
 * Hide invite
 */
function hideInvite() {
    $('#inviteChannelModal').modal('hide');
    $('#inviteChannelBody').html('');
}

/*
 * Waits for list of files to be passed to client 
 */
socket.on("getFiles", function (data) {
    files = JSON.parse(data);
    //creates list to be passed to servlet
    for (var i = 0; i < files.length; i++) {
        filesQuery += files[i] + "&";
    }
});

//Handles the modal that contains file upload/download functionality
$("#myTrigger").on("click", function (e) {
    socket.emit("askForFiles");
    e.preventDefault();
    $("#myModal")
        .html('<object height="100%" type="text/html" data="http://localhost:8080/UploadServletApp/upload.jsp?' + filesQuery + '" ></object>');
    filesQuery = "";
});