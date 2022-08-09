package com.System.controller;

import com.System.entity.Poll;
import com.System.payload.*;
import com.System.repository.PollRepository;
import com.System.repository.UserRepository;
import com.System.security.CurrentUser;
import com.System.security.UserPrincipal;
import com.System.service.PollService;
import com.System.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth/poll")
public class PollController {

    @Autowired
    PollRepository pollRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private PollService pollService;
    
    /**
	 * @param pollRequest
	 * @return  ResponseEntity<SignUpResponse>
	 */
    @PostMapping("/createPoll")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SignUpResponse> createPoll(@Valid @RequestBody PollRequest pollRequest)
    {
        Poll poll = pollService.createPoll(pollRequest);
        return new ResponseEntity<>(new SignUpResponse(true, "Poll created successfully"), HttpStatus.ACCEPTED);
    }
    
    /**
     * @param id
     * @returnResponseEntity<SignUpResponse>
     */
    @PostMapping("/deletePoll")
    @PreAuthorize(("hasRole('ADMIN')"))
    public  ResponseEntity<SignUpResponse> deletePoll(@Valid @RequestParam Long id)
    {
        pollService.deleteByID(id);
        return new ResponseEntity<>(new SignUpResponse(true, "Poll deleted successfully"), HttpStatus.ACCEPTED);
    }

    /**
     * @param currentUser
     * @param pollId
     * @return particular poll getting used
     * @implNote modelMapper class to  response
     */
    @GetMapping("/{pollId}")
    @PreAuthorize("hasRole('ADMIN')")
    public PollResponse getPollById(@CurrentUser UserPrincipal currentUser, @PathVariable Long pollId)
    {
        return pollService.getPollById(pollId, currentUser);
    }
    
    /**
     * @param currentUser
     * @param page
     * @param size
     * @return  PagedResponse<>(pollResponses)
     * @implNote Polls viewing in user and admin
     */
    @GetMapping("/getAllPolls")
    @PreAuthorize("hasRole('ADMIN')")
    public PagedResponse<PollResponse> getPolls(@CurrentUser UserPrincipal currentUser,
                                                @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) 
    {
        return pollService.getAllPolls(currentUser, page, size);
    }  
}
