package com.compositeService.composite.controllers;

import com.compositeService.composite.Model.User;
import com.compositeService.composite.Model.UserProfile;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import sun.net.www.http.HttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.util.*;


@RestController
public class compController {

    @GetMapping("/")
    public String base(){
        return "Hello Five Lions Users ";
    }

    @GetMapping("/call")
    public String call(){
        String uri = "http://five-lions-e6156.com/v1/post/62ff0f09-a659-437a-bb34-2ea58ab1ca83";
        RestTemplate restTemplate=new RestTemplate();
        String result = restTemplate.getForObject(uri, String.class);
        System.out.println(result);

        String uri2 = "https://five-lions-e6156.com/v1/userprofile/findById/dff9";
        RestTemplate restTemplate2=new RestTemplate();
        String result2 = restTemplate2.getForObject(uri2, String.class);
        System.out.println(result2);
        return result2 + result;
    }

    @GetMapping("/email")
    public String emailCall(@RequestParam String email){
        String uri = "https://emailvalidation.abstractapi.com/v1/?api_key=e4f5ed481a644b6f81576fe9ac6dd367&email="+email;
       RestTemplate restTemplate=new RestTemplate();
       String result = restTemplate.getForObject(uri, String.class);
       System.out.println(result);
       String[] temp = result.split(",");
       System.out.println(temp[2]);
        System.out.println(temp[2].length());
        String check = "\"deliverability\":\"DELIVERABLE\"";
        System.out.println(check.length());
        System.out.println("\"deliverability\":\"DELIVERABLE\"");
        if(temp[2].equals("\"deliverability\":\"DELIVERABLE\""))
        {
            System.out.println("THE same");
        }
        return  "stop";
    }




    //if a user is deleted so is there profile
    @DeleteMapping("/deleteUserHard/{someID}")
    public @ResponseBody String getname4(@PathVariable(value="someID") String id) throws MalformedURLException {

        //look up user to get email
        String uri =  "https://o8ngqn1quk.execute-api.us-east-1.amazonaws.com/Temp/userID/" + id;
        RestTemplate restTemplate=new RestTemplate();
        User result = restTemplate.getForObject(uri, User.class);
        String email = result.getEmail();

        //look up  profile by email to get id
        String uri2 =  "https://http://five-lions-e6156.com/v1/userprofile/findByEmail/" + email;
        RestTemplate restTemplate2 =new RestTemplate();
        UserProfile result2 = restTemplate2.getForObject(uri2, UserProfile.class);
        UUID idOfProfile = result2.getId();

        //  delete profile by id
        String uri3 =  "https://http://five-lions-e6156.com/v1/post/09b8952e-c7dd-46d5-a20a-cdfa0a255b47/v1/userprofile/deleteById" + idOfProfile;
        RestTemplate restTemplate3 =new RestTemplate();
        restTemplate3.delete(uri2);

        //delete User by ID
        String uri4 =  "https://o8ngqn1quk.execute-api.us-east-1.amazonaws.com/Temp/delete/" + id;
        RestTemplate restTemplate4=new RestTemplate();
        restTemplate4.delete(uri4);

        return "Deleted";

        //return String.format(mysql_query2("jdbc:mysql://users-e6156.cexqeqvqreq2.us-east-1.rds.amazonaws.com:3306/UserData?autoReconnect=true&useSSL=false","root","dbuserdbuser","DELETE FROM UserData.user_profile WHERE id ='"+id+"'" ), id);

    }


    @PutMapping("/update/{id}")
    @ResponseBody
    public Optional<User> upUser(@PathVariable UUID id,  @RequestParam(required = false) String uname, @RequestParam(required = false) String email) {
        // look up user profile
        String uri2 =  "http://five-lions-e6156.com/v1/post/09b8952e-c7dd-46d5-a20a-cdfa0a255b47/v1/userprofile/findByUserId/" + id;
        RestTemplate restTemplate2 =new RestTemplate();
        UserProfile result2 = restTemplate2.getForObject(uri2, UserProfile.class);
        UUID idOfProfile = result2.getId();


        // update user Profile
        String uri3 =  "http://five-lions-e6156.com/v1/post/09b8952e-c7dd-46d5-a20a-cdfa0a255b47/v1/userprofile/updateById/" + idOfProfile;
        RestTemplate restTemplate3 =new RestTemplate();
        restTemplate3.put(uri3, UserProfile.class);


        // update user
        String uri4 =  "https://o8ngqn1quk.execute-api.us-east-1.amazonaws.com/update/"+id;
        RestTemplate restTemplate=new RestTemplate();
        restTemplate.put(uri4, User.class);

/*        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<User> entity = new HttpEntity<User>(user,headers);

        restTemplate.exchange(uri4 +id, HttpMethod.PUT, entity, String.class).getBody();
**/
        return null;

    }

    @PostMapping("/createUser")
    public UserProfile addUserProfile(@RequestBody User user) {
        String uri5 = "https://o8ngqn1quk.execute-api.us-east-1.amazonaws.com/Temp/newuser";
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<User> request = new HttpEntity<>(new User(user.getUserName(), user.getEmail(), user.getFirstName(), user.getLastName()));

        User foo = restTemplate.postForObject(uri5, request, User.class);

        String uri6 = "http://five-lions-e6156.com/v1/post/09b8952e-c7dd-46d5-a20a-cdfa0a255b47/v1/userprofile";
        RestTemplate restTemplate2 = new RestTemplate();
        HttpEntity<UserProfile> request2 = new HttpEntity<>(new UserProfile(user.getUserName(), user.getEmail()));
        UserProfile foo2 = restTemplate2.postForObject(uri6, request2, UserProfile.class);

        return foo2;
    }

    @PostMapping("/follow/{celeb}/{follower}")
    @ResponseBody
    public String followUser(@PathVariable UUID celeb,@PathVariable UUID follower) {
        DynamoDbClient dynamoDB = DynamoDbClient.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(System.getenv("ACCESS_KEY"), System.getenv("SECRET_KEY"))))
                .build();
        Map<String, AttributeValue> rel =  new HashMap<>();
        rel.put("celebrity",AttributeValue.fromS(celeb.toString()));
        rel.put("follower",AttributeValue.fromS(follower.toString()));

        PutItemRequest req = PutItemRequest.builder()
                .tableName("RelationTracker")
                .item(rel).build();

        dynamoDB.putItem(req);
        return String.format(mysql_query2("jdbc:mysql://users-e6156.cexqeqvqreq2.us-east-1.rds.amazonaws.com:3306/UserData?autoReconnect=true&useSSL=false","root","dbuserdbuser","INSERT INTO UserData.followers(celebrity, follower) VALUES ('"+celeb+"', '"+follower+"')" ));
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
