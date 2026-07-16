package fr.bl.drit.asm.agent.insnManipulation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import java.util.ArrayList;
import java.util.Arrays;

import fr.bl.drit.asm.agent.insnProbes.BanProb;
import fr.bl.drit.asm.agent.insnProbes.JumpProbe;
import fr.bl.drit.asm.agent.insnProbes.ParametersProbe;
import fr.bl.drit.asm.agent.insnProbes.ReturnProbe;
import fr.bl.drit.asm.agent.insnProbes.SwitchProbe;
import static fr.bl.drit.asm.agent.dataRecorder.RecorderProxy.treatMessage;

public class MethodProbesInjector extends MethodNode{

    private ParametersProbe parametersProbe;
    private JumpProbe jumpProbe;
    private SwitchProbe switchProbe;
    private BanProb banProb;
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

    public void setBanProb(BanProb banProb) {
        this.banProb = banProb;
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

        ArrayList<AbstractInsnNode> insnList = new ArrayList<>(Arrays.asList(instructions.toArray()));

        insnList = parametersProbe.getParameters(access, desc, insnList);
        insnList = onEnter(insnList);

        for (AbstractInsnNode insn : instructions.toArray()) {
            int opcode = insn.getOpcode();

            boolean isJump = (opcode >= Opcodes.IFEQ && opcode <= Opcodes.IF_ACMPNE)
                            || opcode == Opcodes.IFNULL || opcode == Opcodes.IFNONNULL;
            boolean isSwitch = opcode == Opcodes.TABLESWITCH || opcode == Opcodes.LOOKUPSWITCH;
            boolean isReturn = (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN);
            boolean isThrow  = opcode == Opcodes.ATHROW;

            if(banProb.isBanedInsn(insn)) {
                banProb.removeBanedInsn(insn);
                continue;
            }

            if(isJump) {
                insnList = jumpProbe.jumpInsnAnalyse(insn, insnList);
                continue;
            }

            if(isSwitch){
                insnList = switchProbe.getSwitchInsnAnalyse(insn, insnList);
                continue;
            }

            if(isReturn || isThrow) {
                insnList = onExit(insn, insnList);
                insnList = returnProbe.getReturnValue(opcode, insn, insnList);
                continue;
            }
        }

        instructions = insnList.stream().collect(InsnList::new, InsnList::add, InsnList::add);

        accept(mv);
    }

    private ArrayList<AbstractInsnNode> onEnter(ArrayList<AbstractInsnNode> insnList) {
        InsnList enterProbe = treatMessage("ENTER", "method: " + fullyQualifiedName);

        insnList.addAll(0, Arrays.asList(enterProbe.toArray()));

        return insnList;
    }

    private ArrayList<AbstractInsnNode> onExit(AbstractInsnNode insn, ArrayList<AbstractInsnNode> insnList) {
        InsnList exitProbe = treatMessage("EXIT", "method: " + fullyQualifiedName);

        insnList.addAll(insnList.indexOf(insn), Arrays.asList(exitProbe.toArray()));

        return insnList;
    }
}
