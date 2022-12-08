package com.User.service;


import com.User.dao.UserDAO;
import com.User.model.UserPage;
import com.User.model.User;
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

    public Page<User> getUsers(UserPage userPage){
        Pageable pageable = PageRequest.of(userPage.getPageNumber(),userPage.getPageSize());

        return dao.findAll(pageable);

    }
}
