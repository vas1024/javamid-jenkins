package javamid.jenkins.controllers;

import javamid.jenkins.dto.UserDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/users")
public class UserController {

  private final Map<Long, UserDto> users = new ConcurrentHashMap<>();
  private final AtomicLong idGenerator = new AtomicLong(1);

  @GetMapping("/{id}")
  public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
    UserDto userDto = users.get(id);
    if (userDto != null) {
      return ResponseEntity.ok(userDto);
    }
    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
  }

  @PostMapping
  public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto) {
    long id = idGenerator.getAndIncrement();
    userDto.setId(id);
    users.put(id, userDto);
    URI location = URI.create("/users/" + id);
    return ResponseEntity.created(location).body(userDto);
  }
}