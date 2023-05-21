import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.lang.Math;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
		clearScreen();
		drawCoverArt();
		System.out.print("\n\033[4mEnter anything to begin:\033[24m ");
		var scan = new Scanner(System.in);
		scan.nextLine();
		clearScreen();
		System.out.print("\033[3mIndexing documents, please wait...\033[23m");
        var dir = new File("java");
        var corpus = new ArrayList<TfidfDocument>();
        walkDirectory(dir, corpus);
		mainLoop(corpus);
		clearScreen();
		drawCoverArt();
		System.out.println("\n\033[3mSee you next time!\033[23m");
    }

	private static void mainLoop(List<TfidfDocument> corpus) {
		final var ZERO = 0.0000000000;
		final var ENTRIES_PER_PAGE = 10;
		
		while (true) {
			clearScreen();
			System.out.print("\033[4mSearch:\033[24m ");
			var scan = new Scanner(System.in);
			var qry = scan.nextLine();
			search(qry, corpus);
			
			var docsWithScore = corpus
				.stream()
				.filter(doc -> doc.getScore() > ZERO)
				.collect(Collectors.toList());

			var pages = new LinkedList<ArrayList<TfidfDocument>>();
			
			// create pages
			for (var i = 0; i < docsWithScore.size(); ++i)
				if (i % ENTRIES_PER_PAGE == 0)
					pages.add(new ArrayList<TfidfDocument>(ENTRIES_PER_PAGE));

			for (var i = 0; i < pages.size(); ++i)
				for (var j = 0; j < ENTRIES_PER_PAGE; ++j) {
					var idx = i * ENTRIES_PER_PAGE + j;

					if (idx < docsWithScore.size())
						pages.get(i).add(docsWithScore.get(idx));
					else
						break;
				}

			var curPageIdx = 0;
			var input = "";

			while (!input.equals("YES") && !input.equals("NO")) {
				clearScreen();
				System.out.println("\033[4mSearch:\033[24m " + qry);
				var hasPrev = false;
				var hasNext = false;

				if (pages.size() > 0) {
					System.out.println("\nResults for page \033[36m" + (curPageIdx + 1) + "\033[39m of \033[36m" + pages.size() + "\033[39m:");
					drawTable(pages.get(curPageIdx));
				} else
					System.out.println("\nNo results found!\n");

				if (curPageIdx - 1 >= 0) {
					hasPrev = true;
					System.out.println("Go to previous page? <\033[36mPREV\033[39m>");	
				}
				if (curPageIdx + 1 < pages.size()) {
					hasNext = true;
					System.out.println("Go to next page? <\033[36mNEXT\033[39m>");
				}
				System.out.println("Search again? <\033[36mYES\033[39m/\033[36mNO\033[39m>");
				System.out.print("\n\033[4mInput:\033[24m ");
				input = scan.nextLine().toUpperCase();

				if (hasPrev && input.equals("PREV"))
					--curPageIdx;
				else if (hasNext && input.equals("NEXT"))
					++curPageIdx;
			}
				
			if(!input.equals("YES"))
				break;
			else
				for (var i = 0; i < corpus.size(); ++i)
					corpus.get(i).resetScore();
		}
	} 

	private static void drawCoverArt() {
		System.out.print("\033[35m");
		System.out.println(" /$$$$$$$$ /$$                                               /$$               ");
		System.out.println("|__  $$__/| $$                                              | $$               ");
		System.out.println("   | $$   | $$$$$$$   /$$$$$$   /$$$$$$   /$$$$$$   /$$$$$$ | $$  /$$$$$$      ");
		System.out.println("   | $$   | $$__  $$ /$$__  $$ /$$__  $$ /$$__  $$ /$$__  $$| $$ /$$__  $$     ");
		System.out.println("   | $$   | $$  \\ $$| $$$$$$$$| $$  \\ $$| $$  \\ $$| $$  \\ $$| $$| $$$$$$$$ ");
		System.out.println("   | $$   | $$  | $$| $$_____/| $$  | $$| $$  | $$| $$  | $$| $$| $$_____/     ");
		System.out.println("   | $$   | $$  | $$|  $$$$$$$|  $$$$$$/|  $$$$$$/|  $$$$$$$| $$|  $$$$$$$     ");
		System.out.println("   |__/   |__/  |__/ \\_______/ \\______/  \\______/  \\____  $$|__/ \\_______/");
		System.out.println("                                                   /$$  \\ $$                  ");
		System.out.println("                                                  |  $$$$$$/                   ");
		System.out.println("                                                   \\______/                   ");
		System.out.print("\033[39m");
	}

	private static void drawTable(List<TfidfDocument> docsWithScore) {
		final var HEADERS = new String[] { "Class/File Path", "Document URL", "TF-IDF Score"};
		final var INITIAL_COLUMN_WIDTHS = new int[] { HEADERS[0].length(), HEADERS[1].length(), HEADERS[2].length() };
		final var COLORS = new String[] { "\033[31m", "\033[33m", "\033[32m", "\033[34m" };
		var maxColWidths = new int[] { INITIAL_COLUMN_WIDTHS[0], INITIAL_COLUMN_WIDTHS[1], INITIAL_COLUMN_WIDTHS[2] };

		for (var doc : docsWithScore) {
			if (doc.getClassPath().length() > maxColWidths[0]) maxColWidths[0] = doc.getClassPath().length();
			if (doc.getUrl().length() > maxColWidths[1]) maxColWidths[1] = doc.getUrl().length();	
		}
		System.out.print("+");		
		
		for (var i = 0; i < maxColWidths[0] + 2; ++i) System.out.print("-");

		System.out.print("+");

		for (var i = 0; i < maxColWidths[1] + 2; ++i) System.out.print("-");

		System.out.print("+");

		for (var i = 0; i < maxColWidths[2] + 2; ++i) System.out.print("-");

		System.out.println("+");
		System.out.print("| \033[43m" + HEADERS[0] + "\033[49m ");
		
		for (var i = 0; i < maxColWidths[0] - INITIAL_COLUMN_WIDTHS[0]; ++i) System.out.print(" ");

		System.out.print("| \033[43m" + HEADERS[1] + "\033[49m ");
		
		for (var i = 0; i < maxColWidths[1] - INITIAL_COLUMN_WIDTHS[1]; ++i) System.out.print(" ");

		System.out.println("| \033[43m" + HEADERS[2] + "\033[49m |");
		System.out.print("+");		
		
		for (var i = 0; i < maxColWidths[0] + 2; ++i) System.out.print("-");

		System.out.print("+");

		for (var i = 0; i < maxColWidths[1] + 2; ++i) System.out.print("-");

		System.out.print("+");

		for (var i = 0; i < maxColWidths[2] + 2; ++i) System.out.print("-");

		System.out.println("+");

		for (var i = 0; i < docsWithScore.size(); ++i) {
			var textColor = COLORS[i % COLORS.length];
			var rightPadding = maxColWidths[0] - docsWithScore.get(i).getClassPath().length();
			System.out.print("| " + textColor + docsWithScore.get(i).getClassPath());
				
			for (var j = 0; j < rightPadding; ++j) System.out.print(" ");
	
			rightPadding = maxColWidths[1] - docsWithScore.get(i).getUrl().length();
			System.out.print("\033[39m | " + textColor + docsWithScore.get(i).getUrl());
	
			for (var j = 0; j < rightPadding; ++j) System.out.print(" ");
					
			System.out.println("\033[39m | " + textColor + String.format("%.10f", docsWithScore.get(i).getScore()) + "\033[39m |");
		}
		System.out.print("+");		
		
		for (var i = 0; i < maxColWidths[0] + 2; ++i) System.out.print("-");
	
		System.out.print("+");
	
		for (var i = 0; i < maxColWidths[1] + 2; ++i) System.out.print("-");
	
		System.out.print("+");
	
		for (var i = 0; i < maxColWidths[2] + 2; ++i) System.out.print("-");
	
		System.out.println("+");
		System.out.println();
	}

	private static void clearScreen() {
		System.out.print("\033\143");
		System.out.println("\033[1m========== Theoogle - Java Docs Search Engine ==========\033[22m\n");
	}

    private static void walkDirectory(File dir, ArrayList<TfidfDocument> corpus) {
        if (dir.isDirectory())
            for (var entry : dir.listFiles())
                walkDirectory(entry, corpus);
        else
            corpus.add(new TfidfDocument(dir));
    }

	private static double tf(String term, TfidfDocument doc) {
		var termFreq = doc.termCounts.containsKey(term.toLowerCase()) ? doc.termCounts.get(term.toLowerCase()) : 0;
		
		return (double) termFreq / doc.getLength();
	}

	private static double idf(String term, List<TfidfDocument> corpus) {
		var docsWithTerm = 0;
		
		for (var doc : corpus)
			if (doc.termCounts.containsKey(term.toLowerCase()))
				++docsWithTerm;
		
		if (docsWithTerm == 0)
			docsWithTerm = 1;
		
		return Math.log10((double) corpus.size() / docsWithTerm);
	}

	private static double tfidf(String term, TfidfDocument doc, List<TfidfDocument> corpus) {
		return tf(term, doc) * idf(term, corpus);
	}

	private static void search(String qry, List<TfidfDocument> corpus) {
		var terms = qry.split("\\s+");

		for (var term : terms)
			if (!TfidfDocument.STOP_WORDS.contains(term.toLowerCase()))
				for (var i = 0; i < corpus.size(); ++i)
					corpus.get(i).updateScore(tfidf(term.toLowerCase(), corpus.get(i), corpus));

		corpus.sort((doc1, doc2) -> ((Double) doc2.getScore()).compareTo(doc1.getScore()));
	}
}