package com.ted.controller;

import com.ted.model.Experience;
import com.ted.request.ExperienceRequest;
import com.ted.service.ExperienceService;
import com.ted.response.ApiResponse;
import com.ted.exception.NotAuthorizedException;
import com.ted.security.CurrentUser;
import com.ted.security.UserDetailsImpl;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.http.ResponseEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ExperienceController {

    @Autowired
    private ExperienceService experienceService;

    // Adds an experience for a user
    @PostMapping("/users/{userId}/experience")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> create(@Valid @RequestBody ExperienceRequest experienceRequest,
                                    @PathVariable(value = "userId") Long userId,
                                    @Valid @CurrentUser UserDetailsImpl currentUser) {
        // Check if the logged in user is authorized to access the path
        if (currentUser.getId() != userId) {
            throw new NotAuthorizedException("You are not authorized to create this resource.");
        }

        Experience experience = experienceService.create(userId, experienceRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{experienceId}")
                .buildAndExpand(experience.getId()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "Experience Created.", experience));
    }

    // Returns a user's experience
    @GetMapping("/users/{userId}/experience")
    @PreAuthorize("hasRole('USER')")
    public List<Experience> getAll(@PathVariable(value = "userId") Long userId,
                                   @Valid @CurrentUser UserDetailsImpl currentUser) {
        // Check if the logged in user is authorized to access the path
        if (currentUser.getId() != userId) {
            throw new NotAuthorizedException("You are not authorized to access this resource.");
        }

        return experienceService.getAll(userId);
    }

}
