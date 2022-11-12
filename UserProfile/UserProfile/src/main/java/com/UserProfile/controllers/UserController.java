package com.UserProfile.controllers;
import com.UserProfile.dao.UserDAO;
import com.UserProfile.model.UserPage;
import com.UserProfile.model.UserProfile;
import com.UserProfile.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.web.*;
import org.springframework.hateoas.server.mvc.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.hateoas.*;

import javax.persistence.criteria.*;
import java.sql.*;
import java.util.*;


@RestController
public class UserController {
	private final UserDAO dao;
	private final int limit =2;


	public UserController (UserDAO dao, UserService userService){
		this.dao=dao;

		this.userService = userService;
	}

	//adds a user from JSON input and adds a self referencing Link for Hateoas
	@PostMapping("/newuser")
	public UserProfile addUserProfile(@RequestBody UserProfile user) {
		dao.save(user);
		String self = "/userID/"+user.getId();
		Link link = WebMvcLinkBuilder.linkTo(UserController.class).slash(self).withRel("self");
		user.add(link);
		return user;
	}

	//returns a user with specific ID using path params
	@GetMapping("/userID/{userId}")
	public Optional<UserProfile> getUser(@PathVariable UUID userId) {
		String self = "/userID/"+userId;
		Link link = WebMvcLinkBuilder.linkTo(UserController.class).slash(self).withRel("SELF");
		Optional<UserProfile> uid = dao.findById(userId);
		dao.findById(userId).ifPresent(user->user.add(link));
		System.out.println(uid);
		System.out.println(uid.getClass());
		return uid;
	}

	//returns a user by id using query params
	@GetMapping("/userbyid")
	public Optional<UserProfile> getUser2(@RequestParam UUID userId) {
		Optional<UserProfile> post = dao.findById(userId);
		String self = "/userID/"+userId;
		Link link = WebMvcLinkBuilder.linkTo(UserController.class).slash(self).withRel("self");
		dao.findById(userId).ifPresent(user->user.add(link));
		return post;
	}

	//method to manage multiple query params WIP
	@GetMapping("/userBy")
	public List<Optional<UserProfile>> getUser4(@RequestParam(required = false) String uname,@RequestParam(required = false) String fname,@RequestParam(required = false) String lname,@RequestParam(required = false) String email,@RequestParam(required = false) String offset,@RequestParam(required = false) String limit) {

		String base = "SELECT * FROM UserData.user_profile ";
		String where2  ="WHERE ";


		if(fname != null){
			 where2 = where2 + "first_name = "+ "'" + fname + "'";

		}
		if(lname !=null){
			if(where2.contains("first_name"))
			{
				where2=where2 +" AND ";
			}
			where2 = where2 + "last_name = "+ "'" +lname+ "'" ;
		}
		if(email !=null){
			if(where2.contains("first_name") || where2.contains("last_name"))
			{
				where2=where2 +" AND ";
			}
			where2 = where2 + "email = "+"'" + email+ "'" ;
		}
		if(uname !=null){
			if(where2.contains("first_name") || where2.contains("last_name") || where2.contains("email"))
			{
				where2=where2 +" AND ";
			}
			where2 = where2 + "user_name = "+ "'" + uname+ "'" ;
		}

		if (where2 != "WHERE "){
			base = base + where2;

		}

		if(limit!= null && Integer.parseInt(limit)<10){

				base = base + " LIMIT " + limit;

		}
		else{
			base = base + " LIMIT " + 10;
		}
		if(offset!= null ){
			base = base + " OFFSET "+ offset;
		}
		List<Optional<UserProfile>> userReturn = mysql_query3("jdbc:mysql://users-e6156.cexqeqvqreq2.us-east-1.rds.amazonaws.com:3306/UserData?autoReconnect=true&useSSL=false","root","dbuserdbuser",base);

		System.out.println(base);
		Optional<List<UserProfile>> post = dao.findByFirstName(fname);

		return userReturn;
	}

