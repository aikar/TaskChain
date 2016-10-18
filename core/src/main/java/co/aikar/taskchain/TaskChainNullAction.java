/*
 * Copyright (c) 2016. Starlis LLC / dba Empire Minecraft
 *
 * This source code is proprietary software and must not be redistributed without Starlis LLC's approval
 *
 */

package co.aikar.taskchain;

/**
 * Defines actions to perform when a chain is used with .abortIfNull
 * Override desired arguments needed to provide actions
 * @param <A1>
 * @param <A2>
 * @param <A3>
 */
@SuppressWarnings("WeakerAccess")
public interface TaskChainNullAction <A1, A2, A3> {
    default void onNull(A1 arg1) {

    }
    default void onNull(A1 arg1, A2 arg2) {
        onNull(arg1);
    }
    default void onNull(A1 arg1, A2 arg2, A3 arg3) {
        onNull(arg1, arg2);
    }
}
