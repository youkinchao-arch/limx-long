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

    // ---- P1: audit trail + security hardening ----

    private String loginToken(String username, String password) throws Exception {
        MvcResult result = mockMvc
                .perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("access_token").asText();
    }

    private void createUser(String username, String password) throws Exception {
        String body = "{\"username\":\"" + username + "\",\"full_name\":\"Sec " + username
                + "\",\"password\":\"" + password + "\"}";
        mockMvc.perform(post("/api/v1/auth/users")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void auditLogRecordsWriteOperations() throws Exception {
        String token = bearer();
        mockMvc.perform(post("/api/v1/documents")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"doc_no\":\"DOC-AUDIT\",\"title\":\"Audited\",\"status\":\"draft\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/audit/logs")
                        .header("Authorization", token)
                        .param("entity_type", "Document"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.items[0].action", is("CREATE")))
                .andExpect(jsonPath("$.items[0].entity_type", is("Document")));
    }

    @Test
    void weakPasswordRejectedOnUserCreate() throws Exception {
        mockMvc.perform(post("/api/v1/auth/users")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"weakpw_user\",\"full_name\":\"Weak\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", notNullValue()));
    }

    @Test
    void changePasswordInvalidatesOldTokenAndRotatesCredentials() throws Exception {
        createUser("pwchange_user", "Initial123");
        String oldToken = "Bearer " + loginToken("pwchange_user", "Initial123");

        MvcResult res = mockMvc
                .perform(post("/api/v1/auth/change-password")
                        .header("Authorization", oldToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"old_password\":\"Initial123\",\"new_password\":\"Updated456\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token", notNullValue()))
                .andReturn();
        String newToken = "Bearer " + objectMapper.readTree(res.getResponse().getContentAsString())
                .get("access_token").asText();

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", oldToken))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", newToken))
                .andExpect(status().isOk());
        loginToken("pwchange_user", "Updated456");
    }

    @Test
    void logoutInvalidatesToken() throws Exception {
        createUser("logout_user", "Logout123");
        String token = "Bearer " + loginToken("logout_user", "Logout123");

        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/auth/logout").header("Authorization", token))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/auth/me").header("Authorization", token))
                .andExpect(status().isUnauthorized());
    }

    // ---- P2: controlled document approval workflow ----

    private long createDocument(String docNo) throws Exception {
        MvcResult created = mockMvc
                .perform(post("/api/v1/documents")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"doc_no\":\"" + docNo + "\",\"title\":\"SOP\",\"status\":\"draft\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void documentApprovalWorkflowHappyPath() throws Exception {
        String token = bearer();
        long id = createDocument("DOC-WF-1");

        mockMvc.perform(post("/api/v1/documents/" + id + "/submit")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("under_review")))
                .andExpect(jsonPath("$.submitted_by_name", is("admin")));

        mockMvc.perform(post("/api/v1/documents/" + id + "/approve")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"looks good\",\"effective_date\":\"2030-01-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("approved")))
                .andExpect(jsonPath("$.approved_by_name", is("admin")))
                .andExpect(jsonPath("$.effective_date", is("2030-01-01")));

        mockMvc.perform(get("/api/v1/documents/" + id + "/reviews")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(2)))
                .andExpect(jsonPath("$[0].action", is("APPROVE")))
                .andExpect(jsonPath("$[1].action", is("SUBMIT")));
    }

    @Test
    void documentApproveRejectedWhenNotUnderReview() throws Exception {
        String token = bearer();
        long id = createDocument("DOC-WF-2");

        mockMvc.perform(post("/api/v1/documents/" + id + "/approve")
                        .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", notNullValue()));
    }

    @Test
    void documentApproveRequiresApprovePermission() throws Exception {
        long id = createDocument("DOC-WF-3");
        mockMvc.perform(post("/api/v1/documents/" + id + "/submit").header("Authorization", bearer()))
                .andExpect(status().isOk());

        createUser("doc_no_approve", "NoApprove123");
        String restricted = "Bearer " + loginToken("doc_no_approve", "NoApprove123");
        mockMvc.perform(post("/api/v1/documents/" + id + "/approve").header("Authorization", restricted))
                .andExpect(status().isForbidden());
    }

    @Test
    void documentRejectReturnsToRejectedAndCanResubmit() throws Exception {
        String token = bearer();
        long id = createDocument("DOC-WF-4");

        mockMvc.perform(post("/api/v1/documents/" + id + "/submit").header("Authorization", token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/documents/" + id + "/reject")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"needs work\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("rejected")));
        mockMvc.perform(post("/api/v1/documents/" + id + "/submit").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("under_review")));
    }

    @Test
    void accountLocksAfterTooManyFailedAttempts() throws Exception {
        createUser("lockout_user", "Lockout123");
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .param("username", "lockout_user")
                            .param("password", "wrong-password"))
                    .andExpect(status().isUnauthorized());
        }
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "lockout_user")
                        .param("password", "Lockout123"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail", notNullValue()));
    }

    // ---- P2: report approval + e-signature + issue workflow ----

    private long createReport(String reportNo) throws Exception {
        MvcResult created = mockMvc
                .perform(post("/api/v1/reports")
                        .header("Authorization", bearer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"report_no\":\"" + reportNo + "\",\"title\":\"Test Report\",\"status\":\"draft\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void reportFullWorkflowHappyPath() throws Exception {
        String token = bearer();
        long id = createReport("RPT-WF-1");

        mockMvc.perform(post("/api/v1/reports/" + id + "/submit")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("under_review")))
                .andExpect(jsonPath("$.submitted_by_name", is("admin")));

        mockMvc.perform(post("/api/v1/reports/" + id + "/approve")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"looks good\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("approved")))
                .andExpect(jsonPath("$.approved_by_name", is("admin")));

        mockMvc.perform(post("/api/v1/reports/" + id + "/sign")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"admin123\",\"meaning\":\"Reviewed and approved\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("signed")))
                .andExpect(jsonPath("$.signed_by_name", is("admin")))
                .andExpect(jsonPath("$.signature_hash", notNullValue()));

        mockMvc.perform(post("/api/v1/reports/" + id + "/issue")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"issue_date\":\"2030-06-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("issued")))
                .andExpect(jsonPath("$.issued_by_name", is("admin")))
                .andExpect(jsonPath("$.issue_date", is("2030-06-01")));

        mockMvc.perform(get("/api/v1/reports/" + id + "/signatures")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(4)))
                .andExpect(jsonPath("$[0].action", is("ISSUE")))
                .andExpect(jsonPath("$[3].action", is("SUBMIT")));
    }

    @Test
    void reportSignRequiresCorrectPassword() throws Exception {
        String token = bearer();
        long id = createReport("RPT-WF-2");
        mockMvc.perform(post("/api/v1/reports/" + id + "/submit").header("Authorization", token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/reports/" + id + "/approve").header("Authorization", token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/reports/" + id + "/sign")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"wrongpass\",\"meaning\":\"Test\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", notNullValue()));
    }

    @Test
    void reportRejectAndResubmit() throws Exception {
        String token = bearer();
        long id = createReport("RPT-WF-3");
        mockMvc.perform(post("/api/v1/reports/" + id + "/submit").header("Authorization", token))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/reports/" + id + "/reject")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comment\":\"needs revision\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("rejected")));
        mockMvc.perform(post("/api/v1/reports/" + id + "/submit").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("under_review")));
    }

    @Test
    void reportApproveRequiresApprovePermission() throws Exception {
        long id = createReport("RPT-WF-4");
        mockMvc.perform(post("/api/v1/reports/" + id + "/submit").header("Authorization", bearer()))
                .andExpect(status().isOk());
        createUser("rpt_no_approve", "NoApprove123");
        String restricted = "Bearer " + loginToken("rpt_no_approve", "NoApprove123");
        mockMvc.perform(post("/api/v1/reports/" + id + "/approve").header("Authorization", restricted))
                .andExpect(status().isForbidden());
    }

    @Test
    void reportPdfDownload() throws Exception {
        String token = bearer();
        long id = createReport("RPT-PDF-1");
        mockMvc.perform(get("/api/v1/reports/" + id + "/pdf")
                        .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }
}
