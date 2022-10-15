package com.Post.dao;

import com.Post.model.Post;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Component
public interface PostDao extends PagingAndSortingRepository<Post, UUID> {
    Optional<Post> findById(UUID id);

    List<Post> findAllByIdIn(Iterable<UUID> ids);
}