package de.psicho.LeechUrl;

import java.io.IOException;

import static org.apache.commons.lang3.ArrayUtils.addAll;

public class Main {

    public static void main(String[] args) throws IOException {
        PredictionApi.argsHelper(addAll(args, "sort-pics", "us-central1"), System.out);
    }
}
