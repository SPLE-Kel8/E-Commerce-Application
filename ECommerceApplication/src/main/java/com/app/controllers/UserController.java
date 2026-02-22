package com.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.config.AppConstants;
import com.app.payloads.UserDTO;
import com.app.payloads.UserResponse;
import com.app.security.AuthUtil;
import com.app.services.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

/**
 * User Controller - manages user operations.
 * 
 * SECURITY: Users can only view/update their own profile.
 * Admins can access any user's profile.
 */
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "E-Commerce Application")
public class UserController {
	
	@Autowired
	private UserService userService;

	@Autowired
	private AuthUtil authUtil;
	
	@GetMapping("/admin/users")
	public ResponseEntity<UserResponse> getUsers(
			@RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
			@RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
			@RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_USERS_BY, required = false) String sortBy,
			@RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
		
		UserResponse userResponse = userService.getAllUsers(pageNumber, pageSize, sortBy, sortOrder);
		
		return new ResponseEntity<UserResponse>(userResponse, HttpStatus.FOUND);
	}
	
	/**
	 * Get user by ID
	 * SECURITY: Users can only view their own profile. Admins can view any.
	 */
	@GetMapping("/public/users/{userId}")
	public ResponseEntity<UserDTO> getUser(@PathVariable Long userId) {
		authUtil.validateUserAccess(userId);
		UserDTO user = userService.getUserById(userId);
		
		return new ResponseEntity<UserDTO>(user, HttpStatus.FOUND);
	}
	
	/**
	 * Update user by ID
	 * SECURITY: Users can only update their own profile. Admins can update any.
	 */
	@PutMapping("/public/users/{userId}")
	public ResponseEntity<UserDTO> updateUser(@RequestBody UserDTO userDTO, @PathVariable Long userId) {
		authUtil.validateUserAccess(userId);
		UserDTO updatedUser = userService.updateUser(userId, userDTO);
		
		return new ResponseEntity<UserDTO>(updatedUser, HttpStatus.OK);
	}
	
	@DeleteMapping("/admin/users/{userId}")
	public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
		String status = userService.deleteUser(userId);
		
		return new ResponseEntity<String>(status, HttpStatus.OK);
	}
}
