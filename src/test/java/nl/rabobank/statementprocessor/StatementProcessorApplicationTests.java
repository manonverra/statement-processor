package nl.rabobank.statementprocessor;

import com.google.gson.Gson;
import nl.rabobank.statementprocessor.model.Result;
import nl.rabobank.statementprocessor.model.ValidationResult;
import nl.rabobank.statementprocessor.view.RecordView;
import nl.rabobank.statementprocessor.view.ValidationResultView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StatementProcessorApplicationTests {

	private static final String URI = "/statement/validate";

	private static final String INPUT_HAPPY_FLOW = "{\n" +
			"  \"records\": [\n" +
			"      {\n" +
			"          \"transactionReference\": 123456789,\n" +
			"          \"accountNumber\": \"NL34RABO0123456789\",\n" +
			"          \"startBalance\": 1234,\n" +
			"          \"mutation\": 34,\n" +
			"          \"description\": \"Groceries\",\n" +
			"          \"endBalance\": 1200 \n" +
			"      },\n" +
			"      {\n" +
			"          \"transactionReference\": 111111111,\n" +
			"          \"accountNumber\": \"NL34RABO0111111111\",\n" +
			"          \"startBalance\": 1111,\n" +
			"          \"mutation\": 11,\n" +
			"          \"description\": \"Something else\",\n" +
			"          \"endBalance\": 1100 \n" +
			"      }\n" +
			"  ]\n" +
			"}";

	private static final String INPUT_DUPLICATE_REFERENCES = "{\n" +
			"  \"records\": [\n" +
			"      {\n" +
			"          \"transactionReference\": 123456789,\n" +
			"          \"accountNumber\": \"NL34RABO0123456789\",\n" +
			"          \"startBalance\": 1234,\n" +
			"          \"mutation\": 34,\n" +
			"          \"description\": \"Groceries\",\n" +
			"          \"endBalance\": 1200 \n" +
			"      },\n" +
			"      {\n" +
			"          \"transactionReference\": 123456789,\n" +
			"          \"accountNumber\": \"NL34RABO0111111111\",\n" +
			"          \"startBalance\": 1111,\n" +
			"          \"mutation\": 11,\n" +
			"          \"description\": \"Something else\",\n" +
			"          \"endBalance\": 1100 \n" +
			"      }\n" +
			"  ]\n" +
			"}";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void shouldReturn200_whenValidationIsExecuted() throws Exception {
		this.mockMvc.perform(post(URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(INPUT_HAPPY_FLOW))
				.andExpect(status().isOk());
	}

	@Test
	void shouldReturnValidationResult_whenControllerIsCalled() throws Exception {
		ValidationResult validationResult = new ValidationResult(Result.SUCCESSFUL, new ArrayList<>());
		String expectedContent = new Gson().toJson(validationResult);

		MvcResult mvcResult = this.mockMvc.perform(post(URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(INPUT_HAPPY_FLOW))
				.andReturn();
		assertEquals(expectedContent, mvcResult.getResponse().getContentAsString());
	}

	@Test
	void shouldReturnErrorRecords_whenControllerIsCalled() throws Exception {
		RecordView errorRecord = new RecordView(123456789, "NL34RABO0111111111");
		ValidationResultView validationResultView = new ValidationResultView(Result.DUPLICATE_REFERENCE, Arrays.asList(errorRecord));
		String expectedContent = new Gson().toJson(validationResultView);

		MvcResult mvcResult = this.mockMvc.perform(post(URI)
				.contentType(MediaType.APPLICATION_JSON)
				.content(INPUT_DUPLICATE_REFERENCES))
				.andReturn();
		assertEquals(expectedContent, mvcResult.getResponse().getContentAsString());
	}

}
