package com.hayato.apilearning;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;

@RestController
public class HelloController {
  
  private final UserService userService;

  public HelloController(UserService userService) {
      this.userService = userService;
  }

  @GetMapping("/hello")
  public String hello() {
      return "Hello World";
  }
  
  @GetMapping("/hello/{name}")
  public String helloName(@PathVariable String name) {
      return "こんにちは" + name;
  }

  @GetMapping("/users/{id}")
  public ResponseEntity<UserResponse> getUser(
        @PathVariable long id) {

    return ResponseEntity.ok(
            userService.getUser(id)
    );
  }

  @GetMapping("/greeting")
  public String greeting(@RequestParam String name) {
      return "こんにちは" + name;
  }

  @GetMapping("/profile")
    public String profile(
      @RequestParam String name,
      @RequestParam int age) {
      return "こんにちは " + name + "さん。" + age + "歳ですね。";
    }
  @GetMapping("/welcome")
    public String welcome(
      @RequestParam(defaultValue = "ゲスト") String name) {
      return "こんにちは " + name;
    }

  @GetMapping("/greeting-json")
    public GreetingResponse greetingJson(
      @RequestParam(defaultValue = "ゲスト") String name) {
      return new GreetingResponse(
            name,
            "こんにちは " + name
            );
    }

  @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(
        @Valid @RequestBody UserRequest request) {

    UserResponse response = userService.createUser(request);

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(response);
    }

  @GetMapping("/users")
  public List<UserResponse> getUsers() {
    return userService.getUsers();
  }

  @PutMapping("/users/{id}")
  public ResponseEntity<UserResponse> updateUser(
        @PathVariable long id,
        @Valid @RequestBody UserRequest request) {

      return ResponseEntity.ok(
            userService.updateUser(id, request)
    );
  }

  @DeleteMapping("/users/{id}")
  public ResponseEntity<Void> deleteUser(
          @PathVariable long id) {

      userService.deleteUser(id);

      return ResponseEntity.noContent().build();
  }
}
