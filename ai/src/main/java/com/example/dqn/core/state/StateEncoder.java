package com.example.dqn.core.state;

/**
 * Interface responsible for encoding domain-specific state representations S
 * into numeric feature vectors suitable for feeding into a Neural Network.
 *
 * @param <S> the domain state type.
 */
public interface StateEncoder<S extends State> {

    /**
     * Encodes the domain-specific state into a 1D float array.
     *
     * @param state the domain state to encode.
     * @return a float array representing the encoded features of the state.
     */
    float[] encode(S state);
}
