package com.User.controllers;
import com.User.dao.UserDAO;
import com.User.model.UserPage;
import com.User.model.User;
import com.User.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.web.*;
import org.springframework.hateoas.server.mvc.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.*;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.annotation.*;
import org.springframework.security.oauth2.core.user.*;
import org.springframework.stereotype.*;
import org.springframework.ui.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.hateoas.*;

import java.sql.*;
import java.util.*;

//References for Oath2
//http://www.zakariaamine.com/2018-03-01/using-oauth2-in-spring-scopes/
//https://dzone.com/articles/add-login-to-your-spring-boot-app-in-10-mins  reference for the login and HTML scaffolding
//https://spring.io/guides/tutorials/spring-boot-oauth2/


//@RestController
@Controller
public class UserController {
	private final UserDAO dao;
	private final int limit =2;


	public UserController (UserDAO dao, UserService userService){
		this.dao=dao;

		this.userService = userService;
	}
	//@CrossOrigin
	@RequestMapping("/")
	public String index() {
		return "index";
	}

	//@CrossOrigin
	@RequestMapping("/securedPage")
	public String securedPage(Model model,
							  @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
							  @AuthenticationPrincipal OAuth2User oauth2User) {
		model.addAttribute("userName", oauth2User.getName());
		model.addAttribute("clientName", authorizedClient.getClientRegistration().getClientName());
		model.addAttribute("userAttributes", oauth2User.getAttributes());
		return "securedPage";
	}

	// Login form
	//@CrossOrigin
	@RequestMapping("/login.html")
	public String login() {
		return "login.html";
	}

	// Login form with error
	//@CrossOrigin
	@RequestMapping("/login-error.html")
	public String loginError(Model model) {
		model.addAttribute("loginError", true);
		return "login.html";
	}

	//adds a user from JSON input and adds a self referencing Link for Hateoas
	//@CrossOrigin
	//@PreAuthorize("#oauth2.hasScope('write')")
	@PostMapping("/newuser")
	@ResponseBody
	public User addUserProfile(@RequestBody User user) {
		dao.save(user);
		String self = "/userID/"+user.getId();
		Link link = WebMvcLinkBuilder.linkTo(UserController.class).slash(self).withRel("self");
		user.add(link);
		return user;
	}

	@PostMapping("/follow/{celeb}/{follower}")
	@ResponseBody
	public String followUser(@PathVariable UUID celeb,@PathVariable UUID follower) {
		return String.format(mysql_query2("jdbc:mysql://users-e6156.cexqeqvqreq2.us-east-1.rds.amazonaws.com:3306/UserData?autoReconnect=true&useSSL=false","root","dbuserdbuser","INSERT INTO UserData.followers(celebrity, follower) VALUES ('"+celeb+", "+follower+"')" ));
	}

	//returns a user with specific ID using path params
	//@CrossOrigin
	//@PreAuthorize("#oauth2.hasScope('read')")
	@GetMapping("/userID/{userId}")
	@ResponseBody
	public Optional<User> getUser(@PathVariable UUID userId) {
		String self = "/userID/"+userId;
		System.out.println(userId.getClass());
		Link link = WebMvcLinkBuilder.linkTo(UserController.class).slash(self).withRel("SELF");
		Optional<User> uid = dao.findById(userId);
		System.out.println(uid);
		dao.findById(userId).ifPresent(user->user.add(link));
		System.out.println(uid);
		System.out.println(uid.getClass());
		return uid;
	}

	//returns a user by id using query params
	//@CrossOrigin
	//@PreAuthorize("#oauth2.hasScope('read')")
	@GetMapping("/userbyid")
	@ResponseBody
	public Optional<User> getUser2(@RequestParam UUID userId) {
		Optional<User> post = dao.findById(userId);
		String self = "/userID/"+userId;
		Link link = WebMvcLinkBuilder.linkTo(UserController.class).slash(self).withRel("self");
		dao.findById(userId).ifPresent(user->user.add(link));
		return post;
	}

	//implement pagination in a return all users
	private final UserService userService;

	// Add links

	//@CrossOrigin
	//@PreAuthorize("#oauth2.hasScope('read')")
	@GetMapping("/usersBy")
	@ResponseBody
	public ResponseEntity<PagedModel<EntityModel<User>>>hateoasUsers(UserPage userPage, @RequestParam (required = false) String limits, @RequestParam (required = false) String offset){
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

		Page<User> page1 = userService.getUsers(userPage);


		for(User x: page1){
			String userMethod ="userbyid?userId="+x.getId();
			x.add(WebMvcLinkBuilder.linkTo(UserController.class).slash(userMethod).withRel("SELF"));
		}

		PagedResourcesAssembler<User> pagedResourcesAssembler = new PagedResourcesAssembler<User>(null,null);
		PagedModel<EntityModel<User>> coll = pagedResourcesAssembler.toModel(page1,link);
		System.out.println(coll);



	//	userService.getUsers(userPage)
		return new ResponseEntity<>(coll , HttpStatus.OK);
	}





	//updates a users name given an id
	//TODO - expand to all params excluding UUID
	//@CrossOrigin
	//@PreAuthorize("#oauth2.hasScope('write')")
	@PutMapping("/update/{userId}")
	@ResponseBody
	public Optional<User> upUser(@PathVariable UUID userId, @RequestParam(required = false) String fname, @RequestParam(required = false) String lname, @RequestParam(required = false) String uname, @RequestParam(required = false) String email) {
		Optional<User> post = dao.findById(userId);

		if(fname!=null) {
			post.ifPresent(posts -> posts.setFirstName(fname));
			post.ifPresent(posts -> dao.save(posts));
		}
		if(lname!=null) {
			post.ifPresent(posts -> posts.setLastName(lname));
			post.ifPresent(posts -> dao.save(posts));
		}

		if(uname!=null) {
			post.ifPresent(posts -> posts.setUserName(uname));
			post.ifPresent(posts -> dao.save(posts));
		}

		if(email!=null) {
			post.ifPresent(posts -> posts.setEmail(email));
			post.ifPresent(posts -> dao.save(posts));
		}




		String self = "/userID/"+userId;
		Link link = WebMvcLinkBuilder.linkTo(UserController.class).slash(self).withRel("SELF");
		dao.findById(userId).ifPresent(user->user.add(link));
		return post;
	}

	//Delete by ID
	//@CrossOrigin
	//@PreAuthorize("#oauth2.hasScope('write')")
	@DeleteMapping ("/delete/{someID}")
	public @ResponseBody String getname4(@PathVariable(value="someID") String id) {

		return String.format(mysql_query2("jdbc:mysql://users-e6156.cexqeqvqreq2.us-east-1.rds.amazonaws.com:3306/UserData?autoReconnect=true&useSSL=false","root","dbuserdbuser","DELETE FROM UserData.user_profile WHERE id ='"+id+"'" ), id);
	}
/*
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

	//return all users with optional limit by request param


	//return by firstname
	@RequestMapping("/userfirstname/{someID}")
	public ResponseEntity<String> getname(@PathVariable(value="someID") String id) {

		return new ResponseEntity<String>(String.format(mysql_query("jdbc:mysql://users-e6156.cexqeqvqreq2.us-east-1.rds.amazonaws.com:3306/UserData?autoReconnect=true&useSSL=false","root","dbuserdbuser","SELECT * FROM UserData.user_profile WHERE first_name ='"+id+"'" ), id),HttpStatus.OK);
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
	}**/

/*
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
	}**/

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
/*
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


**/
}
