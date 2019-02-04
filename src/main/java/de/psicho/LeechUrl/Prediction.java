/*
 * Copyright 2018 Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This application demonstrates how to perform basic operations on prediction
 * with the Google AutoML Vision API.
 * For more information, the documentation at
 * https://cloud.google.com/vision/automl/docs.
 */

package de.psicho.LeechUrl;

// Imports the Google Cloud client library

import static com.google.common.collect.Lists.newArrayList;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.api.gax.paging.Page;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.ReadChannel;
import com.google.cloud.automl.v1beta1.AnnotationPayload;
import com.google.cloud.automl.v1beta1.ExamplePayload;
import com.google.cloud.automl.v1beta1.Image;
import com.google.cloud.automl.v1beta1.ModelName;
import com.google.cloud.automl.v1beta1.PredictResponse;
import com.google.cloud.automl.v1beta1.PredictionServiceClient;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.StorageOptions;
import com.google.protobuf.ByteString;
import com.opencsv.CSVWriter;

import lombok.extern.slf4j.Slf4j;

/**
 * @see <a href="https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/vision/automl">Java Sample by Google</a>
 *      Usage: set ENV var GOOGLE_APPLICATION_CREDENTIALS to json file containing service credentials
 */
@Slf4j
public class Prediction {

    private static final String BUCKET_NAME = "sort-pics-vcm";
    private static final String PROJECT_ID = "sort-pics";
    private static final String COMPUTE_REGION = "us-central1";
    private static final String MODEL_ID = "ICN4029841784534137370";
    private static final String SCORE_THRESHOLD = null; // e.g. "0.8" or null (null: do not use param)
    private static final String BASE_PATH = "_unrated";
    private static final String FILE_TYPES_TO_RATE = "image/jpeg";
    private static final Storage STORAGE = StorageOptions.newBuilder().build().getService();
    private static final int BATCH_INCREMENT = 20;

    /**
     * Demonstrates using the AutoML client to predict an image.
     * 
     * @param args optional from [0] and to [1]. If not provided, from = 0 and to = Integer.MAX_VALUE.
     */
    public static void batchPredict(String[] args) {
        Slice slice = new Slice(args);

        // Instantiate client for prediction service.
        PredictionServiceClient predictionClient;
        try {
            predictionClient = PredictionServiceClient.create();
        } catch (IOException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
            return;
        }

        // Get the full path of the model
        ModelName modelName = ModelName.of(PROJECT_ID, COMPUTE_REGION, MODEL_ID);

        // Additional parameters that can be provided for prediction e.g. score threshold.
        // If score threshold is not met by a picture, it will get no results.
        Map<String, String> params = new HashMap<>();
        if (SCORE_THRESHOLD != null) {
            params.put("score_threshold", SCORE_THRESHOLD);
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter("LeechUrl2.prediction.csv"), ',')) {
            AtomicInteger counter = new AtomicInteger(0);
            Page<Blob> fileList = STORAGE.list(BUCKET_NAME, BlobListOption.prefix(BASE_PATH));
            // @formatter:off
            StreamSupport.stream(fileList.iterateAll().spliterator(), false)
                         .skip(slice.from)
                         .limit(slice.to)
                         .forEach(predict(predictionClient, modelName, params, counter, writer));
            // @formatter:on
        } catch (IOException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
        }
    }

    private static Consumer<Blob> predict(PredictionServiceClient predictionClient, ModelName modelName,
        Map<String, String> params, AtomicInteger counter, CSVWriter writer) throws IOException {
        return blob -> {
            if (blob.getContentType().equals(FILE_TYPES_TO_RATE)) {
                int count = counter.incrementAndGet();
                // Alternative: ByteString content = getFileFromBucket(filePath);
                ByteString content = ByteString.copyFrom(blob.getContent());
                PredictResponse response;
                List<String> predicted = newArrayList(shortName(blob.getName()));
                try {
                    response = predict(predictionClient, modelName, params, content);
                    // Process the result
                    for (AnnotationPayload annotationPayload : response.getPayloadList()) {
                        predicted.add(annotationPayload.getDisplayName());
                        predicted.add(Float.toString(annotationPayload.getClassification().getScore()));
                    }
                } catch (ApiException ex) {
                    log.error(ExceptionUtils.getStackTrace(ex));
                    predicted.add("error");
                    predicted.add("1");
                }

                writer.writeNext(predicted.toArray(new String[0]));
                if (count % BATCH_INCREMENT == 0) {
                    log.info("{}: {}", count, shortName(blob.getName()));
                    try {
                        writer.flush();
                    } catch (IOException ex) {
                        log.error(ExceptionUtils.getStackTrace(ex));
                    }
                }
            }
        };
    }

    // Optional: might use batch mode
    // * https://cloud.google.com/ml-engine/docs/tensorflow/batch-predict
    // * https://cloud.google.com/ml-engine/reference/rest/v1/projects.jobs#PredictionInput
    private static PredictResponse predict(PredictionServiceClient predictionClient, ModelName modelName,
        Map<String, String> params, ByteString content) {

        // Assign the image to payload
        Image image = Image.newBuilder().setImageBytes(content).build();
        ExamplePayload examplePayload = ExamplePayload.newBuilder().setImage(image).build();

        // Perform the AutoML Prediction request
        return predictionClient.predict(modelName, examplePayload, params);
    }

    private static String shortName(String filePath) {
        return StringUtils.substringAfterLast(filePath, "/");
    }

    private static ByteString getFileFromBucket(String filePath) {
        try (ReadChannel reader = STORAGE.reader(BUCKET_NAME, filePath);
            InputStream inputStream = Channels.newInputStream(reader)) {
            return ByteString.copyFrom(IOUtils.toByteArray(inputStream));
        } catch (IOException ex) {
            log.error(ExceptionUtils.getStackTrace(ex));
        }
        return null;
    }

    static class Slice {

        Integer from = 0;
        Integer to = Integer.MAX_VALUE;

        Slice(String[] args) {
            if (args.length > 0) {
                try {
                    from = Integer.parseInt(args[0]);
                } catch (NumberFormatException ignored) {
                    // totally ok, use default value
                }
            }
            if (args.length > 1) {
                try {
                    to = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {
                    // totally ok, use default value
                }
            }
        }
    }
}
