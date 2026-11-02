package edu.nit.placement;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * End-to-end API smoke test against the in-memory store.
 * Exercises seed data, login, role guards and the main flows.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String login(String email, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(email))
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("token").asText();
    }

    @Test
    void studentCanBrowseEligibleDrivesAndApply() throws Exception {
        String token = login("student@nit.edu", "Student@123");

        MvcResult dashboard = mvc.perform(get("/api/student/dashboard")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].companyName").isNotEmpty())
                .andReturn();

        JsonNode drives = objectMapper.readTree(dashboard.getResponse().getContentAsString());
        long infosysId = -1;
        for (JsonNode drive : drives) {
            if ("Infosys".equals(drive.get("companyName").asText())) {
                infosysId = drive.get("id").asLong();
            }
        }
        assertTrue(infosysId > 0, "Seeded Infosys drive should be visible");

        mvc.perform(post("/api/student/announcements/" + infosysId + "/apply")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applied").value(true));

        mvc.perform(get("/api/student/applications")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mvc.perform(get("/api/student/deadlines")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mvc.perform(get("/api/notifications")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void studentCannotReachAdminEndpoints() throws Exception {
        String token = login("student@nit.edu", "Student@123");
        mvc.perform(get("/api/admin/stats").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mvc.perform(get("/api/student/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminSeesStatistics() throws Exception {
        String token = login("admin@nit.edu", "Admin@123");
        mvc.perform(get("/api/admin/stats").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents").isNumber())
                .andExpect(jsonPath("$.totalDrives").isNumber());
    }

    @Test
    void coordinatorCanPublishADrive() throws Exception {
        String token = login("coordinator@nit.edu", "Coord@123");
        String payload = """
                {
                  "companyName": "Adobe",
                  "roleTitle": "Member of Technical Staff",
                  "details": "Demo drive created by the integration test",
                  "ctc": 9.5,
                  "location": "Noida",
                  "lastDate": "2027-01-15",
                  "testDate": "2027-01-20",
                  "interviewDate": "2027-01-25",
                  "venue": "Placement Cell",
                  "eligibility": {
                    "branches": ["CSE", "ECE"],
                    "minCgpa": 7.0,
                    "maxBacklogs": 0,
                    "batch": "2026"
                  },
                  "rounds": [
                    {"name": "Online Test", "date": "2027-01-20"},
                    {"name": "Interview", "date": "2027-01-25"}
                  ]
                }
                """;
        mvc.perform(post("/api/announcements")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Adobe"))
                .andExpect(jsonPath("$.eligibility.minCgpa").value(7.0))
                .andExpect(jsonPath("$.rounds.length()").value(2));
    }

    @Test
    void adminCanReadUserPasswordHash() throws Exception {
        String token = login("admin@nit.edu", "Admin@123");
        // Pre-defined user order (database/02-seed.sql): Manoj = 1 … admin = 8.
        mvc.perform(get("/api/admin/users/1/password-hash").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("student@nit.edu"))
                .andExpect(jsonPath("$.algorithm").value("bcrypt"))
                .andExpect(jsonPath("$.passwordHash").value(startsWith("$2")));
    }

    @Test
    void registerCreatesStudentAccount() throws Exception {
        String uniqueEmail = "new.student." + System.currentTimeMillis() + "@nit.edu";
        // 01-schema.sql enforces uk_student_roll_no (UNIQUE) on student_profiles —
        // the roll number must be fresh on every test run.
        String uniqueRollNo = "25CS" + String.valueOf(System.currentTimeMillis()).substring(5);
        String payload = """
                {
                  "name": "New Student",
                  "email": "%s",
                  "password": "NewStudent@123",
                  "rollNo": "%s",
                  "branch": "CSE",
                  "batch": "2026",
                  "cgpa": 8.0,
                  "backlogs": 0
                }
                """.formatted(uniqueEmail, uniqueRollNo);
        mvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("STUDENT"))
                .andExpect(jsonPath("$.user.dashboardKey").value("student"))
                .andExpect(jsonPath("$.user.email").value(uniqueEmail));
    }
}
