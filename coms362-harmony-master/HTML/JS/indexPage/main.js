(function(){
    $(document).ready(async function (e) {

	const inputName = $("#InputName");
	const inputUsername = $("#InputUsername");
	const inputPassword = $("#InputPassword");
	const inputEmail = $("#InputEmail");
	const submitBtn = $("#submitBtn");

	//submitBtn.click(function() {
	function validateForm(){
	    let validName = validateName(getNameVal());
	    let validUsername = validateUsername(getUsernameVal());
	    let validPassword = validatePassword(getPasswordVal());
	    let validEmail = validateEmail(getEmailVal());

	    if(!validEmail){
		alert("Invalid Email");
		return false;
	    }
	    if (!validName) {
		alert("Invalid Name");
		return false;
	    }
	    if(!validUsername){
		alert("Invalid Username");
		return false;
	    }
	    if (!validPassword){
		alert("Invalid Password");
		return false;
	    }

	    let u = new User(getNameVal(), getPasswordVal(), getEmailVal());
	    console.log(u);

	    return true;
	};


	function getNameVal(){
	    return inputname.val();
	}

	function getUsernameVal(){
	    return inputusername.val();
	}

	function getPasswordVal(){
	    return inputpassword.val();
	}

	function getEmailVal(){
	    return inputemail.val();
	}

	let socket = io('http://localhost:3301');

	socket.on('connection', function(){
	    console.log('a user connected');

	});

	socket.on('accounts', function(data){
		conosle.log(data);
	});

	socket.on('disconnect', function(){
	    console.log('user disconnected');
	});
    });
})()
