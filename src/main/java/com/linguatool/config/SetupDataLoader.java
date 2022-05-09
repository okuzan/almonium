package com.linguatool.config;

import com.linguatool.dto.SocialProvider;
import com.linguatool.model.user.Role;
import com.linguatool.model.user.User;
import com.linguatool.repo.RoleRepository;
import com.linguatool.repo.UserRepository;
import com.linguatool.service.UserService;
import com.linguatool.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SetupDataLoader implements ApplicationListener<ContextRefreshedEvent> {

    private boolean alreadySetup = false;

    @Autowired
    private UserRepository userRepository;

	@Autowired
	private UserServiceImpl userService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public void onApplicationEvent(final ContextRefreshedEvent event) {
		if (alreadySetup) {
			return;
		}
		// Create initial roles
//		Role userRole = createRoleIfNotFound(Role.ROLE_USER);
//		Role adminRole = createRoleIfNotFound(Role.ROLE_ADMIN);
//		createUserIfNotFound("admin@mail.com", Set.of(userRole, adminRole));
//		createUserIfNotFound("admin2@mail.com", Set.of(userRole, adminRole));
//		createUserIfNotFound("admin3@mail.com", Set.of(userRole, adminRole));
//		userRepository.getById(2L);
//		userRepository.getById(1L);
//		userService.createFriendshipRequest(2L, 3L);
		userService.confirmFriendshipRequest(3L, 2L);
//		alreadySetup = true;
	}

	@Transactional
	User createUserIfNotFound(final String email, Set<Role> roles) {
		User user = userRepository.findByEmail(email);
		if (user == null) {
			user = new User();
			user.setUsername("Admin");
			user.setEmail(email);
			user.setPassword(passwordEncoder.encode("password"));
			user.setRoles(roles);
			user.setProvider(SocialProvider.LOCAL.getProviderType());
			user.setEnabled(true);
			LocalDateTime now = LocalDateTime.now();
			user.setCreated(now);
			user.setModified(now);
			user = userRepository.save(user);
		}
		return user;
	}

	@Transactional
	Role createRoleIfNotFound(final String name) {
		Role role = roleRepository.findByName(name);
		if (role == null) {
			role = roleRepository.save(new Role(name));
		}
		return role;
	}
}
