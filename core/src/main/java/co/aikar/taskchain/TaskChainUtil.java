/*
 * Copyright (c) 2016. Starlis LLC / dba Empire Minecraft
 *
 * This source code is proprietary software and must not be redistributed without Starlis LLC's approval
 *
 */

package co.aikar.taskchain;

import java.util.logging.Logger;

final class TaskChainUtil {
    private TaskChainUtil() {
    }

    /**
     * Util method for example logging
     * @param log
     */
    static void log(String log) {
        for (String s : log.split("\n")) {
            Logger.getGlobal().info(s);
        }
    }

    public static void logError(String log) {
        for (String s : log.split("\n")) {
            Logger.getGlobal().severe(s);
        }
    }

    /**
     * Throws an exception without it needing to be in the method signature
     * @param t
     */
    static void sneakyThrows(Throwable t) {
        //noinspection RedundantTypeArguments
        throw TaskChainUtil.<RuntimeException>superSneaky( t );
    }

    /**
     * Magical method needed to trick Java
     * @param t
     * @param <T>
     * @return
     * @throws T
     */
    private static <T extends Throwable> T superSneaky(Throwable t) throws T {
        throw (T) t;
    }
}
