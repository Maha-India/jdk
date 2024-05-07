/*
 * Copyright (c) 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
package jdk.internal.event;

import sun.nio.ch.FileChannelImpl;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.lang.Throwable;
import java.lang.reflect.Field;

/**
 * Helper class for enabling JFR tracing.
 */
public final class JFRTracing {

  public static void enable() throws NoSuchFieldException, IllegalAccessException {
      enable(Throwable.class);
      enable(FileInputStream.class);
      enable(FileOutputStream.class);
      enable(FileChannelImpl.class);
      enable(RandomAccessFile.class);
  }

  private static void enable(Class<?> clazz) throws NoSuchFieldException, IllegalAccessException {
      Field field = clazz.getDeclaredField("jfrTracing");
      field.setAccessible(true);
      field.setBoolean(null, true);
  }
}
