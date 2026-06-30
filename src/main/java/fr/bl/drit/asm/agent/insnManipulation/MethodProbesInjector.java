package fr.bl.drit.asm.agent.insnManipulation;

import java.util.ArrayList;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.Type;

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

        getParameters();
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
                jumpInsnAnalyse(insn, visitedLabels);
                continue;
            }

            if(isSwitch){
                continue;
            }

            if(isReturn || isThrow) {
                InsnList exitProbe = new InsnList();
                exitProbe.add(onExit());
                exitProbe.add(getReturnValue(opcode));
                instructions.insertBefore(insn, exitProbe);
                continue;
            }
        }

        accept(mv);
    }

    private void onEnter() {
        InsnList enterProbe = buildPrintln("e:enter, method:" + fullyQualifiedName);
        instructions.insertBefore(instructions.getFirst(), enterProbe);
    }

    private void getParameters() {
        InsnList paramProbe = new InsnList();
        Type[] parameterTypes = Type.getArgumentTypes(desc);

        int localIndex = (access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;

        for (Type param : parameterTypes) {
            String message = "p:param, type:" + param + ", value:";

            paramProbe.add(buildPrintln(message));

            paramProbe.add(new VarInsnNode(
                    param.getOpcode(Opcodes.ILOAD),
                    localIndex));

            paramProbe.add(transformVarToString(param.getSort()));

            paramProbe.add(buildPrintln(""));

            localIndex += param.getSize();
        }

        instructions.insertBefore(instructions.getFirst(), paramProbe);
    }

    private InsnList transformVarToString(int sort) {
        InsnList toStringInsn = new InsnList();

        switch (sort) {
            case Type.BOOLEAN ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "toString", "(Z)Ljava/lang/String;", false));
            case Type.BYTE ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Byte", "toString", "(B)Ljava/lang/String;", false));
            case Type.CHAR ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Character", "toString", "(C)Ljava/lang/String;", false));
            case Type.SHORT ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Short", "toString", "(S)Ljava/lang/String;", false));
            case Type.INT ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;", false));
            case Type.FLOAT ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float", "toString", "(F)Ljava/lang/String;", false));
            case Type.LONG ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long", "toString", "(J)Ljava/lang/String;", false));
            case Type.DOUBLE ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;", false));
            case Type.ARRAY,
                 Type.OBJECT,
                 Type.METHOD ->
                toStringInsn.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;", false));
            case Type.VOID ->
                toStringInsn.add(buildPrintln("void"));
            }

        return toStringInsn;
    }

    private void jumpInsnAnalyse(AbstractInsnNode insn, ArrayList<LabelNode> visitedLabels) {
        LabelNode targetLabel = getTargetLabelFromInsn(insn);

        if(visitedLabels.contains(targetLabel)){
            doWhileInstrumentation();
            return;
        }
        
        AbstractInsnNode targetInsn = insn.getNext();
        ArrayList<LabelNode> insideTheJumpLabels = new ArrayList<>();
        while(true){
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
                whileInstrumentation();
            } else {
                if(insideTheJumpLabels.contains(gotoTargetLabel)){
                    ifInstrumentation();
                } else {
                    ifElseInstrumentation();
                }
            }
        } else {
            ifInstrumentation();
        }
    }

    private LabelNode getTargetLabelFromInsn(AbstractInsnNode insn) {
        if (insn instanceof JumpInsnNode jump) {
            return jump.label;
        }
        return null;
    }

    private void ifInstrumentation(){
        System.out.println("I see a if block");
    }

    private void ifElseInstrumentation(){
        System.out.println("I see a if else block");
    }

    private void whileInstrumentation(){
        System.out.println("I see a while block");
    }

    private void doWhileInstrumentation(){
        System.out.println("I see a do while block");
    }

    private InsnList onExit() {
        return buildPrintln("e:exit, method:" + fullyQualifiedName);
    }

    private InsnList getReturnValue(int opcode){
        InsnList returnProbe = new InsnList();

        if (opcode == Opcodes.IRETURN || opcode == Opcodes.FRETURN) {
            returnProbe.add(new InsnNode(Opcodes.DUP));
        } else if (opcode == Opcodes.LRETURN || opcode == Opcodes.DRETURN) {
            returnProbe.add(new InsnNode(Opcodes.DUP2));
        }

        switch (opcode) {
            case Opcodes.IRETURN ->
                returnProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;", false));
            case Opcodes.FRETURN ->
                returnProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float", "toString", "(F)Ljava/lang/String;", false));
            case Opcodes.LRETURN ->
                returnProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Long", "toString", "(J)Ljava/lang/String;", false));
            case Opcodes.DRETURN ->
                returnProbe.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "toString", "(D)Ljava/lang/String;", false));
            case Opcodes.ARETURN,
                 Opcodes.RETURN,
                 Opcodes.ATHROW ->
                returnProbe.add(buildPrintln("void"));
        }

        if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.DRETURN) {
            returnProbe.add(buildPrintln(""));
        }
        
        return returnProbe;
    }

    private InsnList buildPrintln(String message) {
        InsnList instructions = new InsnList();

        if (!message.isEmpty()) {
            instructions.add(new LdcInsnNode(message));
        }

        instructions.add(new MethodInsnNode(
            Opcodes.INVOKESTATIC,
            "fr/bl/drit/asm/agent/transformer/MyTransformer",
            "jePrint",
            "(Ljava/lang/String;)V",
            false));

        return instructions;
    }
}
