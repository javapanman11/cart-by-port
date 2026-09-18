package com.hayato.apilearning;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(UserRequest request) {

        User user = new User(
                request.getName(),
                request.getAge()
        );

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getAge(),
                "ユーザーを登録しました"
        );
    }

    public List<UserResponse> getUsers() {

        List<User> users = userRepository.findAll();

        List<UserResponse> responses = new ArrayList<>();

        for (User user : users) {

            responses.add(
                    new UserResponse(
                            user.getId(),
                            user.getName(),
                            user.getAge(),
                            "ユーザーを取得しました"
                    )
            );
        }

        return responses;
    }

    public UserResponse getUser(long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getAge(),
            "ユーザーを取得しました"
        );
    }

    @Transactional
    public UserResponse updateUser(
        long id,
        UserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        user.setName(request.getName());
        user.setAge(request.getAge());

        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getAge(),
            "ユーザーを更新しました"
        );
    }

    public void deleteUser(long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        userRepository.delete(user);
    }
}