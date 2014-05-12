package uk.digitalsquid.netspoofer.spoofs;

import java.util.HashMap;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import uk.digitalsquid.netspoofer.R;
import android.content.Context;

public class TitleChange extends HtmlEditorSpoof {
	
	private static final long serialVersionUID = 7266206237358568955L;
	public static final int MODE_FLIP = 1;
	public static final int MODE_REVERSE = 2;

	private static String getTitle(Context context, int mode) {
		switch(mode) {
		case MODE_FLIP:
			return context.getResources().getString(R.string.spoof_title_flip);
		case MODE_REVERSE:
			return context.getResources().getString(R.string.spoof_title_reverse);
		default:
			return "Unknown image spoof";
		}
	}
	private static String getDescription(Context context, int mode) {
		switch(mode) {
		case MODE_FLIP:
			return context.getResources().getString(R.string.spoof_title_flip_description);
		case MODE_REVERSE:
			return context.getResources().getString(R.string.spoof_title_reverse_description);
		default:
			return "";
		}
	}

	private final int mode;

	public TitleChange(Context context, int mode) {
		super(getTitle(context, mode), getDescription(context, mode));
		this.mode = mode;
	}

	@Override
	protected void modifyDocument(Document document, Element body) {
		Elements titles = document.select("title");
		switch(mode) {
		case MODE_FLIP:
			if(titles.size() > 0) {
				Element title = titles.first();
				String reversed =
						new StringBuilder(title.text()).reverse().toString();
				StringBuilder result = new StringBuilder(reversed.length());
				for(char c : reversed.toCharArray()) {
					Character ud = upsideDown.get(c);
					if(ud == null) ud = c;
					result.append(ud);
				}
				title.text(result.toString());
			}
			break;
		case MODE_REVERSE:
			if(titles.size() > 0) {
				Element title = titles.first();
				title.text(new StringBuilder(title.text()).reverse().toString());
			}
			break;
		}
	}

    static final HashMap<Character, Character> upsideDown =
        new HashMap<Character, Character>() {
			private static final long serialVersionUID = -9085470439710161129L;
			{
                put('z','\u007A');
                put('y','\u028E');
                put('x','\u0078');
                put('w','\u028D');
                put('v','\u028C');
                put('u','\u006E');
                put('t','\u0287');
                put('s','\u0073');
                put('r','\u0279');
                put('q','\u0062');
                put('p','\u0064');
                put('o','\u006F');
                put('n','\u0075');
                put('m','\u026F');
                put('l','\u006C');
                put('k','\u029E');
                put('j','\u027E');
                put('i','\u0131');
                put('g','\u0265');
                put('g','\u0253');
                put('f','\u025F');
                put('e','\u01DD');
                put('d','\u0070');
                put('c','\u0254');
                put('b','\u0071');
                put('a','\u0250');
                put('Z','\u005A');
                put('Y','\u2144');
                put('X','\u0058');
                put('W','\u004D');
                put('V','\u039B');
                put('U','\u2229');
                put('T','\u22A5');
                put('S','\u0053');
                put('R','\u1D1A');
                put('Q','\u038C');
                put('P','\u0500');
                put('O','\u004F');
                put('N','\u004E');
                put('M','\u0057');
                put('L','\u2142');
                put('K','\u22CA');
                put('J','\u017F');
                put('I','\u0049');
                put('G','\u0048');
                put('G','\u2141');
                put('F','\u2132');
                put('E','\u018E');
                put('D','\u15E1');
                put('C','\u0186');
                put('B','\u1041');
                put('A','\u2200');
                put('0','\u0030');
                put('9','\u0036');
                put('8','\u0038');
                put('7','\u3125');
                put('6','\u0039');
                put('5','\u078E');
                put('4','\u3123');
                put('3','\u218B');
                put('2','\u218A');
                put('1','\u21C2');
                put('&','\u214B');
                put('_','\u203E');
                put('?','\u00BF');
                put('!','\u00A1');
                put('"','\u201E');
                put('\'','\u002C');
                put('.','\u02D9');
                put(';','\u061B');
            }
        };
}
