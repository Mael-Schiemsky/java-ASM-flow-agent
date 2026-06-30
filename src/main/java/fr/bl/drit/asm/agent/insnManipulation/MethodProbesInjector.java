package fr.bl.drit.asm.agent.insnManipulation;

import java.util.ArrayList;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
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

        ArrayList<LabelNode> visitedLabels = new ArrayList<>();
        for (AbstractInsnNode insn : instructions.toArray()) {
            int opcode = insn.getOpcode();
            boolean isReturn = (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN);
            boolean isThrow  = opcode == Opcodes.ATHROW;
            boolean isJump = (opcode >= Opcodes.IFEQ && opcode <= Opcodes.IF_ACMPNE)
                                || opcode == Opcodes.IFNULL || opcode == Opcodes.IFNONNULL;
            boolean isSwitch = opcode == Opcodes.TABLESWITCH || opcode == Opcodes.LOOKUPSWITCH;

            if(insn instanceof LabelNode label) {
                visitedLabels.add(label);
                continue;
            }

            if(isJump) {
                insnProbes = jumpInsnAnalyse(insn, visitedLabels);
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
        InsnList enterProbe = buildPrintln("e:enter, method:" + fullyQualifiedName);
        instructions.insertBefore(instructions.getFirst(), enterProbe);
    }

    private InsnList onExit() {
        return buildPrintln("e:exit, method:" + fullyQualifiedName);
    }
}
