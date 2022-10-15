package com.UserProfile.dao;

import com.UserProfile.model.UserProfile;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Component
public interface UserProfileDao extends PagingAndSortingRepository<UserProfile, UUID> {
    UserProfile findByUserId(UUID userId);

    Optional<UserProfile> findById(UUID id);

    List<UserProfile> findAllByIdIn(Iterable<UUID> ids);
}