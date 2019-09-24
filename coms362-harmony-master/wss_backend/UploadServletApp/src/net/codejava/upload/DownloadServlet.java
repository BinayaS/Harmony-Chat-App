package net.codejava.upload;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
 
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
 
//Servlet implementation class for DownloadServlet
public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final String UPLOAD_DIRECTORY = "upload/";
 
    //A Java servlet that handles file download to client.
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String url = request.getQueryString();
		String myFile = FilenameUtils.getBaseName(url)
                + "." + FilenameUtils.getExtension(url);
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		//Gets path the files are stored
		String filePath = getServletContext().getRealPath("")
	            + "../../../../../../../" + UPLOAD_DIRECTORY;
		
		response.setContentType("APPLICATION/OCTET-STREAM");
		response.setHeader("Content-Disposition", "attachment; filename=\""
				+ myFile + "\"");
 
		FileInputStream fileInputStream = new FileInputStream(filePath
				+ myFile);
 
		//writes file to client
		int i;
		while ((i = fileInputStream.read()) != -1) {
			out.write(i);
		}
		fileInputStream.close();
		out.close();
	}
 
}