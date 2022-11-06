package com.UserProfile.dao;
import com.UserProfile.model.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
@Component
@Transactional
public interface UserDAO extends PagingAndSortingRepository<UserProfile,UUID>{



    Optional<UserProfile> findById(UUID id);

    UserProfile save(UserProfile userProfile);


    Page<UserProfile> findAll ( Pageable pageable);


}