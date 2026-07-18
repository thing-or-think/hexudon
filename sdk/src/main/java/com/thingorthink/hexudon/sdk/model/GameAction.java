package com.thingorthink.hexudon.sdk.model;

/**
 * Represents a single action that an agent can perform during a turn.
 * <p>
 * Implementations convert themselves to the integer protocol code
 * required by the Hexudon server.
 */
public interface GameAction {

    /**
     * Returns the protocol code of this action.
     *
     * @return integer value sent to the server
     */
    int toProtocolCode();
}
