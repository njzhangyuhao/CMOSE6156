package com.UserProfile.dao;
import com.UserProfile.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
@Component
@Transactional
public interface UserDAO extends PagingAndSortingRepository<User,UUID>{



   Optional<User> findById(UUID id);
    Optional<List<User>> findByFirstName(String firstName);
    Optional<User> findByLastName(String lastName);
    Optional<User> findByUserName(String userName);
    Optional<User> findByEmail(String email);

    User save(User userProfile);


    Page<User> findAll (Pageable pageable);




}