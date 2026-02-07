import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Compares ONLY the first CSV column (TOKEN) against lexer token types,
 * but prints the full CSV row (TOKEN,ITEM) on mismatches.
 *
 * @author javiergs
 * @version 2026
 */
class LexerTestGrade {

	private int grade = 0;

	private List<String[]> loadResultsCSV(String filename) throws IOException {
		List<String[]> rows = new ArrayList<>();
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			boolean headerSkipped = false;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				// Skip header if present
				if (!headerSkipped) {
					headerSkipped = true;
					if (line.equalsIgnoreCase("TOKEN,ITEM") || line.toUpperCase().startsWith("TOKEN,")) {
						continue;
					}
				}
				// Split into two columns max (so ITEM can contain commas if needed later)
				String[] parts = line.split(",", 2);
				String tokenCol = parts.length > 0 ? parts[0].trim() : "";
				String itemCol = parts.length > 1 ? parts[1].trim() : "";
				// Keep even if itemCol is empty; tokenCol must exist
				if (!tokenCol.isEmpty()) {
					rows.add(new String[]{tokenCol, itemCol});
				}
			}
		}
		return rows;
	}

	private boolean matchesExpectedType(String actualType, String expectedType) {
		if (actualType == null || expectedType == null) {
			return false;
		}
		if (actualType.equals(expectedType)) {
			return true;
		}
		if (expectedType.equals("IDENTIFIER") && actualType.equals("ID")) return true;
		if (expectedType.equals("INTEGER") && actualType.equals("INT")) return true;
		if (expectedType.equals("HEXADECIMAL") && actualType.equals("HEX")) return true;
		// if (expectedType.equals("DELIMITER") && actualType.equals("SEPARATOR")) return true;
		return false;
	}

	@Test
	void testingTokens() throws IOException {
		File file = new File("src/test/resources/input.txt");
		Lexer lexer = new Lexer(file);
		lexer.run();
		List<Token> tokens = lexer.getTokens();
		List<String[]> expectedRows = loadResultsCSV("src/test/resources/output.csv");
		int comparisons = Math.min(tokens.size(), expectedRows.size());
		int errors = 0;
		for (int i = 0; i < comparisons; i++) {
			Token token = tokens.get(i);
			String expectedType = expectedRows.get(i)[0]; // first column only
			String expectedItem = expectedRows.get(i)[1]; // for printing only
			String actualType = token.getType().toString();
			String actualValue = token.getValue();
			if (matchesExpectedType(actualType, expectedType)) {
				grade++;
				System.out.println((i + 1) + ". CORRECT: " + actualValue + " is " + expectedType);
			} else {
				errors++;
				System.out.println((i + 1) + ". ERROR:");
				System.out.println("   Actual   : " + actualType + "," + actualValue);
				System.out.println("   Expected : " + expectedType + "," + expectedItem);
			}
		}
		if (tokens.size() != expectedRows.size()) {
			System.out.println("WARNING: token count mismatch");
			System.out.println("   Lexer tokens : " + tokens.size());
			System.out.println("   Expected     : " + expectedRows.size());
		}
		System.out.println("SUMMARY:");
		System.out.println("   Compared : " + comparisons);
		System.out.println("   Correct  : " + grade);
		System.out.println("   Errors   : " + errors);
	}
}
