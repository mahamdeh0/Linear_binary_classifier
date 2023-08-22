package com.ai;

public class PerceptronAlgorithm {
    private double[] weights;
    private double learningRate;
    private double mse;

    public PerceptronAlgorithm(int numInputs, double learningRate) {
        weights = new double[numInputs + 1];
        this.learningRate = learningRate;
        initializeWeights();
    }

    private void initializeWeights() {
        for (int i = 0; i < weights.length; i++) {
            weights[i] = Math.random() * 2 - 1; 
        }
    }

    public int predict(double[] inputs) {
        double sum = weights[0];

        for (int i = 0; i < inputs.length; i++) {
            sum += inputs[i] * weights[i + 1];
        }

        return (sum >= 0) ? 1 : -1;
    }

    public void train(double[][] inputs, int[] labels, int maxIterations) {
        mse = 0;

        for (int iteration = 1; iteration <= maxIterations; iteration++) {
            mse = 0;

            for (int i = 0; i < inputs.length; i++) {
                int predicted = predict(inputs[i]);
                int error = labels[i] - predicted;

                weights[0] += learningRate * error; 

                for (int j = 0; j < inputs[i].length; j++) {
                    weights[j + 1] += learningRate * error * inputs[i][j];
                }

                mse += error * error;
            }

            mse /= inputs.length;
        }
    }

    public double[] getWeights() {
        return weights;
    }

    public double getMSE() {
        return mse;
    }
}
