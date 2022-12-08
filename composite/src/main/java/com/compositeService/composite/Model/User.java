package com.compositeService.composite.Model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;


@Entity
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
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


    public String getFirstName() {
        return this.firstName;
    }
    public User(String userName, String email, String firstName, String lastName) {
        this.userName = userName;
        this.email = email;
        this.firstName = firstName;
        this.lastName= lastName;
    }

    public User() {

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

    public String getUserName(){return this.userName;}

    public String getEmail(){return this.email;}


}