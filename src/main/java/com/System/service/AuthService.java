package com.System.service;

import java.util.Collections;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.System.controller.AuthController;
import com.System.entity.Role;
import com.System.entity.RoleName;
import com.System.entity.User;
import com.System.payload.SignInRequest;
import com.System.payload.SignInResponse;
import com.System.payload.SignUpRequest;
import com.System.payload.SignUpResponse;
import com.System.repository.RoleRepository;
import com.System.repository.UserRepository;
import com.System.security.JwtTokenProvider;

@Service
public class AuthService
{
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

	private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

	/**
	 * 
	 * @param signUpRequest -(name,userName,email,password)
	 * @return ResponseEntity<SignInResponse>
	 * @implNote New User Register and save database
	 */
	public ResponseEntity<SignInResponse> registerUser(@Valid SignUpRequest signUpRequest)
	{
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return new ResponseEntity(new SignUpResponse(false, "Username is already taken"), HttpStatus.BAD_REQUEST);
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return new ResponseEntity(new SignUpResponse(false, "Email is already taken"), HttpStatus.BAD_REQUEST);
		}

		User user = new User(signUpRequest.getName(), signUpRequest.getUsername(), signUpRequest.getEmail(), signUpRequest.getPassword());

		user.setPassword(passwordEncoder.encode(user.getPassword()));
		if(userRepository.count() == 0)
		{
			Role userRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
					.orElseThrow(() -> new NullPointerException("Admin Role not set."));
			user.setRoles(Collections.singleton(userRole));
			userRepository.save(user);
		}
		else {
			Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
					.orElseThrow(() -> new NullPointerException("User Role not set."));
			user.setRoles(Collections.singleton(userRole));
			userRepository.save(user);
		}

		logger.info("Your data is registered successfully");
		return new ResponseEntity(new SignUpResponse(true, "User registered successfully"), HttpStatus.ACCEPTED);
	}

	/**
	 * 
	 * @param SignInRequest (UsernameOrEmail,Password)
	 * @return ResponseEntity<SignInResponse>
	 * @implNote Validating user’s only signIn
	 * @implNote  Generate a JWT authentication token and return the token in the response
	 */
	public ResponseEntity<SignInResponse> userAuthentication(SignInRequest signInRequest)
	{
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
						signInRequest.getUsernameOrEmail(),
						signInRequest.getPassword()
						)
				);

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = tokenProvider.generateToken(authentication);

		logger.info("Your data validated & signIn successfully");
		return ResponseEntity.ok(new SignInResponse(jwt));
	}

}
