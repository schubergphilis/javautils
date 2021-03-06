/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.schubergphilis.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.hamcrest.Matcher;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class FileUtils {

    private FileUtils() {
    }

    public static void deleteDirectory(File dir, boolean recursively) throws IOException {
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException("Supplied directory '" + dir.getPath() + "' is either a file or it does not exist.");
        }
        if (!recursively) {
            if (!dir.delete()) {
                throw new IOException("Could not delte directory '" + dir.getPath()
                        + "' probably because it is not empty.");
            }
        } else {
            org.apache.commons.io.FileUtils.deleteDirectory(dir);
        }
    }

    public static void copyDirectory(File srcDir, File dstDir) throws IOException {
        org.apache.commons.io.FileUtils.copyDirectory(srcDir, dstDir);
    }

    public static File createTemporaryFile(String filenamePrefix) throws IOException {
        return File.createTempFile(filenamePrefix, Long.toString(System.currentTimeMillis()));
    }

    public static File createTemporaryFile() throws IOException {
        return createTemporaryFile("tempFile");
    }

    public static String replaceFileSeparator(String path, String fileSeparatorRegex, String replacement) {
        return path.replaceAll(fileSeparatorRegex, replacement);
    }

    public static String replaceFileSeparator(String path, String replacement) {
        return replaceFileSeparator(path, System.getProperty("file.separator"), replacement);
    }

    public static List<String> readLines(File file) throws IOException {
        return readLines(file, false);
    }

    public static List<String> readLines(File file, boolean removeTraillingWhiteSpace) throws IOException {
        List<String> lines = org.apache.commons.io.FileUtils.readLines(file);
        if (!removeTraillingWhiteSpace) {
            return lines;
        } else {
            List<String> trimmedLines = new ArrayList<String>(lines.size());
            for (String line : lines) {
                String trimmedLine = line.trim();
                if (!trimmedLine.isEmpty()) {
                    trimmedLines.add(trimmedLine);
                }
            }
            return trimmedLines;
        }
    }

    public static void writeToFile(String contents, File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(contents);
        fileWriter.close();
    }

    public static void writeToFile(List<String> patch, File file) throws IOException {
        FileWriter fileWriter = new FileWriter(file);
        for (String delta : patch) {
            fileWriter.write(delta);
            fileWriter.write("\n");
        }
        fileWriter.close();
    }

    public static boolean filesHaveSameContentsNotConsideringTraillingWhiteSpace(File fileV1, File fileV2)
            throws IOException {
        boolean removeTraillingWhiteSpace = true;
        List<String> linesFileV1 = FileUtils.readLines(fileV1, removeTraillingWhiteSpace);
        List<String> linesFileV2 = FileUtils.readLines(fileV2, removeTraillingWhiteSpace);

        if (linesFileV1.size() != linesFileV2.size()) {
            return false;
        } else {
            return linesFileV1.equals(linesFileV2);
        }
    }

    public static Set<File> gatherFilesThatMatchCriteria(File baseDir, Matcher<String> criteria) {
        Set<File> gatheredFiles = new TreeSet<>(new AbsolutePathFileComparator());

        for (File file : baseDir.listFiles()) {
            if (file.isDirectory()) {
                gatheredFiles.addAll(gatherFilesThatMatchCriteria(file, criteria));
            } else if (criteria.matches(file.getPath())) {
                gatheredFiles.add(file);
            }
        }

        return gatheredFiles;
    }

    public static List<String> getPatch(File original, File revised) throws IOException {
        List<String> originalLines = readLines(original);
        List<String> revisedLines = readLines(revised);
        Patch diff = DiffUtils.diff(originalLines, revisedLines);

        List<Delta> deltas = diff.getDeltas();
        List<String> patchDeltas = new ArrayList<>(deltas.size());
        for (Delta delta : deltas) {
            patchDeltas.add(delta.toString());
        }

        return patchDeltas;
    }

}