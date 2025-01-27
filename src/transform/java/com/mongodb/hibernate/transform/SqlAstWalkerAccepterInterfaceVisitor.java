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

import static org.objectweb.asm.Opcodes.ASM9;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class SqlAstWalkerAccepterInterfaceVisitor extends ClassVisitor {

    public SqlAstWalkerAccepterInterfaceVisitor(ClassVisitor classVisitor) {
        super(ASM9, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(
            int access, String name, String descriptor, String signature, String[] exceptions) {
        if ("accept".equals(name)) {
            descriptor = "(Lorg/hibernate/sql/ast/SqlAstWalker;)Ljava/lang/Object;";
        }
        return getDelegate().visitMethod(access, name, descriptor, signature, exceptions);
    }
}
