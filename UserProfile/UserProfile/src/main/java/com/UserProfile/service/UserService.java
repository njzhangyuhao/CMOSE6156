package com.UserProfile.service;


import com.UserProfile.dao.UserDAO;
import com.UserProfile.model.UserPage;
import com.UserProfile.model.UserProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserDAO dao;

    public UserService(UserDAO dao){
        this.dao =dao;
    }

    public Page<UserProfile> getUsers(UserPage userPage){
        Pageable pageable = PageRequest.of(userPage.getPageNumber(),userPage.getPageSize());
        return dao.findAll(pageable);

    }
}
