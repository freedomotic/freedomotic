/*
 * The MIT License
 *
 * Copyright 2011 Peter Kocsis <p. kocsis. 2. 7182 at gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.ftdi;

import java.io.IOException;

/**
 * If something bad thing happen in native file.
 * @author Peter Kocsis <p. kocsis. 2. 7182 at gmail.com>
 */
public class FTD2XXException extends IOException {

    public FTD2XXException(FT_STATUS ftStatus) {
        super("D2XX error, ftStatus:" + ftStatus);
    }

    public FTD2XXException(int ftStatus) {
        this(FT_STATUS.values()[ftStatus]);
    }

    public FTD2XXException(Throwable cause) {
        super(cause);
    }

    public FTD2XXException(String message, Throwable cause) {
        super(message, cause);
    }

    public FTD2XXException(String message) {
        super(message);
    }

    public FTD2XXException() {
    }
}
