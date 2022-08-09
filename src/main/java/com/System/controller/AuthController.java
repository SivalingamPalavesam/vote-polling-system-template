package com.System.controller;

import com.System.payload.SignInRequest;
import com.System.payload.SignInResponse;
import com.System.payload.SignUpRequest;
import com.System.repository.RoleRepository;
import com.System.repository.UserRepository;
import com.System.security.JwtTokenProvider;
import com.System.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder passwordEncoder;

	@Autowired
	JwtTokenProvider tokenProvider;

	@Autowired
	AuthService  authService;

	@PostMapping("/signUp")
	public ResponseEntity<SignInResponse> registerUser(@Valid @RequestBody SignUpRequest signUpRequest)
	{
		return authService.registerUser(signUpRequest);
	}

	@PostMapping("/signIn")
	public ResponseEntity<SignInResponse> userAuthentication(@RequestBody SignInRequest signInRequest)
	{
		return authService.userAuthentication(signInRequest);
	}
}