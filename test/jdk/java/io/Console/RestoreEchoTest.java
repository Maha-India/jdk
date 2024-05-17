/*
 * Copyright (c) 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import java.io.Console;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Paths;

import jdk.internal.io.JdkConsoleImpl;

import jdk.test.lib.process.OutputAnalyzer;
import jdk.test.lib.process.ProcessTools;
import jtreg.SkippedException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;


/**
 * @test
 * @bug 8332161
 * @summary Tests JdkConsoleImpl restores the echo state after readPassword() call
 *     This test relies on the static JdkConsoleImpl.get/setEcho() methods, which
 *     queries/sets the platform's echo state.
 * @library /test/lib
 * @modules java.base/jdk.internal.io:+open
 * @run junit RestoreEchoTest
 */
public class RestoreEchoTest {

    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    public void testRestoreEcho() throws Throwable {
        // check "expect" command availability
        var expect = Paths.get("/usr/bin/expect");
        if (!Files.exists(expect) || !Files.isExecutable(expect)) {
            throw new SkippedException("'" + expect + "' not found");
        }

        expectRunner("false");
        expectRunner("true");
    }

    private static void expectRunner(String initialEcho) throws Throwable {
        // invoking "expect" command
        var testSrc = System.getProperty("test.src", ".");
        var testClasses = System.getProperty("test.classes", ".");
        var jdkDir = System.getProperty("test.jdk");
        OutputAnalyzer output = ProcessTools.executeProcess(
                "expect",
                "-n",
                testSrc + "/restoreEcho.exp",
                initialEcho,
                jdkDir + "/bin/java",
                "--add-opens=java.base/jdk.internal.io=ALL-UNNAMED",
                "-Djdk.console=java.base",
                "-classpath", testClasses,
                "RestoreEchoTest",
                initialEcho);
        output.reportDiagnosticSummary();
        var eval = output.getExitValue();
        if (eval != 0) {
            throw new RuntimeException("Test failed. Exit value from 'expect' command: " + eval);
        }
    }

    public static void main(String... args) throws Throwable {
        if (!"java.base".equals(System.getProperty("jdk.console"))) {
            throw new RuntimeException("Test failed. jdk.console is not java.base");
        }

        MethodHandle MH_getEcho = MethodHandles.privateLookupIn(JdkConsoleImpl.class, MethodHandles.lookup())
                .findStatic(JdkConsoleImpl.class, "getEcho", MethodType.methodType(boolean.class));
        MethodHandle MH_setEcho = MethodHandles.privateLookupIn(JdkConsoleImpl.class, MethodHandles.lookup())
                .findStatic(JdkConsoleImpl.class, "setEcho", MethodType.methodType(void.class, boolean.class));

        var initialEcho = Boolean.parseBoolean(args[0]);
        var originalEcho = (boolean)MH_getEcho.invokeExact();
        MH_setEcho.invokeExact(initialEcho);

        Console con = System.console();
        if (con == null) {
            throw new RuntimeException("Test failed. System.console() returned null");
        }

        // testing readLine()
        String input = con.readLine("prompt: ");
        con.printf("input is %s\n", input);

        // testing readPassword()
        input = String.valueOf(con.readPassword("password prompt: "));
        con.printf("password is %s\n", input);

        var echoAfter = (boolean)MH_getEcho.invokeExact();
        MH_setEcho.invokeExact(originalEcho);

        var echoStates =
                """
                    Platform's original echo state: %s
                    Echo state before readPassword(): %s
                    Echo state after readPassword(): %s
                """.formatted(originalEcho, initialEcho, echoAfter);
        if (initialEcho != echoAfter) {
            throw new RuntimeException(
                """
                Initial echo state was not correctly restored.
                %s
                """.formatted(echoStates));
        } else {
            System.out.println(echoStates);
        }
    }
}
