package fr.bl.drit.asm.agent.methodInjector;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public class MyMethodNode extends MethodNode{

    private String fullyQualifiedName = "";

    public MyMethodNode(int api, int access, String name, String descriptor, String signature, String[] exceptions, String className, MethodVisitor mv) {
        super(api, access, name, descriptor, signature, exceptions);
        this.fullyQualifiedName = className + "#" + name + descriptor + (signature != null ? " " + signature : "");
        this.mv = mv;
    }

    @Override
    public void visitEnd() {
        super.visitEnd();

        if ((access & Opcodes.ACC_ABSTRACT) != 0 || (access & Opcodes.ACC_NATIVE) != 0) {
            accept(mv);
            return;
        }

        InsnList enterProbe = buildPrintln("e:enter, method:" + fullyQualifiedName);
        if (instructions.size() > 0) {
            instructions.insertBefore(instructions.getFirst(), enterProbe);
        } else {
            instructions.add(enterProbe);
        }

        for (AbstractInsnNode insn : instructions.toArray()) {
            int opcode = insn.getOpcode();
            boolean isReturn = (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN);
            boolean isThrow  = opcode == Opcodes.ATHROW;
            if (!isReturn && !isThrow) continue;

            InsnList probe = new InsnList();

            probe.add(buildPrintln("e:exit, method:" + fullyQualifiedName));

            if (opcode == Opcodes.IRETURN || opcode == Opcodes.FRETURN) {
                probe.add(new InsnNode(Opcodes.DUP));
            } else if (opcode == Opcodes.LRETURN || opcode == Opcodes.DRETURN) {
                probe.add(new InsnNode(Opcodes.DUP2));
            }

            switch (opcode) {
                case Opcodes.IRETURN:
                    probe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;", false));
                    break;
                case Opcodes.FRETURN:
                    probe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float", "toString", "(F)Ljava/lang/String;", false));
                    break;
                case Opcodes.LRETURN:
                    probe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long", "toString", "(J)Ljava/lang/String;", false));
                    break;
                case Opcodes.DRETURN:
                    probe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;", false));
                    break;
                case Opcodes.ARETURN:
                case Opcodes.RETURN:
                case Opcodes.ATHROW:
                    probe.add(buildPrintln("void"));
                    break;
            }

            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.DRETURN) {
                probe.add(buildPrintln(""));
            }

            instructions.insertBefore(insn, probe);
        }

        accept(mv);
    }

    private InsnList buildPrintln(String message) {
        InsnList list = new InsnList();
        list.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
        if (message.isEmpty()) {
            list.add(new InsnNode(Opcodes.SWAP));
        } else {
            list.add(new LdcInsnNode(message));
        }
        list.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false));
        return list;
    }
}
