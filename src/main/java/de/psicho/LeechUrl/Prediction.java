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

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.api.gax.paging.Page;
import com.google.cloud.ReadChannel;
import com.google.cloud.automl.v1beta1.AnnotationPayload;
import com.google.cloud.automl.v1beta1.ExamplePayload;
import com.google.cloud.automl.v1beta1.Image;
import com.google.cloud.automl.v1beta1.ModelName;
import com.google.cloud.automl.v1beta1.PredictResponse;
import com.google.cloud.automl.v1beta1.PredictionServiceClient;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.protobuf.ByteString;

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
    private static final String SCORE_THRESHOLD = null; // e.g. 0.8
    private static final String BASE_PATH = "_unrated";
    private static final Storage STORAGE = StorageOptions.newBuilder().build().getService();

    /**
     * Demonstrates using the AutoML client to predict an image.
     */
    public static void batchPredict() {

        // Instantiate client for prediction service.
        PredictionServiceClient predictionClient;
        try {
            predictionClient = PredictionServiceClient.create();
        } catch (IOException ex) {
            ex.printStackTrace();
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

        // Optional: may use batch mode
        // * https://cloud.google.com/ml-engine/docs/tensorflow/batch-predict
        // * https://cloud.google.com/ml-engine/reference/rest/v1/projects.jobs#PredictionInput
        // FIXME: Implement logging and from/to mechanism
        Page<Blob> fileList = STORAGE.list(BUCKET_NAME, Storage.BlobListOption.prefix(BASE_PATH));
        int i = 0;
        for (Blob blob : fileList.iterateAll()) {
            if (blob.getContentType().equals("image/jpeg")) {
                // Alternative: ByteString content = getFileFromBucket(filePath);
                ByteString content = ByteString.copyFrom(blob.getContent());
                predict(predictionClient, modelName, params, content, blob.getName());
            }
            if (i % 3 == 0) {
                log.info("{}: {}", i, shortName(blob.getName()));
            }
            if (i++ == 10)
                return;
        }
    }

    private static void predict(PredictionServiceClient predictionClient, ModelName modelName, Map<String, String> params,
        ByteString content, String filePath) {

        // Assign the image to payload
        Image image = Image.newBuilder().setImageBytes(content).build();
        ExamplePayload examplePayload = ExamplePayload.newBuilder().setImage(image).build();

        // Perform the AutoML Prediction request
        PredictResponse response = predictionClient.predict(modelName, examplePayload, params);

        // Process the result
        System.out.println(format("Prediction results for %s:", shortName(filePath)));
        for (AnnotationPayload annotationPayload : response.getPayloadList()) {
            System.out.println("Predicted class name  :" + annotationPayload.getDisplayName());
            System.out.println("Predicted class score :" + annotationPayload.getClassification().getScore());
        }
    }

    private static String shortName(String filePath) {
        return StringUtils.substringAfterLast(filePath, "/");
    }

    private static ByteString getFileFromBucket(String filePath) {
        try (ReadChannel reader = STORAGE.reader(BUCKET_NAME, filePath);
            InputStream inputStream = Channels.newInputStream(reader);) {
            return ByteString.copyFrom(IOUtils.toByteArray(inputStream));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
