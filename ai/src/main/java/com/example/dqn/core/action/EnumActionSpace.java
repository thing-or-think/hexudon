package com.example.dqn.core.action;

/**
 * Generic ActionSpace implementation for Enum-based Actions.
 *
 * @param <A> the enum type representing actions.
 */
public class EnumActionSpace<A extends Enum<A> & Action> implements ActionSpace<A> {

    private final Class<A> enumClass;

    public EnumActionSpace(Class<A> enumClass) {
        if (enumClass == null) {
            throw new IllegalArgumentException("Enum class cannot be null");
        }
        this.enumClass = enumClass;
    }

    @Override
    public int size() {
        return enumClass.getEnumConstants().length;
    }

    @Override
    public A actionAt(int index) {
        if (index < 0 || index >= size()) {
            throw new IllegalArgumentException("Invalid index for ActionSpace: " + index);
        }
        return enumClass.getEnumConstants()[index];
    }

    @Override
    public int indexOf(A action) {
        if (action == null) {
            throw new IllegalArgumentException("Action cannot be null");
        }
        return action.ordinal();
    }
}
