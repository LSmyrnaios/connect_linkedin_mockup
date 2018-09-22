package com.ted.service;

import com.ted.model.Relationship;
import com.ted.model.User;
import com.ted.repository.RelationshipRepository;
import com.ted.repository.UserRepository;
import com.ted.request.RelationshipRequest;
import com.ted.exception.ResourceNotFoundException;
import com.ted.exception.NotAuthorizedException;
import com.ted.response.ApiResponse;
import com.ted.response.NetworkResponse;
import com.ted.security.UserDetailsImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RelationshipService {

    @Autowired
    private RelationshipRepository relationshipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ValidatePathService validatePathService;

    private static final Logger logger = LoggerFactory.getLogger(RelationshipService.class);

    // Creates a friend request
    public Relationship create(Long userId, RelationshipRequest relationshipRequest) {
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check that the request receiver exists
        User receiver = userRepository.findById(relationshipRequest.getReceiver())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", relationshipRequest.getReceiver()));

        Relationship relationship = new Relationship();

        relationship.setSender(me);
        relationship.setReceiver(receiver);
        relationship.setStatus(0);
        relationship.setActionUser(me);

        return relationshipRepository.save(relationship);
    }

    // Returns a user's connections, received friend requests and sent friend requests
    public NetworkResponse getAll(Long userId) {
        NetworkResponse networkResponse = new NetworkResponse();

        networkResponse.setConnections(relationshipRepository.getConnectionsByUserId(userId));
        networkResponse.setReceivedRequests(relationshipRepository.getReceivedRequestsByUserId(userId));
        networkResponse.setSentRequests(relationshipRepository.getSentRequestsByUserId(userId));

        return networkResponse;
    }

    // A user accepts a request
    public Relationship updateById(Long relationshipId, UserDetailsImpl currentUser) {
        Relationship relationship = validatePathService.validatePathAndGetRelationship(relationshipId);

        // Check if the user is the receiver of the request
        if (currentUser.getId() != relationship.getReceiver().getId()) {
            throw new NotAuthorizedException("You are not authorized to access this resource.");
        }

        relationship.setStatus(1);

        return relationshipRepository.save(relationship);
    }

    // A user declines a friend request
    public ResponseEntity<?> deleteById(Long relationshipId, UserDetailsImpl currentUser) {
        Relationship relationship = validatePathService.validatePathAndGetRelationship(relationshipId);

        // Check if the user is the receiver of the request
        if (currentUser.getId() != relationship.getReceiver().getId()) {
            throw new NotAuthorizedException("You are not authorized to access this resource.");
        }

        relationshipRepository.delete(relationship);

        return ResponseEntity.ok().body(new ApiResponse(true, "Successfully deleted relationship."));
    }

}