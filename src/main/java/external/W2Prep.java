package external;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

/**
 * Not actually an external library but I want a text cleaner for word2vec and
 * I'm being lazy and just putting it in an existing project
 * 
 */
public class W2Prep {
	public static HashSet<String> uniqueWords = new HashSet<>();
	public static int wordCount = 0;
	
	public static void main(String[] args) throws Exception {
		Set<String> dict = readDictionary(new BufferedReader(new FileReader(new File(args[2]))));
		PrintWriter out = new PrintWriter(new FileWriter(new File(args[1])));
		Set<String> missing = new HashSet<>();
		File in = new File(args[0]);
		if (in.isDirectory()) {
			for (File f : in.listFiles()) {
				missing.addAll(convert(new FileReader(f), out, dict));
			}
		} else {
			missing.addAll(convert(new FileReader(in), out, dict));
		}
		out.println();
		out.println();
		out.flush();
		out.close();
		PrintWriter missingOut = new PrintWriter(new FileWriter(new File(args[3])));
		missing.forEach(missingOut::println);
		missingOut.flush();
		missingOut.close();
		System.out.println("Word count: "+wordCount+" Unique words: "+uniqueWords.size()+" Missing: "+missing.size());
		//System.out.println(((int)'\''));
	}
	
	public static Set<String> readDictionary(BufferedReader in) throws Exception {
		HashSet<String> dict = new HashSet<>();
		String s = in.readLine();
		while(s != null) {
			dict.add(s);
			s = in.readLine();
		}
		return dict;
	}

	public static Set<String> convert(InputStreamReader in, PrintWriter out, Set<String> dict) throws Exception {
		char[] buf = new char[100000];
		int read = in.read(buf);
		Set<String> missing = new HashSet<>();
		char lastOut = ' ';
		StringBuilder sb = new StringBuilder();
		while (read > -1) {
			for (int i = 0; i < read; i++) {
				char c = buf[i];
				String outCr = String.valueOf(c);
				if(c == '-')  {
					outCr = "bbbbbbbbbb";
				}else if(c == '\'')  {
					outCr = "dddddddddd";
				}else if(c > 300) {
					outCr = " ";
				}


				if(buf[i] == 'â' || buf[i] =='œ') {
					continue;
				}
				if(Character.isAlphabetic(outCr.charAt(0))) {
					out.print(outCr.toLowerCase());
					lastOut = c;
					sb.append(outCr);
				}
				else if(c == 10) {
					out.println();
					lastOut = ' ';
				} else if(lastOut != ' ') {
					wordCount++;
					out.print(' ');
					lastOut = ' ';
					String word = sb.toString().toLowerCase();
					if(!dict.contains(word)) {
						missing.add(word);
					}
					uniqueWords.add(word);
					sb = new StringBuilder();
				}
				

			}
			read = in.read(buf);
		}
		return missing;
	}

}