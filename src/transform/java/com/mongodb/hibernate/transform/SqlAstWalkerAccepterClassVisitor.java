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

import org.hibernate.sql.ast.SqlAstWalker;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class SqlAstWalkerAccepterClassVisitor extends ClassVisitor {

    public static class TransformMethodVisitor extends MethodVisitor {

        private boolean invokedSqlAstWalker;
        private boolean invokedSpecial;
        private boolean needIncrementMaxStack;

        private static final String SQL_AST_WALKER_OWNER =
                SqlAstWalker.class.getName().replace('.', '/');

        TransformMethodVisitor(MethodVisitor mv) {
            super(ASM9, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            if (opcode == Opcodes.INVOKESPECIAL) {
                invokedSpecial = true;
            }
            if (opcode == Opcodes.INVOKEINTERFACE && SQL_AST_WALKER_OWNER.equals(owner)) {
                invokedSqlAstWalker = true;
                if (descriptor.endsWith("V")) {
                    descriptor = descriptor.substring(0, descriptor.length() - 1) + "Ljava/lang/Object;";
                }
            }
            getDelegate().visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode == Opcodes.RETURN) {
                // edge cases including exception throwing or empty method body
                if (!invokedSqlAstWalker && !invokedSpecial) {
                    getDelegate().visitInsn(Opcodes.ACONST_NULL);
                    needIncrementMaxStack = true;
                }
                opcode = Opcodes.ARETURN;
            }
            getDelegate().visitInsn(opcode);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            if (needIncrementMaxStack) {
                maxStack++;
            }
            getDelegate().visitMaxs(maxStack, maxLocals);
        }
    }

    public SqlAstWalkerAccepterClassVisitor(ClassVisitor classVisitor) {
        super(ASM9, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(
            int access, String name, String descriptor, String signature, String[] exceptions) {
        if ("accept".equals(name)) {
            var mv = cv.visitMethod(
                    access, name, "(Lorg/hibernate/sql/ast/SqlAstWalker;)Ljava/lang/Object;", signature, exceptions);
            return new TransformMethodVisitor(mv);
        } else {
            return cv.visitMethod(access, name, descriptor, signature, exceptions);
        }
    }
}
