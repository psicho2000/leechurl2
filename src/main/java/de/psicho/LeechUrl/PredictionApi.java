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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.automl.v1beta1.AnnotationPayload;
import com.google.cloud.automl.v1beta1.ExamplePayload;
import com.google.cloud.automl.v1beta1.Image;
import com.google.cloud.automl.v1beta1.ModelName;
import com.google.cloud.automl.v1beta1.PredictResponse;
import com.google.cloud.automl.v1beta1.PredictionServiceClient;
import com.google.cloud.automl.v1beta1.PredictionServiceSettings;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;

/**
 * @see <a href="https://github.com/GoogleCloudPlatform/java-docs-samples/tree/master/vision/automl">Java Sample by Google</a>
 */
public class PredictionApi {

    private static final String BUCKET_NAME = "sort-pics-vcm";
    private static final String PROJECT_ID = "sort-pics";
    private static final String COMPUTE_REGION = "us-central1";
    private static final String MODEL_ID = "ICN4029841784534137370";
    private static final String SCORE_THRESHOLD = "0.8";
    private static final String CREDENTIAL_PATH = "D:\\Eigenes\\Desktop\\sort-pics-9f5bbbe58175.json";
    private static final Credentials CREDENTIALS = createCredentials();

    /**
     * Demonstrates using the AutoML client to predict an image.
     *
     * @throws IOException on Input/Output errors.
     */
    public static void batchPredict() throws IOException {

        // Instantiate client for prediction service.
        PredictionServiceSettings predictionServiceSettings =
            PredictionServiceSettings.newBuilder().setCredentialsProvider(() -> CREDENTIALS).build();
        PredictionServiceClient predictionClient = PredictionServiceClient.create(predictionServiceSettings);

        // Get the full path of the model.
        ModelName name = ModelName.of(PROJECT_ID, COMPUTE_REGION, MODEL_ID);

        // Additional parameters that can be provided for prediction e.g. Score Threshold
        Map<String, String> params = new HashMap<>();
        params.put("score_threshold", SCORE_THRESHOLD);

        // FIXME: foreach or rather use batch mode
        // https://cloud.google.com/ml-engine/docs/tensorflow/batch-predict
        // https://cloud.google.com/ml-engine/reference/rest/v1/projects.jobs#PredictionInput
        String filename = "abstract_headphones.jpg";

        // Read the image and assign to payload.
        ByteString content = getFileFromBucket(filename);
        Image image = Image.newBuilder().setImageBytes(content).build();
        ExamplePayload examplePayload = ExamplePayload.newBuilder().setImage(image).build();

        // Perform the AutoML Prediction request
        PredictResponse response = predictionClient.predict(name, examplePayload, params);

        System.out.println("Prediction results:");
        for (AnnotationPayload annotationPayload : response.getPayloadList()) {
            System.out.println("Predicted class name :" + annotationPayload.getDisplayName());
            System.out.println("Predicted class score :" + annotationPayload.getClassification().getScore());
        }
    }

    private static Credentials createCredentials() {
        GoogleCredentials credentials = null;
        try {
            credentials = GoogleCredentials.fromStream(new FileInputStream(CREDENTIAL_PATH))
                .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return credentials;
    }

    public static ByteString getFileFromBucket(String filename) {
        String basePath = "_unrated";
        String filePath = basePath + "/" + filename;

        Storage storage = StorageOptions.newBuilder().setCredentials(CREDENTIALS).build().getService();

        try (ReadChannel reader = storage.reader(BUCKET_NAME, filePath)) {
            InputStream inputStream = Channels.newInputStream(reader);
            return ByteString.copyFrom(IOUtils.toByteArray(inputStream));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
