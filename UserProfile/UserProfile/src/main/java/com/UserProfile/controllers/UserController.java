package com.UserProfile.controllers;
import com.UserProfile.dao.UserDAO;
import com.UserProfile.model.UserProfile;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.*;
import java.util.Map;
import java.util.UUID;


@RestController
public class UserController {
	private final UserDAO dao;

	public UserController (UserDAO dao){
		this.dao=dao;
	}

	@PostMapping("/newuser")
	public UserProfile addUserProfile(@RequestBody UserProfile user) {
		dao.save(user);
		return user;
	}


	@GetMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}

	@GetMapping("/user")
	public String sayHell(@RequestParam(value = "myName", defaultValue = "World") String name) {

		return String.format(mysql_query("jdbc:mysql://localhost:3306/e6156_project","root","dbuserdbuser","SELECT * FROM  e6156_project.users"), name);
	}
	@RequestMapping("/user/{someID}")
	public @ResponseBody String getAttr(@PathVariable(value="someID") String id, String someAttr) {
		return String.format(mysql_query("jdbc:mysql://localhost:3306/e6156_project","root","dbuserdbuser","SELECT * FROM e6156_project.users WHERE first_name ='"+id+"'" ), id);
	}

	@GetMapping("/testuser")
	public String basic(@RequestParam(value = "myName", defaultValue = "World") String name) {

		return String.format(mysql_query("jdbc:mysql://users-e6156.cexqeqvqreq2.us-east-1.rds.amazonaws.com:3306/UserData?autoReconnect=true&useSSL=false","root","dbuserdbuser","SELECT * FROM  UserData.user_profile"), name);
	}

	@RequestMapping("/testuser/{someID}")
	public @ResponseBody String getname(@PathVariable(value="someID") String id, String someAttr) {

		return String.format(mysql_query("jdbc:mysql://users-e6156.cexqeqvqreq2.us-east-1.rds.amazonaws.com:3306/UserData?autoReconnect=true&useSSL=false","root","dbuserdbuser","SELECT * FROM UserData.user_profile WHERE first_name ='"+id+"'" ), id);
	}

	@RequestMapping("/testid/{someID}")
	public @ResponseBody String getID(@PathVariable(value="someID") String id, String someAttr) {

		return String.format(mysql_query("jdbc:mysql://users-e6156.cexqeqvqreq2.us-east-1.rds.amazonaws.com:3306/UserData?autoReconnect=true&useSSL=false","root","dbuserdbuser","SELECT * FROM UserData.user_profile WHERE User_name ='"+id+"'" ), id);
	}

	@RequestMapping("/testlast/{someID}")
	public @ResponseBody String getLast(@PathVariable(value="someID") String id, String someAttr) {

		return String.format(mysql_query("jdbc:mysql://users-e6156.cexqeqvqreq2.us-east-1.rds.amazonaws.com:3306/UserData?autoReconnect=true&useSSL=false","root","dbuserdbuser","SELECT * FROM UserData.user_profile WHERE last_name ='"+id+"'" ), id);
	}

	@Autowired
	JdbcTemplate jdbc;
	@RequestMapping("/insert/{un}/{em}/{fn}/{ln}")
	public @ResponseBody String index2(@PathVariable (value = "un") String name2, @PathVariable(value = "em") String em2,  @PathVariable(value = "fn") String fn2,  @PathVariable(value = "ln") String ln2,String someAttr){
		System.out.println(name2);

		jdbc.execute("INSERT INTO UserData.user_profile (id,User_Name,email,first_name,last_name) VALUES(\""+ UUID.randomUUID() + " \",\"" + name2 + " \",\"" + em2 + "\",\""+fn2 +"\",\""+ln2+"\")");
		return"data inserted Successfully";
	}




	/*

	@Autowired
	private UserDao dao;
	@PostMapping("/newUser")
	public Optional<UserProfile> saveUser(@RequestParam UserProfile user){

		return dao.saveUser(user);
	}**/
/*

	private final UserDao dao;
	@Autowired
	public UserController(UserDao dao) {
		this.dao = dao;
	}

	@GetMapping("/findByEmail/{email}")
	public Optional<UserProfile> getUserByEmail(@PathVariable String email) {
		return UserDao.findByEmail(email);
	}
**/








	public String mysql_query(String  DB_URL, String USER, String PASS,String QUERY) {

		System.out.println(QUERY);
		String finalresult  ="";
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
				result = result + " " + ("First_Name: " + rs.getString("first_name"));
				result = result + " " + ("Last_Name: " + rs.getString("last_name"));
				System.out.println(result);
				finalresult=finalresult+result;

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return finalresult;
	}

}
