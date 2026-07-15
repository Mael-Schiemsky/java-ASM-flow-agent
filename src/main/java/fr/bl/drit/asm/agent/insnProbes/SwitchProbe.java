package fr.bl.drit.asm.agent.insnProbes;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;

import static fr.bl.drit.asm.agent.dataRecorder.RecorderProxy.treatMessage;

import java.util.ArrayList;
import java.util.List;

public class SwitchProbe {
    private ArrayList<AbstractInsnNode> bannedInsn = new ArrayList<>();

    public boolean isBannedInsn(AbstractInsnNode insn) {
        return bannedInsn.contains(insn);
    }

    public ArrayList<AbstractInsnNode> getBannedInsn(){
        return bannedInsn;
    }

    public void removeBannedInsn(AbstractInsnNode insn) {
        bannedInsn.remove(insn);
    }

    public InsnList getSwitchInsnAnalyse(AbstractInsnNode insn) {
        InsnList insnProbes = new InsnList();

        insnProbes.add(getCorrespondingLineNumber(insn));
        insnProbes.add(switchCaseAnalyse(insn));
        insnProbes.add(treatMessage(""));
        
        return insnProbes;
    }

    private InsnList switchCaseAnalyse(AbstractInsnNode insn) {
        InsnList toStringInsn = new InsnList();

        AbstractInsnNode tempInsn = searchPreviousInsn(insn);
        if(isStringHashCodeInsn(tempInsn)) {
            addBannedInsnFromSwitchString(insn);
            toStringInsn.add(searchPreviousInsn(tempInsn).clone(null));
            return toStringInsn;
        }

        toStringInsn.add(new InsnNode(Opcodes.DUP));
        toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;", false));

        return toStringInsn;
    }

    private InsnList getCorrespondingLineNumber(AbstractInsnNode insn) {
        int myLineNumber = -1;

        AbstractInsnNode prevInsn = insn.getPrevious();
        while (prevInsn != null) {
            if (prevInsn instanceof LineNumberNode lineInsn) {
                myLineNumber = lineInsn.line;
                break;
            }
            prevInsn = prevInsn.getPrevious();
        }

        return treatMessage("[\u001B[33m" + "SWITCH" + "\u001B[0m] " + "switch instruction: " + insn.getOpcode()
                            + " corresponding to the line " + myLineNumber);
    }

    private AbstractInsnNode searchPreviousInsn(AbstractInsnNode insn) {
        AbstractInsnNode prevInsn = insn.getPrevious();

        while(isNotJBCinsn(prevInsn)) {
            prevInsn = prevInsn.getPrevious();
        }
        return prevInsn;   
    }

    private boolean isNotJBCinsn(AbstractInsnNode insn) {
        return insn instanceof LabelNode || insn instanceof LineNumberNode || insn instanceof FrameNode
            || insn.getOpcode() == Opcodes.NOP;
    }

    private boolean isStringHashCodeInsn(AbstractInsnNode insn) {
        return insn instanceof MethodInsnNode methodInsn
                && methodInsn.getOpcode() == Opcodes.INVOKEVIRTUAL
                && methodInsn.owner.equals("java/lang/String")
                && methodInsn.name.equals("hashCode")
                && methodInsn.desc.equals("()I");
    }

    private void addBannedInsnFromSwitchString(AbstractInsnNode insn) {
        LabelNode dflt = getDefaultLabel(insn);
        List<LabelNode> labels = getLabels(insn);

        for(LabelNode label : labels) {
            AbstractInsnNode tempInsn = label.getNext();

            while(!(tempInsn instanceof LabelNode)) {
                boolean isJump = (tempInsn.getOpcode() >= Opcodes.IFEQ && tempInsn.getOpcode() <= Opcodes.IF_ACMPNE)
                                || tempInsn.getOpcode() == Opcodes.IFNULL || tempInsn.getOpcode() == Opcodes.IFNONNULL;

                if(isJump) {
                    bannedInsn.add(tempInsn);
                    break;
                }

                tempInsn = tempInsn.getNext();
            }
        }

        AbstractInsnNode tempInsn = dflt.getNext();
        while(!(tempInsn instanceof LabelNode)) {
            boolean isSwitch = tempInsn.getOpcode() == Opcodes.TABLESWITCH || tempInsn.getOpcode() == Opcodes.LOOKUPSWITCH;

            if(isSwitch) {
                bannedInsn.add(tempInsn);
                break;
            }
            
            tempInsn = tempInsn.getNext();
        }        
    }

    private LabelNode getDefaultLabel(AbstractInsnNode insn) {
        if(insn instanceof TableSwitchInsnNode tableSwitchInsn) {
            return tableSwitchInsn.dflt;
        }
        
        if(insn instanceof LookupSwitchInsnNode lookupSwitchInsn) {
            return lookupSwitchInsn.dflt;
        }

        return new LabelNode();
    }

    private List<LabelNode> getLabels(AbstractInsnNode insn) {
        if(insn instanceof TableSwitchInsnNode tableSwitchInsn) {
            return tableSwitchInsn.labels;
        }
        
        if(insn instanceof LookupSwitchInsnNode lookupSwitchInsn) {
            return lookupSwitchInsn.labels;
        }
        return null;
    }
}
