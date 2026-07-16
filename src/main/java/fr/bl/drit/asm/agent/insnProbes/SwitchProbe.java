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
import java.util.Arrays;
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

    public ArrayList<AbstractInsnNode> getSwitchInsnAnalyse(AbstractInsnNode insn, ArrayList<AbstractInsnNode> insnList) {
        AbstractInsnNode prevInsn = searchPreviousInsn(insn);

        if(isStringHashCodeInsn(prevInsn)) {
            switchStringProbe(insn, prevInsn, insnList);
        } else if(prevInsn.getOpcode() == Opcodes.IALOAD) {
            prevInsn = searchPreviousInsn(prevInsn);
            if(prevInsn instanceof MethodInsnNode methodInsn
                && methodInsn.getOpcode() == Opcodes.INVOKEVIRTUAL
                && methodInsn.name.equals("ordinal")
                && methodInsn.desc.equals("()I")) {
                switchEnumProbe(prevInsn, insnList);
            }
        } else {
            switchIntProbe(insn, insnList);
        }

        return insnList;    
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

    private void switchStringProbe(AbstractInsnNode insn, AbstractInsnNode prevInsn, ArrayList<AbstractInsnNode> insnList) {
        InsnList switchProbe = new InsnList();

        switchProbe.add(getCorrespondingLineNumber(insn));
        addBannedInsnFromSwitchString(insn);
        switchProbe.add(searchPreviousInsn(prevInsn).clone(null));
        switchProbe.add(treatMessage(""));

        insnList.addAll(insnList.indexOf(insn), Arrays.asList(switchProbe.toArray()));
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

    private void switchEnumProbe(AbstractInsnNode prevInsn, ArrayList<AbstractInsnNode> insnList) {
        InsnList switchProbe = new InsnList();

        switchProbe.add(getCorrespondingLineNumber(prevInsn));
        switchProbe.add(new InsnNode(Opcodes.DUP));
        switchProbe.add(new MethodInsnNode(
                            Opcodes.INVOKEVIRTUAL,
                            "java/lang/Enum",
                            "toString",
                            "()Ljava/lang/String;",
                            false));
        switchProbe.add(treatMessage(""));

        insnList.addAll(insnList.indexOf(prevInsn), Arrays.asList(switchProbe.toArray()));
    }

    private void switchIntProbe(AbstractInsnNode insn, ArrayList<AbstractInsnNode> insnList) {
        InsnList switchProbe = new InsnList();

        switchProbe.add(getCorrespondingLineNumber(insn));
        switchProbe.add(new InsnNode(Opcodes.DUP));
        switchProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;", false));
        switchProbe.add(treatMessage(""));

        insnList.addAll(insnList.indexOf(insn), Arrays.asList(switchProbe.toArray()));
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
}
