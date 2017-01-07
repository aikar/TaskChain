/*
 * Copyright (c) 2016-2017 Daniel Ennis (Aikar) - MIT License
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
