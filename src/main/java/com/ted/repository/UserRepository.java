package com.ted.repository;

import com.ted.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // FIND-QUERIES
        // Returns a user with a specific id
        Optional<User> findById(Long id);

        // Returns a user with a specific email
        Optional<User> findByEmail(String email);

    // Returns all users
    @Query("from User user")
    List<User> getAll();

    // Returns user_id based on user_email.
    @Query(value = "select user_id from Users u where u.email = :email", nativeQuery = true)
    int getIdByEmail(@Param("email") String email);

    // PICTURE-QUERIES
        // Get the picture of a user.
        @Query(value = "select picture from Users u where u.user_id = :user_id", nativeQuery = true)
        List<String> getPictureById(@Param("user_id") int user_id); // Returns the numOfRows affected.. so either 1 or 0.

        // Update the "picture" of a user.
        @Transactional
        @Modifying
        @Query(value = "update Users u set u.picture = :picture where u.user_id = :user_id", nativeQuery = true)
        int updatePictureById(@Param("picture") String picture, @Param("user_id") int user_id); // Returns the numOfRows affected.. so either 1 or 0.
}
