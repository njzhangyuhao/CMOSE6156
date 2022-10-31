package com.UserProfile.dao;

import com.UserProfile.model.UserProfile;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

@Component
@Transactional
public interface UserDAO extends PagingAndSortingRepository<UserProfile,UUID>{

    Optional<UserProfile> findById(UUID id);

    UserProfile save(UserProfile userProfile);
}
