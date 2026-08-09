package io.github.mksfilmoteka.user.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static io.github.mksfilmoteka.user.profile.UserProfileTestData.IDENTITY_SUB;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SecurityConfigTest.TestController.class)
@Import({SecurityConfig.class, KeycloakRealmRoleConverter.class, SecurityConfigTest.TestController.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/test")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldAllowAuthenticatedRequest() throws Exception {
        mockMvc.perform(get("/test").with(jwt().jwt(jwt -> jwt.subject(IDENTITY_SUB))))
                .andExpect(status().isOk())
                .andExpect(content().string("ok"));
    }

    @Test
    void shouldAllowSwaggerUiAndApiDocs() throws Exception {
        mockMvc.perform(get("/swagger-ui.html")).andExpect(status().isOk());
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
        mockMvc.perform(get("/api-docs")).andExpect(status().isOk());
        mockMvc.perform(get("/api-docs/profile")).andExpect(status().isOk());
    }

    @RestController
    static class TestController {
        @GetMapping("/test")
        String test() {
            return "ok";
        }

        @GetMapping({"/swagger-ui.html", "/swagger-ui/index.html", "/api-docs", "/api-docs/{group}"})
        String swagger() {
            return "ok";
        }
    }
}
