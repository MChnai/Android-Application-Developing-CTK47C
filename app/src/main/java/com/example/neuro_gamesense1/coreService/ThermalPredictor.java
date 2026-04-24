package com.example.neuro_gamesense1.coreService;

import android.content.Context;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

import java.io.InputStream;
import java.util.Collections;

public class ThermalPredictor {
    private static OrtEnvironment env;
    private static OrtSession session;

    public ThermalPredictor(Context context) {
        try {
            if (env == null) {
                env = OrtEnvironment.getEnvironment();

                InputStream is = context.getAssets().open("thermal_rf_model.onnx");
                byte[] modelBytes = new byte[is.available()];
                is.read(modelBytes);
                is.close();

                session = env.createSession(modelBytes, new OrtSession.SessionOptions());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double score(double temp, double iSmooth, double tempSlope, int audioState) {
        if (session == null || env == null) return temp;

        try {
            double interaction = temp * iSmooth;
            double tempSq = temp * temp;
            float[][] inputFeatures = new float[1][6];

            inputFeatures[0][0] = (float) temp;
            inputFeatures[0][1] = (float) iSmooth;
            inputFeatures[0][2] = (float) interaction;
            inputFeatures[0][3] = (float) tempSq;
            inputFeatures[0][4] = (float) audioState;
            inputFeatures[0][5] = (float) tempSlope;

            OnnxTensor tensor = OnnxTensor.createTensor(env, inputFeatures);
            OrtSession.Result result = session.run(Collections.singletonMap("float_input", tensor));

            float[][] output = (float[][]) result.get(0).getValue();

            tensor.close();
            result.close();

            return (double) output[0][0];

        } catch (Exception e) {
            e.printStackTrace();
            return temp;
        }
    }
}