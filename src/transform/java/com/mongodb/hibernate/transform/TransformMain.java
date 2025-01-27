/*
 * Copyright 2025-present MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongodb.hibernate.transform;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.hibernate.sql.ast.SqlAstWalker;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.sql.ast.tree.Statement;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.reflections.Reflections;

public class TransformMain {

    private static byte[] transformSqlAstWalkerAcceptorInterfaces(Class<?> clazz) throws IOException {
        return doTransform(clazz, SqlAstWalkerAccepterInterfaceVisitor::new);
    }

    private static byte[] transformSqlAstWalkerInterface() throws IOException {
        return doTransform(SqlAstWalker.class, SqlAstWalkerInterfaceVisitor::new);
    }

    @SuppressWarnings("ReturnValueIgnored")
    private static Optional<byte[]> transformSqlAstWalkerAcceptorClasses(Class<?> clazz) throws IOException {
        try {
            clazz.getDeclaredMethod("accept", SqlAstWalker.class);
            return Optional.of(doTransform(clazz, SqlAstWalkerAccepterClassVisitor::new));
        } catch (NoSuchMethodException ignored) {
            return Optional.empty();
        }
    }

    private static byte[] doTransform(Class<?> clazz, Function<ClassWriter, ClassVisitor> visitor) throws IOException {
        String className = clazz.getName();
        ClassReader reader = new ClassReader(className);
        ClassWriter writer = new ClassWriter(reader, 0);
        ClassVisitor classVisitor = visitor.apply(writer);
        reader.accept(classVisitor, 0);
        return writer.toByteArray();
    }

    private static void saveTransformResult(byte[] content, Class<?> clazz, String outputRootFolder) {
        try {
            Path path = Paths.get(outputRootFolder, clazz.getName().replace('.', '/') + ".class");
            File folder = path.getParent().toFile();
            if (!folder.exists() && !folder.mkdirs()) {
                throw new IOException("Failed to create folder: " + folder);
            }
            Files.write(path, content);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Usage: TransformMain <output_folder>");
        }
        String outputRootFolder = args[0];

        var reflections = new Reflections("org.hibernate.sql");

        // transform SqlAstWalker interface
        saveTransformResult(transformSqlAstWalkerInterface(), SqlAstWalker.class, outputRootFolder);

        // transform SqlAstNode and Statement interfaces
        saveTransformResult(transformSqlAstWalkerAcceptorInterfaces(SqlAstNode.class), SqlAstNode.class, outputRootFolder);
        saveTransformResult(transformSqlAstWalkerAcceptorInterfaces(Statement.class), Statement.class, outputRootFolder);

        // transform child classes of SqlAstNode and Statement
        for (Class<?> clazz : List.of(SqlAstNode.class, Statement.class)) {
            var sqlAstNodeClasses = reflections.getSubTypesOf(clazz);
            for (var sqlAstNodeClass : sqlAstNodeClasses) {
                transformSqlAstWalkerAcceptorClasses(sqlAstNodeClass).ifPresent(bytes -> {
                    saveTransformResult(bytes, sqlAstNodeClass, outputRootFolder);
                });
            }
        }
    }
}
