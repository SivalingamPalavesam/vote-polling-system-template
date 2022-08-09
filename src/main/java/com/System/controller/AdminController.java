package com.System.controller;


import com.System.payload.PagedResponse;
import com.System.payload.PollResponse;
import com.System.security.CurrentUser;
import com.System.security.UserPrincipal;
import com.System.service.AdminService;
import com.System.util.AppConstants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/admin")
public class AdminController {

	@Autowired
	AdminService adminService;

	/**
	 * 
	 * @param username
	 * @param currentUser
	 * @param page
	 * @param size
	 * @return PagedResponse(pollResponses,Number(),Size(),TotalElements(), TotalPages(), isLast())
	 * @implSpec already pollResponses ==> all polls value stored
	 */
	@GetMapping("/users/{username}/votes")
	@PreAuthorize("hasRole('ADMIN')")
	public PagedResponse<PollResponse> getPollsVotedBy(@PathVariable(value = "username") String username,
			@CurrentUser UserPrincipal currentUser,
			@RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
			@RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {

		return adminService.getPollsVotedBy(username, currentUser, page, size);
	}
}
