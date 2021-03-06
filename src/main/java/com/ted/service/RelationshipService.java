package com.ted.service;

import com.ted.exception.NotAuthorizedException;
import com.ted.model.Relationship;
import com.ted.model.User;
import com.ted.repository.RelationshipRepository;
import com.ted.request.RelationshipRequest;
import com.ted.request.ConversationRequest;
import com.ted.response.ApiResponse;
import com.ted.response.NetworkResponse;
import com.ted.security.UserDetailsImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class RelationshipService {

    private static final Logger logger = LoggerFactory.getLogger(RelationshipService.class);

    @Autowired
    private RelationshipRepository relationshipRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ConversationService conversationService;

    @Autowired
    private ValidatePathService validatePathService;

    // Creates a friend request
    @Transactional
    public Relationship create(Long userId, RelationshipRequest relationshipRequest) {
        User me = userService.getById(userId);

        // Check that the request receiver exists
        User receiver = userService.getById(relationshipRequest.getReceiver());

        Relationship relationship = new Relationship();

        relationship.setSender(me);
        relationship.setReceiver(receiver);
        relationship.setStatus(0);
        relationship.setSeen(false);

        return relationshipRepository.save(relationship);
    }

    // Returns a user's connections, received friend requests and sent friend requests
    public NetworkResponse getAll(Long userId) {
        List<User> network = new ArrayList<>();
        List<Relationship> connections = relationshipRepository.getConnectionsByUserId(userId);
        List<Relationship> receivedRequests = relationshipRepository.getReceivedRequestsByUserId(userId);
        List<Relationship> sentRequests = relationshipRepository.getSentRequestsByUserId(userId);

        // Extract the User objects that form the network.
        for (Relationship c : connections) {
            if (userId == c.getSender().getId()) {
                network.add(userService.getById(c.getReceiver().getId()));
            } else {
                network.add(userService.getById(c.getSender().getId()));
            }
        }

        // Create the network response
        NetworkResponse networkResponse = new NetworkResponse();

        networkResponse.setConnections(network);
        networkResponse.setReceivedRequests(receivedRequests);
        networkResponse.setSentRequests(sentRequests);

        // Update the received requests as seen.
        for (Relationship r : receivedRequests) {
            r.setSeen(true);
            relationshipRepository.save(r);
        }

        return networkResponse;
    }

    // A user accepts a request
    @Transactional
    public Relationship updateById(Long relationshipId, UserDetailsImpl currentUser) {
        Relationship relationship = validatePathService.validatePathAndGetRelationship(relationshipId);

        // Check if the user is the receiver of the request
        if (currentUser.getId() != relationship.getReceiver().getId()) {
            throw new NotAuthorizedException("You are not authorized to access this resource.");
        }

        relationship.setStatus(1);

        // Create a conversation between the two users
        ConversationRequest conversationRequest = new ConversationRequest(relationship.getSender().getId());
        conversationService.create(currentUser.getId(), conversationRequest);

        return relationshipRepository.save(relationship);
    }

    // A user declines a friend request
    @Transactional
    public ResponseEntity<?> deleteById(Long relationshipId, UserDetailsImpl currentUser) {
        Relationship relationship = validatePathService.validatePathAndGetRelationship(relationshipId);

        // Check if the user is the receiver of the request
        if (currentUser.getId() != relationship.getReceiver().getId()) {
            throw new NotAuthorizedException("You are not authorized to access this resource.");
        }

        relationshipRepository.delete(relationship);

        return ResponseEntity.ok().body(new ApiResponse(true, "Successfully deleted relationship."));
    }

    // Checks if there is a relationship between two users
    public boolean areRelated(Long userOne, Long userTwo) {
        return relationshipRepository.areRelated(userOne, userTwo).size() > 0;
    }

    // Checks if there is a connection between two users
    public boolean areConnected(Long userOne, Long userTwo) {
        return relationshipRepository.areConnected(userOne, userTwo).size() > 0;
    }

}
