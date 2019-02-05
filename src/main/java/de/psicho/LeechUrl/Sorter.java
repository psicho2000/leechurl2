package de.psicho.LeechUrl;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.opencsv.CSVReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Sorter {

    private static final String PREDICTION_FILE = "D:\\Eigenes\\Desktop\\sort-pics\\LeechUrl2.prediction.csv";
    private static final String PIC_PATH = "D:\\Eigenes\\Desktop\\downloads\\";

    public static void sort() {
        try (Reader reader = Files.newBufferedReader(Paths.get(PREDICTION_FILE)); CSVReader csvReader = new CSVReader(reader)) {
            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                if (nextRecord.length == 3) {
                    File srcFile = new File(PIC_PATH + nextRecord[0]);
                    File destFile = new File(PIC_PATH + nextRecord[1] + "\\" + nextRecord[0]);
                    FileUtils.moveFile(srcFile, destFile);
                }
            }
        } catch (Exception ex) {
            log.info(ExceptionUtils.getStackTrace(ex));
        }
    }
}
