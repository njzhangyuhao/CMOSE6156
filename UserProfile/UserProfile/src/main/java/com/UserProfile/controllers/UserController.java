package com.UserProfile.controllers;

import com.UserProfile.dao.UserDAO;
import com.UserProfile.model.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.*;
import java.util.UUID;
import java.util.Optional;


@RestController
public class UserController {
	private final UserDAO dao;
	private final int limit =2;


	public UserController (UserDAO dao){
		this.dao=dao;

	}

	//adds a user from JSON input
	@PostMapping("/newuser")
	public UserProfile addUserProfile(@RequestBody UserProfile user) {
		dao.save(user);
		return user;
	}

	//returns a user with specific ID using path
	@GetMapping("/userID/{userId}")
	public Optional<UserProfile> getUser(@PathVariable UUID userId) {
		Optional<UserProfile> uid = dao.findById(userId);

		return uid;
	}

	//returns a user by id using query params
	@GetMapping("/userbyid")
	public Optional<UserProfile> getUser2(@RequestParam UUID userId) {
		Optional<UserProfile> post = dao.findById(userId);

		return post;
	}

	@GetMapping("/paging")
	public String pagin(){
		//System.out.println(ln);
		Pageable pageable = PageRequest.of(1,1);
		System.out.println(pageable);

		Page<UserProfile>p1 = dao.findAll(pageable);

		//List<UserProfile> allLast = p1.getContent();
		return "ok";
	}


	//updates a users name given an id
	@GetMapping("/update")
	public Optional<UserProfile> upUser(@RequestParam UUID userId,@RequestParam String fname) {
		Optional<UserProfile> post = dao.findById(userId);
		System.out.println(fname);
		post.ifPresent(posts->posts.setFirstName(fname));
		post.ifPresent(posts->dao.save(posts));
		return post;
	}



	//general response
	@GetMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}

	//local method for testing
	@GetMapping("/user")
	public String sayHell(@RequestParam(value = "myName", defaultValue = "World") String name) {

		return String.format(mysql_query("jdbc:mysql://localhost:3306/e6156_project","root","dbuserdbuser","SELECT * FROM  e6156_project.users"), name);
	}

	//local method for testing
	@RequestMapping("/user/{someID}")
	public @ResponseBody String getAttr(@PathVariable(value="someID") String id, String someAttr) {
		return String.format(mysql_query("jdbc:mysql://localhost:3306/e6156_project","root","dbuserdbuser","SELECT * FROM e6156_project.users WHERE first_name ='"+id+"'" ), id);
	}

	//return all users with optional limit

	@GetMapping("/testuser")
	public String basic(@RequestParam (required = false) String limits) {

		if (limits == null || Integer.parseInt(limits) > limit){

			limits=Integer.toString(limit);
		}
		int pageNumber = 0;
		int offset = (Integer.parseInt(limits) * pageNumber) - Integer.parseInt(limits);
		System.out.println(offset);


		return String.format(mysql_query("jdbc:mysql://users-e6156.cexqeqvqreq2.us-east-1.rds.amazonaws.com:3306/UserData?autoReconnect=true&useSSL=false","root","dbuserdbuser","SELECT * FROM  UserData.user_profile Limit " + limits ), limits);
	}




	@RequestMapping("/testuser/{someID}")
	public @ResponseBody String getname(@PathVariable(value="someID") String id, String someAttr) {

		return String.format(mysql_query("jdbc:mysql://users-e6156.cexqeqvqreq2.us-east-1.rds.amazonaws.com:3306/UserData?autoReconnect=true&useSSL=false","root","dbuserdbuser","SELECT * FROM UserData.user_profile WHERE first_name ='"+id+"'" ), id);
	}

	@RequestMapping("/delete/{someID}")
	public @ResponseBody String getname4(@PathVariable(value="someID") String id, String someAttr) {

		return String.format(mysql_query2("jdbc:mysql://users-e6156.cexqeqvqreq2.us-east-1.rds.amazonaws.com:3306/UserData?autoReconnect=true&useSSL=false","root","dbuserdbuser","DELETE FROM UserData.user_profile WHERE id ='"+id+"'" ), id);
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

	public String mysql_query2(String  DB_URL, String USER, String PASS,String QUERY) {

		System.out.println(QUERY);
		String finalresult  ="";
		String result = null;
		try {
				Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
				Statement stmt = conn.createStatement();
				int w = stmt.executeUpdate(QUERY);


			// Extract data from result set

			//finalresult= Integer.toString(rs);


		} catch (SQLException e) {
			e.printStackTrace();
		}
		return finalresult;
	}

}
