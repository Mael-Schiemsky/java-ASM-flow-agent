package fr.bl.drit.asm.agent.insnManipulation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import fr.bl.drit.asm.agent.insnProbes.JumpProbe;
import fr.bl.drit.asm.agent.insnProbes.ParametersProbe;
import fr.bl.drit.asm.agent.insnProbes.ReturnProbe;
import fr.bl.drit.asm.agent.insnProbes.SwitchProbe;
import static fr.bl.drit.asm.agent.dataRecorder.RecorderProxy.treatMessage;

public class MethodProbesInjector extends MethodNode{

    private ParametersProbe parametersProbe;
    private JumpProbe jumpProbe;
    private SwitchProbe switchProbe;
    private ReturnProbe returnProbe;

    private String fullyQualifiedName = "";

    public MethodProbesInjector(int api, int access, String name, String descriptor, String signature, String[] exceptions, String className, MethodVisitor mv) {
        super(api, access, name, descriptor, signature, exceptions);

        this.fullyQualifiedName = className + "#" + name + descriptor + (signature != null ? " " + signature : "");
        this.mv = mv;
    }

    public void setJumpProbe(JumpProbe jumpProbe) {
        this.jumpProbe = jumpProbe;
    }

    public void setParametersProbe(ParametersProbe parametersProbe) {
        this.parametersProbe = parametersProbe;
    }

    public void setSwitchProbe(SwitchProbe switchProbe) {
        this.switchProbe = switchProbe;
    }

    public void setReturnProbe(ReturnProbe returnProbe) {
        this.returnProbe = returnProbe;
    }

    @Override
    public void visitEnd() {
        super.visitEnd();

        if ((access & Opcodes.ACC_ABSTRACT) != 0 || (access & Opcodes.ACC_NATIVE) != 0) {
            accept(mv);
            return;
        }

        InsnList insnProbes;

        insnProbes = parametersProbe.getParameters(access, desc, instructions);
        instructions.insertBefore(instructions.getFirst(), insnProbes);

        onEnter();

        for (AbstractInsnNode insn : instructions.toArray()) {
            int opcode = insn.getOpcode();

            boolean isJump = (opcode >= Opcodes.IFEQ && opcode <= Opcodes.IF_ACMPNE)
                                || opcode == Opcodes.IFNULL || opcode == Opcodes.IFNONNULL;
            boolean isSwitch = opcode == Opcodes.TABLESWITCH || opcode == Opcodes.LOOKUPSWITCH;
            boolean isReturn = (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN);
            boolean isThrow  = opcode == Opcodes.ATHROW;

            if(switchProbe.isBannedInsn(insn)) {
                switchProbe.removeBannedInsn(insn);
                continue;
            }

            if(isJump) {
                insnProbes = jumpProbe.jumpInsnAnalyse(insn);
                instructions.insertBefore(insn, insnProbes);
                continue;
            }

            if(isSwitch){
                insnProbes = switchProbe.getSwitchInsnAnalyse(insn);
                instructions.insertBefore(insn, insnProbes);
                continue;
            }

            if(isReturn || isThrow) {
                insnProbes = onExit();
                insnProbes.add(returnProbe.getReturnValue(opcode));

                instructions.insertBefore(insn, insnProbes);
                continue;
            }
        }

        accept(mv);
    }

    private void onEnter() {
        InsnList enterProbe = treatMessage("[\u001B[32m" + "ENTER" + "\u001B[0m] " + "method: " + fullyQualifiedName);
        instructions.insertBefore(instructions.getFirst(), enterProbe);
    }

    private InsnList onExit() {
        return treatMessage("[\u001B[31m" + "EXIT" + "\u001B[0m] " + "method: " + fullyQualifiedName);
    }
}
