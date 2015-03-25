package uk.digitalsquid.netspoofer.report;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.StringTokenizer;

import uk.digitalsquid.netspoofer.R;

/**
 * Generates random phrases, such as "Correct Horse Battery Staple"
 */
public class CHBSGenerator {
    private final ArrayList<String> words = new ArrayList<String>();

    private final Random random = new Random();

    public CHBSGenerator(Context context) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                context.getResources().openRawResource(R.raw.wordlist)));
        StringTokenizer tkz = new StringTokenizer(reader.readLine(), ",");
        while (tkz.hasMoreTokens()) {
            words.add(tkz.nextToken());
        }
    }

    public String generate() {
        return String.format(
                Locale.ENGLISH, "%s-%s-%s-%s",
                words.get(random.nextInt(words.size())),
                words.get(random.nextInt(words.size())),
                words.get(random.nextInt(words.size())),
                words.get(random.nextInt(words.size()))
                );
    }
}
