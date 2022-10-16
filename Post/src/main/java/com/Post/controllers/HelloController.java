package com.Post.controllers;

import org.springframework.web.bind.annotation.*;

import java.sql.*;

@RestController
public class HelloController {

	@GetMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}

	@GetMapping("/user")
	public String sayHell(@RequestParam(value = "myName", defaultValue = "World") String name) {

		return String.format(mysql_query("jdbc:mysql://localhost:3306/e6156_project","root","dbuserdbuser","SELECT * FROM  e6156_project.users"), name);
	}

	@RequestMapping("/user/{someID}")
	public @ResponseBody String getAttr(@PathVariable(value="someID") String id,
										String someAttr) {
		return String.format(mysql_query("jdbc:mysql://localhost:3306/e6156_project","root","dbuserdbuser","SELECT * FROM e6156_project.users WHERE first_name ='"+id+"'" ), id);
	}



	public String mysql_query(String  DB_URL, String USER, String PASS,String QUERY) {

		//String DB_URL = "jdbc:mysql://localhost:3306/e6156_project";
		//String USER = "root";
		//String PASS = "dbuserdbuser";
		//String QUERY = "SELECT * FROM  e6156_project.users";
		System.out.println(QUERY);

		String result = null;
		try (
				Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(QUERY);


		) {
			// Extract data from result set
			while (rs.next()) {
				// Retrieve by column name

				result = ("email: " + rs.getString("email"));
				result = result + " " + ("User_Name: " + rs.getString("User_Name"));
				System.out.println(result);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

}
