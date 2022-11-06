package com.UserProfile.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;


@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfile {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Type(type="org.hibernate.type.UUIDCharType")
    private UUID id;
    private String firstName;
    private String lastName;
    private String userName;
    private String email;



    public UUID getId() {
        return id;
    }


    public String getFirstName(UserProfile user) {
        return user.firstName;
    }
    public UserProfile(String userName, String email,String firstName, String lastName) {
        this.userName = userName;
        this.email = email;
        this.firstName = firstName;
        this.lastName= lastName;
    }

    public UserProfile() {

    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLastName(){return lastName;}


}