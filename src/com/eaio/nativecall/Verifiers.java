/* 
 * Verifiers.java
 * 
 * Created on 07.09.2004.
 *
 * eaio: NativeCall - calling operating system methods from Java
 * Copyright (c) 2004-2006 Johann Burkard (<mailto:jb@eaio.com>)
 * <http://eaio.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.eaio.nativecall;

import java.util.Iterator;
import java.util.ServiceConfigurationError;

/**
 * Verifiers instantiates the matching {@link com.eaio.nativecall.Verifier} for
 * the current operating system. 
 * 
 * @author <a href="mailto:jb@eaio.com">Johann Burkard</a>
 * @version $Id: Verifiers.java,v 1.2 2006/01/06 10:58:33 grnull Exp $
 */
final class Verifiers {

    /**
     * No instances needed.
     */
    private Verifiers() {}

    /**
     * The Verifier.
     */
    private static Verifier v = null;

    /**
     * Find the matching Verifier.
     * 
     * @throws ServiceConfigurationError
     * @throws SecurityException
     */
    static void init() throws ServiceConfigurationError, SecurityException {
        Iterator<Verifier> i =
                java.util.ServiceLoader.load(Verifier.class, Verifier.class.getClassLoader()).iterator();

        Verifier ver = null;
        while (i.hasNext()) {
            ver = i.next();
            if (ver.supports()) {
                v = ver;
                break;
            }
        }

        v = new Win32Verifier();
    }

    /**
     * Returns the Verifier.
     * 
     * @return a Verifier or <code>null</code>
     */
    static Verifier getInstance() {
        return v;
    }

}
