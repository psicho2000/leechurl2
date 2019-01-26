package de.psicho.LeechUrl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Leecher {

    private static final String DOWNLOAD_DESTINATION = "D:\\Eigenes\\Desktop\\downloads\\";
    private static final String DOWNLOAD_IMAGE_FILETYPE = ".jpg";
    private static final String IMAGE_PREFIX = "http://wallpaperswide.com/download/";
    private static final String IMAGE_SUFFIX = "-wallpaper-2560x1440.jpg";
    private static final String PATTERN = "<div class=\"thumb\">.*?<a href=\"/(.*?)-wallpapers\\.html";
    private static final String PAGER = "http://wallpaperswide.com/2560x1440-wallpapers-r/page/";
    private static final Integer PAGE_INDEX_START = 40;
    private static final Integer PAGE_INDEX_END = 4062;

    /**
     * <p>PredictionApi entry for leech</p>
     */
    public static void leech() {
        IntStream.range(PAGE_INDEX_START, PAGE_INDEX_END + 1).forEach(Leecher::extractImagesFromPage);
    }

    private static void extractImagesFromPage(Integer currentIndex) {
        Pattern pattern = Pattern.compile(PATTERN);
        String sourceUrl = PAGER + currentIndex;

        System.out.println(format("Leeching page %d...", currentIndex));

        try (Scanner scanner = new Scanner(new URL(sourceUrl).openStream(), UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            String htmlPage = scanner.hasNext() ? scanner.next() : "";
            String htmlPageOneLiner = htmlPage.replaceAll("\r", "").replaceAll("\n", "");
            Matcher matcher = pattern.matcher(htmlPageOneLiner);
            Set<String> foundLinks = new HashSet<>();
            while (matcher.find()) {
                foundLinks.add(matcher.group(1));
            }
            foundLinks.forEach(Leecher::download);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.out.println(format("Page %d successfully leeched.", currentIndex));
    }

    private static void download(String link) {
        try {
            URL downloadUrl = new URL(IMAGE_PREFIX + link + IMAGE_SUFFIX);
            try (ReadableByteChannel readableByteChannel = Channels.newChannel(downloadUrl.openStream());
                 FileOutputStream fileOutputStream = new FileOutputStream(DOWNLOAD_DESTINATION + link + DOWNLOAD_IMAGE_FILETYPE)) {
                fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
