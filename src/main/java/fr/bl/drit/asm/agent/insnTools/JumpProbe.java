package fr.bl.drit.asm.agent.insnTools;

import java.util.ArrayList;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;

public class JumpProbe {

    public static InsnList jumpInsnAnalyse(AbstractInsnNode insn, ArrayList<LabelNode> visitedLabels) {
        LabelNode targetLabel = getTargetLabelFromInsn(insn);

        if(visitedLabels.contains(targetLabel)){
            return doWhileInstrumentation();
        }
        
        AbstractInsnNode targetInsn = insn.getNext();
        ArrayList<LabelNode> insideTheJumpLabels = new ArrayList<>();
        while(true){
            System.out.println("targetInsn: " + targetInsn);
            if(targetInsn instanceof LabelNode label){
                if(label == targetLabel) break;
                insideTheJumpLabels.add(label);
            }
            targetInsn = targetInsn.getNext();
        }

        AbstractInsnNode previousInsn = targetInsn.getPrevious();
        if(previousInsn instanceof JumpInsnNode && previousInsn.getOpcode() == Opcodes.GOTO){
            LabelNode gotoTargetLabel = getTargetLabelFromInsn(previousInsn);
            if(visitedLabels.contains(gotoTargetLabel)){
                return whileInstrumentation();
            } else {
                if(insideTheJumpLabels.contains(gotoTargetLabel)){
                    return ifInstrumentation();
                } else {
                    return ifElseInstrumentation();
                }
            }
        } else {
            return ifInstrumentation();
        }
    }

    private static LabelNode getTargetLabelFromInsn(AbstractInsnNode insn) {
        if (insn instanceof JumpInsnNode jump) {
            return jump.label;
        }
        return null;
    }

    private static InsnList ifInstrumentation(){
        System.out.println("I see a if block");
        return null;
    }

    private static InsnList ifElseInstrumentation(){
        System.out.println("I see a if else block");
        return null;
    }

    private static InsnList whileInstrumentation(){
        System.out.println("I see a while block");
        return null;
    }

    private static InsnList doWhileInstrumentation(){
        System.out.println("I see a do while block");
        return null;
    }

}
