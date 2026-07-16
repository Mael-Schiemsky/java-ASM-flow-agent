package fr.bl.drit.asm.agent.insnProbes;

import java.util.ArrayList;

import org.objectweb.asm.tree.AbstractInsnNode;

public class BanProb {

    private ArrayList<AbstractInsnNode> banedInsn = new ArrayList<>();

    public boolean isBanedInsn(AbstractInsnNode insn) {
        return banedInsn.contains(insn);
    }

    public ArrayList<AbstractInsnNode> getBanedInsn(){
        return banedInsn;
    }

    public void removeBanedInsn(AbstractInsnNode insn) {
        banedInsn.remove(insn);
    }
    
    public void addBanedInsn(AbstractInsnNode insn) {
        banedInsn.add(insn);
    }
}