	//implement pagination in a return all users
	private final UserService userService;

	@GetMapping("/paging")
	public ResponseEntity<Page<UserProfile>>paginationUsers(UserPage userPage,@RequestParam (required = false) String limits,@RequestParam (required = false) String offset){
		if(limits!=null ){
			userPage.setPageSize(Integer.parseInt(limits));
		}
		if(offset!=null ){
			userPage.setPageNumber(Integer.parseInt(offset));
		}
		return new ResponseEntity<>(userService.getUsers(userPage), HttpStatus.OK);
	}

	// Add links

	@GetMapping("/hateoas")
	public ResponseEntity<PagedModel<EntityModel<UserProfile>>>hateoasUsers(UserPage userPage,@RequestParam (required = false) String limits,@RequestParam (required = false) String offset){
		if(limits!=null ){
			userPage.setPageSize(Integer.parseInt(limits));
		}
		if(offset!=null ){
			userPage.setPageNumber(Integer.parseInt(offset));
		}
		if(limits == null){
			limits =Integer.toString(userPage.getPageSize());
		}
		int pageN = userPage.getPageNumber() +1;
		String next = "hateoas?limits="+limits+"&offset="+pageN;
		String self = "hateoas?limits="+limits+"&offset="+userPage.getPageNumber();
		String prev = "hateoas?limits="+limits+"&offset="+(userPage.getPageNumber()-1);
		Link link = WebMvcLinkBuilder.linkTo(UserController.class).slash(next).withRel("NEXT");
		Link link2 = WebMvcLinkBuilder.linkTo(UserController.class).slash(self).withRel("SELF");
		Link link3 = WebMvcLinkBuilder.linkTo(UserController.class).slash(prev).withRel("PREV");
		userPage.add(link);


		System.out.println(link);
		System.out.println(link2);
		System.out.println(link3);

		Page<UserProfile> page1 = userService.getUsers(userPage);


		for(UserProfile x: page1){
			String userMethod ="userbyid?userId="+x.getId();
			x.add(WebMvcLinkBuilder.linkTo(UserController.class).slash(userMethod).withRel("SELF"));
		}

		PagedResourcesAssembler<UserProfile> pagedResourcesAssembler = new PagedResourcesAssembler<UserProfile>(null,null);
		PagedModel<EntityModel<UserProfile>> coll = pagedResourcesAssembler.toModel(page1,link);
		System.out.println(coll);



	//	userService.getUsers(userPage)
		return new ResponseEntity<>(coll , HttpStatus.OK);
	}





	//updates a users name given an id
	//TODO - expand to all params excluding UUID
	@GetMapping("/update")
	public Optional<UserProfile> upUser(@RequestParam UUID userId,@RequestParam String fname) {
		Optional<UserProfile> post = dao.findById(userId);
		System.out.println(fname);
		post.ifPresent(posts->posts.setFirstName(fname));
		post.ifPresent(posts->dao.save(posts));
		String self = "/userID/"+userId;
		Link link = WebMvcLinkBuilder.linkTo(UserController.class).slash(self).withRel("SELF");
		dao.findById(userId).ifPresent(user->user.add(link));
		return post;
	}



	//general response
	@GetMapping("/")
	public String index() {
		return "Greetings from Spring Boot!";
	}
/*
	//local method for testing
	@GetMapping("/user")
	public String sayHell(@RequestParam(value = "myName", defaultValue = "World") String name) {

		return String.format(mysql_query("jdbc:mysql://localhost:3306/e6156_project","root","dbuserdbuser","SELECT * FROM  e6156_project.users"), name);
	}

	//local method for testing
	@RequestMapping("/user/{someID}")
	public @ResponseBody String getAttr(@PathVariable(value="someID") String id, String someAttr) {
		return String.format(mysql_query("jdbc:mysql://localhost:3306/e6156_project","root","dbuserdbuser","SELECT * FROM e6156_project.users WHERE first_name ='"+id+"'" ), id);
	}**/

