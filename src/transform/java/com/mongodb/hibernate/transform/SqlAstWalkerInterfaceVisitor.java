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

public class SqlAstWalkerInterfaceVisitor extends ClassVisitor {

    public SqlAstWalkerInterfaceVisitor(ClassVisitor classVisitor) {
        super(ASM9, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(
            int access, String name, String descriptor, String signature, String[] exceptions) {
        if (descriptor.endsWith("V")) {
            descriptor = descriptor.substring(0, descriptor.length() - 1) + "Ljava/lang/Object;";
            // the following is for methods containing generics
            if (signature != null && signature.endsWith("V")) {
                signature = signature.substring(0, signature.length() - 1) + "Ljava/lang/Object;";
            }
        }
        return getDelegate().visitMethod(access, name, descriptor, signature, exceptions);
    }
}
