package dynamic_alinment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class main4 {
	static int[][] memo;
	static byte[][] pairs;
	static byte[][] e_dist;
	static String allowed_punct = ",.!?;:،؟؛";

	// TODO decide allowed characters dictionary 
	public static void main(String[] str) throws IOException {
		initilize_char_dict();
		final int NUM_OF_WORDS = 2500; //49700; // 35626; //this is the length of kuzari.aj in words
		// 49667 the length of gniza kuzari in words.
		int start = 0;
		// KUZARI
		// need to replace in file <span class="hebTxt"> to </span>
		System.out.println("NOTE: need to replace in input file all the <span class=\"hebTxt\"> to </span>");
		System.out.println("print any key to continue...");
		System.in.read();
		String JA_path = "C:\\Users\\Owner\\copy2_ONE_BOOK_SELENIUM_without_hebrew.txt";
		String AR_path = "C:\\Users\\Owner\\JUDEO-ARABIC\\file_from_kfir\\דאטא ערוך שלי מאקסל\\WITHOUT_FOOTNOTES_copy_Kuzari-All-LAST-Corrected-Last.txt";
		boolean emunot = false;
		if (emunot) {
			JA_path = "C:\\Users\\Owner\\JUDEO-ARABIC\\emunot_deot_JA_gniza_webs_without_hebrew.txt"; // remove page
																										// num, remove
																										// header ,
																										// change hebrew
																										// tags
			AR_path = "C:\\Users\\Owner\\JUDEO-ARABIC\\emunot_deot_bashir.txt";
		}

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		long milisec = timestamp.getTime();

		String output = "C:\\Users\\Owner\\JUDEO-ARABIC\\file_from_kfir\\for_ctc_train22_FRIDBERG_" + milisec + ".txt";

		// String output_path =
		// "C:\\Users\\Owner\\JUDEO-ARABIC\\file_from_kfir\\for_ctc_train22_FRIDBERG5__.txt";
		// String output_path_pairs =
		// "C:\\Users\\Owner\\JUDEO-ARABIC\\file_from_kfir\\for_ctc_train22_pairs_FRIDBERG5.txt";
//		C:\Users\Owner\JUDEO-ARABIC\file_from_kfir\for_ctc_train22_pairs_FRIDBERG5.txt

		String t1 = read_data_from_file(start, NUM_OF_WORDS, JA_path);
		String t2 = read_data_from_file(start, NUM_OF_WORDS, AR_path);
		boolean debug = false;
		if (debug) {
			t1 = "סילת עמא ענדי מן";
			t2 = "سُئِلْتُ عنديَ من الاحتجاج";
		}
		t1 = t1.replaceAll("[" + allowed_punct + "]", " $0 ").replaceAll("  ", " "); // add space
		t2 = t2.replaceAll("[" + allowed_punct + "]", " $0 ").replaceAll("  ", " "); // TODO some more punctuations??

		// mark code switches in JA
		String[] code_switch = t1.split("</span>");
		List<String> text1_aslist = new ArrayList<String>();

		boolean ja = true;
		for (String code : code_switch) {
			String[] words = code.trim().split("\\s+");
			for (String w : words) {
				if (!ja) {
					w = "H"+w; // w="H"
				}
				text1_aslist.add(w);
			}
			ja = !ja;
		}
		String[] text1 = text1_aslist.toArray(new String[0]);

		String[] text2 = t2.trim().split("\\s+");
		System.out.println("text1 len:" + text1.length + "\ntext2 len:" + text2.length);

		memo = new int[2][text2.length + 1]; // acctually don't need just last line!!!or two
		pairs = new byte[text1.length + 1][text2.length + 1];
		e_dist = new byte[text1.length + 1][text2.length + 1]; // TODO maybe don't need this matrix
		long startTime = System.nanoTime();

		// #######################################
		System.out.println("aligning...");
		align(text1, text2);
		// #######################################
		long endTime = System.nanoTime();

		long duration = (endTime - startTime); // divide by 1000000 to get milliseconds.
		System.out.println("duration: " + duration / 1000000);

		int h = pairs.length - 1;
		int w = pairs[0].length - 1;
		List<String> alinged_pairs = gen_pairs(text1, h, text2, w);
		write_aligned_pairs(alinged_pairs, output, 20);
		System.out.println("duration: " + duration / 1000000);
		System.out.println("text1 len:" + text1.length + "\ntext2 len:" + text2.length);
		System.out.println("t1 len:" + t1.length() + "\nt2 len:" + t2.length());
		System.out.println("\nSome instructions: " + allowed_punct
				+ " replace arab question mark and semicolon and arab comma to an English one. (by notepad find and replace)");
		System.out.println("delete trailing punctuations in the file, if there are");
		System.out.println(output);
	}

	// TODO print with hafifa to augment data shalom kita a -->shalom kita, kita a,
	private static void write_aligned_pairs(List<String> alinged_pairs, String output_path_pairs, int LINE_LIM)
			throws IOException {
		// TODO last argument is redundent

		// FileWriter fw = new FileWriter(output_path);
		// FileWriter fw_pairs = new FileWriter(output_path_pairs);
		FileWriter fw_pairs_clean = new FileWriter(output_path_pairs + "clean.txt");
		//int accum = 0;
		//String heb = "";
		//String ar = "";
		for (String pair : alinged_pairs) {
			System.out.println(pair);
			// fw_pairs.write(pair+'\n');
//			if (accum > LINE_LIM) {
//				fw.write(heb.trim() + '\t' + ar.trim() + '\n');
//				heb = "";
//				ar = "";
//				accum = 0;
//			}
			//String pattern = "<(.*?),(.*?)>(\\d+?)"; // TODO change to tab seperated triplet
			//Pattern r = Pattern.compile(pattern);

			// Now create matcher object.
		//	Matcher m = r.matcher(pair);
			String[] splited=pair.split("\t");
//			if (!m.find()) {
//				System.err.println("regex not matched on pair" + pattern);
//			}		
			String h =splited[0];// m.group(1);
			String a =splited[1];// m.group(2);
			int d = Integer.parseInt(splited[2]);//m.group(3));

			if (h.length() != 0 && h.charAt(0) == 'H' || contain_nikud(h) || contains_hebrew(a)) {
				// fw_pairs.write("NOT skiping: " + pair+'\n');
				fw_pairs_clean.write(h+"\tH"+a+"\t" + d + '\n');
				//fw_pairs_clean.write("H\tH\t" + d + '\n');
				//heb += " " + h;
				//ar += " H"+a;
			//	accum += 2; // accum +=h.length() + 1
			} else if (h.length() == 0 && a.length() == 1 && allowed_punct.contains(a)) {
				// fw_pairs.write("NOT skiping: " + pair+'\n');
				fw_pairs_clean.write(a + '\t' + a + '\t' + d + '\n');
				//heb += " " + a; // heb +=" " + a;
				//ar += " " + a; // ar +=" " + a;
			//	accum += 2;
			} else if (a.length() == 0 && h.length() == 1 && allowed_punct.contains(h)) {
				// fw_pairs.write("NOT skiping: " + pair+'\n');
				fw_pairs_clean.write(h + '\t' + h + '\t' + d + '\n');
				//heb += " " + h; // heb += " H";
				//ar += " " + h; // ar += " H"; //or maybe no space?
			//	accum += 2; // accum += 2;
			} else if (h.length() == 0 || a.length() == 0) {
				// System.out.println("skiping: " + pair);
				if (h.length() == 0) {
					fw_pairs_clean.write("\tS\t" + d + '\n'); //SSS is for SKIP
				} else {
					fw_pairs_clean.write("S\t\t" + d + '\n');
				}
				//fw_pairs_clean.write(h + '\t' + a + '\t' + d + '\n');
				// fw_pairs.write("skiping: " + pair+'\n');
//				System.out.println(" (h.length()/2.0):\n"+h.length()/2.0);
//				System.out.println("e_dist:\n"+m.group(3));
//				System.out.println("h:"+h);
			}else if(d > Math.max(a.length(), h.length()) / 2.0){
				fw_pairs_clean.write("S\tS\t" + d + '\n');
			} else if (allowed_punct.contains(h) && allowed_punct.contains(a)) {
//				if (h.equals(a)) { //DON'T NEED THIS CONDITION BECAUSE SAME ACTION
//					heb += " " + h;
//					ar += " " + h;
//					accum += h.length() + 1;
//				}else {
				fw_pairs_clean.write(h + '\t' + h + '\t' + d + '\n');
				//heb += " " + h;
				//ar += " " + h;
			//	accum += h.length() + 1;
//				}
			} else { // add regularly the word pair:
				fw_pairs_clean.write(h + '\t' + a + '\t' + d + '\n');
				//heb += " " + h;
				//ar += " " + a;
			//	accum += h.length() + 1;
			}
		}
		//fw.close();
		//fw_pairs.close();
		fw_pairs_clean.close();
	}

	private static boolean contains_hebrew(String a) {
		// TODO Auto-generated method stub
		return false;
	}

	private static boolean contain_nikud(String str) {
		// return str.matches(".*[^א-ת'\"\\.,?!;:ֿ].*"); //I added ֿ //TODO just create
		// a black list containing all nikud sign hebrew instead
		// return str.matches(".*[\\u0590-\\u05CF].*"); //this is all the hebrew NIKUD
		// range from : https://en.wikipedia.org/wiki/Hebrew_(Unicode_block)
		return str.matches(".*[\\u0590-\\u05BE \\u05C0\\u05CF].*");
	}

	@SuppressWarnings("unused")
	private static String double_all_letters(String h) {
		String res = "";
		for (int i = 0; i < h.length(); i++) {
			char c = h.charAt(i);
			String c_as_str = Character.toString(c);
			res += c_as_str + c_as_str;
		}
		return res;
	}

	private static String read_data_from_file(int start, final int NUM_OF_WORDS, String path)
			throws UnsupportedEncodingException, FileNotFoundException, IOException {
		String t1 = "";
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF-8"));
		int num_of_words = 0;
		while (num_of_words < start) {
			String read = in.readLine();
			num_of_words += read.split("\\s+").length;
		}
		while (num_of_words < NUM_OF_WORDS + start) {
			String read = in.readLine();
			if (read == null)
				break;
			t1 += "\n" + read;
			num_of_words += read.split("\\s+").length;
		}
		in.close();
		return t1;
	}

	private static void initilize_char_dict() {
		char_dict = new HashMap<String, String>();
		// char_dict.put(" "," ");
		char_dict.put("ش", "ש");
		char_dict.put("ذ", "ד'");
		// char_dict.put(",", ",");
		// char_dict.put(".", ".");
		char_dict.put("ا", "א");
		char_dict.put("ض", "צ'");
		char_dict.put("ع", "ע");
		char_dict.put("ح", "ח");
		char_dict.put("ص", "צ");
		char_dict.put("ط", "ט");
		char_dict.put("ظ", "ט'");
		char_dict.put("ب", "ב");
		char_dict.put("د", "ד");
		char_dict.put("ف", "פ");
		char_dict.put("غ", "ג");
		char_dict.put("ج", "ג'");
		char_dict.put("ه", "ה");
		char_dict.put("ك", "כ");
		char_dict.put("ل", "ל");
		char_dict.put("م", "מ");
		char_dict.put("ن", "נ");
		// char_dict.put("ة", "ה'"); //TODO הֿ
		char_dict.put("ة", "הֿ");
		char_dict.put("ق", "ק");
		char_dict.put("ر", "ר");
		char_dict.put("س", "ס");
		char_dict.put("ت", "ת");
		char_dict.put("ث", "ת'");
		char_dict.put("و", "ו");
		char_dict.put("خ", "כ'");
		char_dict.put("ي", "י");
		char_dict.put("ز", "ז");
		char_dict.put(" ", " ");		
		char_dict.put("أ", "א");
		char_dict.put("إ", "א");
		char_dict.put("ى", "י");
		char_dict.put("آ", "א");
		char_dict.put("ؤ", "ו");
		char_dict.put("ئ", "י");

		char_dict.put("1", "1");
		char_dict.put("2", "2");
		char_dict.put("3", "3");
		char_dict.put("4", "4");
		char_dict.put("5", "5");
		char_dict.put("6", "6");
		char_dict.put("7", "7");
		char_dict.put("8", "8");
		char_dict.put("9", "9");
		char_dict.put("0", "0");
		char_dict.put("[", "[");
		char_dict.put("]", "]");
		// char_dict.put("","");

	}

	private static List<String> gen_pairs(String[] text1, int h, String[] text2, int w) {
		List<String> my_res = new ArrayList<String>();
		while (h >= 0 && w >= 0) {
			short cont = pairs[h][w];
			short ed = 0;
			String w1 = null;
			String w2 = null;
			switch (cont) {
			case (0):// --
				ed = e_dist[h][w];
				w1 = text1[--h];
				w2 = text2[--w];
				break;
			case (1):// -=
				ed = e_dist[h][w];// ==length(w1)?
				w1 = text1[--h];
				w2 = "";
				assert (ed == w1.length());
				break;
			case (2):// =-
				ed = e_dist[h][w];// ==length(w2)?
				w1 = "";
				w2 = text2[--w];
				assert (ed == w2.length());
				break;
			case (-1):
				return my_res;
			}
			String first_pair = w1 + "\t" + w2 + "\t" + ed; 
			my_res.add(0, first_pair);

		}
		return null;// should not reach here
	}

	private static void align(String[] text1, String[] text2) {
		int switch_mem_raw = 0;
		int len1 = text1.length;
		int len2 = text2.length;

		// initialize cell [0,0]
		pairs[0][0] = -1;
		memo[0][0] = 0;
		e_dist[0][0] = 0;

		// initialize first raw
		// String res = "";
		int mem = 0;
		for (int j = 1; j <= len2; j++) {
			// res = " <," + text2[j - 1] + ">" + text2[j - 1].length() + res;
			// mem += text2[j - 1].length();
			mem += edit_dist("", text2[j - 1]);
			pairs[0][j] = 2;
			memo[0][j] = mem;
			// e_dist[0][j] = (byte) text2[j - 1].length();
			e_dist[0][j] = (byte) edit_dist("", text2[j - 1]);

		}
		switch_mem_raw = my_switch(switch_mem_raw);

		for (int i = 1; i <= len1; i++) {
			System.out.println(i + '\r');
			pairs[i][0] = 1;
			memo[switch_mem_raw][0] = text1[i - 1].length() + memo[my_switch(switch_mem_raw)][0];
			e_dist[i][0] = (byte) text1[i - 1].length();
			for (int j = 1; j <= len2; j++) {
				// System.out.println(i+" "+j);
				int e_dist1 = edit_dist(text1[i - 1], text2[j - 1]);
				int short_short = memo[my_switch(switch_mem_raw)][j - 1] + e_dist1;

				int short_same = memo[my_switch(switch_mem_raw)][j] + text1[i - 1].length();
				// int same_short = memo[switch_mem_raw][j - 1] + text2[j - 1].length();
				int same_short = memo[switch_mem_raw][j - 1] + edit_dist("", text2[j - 1]); 
				if (short_short <= Math.min(short_same, same_short)) {
					memo[switch_mem_raw][j] = short_short;
					pairs[i][j] = 0;// " <" + text1[i-1] + "," + text2[j-1] + ">" + e_dist+" --";
					e_dist[i][j] = (byte) e_dist1;

				} else if (short_same < same_short) {
					memo[switch_mem_raw][j] = short_same;
					pairs[i][j] = 1;// " <" + text1[i-1] + ",>" + text1[i-1].length() +" -=";
					e_dist[i][j] = (byte) text1[i - 1].length();

				} else {
					memo[switch_mem_raw][j] = same_short;
					pairs[i][j] = 2;// " <,"+text2[j-1]+">" + text2[j-1].length() +" =-";
					// e_dist[i][j] = (byte) text2[j - 1].length();
					e_dist[i][j] = (byte) edit_dist("", text2[j - 1]);

				}
			}
			switch_mem_raw = my_switch(switch_mem_raw);
		}
	}

	private static int my_switch(int switch_mem_raw) {
		if (switch_mem_raw == 0)
			return 1;
		if (switch_mem_raw == 1)
			return 0;
		return -1; // error
	}

	// private static Map<String, Integer> dists;
	private static Map<String, String> char_dict;

	// ref: https://www.programcreek.com/2013/12/edit-distance-in-java/
	public static int edit_dist(String heb_word, String arab_word) {
		arab_word = translate_to_heb(arab_word);
		heb_word = convert_sofiot(heb_word);
		heb_word = heb_word.replaceAll("[^'\"א-ת 1234567890\\[\\]]", "");

		int len1 = heb_word.length();
		int len2 = arab_word.length();

		// len1+1, len2+1, because finally return dp[len1][len2]
		int[][] dp = new int[len1 + 1][len2 + 1];

		for (int i = 0; i <= len1; i++) {
			dp[i][0] = i;
		}

		for (int j = 0; j <= len2; j++) {
			dp[0][j] = j;
		}

		// iterate though, and check last char
		for (int i = 0; i < len1; i++) {
			char c1 = heb_word.charAt(i);
			for (int j = 0; j < len2; j++) {
				char c2 = arab_word.charAt(j);

				// if last two chars equal
				if (c1 == c2) {

					dp[i + 1][j + 1] = dp[i][j];
				} else {
					int replace = dp[i][j] + 1;
					int insert = dp[i][j + 1] + 1;
					int delete = dp[i + 1][j] + 1;

					int min = replace > insert ? insert : replace;
					min = delete > min ? min : delete;
					dp[i + 1][j + 1] = min;
				}
			}
		}
		return dp[len1][len2];
	}

	private static String convert_sofiot(String heb_word) {
		return heb_word.replace('ם', 'מ').replace('ן', 'נ').replace('ץ', 'צ').replace('ף', 'פ').replace('ך', 'כ');
	}

	private static String translate_to_heb(String arr_word2) {
		String res = "";
		for (char c : arr_word2.toCharArray()) {
			String heb_let = char_dict.get("" + c);// TODO handle this horrible syntax (change dict keys to type
													// Character)
			if (heb_let != null)
				res += heb_let;
		}
		return res;
	}
}
