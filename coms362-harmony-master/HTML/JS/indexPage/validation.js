let socket = io('http://localhost:3301');

socket.on('connection', function () {
    console.log('a user connected');

});

socket.on('disconnect', function () {
    console.log('user disconnected');
});

function validateForm() {
    const inputUsername = $("#InputUsername");
    const inputPassword = $("#InputPassword");
    const submitBtn = $("#submitBtn");

    let username = inputUsername.val();
    let password = inputPassword.val();
    let validUsername = validateUsername(username);
    let validPassword = validatePassword(password);

    if (!validUsername) {
        return false;
    }
    if (!validPassword) {
        return false;
    }
    let u = new User(username, password);
    console.log(u);
    // let doesExist = exist(u);
    // console.log(doesExist);
    // if (doesExist) {
    //     alert("Username already exist");
    //     return false;
    // }
    socket.emit('createUser', JSON.stringify(u));

    socket.on('userCreated', function (data) {
        console.log("User created!\n" + data);
    });

    return true;
}

function validateUsername(username) {
    if (username == null || username == "") {
        alert("Empty Username!");
        return false;
    }

    return true;
}

function validatePassword(password) {
    if (password == null || password == "") {
        alert("Empty Password!");
        return false;
    }
    if (password.length < 3) {
        alert("Password too short!");
        return false;
    }
    return true;
}

function exist(user) {

    socket.emit('existUser', JSON.stringify(user));

    let doesExist;
    socket.on('accounts', function (data) {
        console.log(data);
        doesExist = data;
    });

    return doesExist;
}
