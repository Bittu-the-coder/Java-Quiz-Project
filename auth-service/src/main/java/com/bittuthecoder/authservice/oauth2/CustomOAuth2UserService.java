package com.bittuthecoder.authservice.oauth2;

import com.bittuthecoder.authservice.repository.UserRepository;
import com.bittuthecoder.authservice.models.UserModel;
import com.bittuthecoder.authservice.models.enums.AuthProvider;
import com.bittuthecoder.authservice.models.enums.Role;
import com.bittuthecoder.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request)
            throws OAuth2AuthenticationException {

        OAuth2User oauth2User = super.loadUser(request);

        System.out.println("OAUTH ATTRIBUTES = " + oauth2User.getAttributes());

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from provider");
        }

        AuthProvider provider =
                AuthProvider.valueOf(
                        request.getClientRegistration()
                                .getRegistrationId()
                                .toUpperCase()
                );

        UserModel user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    UserModel newUser = new UserModel();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setRole(Role.STUDENT);
                    newUser.setProvider(provider);
                    newUser.setActive(true);
                    return userRepository.save(newUser);
                });

        return oauth2User;
    }
}
