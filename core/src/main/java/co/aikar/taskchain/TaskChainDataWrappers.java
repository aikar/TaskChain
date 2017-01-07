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

@SuppressWarnings("WeakerAccess")
public final class TaskChainDataWrappers {
    public static class Data2 <D1, D2> {
        public final D1 var1;
        public final D2 var2;
        public Data2(D1 var1, D2 var2) {
            this.var1 = var1;
            this.var2 = var2;
        }
    }
    public static class Data3 <D1, D2, D3> extends Data2<D1, D2> {
        public final D3 var3;
        public Data3(D1 var1, D2 var2, D3 var3) {
            super(var1, var2);
            this.var3 = var3;
        }
    }
    public static class Data4 <D1, D2, D3, D4> extends Data3<D1, D2, D3> {
        public final D4 var4;
        public Data4(D1 var1, D2 var2, D3 var3, D4 var4) {
            super(var1, var2, var3);
            this.var4 = var4;
        }
    }
    public static class Data5 <D1, D2, D3, D4, D5> extends Data4<D1, D2, D3, D4> {
        public final D5 var5;
        public Data5(D1 var1, D2 var2, D3 var3, D4 var4, D5 var5) {
            super(var1, var2, var3, var4);
            this.var5 = var5;
        }
    }
    public static class Data6 <D1, D2, D3, D4, D5, D6> extends Data5<D1, D2, D3, D4, D5> {
        public final D6 var6;
        public Data6(D1 var1, D2 var2, D3 var3, D4 var4, D5 var5, D6 var6) {
            super(var1, var2, var3, var4, var5);
            this.var6 = var6;
        }
    }
}
