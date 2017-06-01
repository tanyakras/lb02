package P02;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    public static void main(String[] args) throws URISyntaxException, IOException, MimeTypeParseException {
        HashSet<URI> hs = new HashSet<>(100);
        URI link = new URI("https://www.mirea.ru/");
        download(link,hs);
    }

    private static void download(URI link,HashSet<URI> hs) throws IOException, MimeTypeParseException {
        if (hs.contains(link)||hs.size()>100) {
            return;
        }
        hs.add(link);
        System.out.println(link);
        URL url = link.toURL();
        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            String ct = connection.getHeaderField("Content-Type");
            MimeType mt = new MimeType(ct);
            String cs = mt.getParameter("charset");
            Pattern p = Pattern.compile("href\\s*=\\s*([^ >]+|\"[^\"]*\"|'[^']*')");
            try (InputStream is = connection.getInputStream()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                while (true) {
                    int c = is.read();
                    if (c < 0) break;
                    bos.write(c);
                }
                String input = bos.toString();
                Matcher m = p.matcher(input);
                while (m.find()) {
                    String href = m.group(1);
                    if (href.startsWith("\"") || href.startsWith("\'")) {
                        href = href.substring(1, href.length() - 1);
                    }
                    URI child = link.resolve(href.trim());
                    download(child, hs);
                }
            }

            connection.disconnect();
        } catch (IOException e) {
            System.out.println("ERROR: " +e);
        }
    }
}

