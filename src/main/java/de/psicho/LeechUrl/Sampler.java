package de.psicho.LeechUrl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Sampler {

    private static final int SAMPLE_SIZE = 300;
    private static final int REFERENCE_SIZE = 31099;
    private static final String REFERENCE_FILE = "D:\\Eigenes\\Desktop\\sort-pics\\LeechUrl2.prediction.csv";
    private static final String SAMPLE_FILE = "D:\\Eigenes\\Desktop\\sort-pics\\LeechUrl2.sample.csv";
    private static final String PIC_SOURCE_PATH = "D:\\Eigenes\\Desktop\\downloads\\";
    private static final String PIC_TARGET_PATH = "D:\\Eigenes\\Desktop\\sort-pics\\samples\\";

    public static void sample() {
        try (Reader reader = Files.newBufferedReader(Paths.get(REFERENCE_FILE)); CSVReader csvReader = new CSVReader(reader);
            CSVWriter writer = new CSVWriter(new FileWriter(SAMPLE_FILE), ',');) {
            int lastLineNumber = 0;
            List<Integer> randoms = randomize();
            for (Integer lineNumber : randoms) {
                csvReader.skip(lineNumber - lastLineNumber);
                String[] nextRecord = csvReader.readNext();
                if (nextRecord.length == 3) {
                    File srcFile = new File(PIC_SOURCE_PATH + nextRecord[0]);
                    File destFile = new File(PIC_TARGET_PATH + nextRecord[0]);
                    FileUtils.copyFile(srcFile, destFile);
                    writer.writeNext(nextRecord);
                } else {
                    log.info("Line {} with pic {} has {} values.", lineNumber, nextRecord[0], nextRecord.length);
                }
                lastLineNumber = lineNumber;
            }
        } catch (IOException ex) {
            log.info(ExceptionUtils.getStackTrace(ex));
        }
    }

    private static List<Integer> randomize() {
        // @formatter:off
        return IntStream.range(0, SAMPLE_SIZE)
                        .mapToObj(i -> ThreadLocalRandom.current().nextInt(0, REFERENCE_SIZE + 1))
                        .sorted(Integer::compareTo)
                        .collect(Collectors.toList());
        // @formatter:on
    }
}