	//return all users with optional limit by request param

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



	//return by firstname
	@RequestMapping("/userfirstname/{someID}")
	public ResponseEntity<String> getname(@PathVariable(value="someID") String id) {

		return new ResponseEntity<String>(String.format(mysql_query("jdbc:mysql://users-e6156.cexqeqvqreq2.us-east-1.rds.amazonaws.com:3306/UserData?autoReconnect=true&useSSL=false","root","dbuserdbuser","SELECT * FROM UserData.user_profile WHERE first_name ='"+id+"'" ), id),HttpStatus.OK);
	}


	//Delete by ID
	@RequestMapping("/delete/{someID}")
	public @ResponseBody String getname4(@PathVariable(value="someID") String id) {

		return String.format(mysql_query2("jdbc:mysql://users-e6156.cexqeqvqreq2.us-east-1.rds.amazonaws.com:3306/UserData?autoReconnect=true&useSSL=false","root","dbuserdbuser","DELETE FROM UserData.user_profile WHERE id ='"+id+"'" ), id);
	}

	//search by username
	@RequestMapping("/userid/{someID}")
	public @ResponseBody String getID(@PathVariable(value="someID") String id, String someAttr) {

		return String.format(mysql_query("jdbc:mysql://users-e6156.cexqeqvqreq2.us-east-1.rds.amazonaws.com:3306/UserData?autoReconnect=true&useSSL=false","root","dbuserdbuser","SELECT * FROM UserData.user_profile WHERE User_name ='"+id+"'" ), id);
	}

	//query by lastname
	@RequestMapping("/userlastname/{someID}")

	public @ResponseBody String getLast(@PathVariable(value="someID") String id) {

		return String.format(mysql_query("jdbc:mysql://users-e6156.cexqeqvqreq2.us-east-1.rds.amazonaws.com:3306/UserData?autoReconnect=true&useSSL=false","root","dbuserdbuser","SELECT * FROM UserData.user_profile WHERE last_name ='"+id+"'" ), id);
	}

	//create user by path variables
	@Autowired
	JdbcTemplate jdbc;
	@RequestMapping("/insert/{un}/{em}/{fn}/{ln}")
	public @ResponseBody String index2(@PathVariable (value = "un") String name2, @PathVariable(value = "em") String em2,  @PathVariable(value = "fn") String fn2,  @PathVariable(value = "ln") String ln2){
		System.out.println(name2);

		jdbc.execute("INSERT INTO UserData.user_profile (id,User_Name,email,first_name,last_name) VALUES(\""+ UUID.randomUUID() + "\",\"" + name2 + " \",\"" + em2 + "\",\""+fn2 +"\",\""+ln2+"\")");

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

	public List<Optional<UserProfile>> mysql_query3(String  DB_URL, String USER, String PASS,String QUERY) {

		System.out.println(QUERY);
		String finalresult  ="";
		List<Optional<UserProfile>> allUsers= new ArrayList<Optional<UserProfile>>();
		try (
				Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(QUERY);


		) {
			// Extract data from result set

			while (rs.next()) {
				// Retrieve by column name
				//System.out.println(rs.getString("id"));
				//UserProfile temp = new UserProfile(rs.getString("id"),rs.getString("User_Name"),rs.getString("email"), rs.getString("first_name"), rs.getString("last_name"));
				UUID tempID= UUID.fromString(rs.getString("id"));
				Optional<UserProfile> thisUser = dao.findById(tempID);
				String self = "/userID/"+rs.getString("id");
				Link link = WebMvcLinkBuilder.linkTo(UserController.class).slash(self).withRel("SELF");
				dao.findById(tempID).ifPresent(user->user.add(link));

				//System.out.println(thisUser.ifPresent(););
				allUsers.add(thisUser);

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return allUsers;
	}



}
