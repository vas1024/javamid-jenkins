package javamid.jenkins;


import javamid.jenkins.controllers.UserController;
import javamid.jenkins.dto.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;


@WebMvcTest(UserController.class)
class UserControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void createUser_shouldReturnCreatedUser() throws Exception {
    UserDto userDto = new UserDto(null, "Анна");

    mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userDto)))
            .andExpect(status().isCreated())
            .andExpect( jsonPath("$.id", notNullValue()))
            .andExpect( jsonPath("$.name").value("Анна"));
  }

  @Test
  void getUser_shouldReturnUserIfExists() throws Exception {
    UserDto userDto = new UserDto(null, "Олег");

    String response = mockMvc.perform(post("/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(userDto)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

    UserDto created = objectMapper.readValue(response, UserDto.class);

    mockMvc.perform(get("/users/" + created.getId()))
            .andExpect(status().isOk())
            .andExpect((ResultMatcher) jsonPath("$.name").value("Олег"));
  }

  @Test
  void getUser_shouldReturnNotFound() throws Exception {
    mockMvc.perform(get("/users/9999"))
            .andExpect(status().isNotFound());
  }
}
