package roomescape;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class FrontendResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("루트 주소에서 사용자 화면으로 이동한다")
    void getRoot_success() throws Exception {
        // when & then
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/user/index.html"));
    }

    @Test
    @DisplayName("사용자 화면을 정적 리소스로 제공한다")
    void getUserPage_success() throws Exception {
        // when & then
        mockMvc.perform(get("/user/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("ROOM ZERO")));
    }

    @Test
    @DisplayName("프론트엔드 API 스크립트를 정적 리소스로 제공한다")
    void getApiScript_success() throws Exception {
        // when & then
        mockMvc.perform(get("/shared/js/api.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/javascript"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("const BASE_URL")));
    }
}
