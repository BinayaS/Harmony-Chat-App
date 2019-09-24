<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/3.3.1/jquery.min.js"/></script>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>File Upload</title>
</head>
<body>
    <div>
        <form method="post" action="UploadServlet" enctype="multipart/form-data">
            Select file to upload: <input type="file" name="uploadFile" />
            <br/><br/>
            <input type="submit" value="Upload" />
        </form>
    </div>
    <div id="UploadDownloadContainer"></div>
    <script>
    	var queryString = decodeURIComponent(window.location.search);
	    queryString = queryString.substring(1);
    	var queries = queryString.split("&");
    	var container = $("#UploadDownloadContainer");
    	for(var i = 0; i < queries.length-1; i++) {
			var reference = "DownloadServlet?"+queries[i];
    	 	$(container).append('<a href='+reference+'>Download</a><br>');
    	}
    </script>
</body>
</html>