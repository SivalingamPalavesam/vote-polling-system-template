package com.System.controller;

import com.System.payload.PollResponse;
import com.System.payload.UserIdentityAvailability;
import com.System.payload.UserSummary;
import com.System.payload.VoteRequest;
import com.System.repository.UserRepository;
import com.System.security.CurrentUser;
import com.System.security.UserPrincipal;
import com.System.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@RestController
@RequestMapping("auth/poll")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	UserRepository userRepository;
 	/**
	 * @param currentUser
	 * @param pollId
	 * @param voteRequest
	 * @return ModelMapper.mapPollToPollResponse(poll, choiceVotesMap, creator, vote.getChoice().getId())
	 */
	@PostMapping("/{pollId}/castVote")
	@PreAuthorize("hasRole('USER')")
	public PollResponse castVote(@CurrentUser UserPrincipal currentUser,
			@PathVariable Long pollId,
			@Valid @RequestBody VoteRequest voteRequest) {
		return userService.castVoteAndGetUpdatedPoll(pollId, voteRequest, currentUser);
	}

	/**
	 * @param currentUser
	 * @return userSummary
	 * @implSpec current login User details
	 */
	@GetMapping("/user/currentLoginUser")
	@PreAuthorize("hasRole('USER')")
	public UserSummary getCurrentUser(@CurrentUser UserPrincipal currentUser ) {
		UserSummary userSummary = new UserSummary(currentUser.getId(), currentUser.getUsername(), currentUser.getName());
		return userSummary;
	}
	/**
	 * @param username
	 * @return boolean value 
	 */
	@GetMapping("/user/checkUsernameAvailability")
	public UserIdentityAvailability checkUsernameAvailability(@RequestParam(value = "username") String username) {
		Boolean isAvailable = userRepository.existsByUsername(username);
		return new UserIdentityAvailability(isAvailable);
	}
	
	/**
	 * @param email
	 * @return boolean value
	 */
	@GetMapping("/user/checkEmailAvailability")
	public UserIdentityAvailability checkEmailAvailability(@RequestParam(value = "email") String email) {
		Boolean isAvailable =userRepository.existsByEmail(email);
		return new UserIdentityAvailability(isAvailable);
	}
}
