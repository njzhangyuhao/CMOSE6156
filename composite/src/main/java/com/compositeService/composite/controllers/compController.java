package com.compositeService.composite.controllers;

import com.compositeService.composite.Model.User;
import com.compositeService.composite.Model.UserProfile;
import com.fasterxml.jackson.databind.util.JSONPObject;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
//import sun.net.www.http.HttpClient;
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

    @GetMapping("/infoID/{id}")
    public String call(@PathVariable UUID id){
        String uri = "http://e6156-profile-service.us-east-1.elasticbeanstalk.com/v1/userprofile/findByUserId/"+id;
        RestTemplate restTemplate=new RestTemplate();
        String result = restTemplate.getForObject(uri, String.class);

        System.out.println(result);
        //return result;


        String uri2 = "http://34.227.158.8:3310/userID/"+id;
        RestTemplate restTemplate2=new RestTemplate();
        String result2 = restTemplate2.getForObject(uri2, String.class);
        System.out.println(result2);

        String uri3 = String.format(mysql_query("jdbc:mysql://awseb-e-jxphsepgxj-stack-awsebrdsdatabase-j9bvajj7xfxd.c0opdlelqqgp.us-east-1.rds.amazonaws.com:3306/ebdb?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false","dbuser","e6156$coms$p3","SELECT * FROM ebdb.post WHERE user_id = '" + id +"'"));
        System.out.println(uri3);


        return result + result2 + uri3;


    }

    @PutMapping("/emailUpdate/{id}")
    public String emailCall(@RequestParam String email,@PathVariable UUID id){
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
            String uri2 = "http://34.227.158.8:3310/update/"+id+"?email="+email;
            RestTemplate restTemplate2=new RestTemplate();
            restTemplate2.put(uri2, String.class);


            String uri3 =  "http://e6156-profile-service.us-east-1.elasticbeanstalk.com/v1/userprofile/findByUserId/" +id;
            RestTemplate restTemplate3=new RestTemplate();
            String profID= restTemplate3.getForObject(uri3, String.class);
            System.out.println(profID);
            String[] temp2 = profID.split(",");
            System.out.println(temp2[0]);
            String[] temp3 = temp2[0].split(":");
            String temp4 = temp3[1].replace("\"","");
            System.out.print(temp4);




            String uri4 =  "http://e6156-profile-service.us-east-1.elasticbeanstalk.com/v1/userprofile/updateById/" +temp4;
            RestTemplate restTemplate4=new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String request = "{\"email\":\""+email +"\"}";
            HttpEntity<String> entity = new HttpEntity<String>(request,headers);
            restTemplate4.put(uri4, entity);

            return "Thanks for the update, new email success";

        }
        return  "Your email is coming back invalid, please double check it";
    }




    //if a user is deleted so is there profile
    @DeleteMapping("/deleteUserHard/{someID}")
    public @ResponseBody String getname4(@PathVariable(value="someID") UUID id) throws MalformedURLException {

        //look up user to get email
        String uri =  "http://34.227.158.8:3310/userID/" + id;
        RestTemplate restTemplate=new RestTemplate();
        User result = restTemplate.getForObject(uri, User.class);
        String email = result.getEmail();
        System.out.println(email);

        //look up  profile by email to get id
        String uri2 =  "http://e6156-profile-service.us-east-1.elasticbeanstalk.com/v1/userprofile/findByEmail/" + email;
        RestTemplate restTemplate2 =new RestTemplate();
        UserProfile result2 = restTemplate2.getForObject(uri2, UserProfile.class);
        UUID idOfProfile = result2.getId();
        System.out.print(idOfProfile);

        //  delete profile by id
        String uri3 =  "http://e6156-profile-service.us-east-1.elasticbeanstalk.com/v1/userprofile/deleteById/" + idOfProfile;
        RestTemplate restTemplate3 =new RestTemplate();
        restTemplate3.delete(uri3);

        //delete User by ID
        String uri4 =  "http://34.227.158.8:3310/delete/" + id;
        RestTemplate restTemplate4=new RestTemplate();
        restTemplate4.delete(uri4);
          /** */
        return "Deleted";

        //return String.format(mysql_query2("jdbc:mysql://users-e6156.cexqeqvqreq2.us-east-1.rds.amazonaws.com:3306/UserData?autoReconnect=true&useSSL=false","root","dbuserdbuser","DELETE FROM UserData.user_profile WHERE id ='"+id+"'" ), id);

    }

/*
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
        String uri4 =  "http://five-lions-e6156.com/update/"+id;
        RestTemplate restTemplate=new RestTemplate();
        restTemplate.put(uri4, User.class);

/*        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<User> entity = new HttpEntity<User>(user,headers);

        restTemplate.exchange(uri4 +id, HttpMethod.PUT, entity, String.class).getBody();
**/
       // return null;

   // }

    @PostMapping("/createUser")
    public UserProfile addUserProfile(@RequestBody User user) {
        String uri5 = "http://34.227.158.8:3310/newuser";
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<User> request = new HttpEntity<>(new User(user.getUserName(), user.getEmail(), user.getFirstName(), user.getLastName()));

        User foo = restTemplate.postForObject(uri5, request, User.class);

        String uri6 = "http://e6156-profile-service.us-east-1.elasticbeanstalk.com/v1/userprofile?email="+foo.getEmail()+"&userName="+foo.getUserName();
        RestTemplate restTemplate2 = new RestTemplate();
        HttpEntity<UserProfile> request2 = new HttpEntity<>(new UserProfile(foo.getUserName(), foo.getEmail()));
        UserProfile foo2 = restTemplate2.postForObject(uri6,request2, UserProfile.class);

        return foo2;
    }

    @PostMapping("/follow/{celeb}/{follower}")
    @ResponseBody
    public String followUser(@PathVariable UUID celeb,@PathVariable UUID follower) {
        System.out.println(System.getenv("ACCESS_KEY"));
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

                result = ("user_id: " + rs.getString("user_id"));
                result = result + " " + ("image_id: " + rs.getString("image_id"));
                result = result + " " + ("subject: " + rs.getString("subject"));
                //result = result + " " + ("Last_Name: " + rs.getString("last_name"));
                //System.out.println(result);
                finalresult=finalresult+result;
                //finalresult=rs.getString("user_id");

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
