package fr.bl.drit.asm.agent.insnManipulation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import static fr.bl.drit.asm.agent.insnTools.JumpProbe.jumpInsnAnalyse;
import static fr.bl.drit.asm.agent.insnTools.ParametersProbe.getParameters;
import static fr.bl.drit.asm.agent.insnTools.ReturnProbe.getReturnValue;
import static fr.bl.drit.asm.agent.insnTools.PrintTools.buildPrintln;

public class MethodProbesInjector extends MethodNode{

    private String fullyQualifiedName = "";

    public MethodProbesInjector(int api, int access, String name, String descriptor, String signature, String[] exceptions, String className, MethodVisitor mv) {
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

        InsnList insnProbes;

        insnProbes = getParameters(access, desc, instructions);
        instructions.insertBefore(instructions.getFirst(), insnProbes);

        onEnter();

        for (AbstractInsnNode insn : instructions.toArray()) {
            int opcode = insn.getOpcode();
            boolean isReturn = (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN);
            boolean isThrow  = opcode == Opcodes.ATHROW;
            boolean isJump = (opcode >= Opcodes.IFEQ && opcode <= Opcodes.IF_ACMPNE)
                                || opcode == Opcodes.IFNULL || opcode == Opcodes.IFNONNULL;
            boolean isSwitch = opcode == Opcodes.TABLESWITCH || opcode == Opcodes.LOOKUPSWITCH;

            if(isJump) {
                insnProbes = jumpInsnAnalyse(insn);
                instructions.insertBefore(insn, insnProbes);
                continue;
            }

            if(isSwitch){
                continue;
            }

            if(isReturn || isThrow) {
                insnProbes = onExit();
                insnProbes.add(getReturnValue(opcode));

                instructions.insertBefore(insn, insnProbes);
                continue;
            }
        }

        accept(mv);
    }

    private void onEnter() {
        InsnList enterProbe = buildPrintln("[\u001B[32m" + "ENTER" + "\u001B[0m] " + "method: " + fullyQualifiedName);
        instructions.insertBefore(instructions.getFirst(), enterProbe);
    }

    private InsnList onExit() {
        return buildPrintln("[\u001B[31m" + "EXIT" + "\u001B[0m] " + "method: " + fullyQualifiedName);
    }
}
