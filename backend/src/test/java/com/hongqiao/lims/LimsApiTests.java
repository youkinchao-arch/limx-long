package com.hongqiao.lims;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LimsApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken() throws Exception {
        MvcResult result = mockMvc
                .perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "admin")
                        .param("password", "admin123"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("access_token").asText();
    }

    private String bearer() throws Exception {
        return "Bearer " + adminToken();
    }

    @Test
    void healthReturnsOk() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ok")));
    }

    @Test
    void loginSucceedsAndReturnsBearerToken() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "admin")
                        .param("password", "admin123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", notNullValue()))
                .andExpect(jsonPath("$.token_type", is("bearer")));
    }

    @Test
    void loginWithWrongPasswordIsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "admin")
                        .param("password", "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail", notNullValue()));
    }

    @Test
    void meReturnsAdminProfileWithPermissions() throws Exception {
        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("admin")))
                .andExpect(jsonPath("$.is_superuser", is(true)))
                .andExpect(jsonPath("$.permissions[0]", is("*")));
    }

    @Test
    void protectedEndpointWithoutTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/personnel")).andExpect(status().isUnauthorized());
    }

    @Test
    void rolesEndpointReturnsSeededRoles() throws Exception {
        mockMvc.perform(get("/api/v1/auth/roles").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(3)));
    }

    @Test
    void personnelCrudLifecycle() throws Exception {
        String token = bearer();
        String payload = "{\"employee_no\":\"E-T100\",\"name\":\"Tester\",\"status\":\"active\"}";
        MvcResult created = mockMvc
                .perform(post("/api/v1/personnel")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employee_no", is("E-T100")))
                .andReturn();
        long id = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(get("/api/v1/personnel").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.page_size", greaterThanOrEqualTo(1)));

        mockMvc.perform(delete("/api/v1/personnel/" + id).header("Authorization", token))
                .andExpect(status().isNoContent());
    }

    @Test
    void equipmentCreateAndQrCode() throws Exception {
        String token = bearer();
        String payload = "{\"asset_no\":\"EQ-T100\",\"name\":\"Balance\",\"status\":\"in_use\"}";
        MvcResult created = mockMvc
                .perform(post("/api/v1/equipment")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.asset_no", is("EQ-T100")))
                .andReturn();
        long id = objectMapper.readTree(created.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(get("/api/v1/equipment/" + id + "/qrcode").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    void dashboardSummaryHasExpectedShape() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/summary").header("Authorization", bearer()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totals.personnel", notNullValue()))
                .andExpect(jsonPath("$.reminders.window_days", is(30)))
                .andExpect(jsonPath("$.equipment_status", notNullValue()));
    }

    @Test
    void documentModuleCrud() throws Exception {
        String token = bearer();
        String payload = "{\"doc_no\":\"DOC-T1\",\"title\":\"SOP\",\"status\":\"draft\"}";
        mockMvc.perform(post("/api/v1/documents")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.doc_no", is("DOC-T1")));

        mockMvc.perform(get("/api/v1/documents").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", notNullValue()));
    }
}
