package wss;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.openhft.hashing.LongHashFunction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Originally created by Pim De Witte.
 * 
 * Performance drastically improved by over an order of magnitude by Thomas G.
 * P. Nappo (Jire). Garbage production has been eliminated as well.
 */
public class BadWords {

	static Long2ObjectMap<String[]> words = new Long2ObjectOpenHashMap<>();
	static int largestWordLength = 0;

	public static void flag(String word) {
		String[] ignore_in_combination_with_words = new String[] {};
		if (word.length() > largestWordLength) {
			largestWordLength = word.length();
		}
		words.put(LongHashFunction.xx().hashChars(word.replaceAll(" ", "")), ignore_in_combination_with_words);
	}

	public static void loadConfigs() {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(
					"https://docs.google.com/spreadsheets/d/1hIEi2YG3ydav1E06Bzf2mQbGZ12kh2fe4ISgLg_UBuM/export?format=csv")
							.openConnection().getInputStream()));
			String line = "";
			int counter = 0;
			while ((line = reader.readLine()) != null) {
				counter++;
				String[] content = null;
				try {
					content = line.split(",");
					if (content.length == 0) {
						continue;
					}
					String word = content[0];
					String[] ignore_in_combination_with_words = new String[] {};
					if (content.length > 1) {
						ignore_in_combination_with_words = content[1].split("_");
					}

					if (word.length() > largestWordLength) {
						largestWordLength = word.length();
					}
					words.put(LongHashFunction.xx().hashChars(word.replace(" ", "")), ignore_in_combination_with_words);

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			System.out.println("Loaded " + counter + " words to filter out");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static final char[][] convert = { { 'o', '0' }, { 'i', '1' }, { 'l', '1' }, { 't', '+' }, { 'e', '3' },
			{ 'i', '!' }, { 'l', '!' }, { 's', '$' }, { 'a', '&' }, { 'a', '@' }, { 'c', '(' }, { 'd', ')' },
			{ 'd', '0' }, { 'g', '6' }, { 't', '7' }, { 'g', '9' }, { 's', '5' }, { 'a', '4' } };

	private static final ThreadLocal<StringBuilder> sb = ThreadLocal.withInitial(StringBuilder::new); // make this
																										// regular if
																										// you don't
																										// need thread
																										// safety.

	/**
	 * Iterates over a String input and checks whether a cuss word was found in a
	 * list, then checks if the word should be ignored (e.g. bass contains the word
	 * *ss).
	 *
	 * @param input
	 * @return
	 */
	public static boolean badWordsFound(String input) {
		if (input == null) {
			return false;
		}

		StringBuilder sb = BadWords.sb.get();
		sb.setLength(0);

		removeLeetspeak: 
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (Character.isLetter(c)) {
				sb.append(Character.toLowerCase(c));
			} else {
				for (char[] conversion : convert) {
					if (c == conversion[1]) {
						sb.append(conversion[0]);
						continue removeLeetspeak;
					}
				}
			}
		}

		// iterate over each letter in the word
		for (int start = 0; start < sb.length(); start++) {
			// from each letter, keep going to find bad words until either the end of the
			// sentence is reached, or the max word length is reached.
			for (int offset = 1; offset < (sb.length() + 1 - start) && offset < largestWordLength; offset++) {
				long hash = LongHashFunction.xx().hashChars(sb, start, offset);
				if (words.containsKey(hash)) {
					// for example, if you want to say the word bass, that should be possible.
					String[] ignoreCheck = words.get(hash);
					boolean ignore = false;
					for (int s = 0; s < ignoreCheck.length; s++) {
						if (indexOf(sb, ignoreCheck[s]) >= 0) {
							ignore = true;
							break;
						}
					}
					if (!ignore) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private static int indexOf(CharSequence source, CharSequence target) {
		int sourceCount = source.length();
		int targetCount = target.length();
		int sourceOffset = 0;
		int targetOffset = 0;

		if (0 >= sourceCount) {
			return (targetCount == 0 ? sourceCount : -1);
		}
		if (targetCount == 0) {
			return 0;
		}

		char first = target.charAt(targetOffset);
		int max = sourceOffset + (sourceCount - targetCount);

		for (int i = sourceOffset; i <= max; i++) {
			/* Look for first character. */
			if (source.charAt(i) != first) {
				while (++i <= max && source.charAt(i) != first)
					;
			}

			/* Found first character, now look at the rest of v2 */
			if (i <= max) {
				int j = i + 1;
				int end = j + targetCount - 1;
				for (int k = targetOffset + 1; j < end && source.charAt(j) == target.charAt(k); j++, k++)
					;

				if (j == end) {
					/* Found whole string. */
					return i - sourceOffset;
				}
			}
		}
		return -1;
	}

}
