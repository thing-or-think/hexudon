package com.example.dqn.adapter.out.network.djl;

import ai.djl.Device;
import ai.djl.Model;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Block;
import ai.djl.nn.Parameter;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.ParameterStore;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.Batch;
import ai.djl.training.loss.Loss;
import ai.djl.training.optimizer.Optimizer;
import ai.djl.training.tracker.Tracker;
import ai.djl.translate.Batchifier;
import com.example.dqn.core.network.QNetwork;
import com.example.dqn.algorithm.dqn.training.TrainingBatch;
import java.io.IOException;
import java.nio.file.Path;

/**
 * DJL-based implementation of QNetwork using PyTorch as the backend engine.
 * Manages NDManager, model parameters, training step, and weights replication.
 */
public class DjlQNetwork implements QNetwork {

    private final NDManager manager;
    private final Model model;
    private final Block block;
    private final Trainer trainer;
    private final int stateDimension;
    private final int actionSpaceSize;

    /**
     * Constructs a DjlQNetwork instance.
     *
     * @param stateDimension the size of the input state vector.
     * @param actionSpaceSize the number of discrete actions.
     * @param hiddenLayers specification of hidden layer nodes.
     * @param learningRate optimizer step learning rate.
     */
    public DjlQNetwork(int stateDimension, int actionSpaceSize, int[] hiddenLayers, double learningRate) {
        this.stateDimension = stateDimension;
        this.actionSpaceSize = actionSpaceSize;

        // Create base manager for lifecycle
        this.manager = NDManager.newBaseManager();

        // Create the Model
        this.model = Model.newInstance("dqn-network");

        // Assemble the Multi-Layer Perceptron (MLP)
        this.block = DjlModelFactory.createMlp(stateDimension, actionSpaceSize, hiddenLayers);
        this.model.setBlock(block);

        // Initialize block weights
        this.block.initialize(manager, DataType.FLOAT32, new Shape(1, stateDimension));

        // Configure Optimizer and Trainer
        DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.l2Loss())
                .optOptimizer(Optimizer.adam().optLearningRateTracker(Tracker.fixed((float) learningRate)).build())
                .optDevices(new Device[]{Device.cpu()});
        
        this.trainer = model.newTrainer(config);
        
        // Initialize trainer with target shape
        this.trainer.initialize(new Shape(1, stateDimension));
    }

    @Override
    public float[] predict(float[] state) {
        try (NDManager subManager = manager.newSubManager()) {
            NDArray stateND = subManager.create(state);
            NDList inputs = new NDList(stateND);
            ParameterStore parameterStore = new ParameterStore(subManager, false);
            NDList outputs = block.forward(parameterStore, inputs, false);
            NDArray qValues = outputs.singletonOrThrow();
            return qValues.toFloatArray();
        }
    }

    @Override
    public float[][] predictBatch(float[][] states) {
        try (NDManager subManager = manager.newSubManager()) {
            NDArray statesND = subManager.create(states);
            NDList inputs = new NDList(statesND);
            ParameterStore parameterStore = new ParameterStore(subManager, false);
            NDList outputs = block.forward(parameterStore, inputs, false);
            NDArray qValues = outputs.singletonOrThrow();
            return to2DFloatArray(qValues);
        }
    }

    @Override
    public float train(TrainingBatch batch, float[][] targets) {
        try (NDManager subManager = manager.newSubManager()) {
            NDArray statesND = subManager.create(batch.states());
            NDArray targetsND = subManager.create(targets);

            NDList inputs = new NDList(statesND);
            NDList labels = new NDList(targetsND);

            // Construct standard DJL Dataset Batch wrapper
            Batch djlBatch = new Batch(
                    subManager,
                    inputs,
                    labels,
                    batch.size(),
                    Batchifier.STACK,
                    Batchifier.STACK,
                    0,
                    0
            );

            // Run gradient step
            EasyTrain.trainBatch(trainer, djlBatch);
            trainer.step();

            // Evaluate and retrieve training loss scalar on this batch
            ParameterStore parameterStore = new ParameterStore(subManager, false);
            NDList predictions = block.forward(parameterStore, inputs, false);
            try (NDArray lossND = trainer.getLoss().evaluate(labels, predictions)) {
                return lossND.mean().toFloatArray()[0];
            }
        }
    }

    @Override
    public void copyParametersFrom(QNetwork source) {
        if (!(source instanceof DjlQNetwork)) {
            throw new IllegalArgumentException("Source must be a DjlQNetwork");
        }
        DjlQNetwork sourceDjl = (DjlQNetwork) source;
        var sourceParams = sourceDjl.block.getParameters();
        var targetParams = this.block.getParameters();

        if (sourceParams.size() != targetParams.size()) {
            throw new IllegalStateException("Source and target networks parameter counts mismatch");
        }

        for (int i = 0; i < sourceParams.size(); i++) {
            Parameter sourceParam = sourceParams.get(i).getValue();
            Parameter targetParam = targetParams.get(i).getValue();

            NDArray sourceArray = sourceParam.getArray();
            NDArray targetArray = targetParam.getArray();
            
            // Set target parameters using byte buffer to support various engines
            targetArray.set(sourceArray.toByteBuffer());
        }
    }

    @Override
    public void save(Path modelPath, String modelName) throws IOException {
        model.save(modelPath, modelName);
    }

    @Override
    public void load(Path modelPath, String modelName) throws Exception {
        model.load(modelPath, modelName);
    }

    @Override
    public void close() {
        trainer.close();
        model.close();
        manager.close();
    }

    private float[][] to2DFloatArray(NDArray ndArray) {
        long[] shape = ndArray.getShape().getShape();
        int rows = (int) shape[0];
        int cols = (int) shape[1];
        float[] flatData = ndArray.toFloatArray();
        float[][] twoDArray = new float[rows][cols];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(flatData, i * cols, twoDArray[i], 0, cols);
        }
        return twoDArray;
    }
}
