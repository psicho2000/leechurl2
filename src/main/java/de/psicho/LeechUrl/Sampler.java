package de.psicho.LeechUrl;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.opencsv.CSVReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Sampler {

    private static final int SAMPLE_SIZE = 300;
    private static final int REFERENCE_SIZE = 31099;
    private static final String REFERENCE_FILE = "D:\\Eigenes\\Desktop\\sort-pics\\LeechUrl2.prediction.csv";
    private static final String SAMPLE_FILE = "D:\\Eigenes\\Desktop\\sort-pics\\LeechUrl2.sample.csv";
    private static final String PIC_SOURCE_PATH = "D:\\Eigenes\\Desktop\\downloads";
    private static final String PIC_TARGET_PATH = "D:\\Eigenes\\Desktop\\sort-pics\\samples";

    public static void sample() {
        List<Integer> randoms = randomize(REFERENCE_SIZE, SAMPLE_SIZE);
        randoms.sort(Integer::compareTo);
        try (Reader reader = Files.newBufferedReader(Paths.get(REFERENCE_FILE)); CSVReader csvReader = new CSVReader(reader);) {
            int lastLineNumber = 0;
            for (Integer lineNumber : randoms) {
                csvReader.skip(lineNumber - lastLineNumber);
                String[] nextRecord = csvReader.readNext();
                if (nextRecord.length == 3) {
                    // copy nextRecord[0] from PIC_SOURCE_PATH to PIC_TARGET_PATH
                    // write nextRecord to SAMPLE_FILE
                } else {
                    log.info("Line {} with pic {} has {} values.", lineNumber, nextRecord[0], nextRecord.length);
                }
                lastLineNumber = lineNumber;
            }
        } catch (IOException ex) {
            log.info(ExceptionUtils.getStackTrace(ex));
        }
    }

    private static List<Integer> randomize(int maxNumber, int resultSize) {
        List<Integer> result = newArrayList();
        IntStream.range(0, resultSize).forEach(i -> result.add(ThreadLocalRandom.current().nextInt(0, maxNumber + 1)));
        return result;
    }
}
