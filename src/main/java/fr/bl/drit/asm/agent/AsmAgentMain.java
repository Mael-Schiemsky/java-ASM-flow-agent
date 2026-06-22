package fr.bl.drit.asm.agent;

import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import fr.bl.drit.asm.agent.classInjector.MyClassNode;

public class AsmAgentMain {

    public static void premain(String agentArgs, Instrumentation inst) {
        init(agentArgs, inst);
        System.out.println("Agent loaded at startup");
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        init(agentArgs, inst);
        System.out.println("Agent attached dynamically");        
    }

    private static void init(String agentArgs, Instrumentation inst) {
        
        final Map<String, String> args = parseArgs(agentArgs);
        final String target = args.get("target");
        final String outputPath = args.get("out");

        if (target.isEmpty()) {
            System.err.println(
                "[flow-agent] No 'target' provided in agent arguments; instrumenter will be disabled.");
            printUsage(System.err);
            return;
        }

        if (outputPath == null) {
            System.err.println(
                "[flow-agent] No 'out' provided in agent arguments; instrumenter will be disabled.");
            printUsage(System.err);
            return;
        }

        addTransformer(inst, target, outputPath);
    }

    private static void addTransformer(Instrumentation inst, String target, String outputPath) {
        inst.addTransformer(new java.lang.instrument.ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String name, Class<?> c, java.security.ProtectionDomain d, byte[] b) {
                if (name.contains(target)) {
                    ClassNode cn = new MyClassNode();

                    ClassReader reader = new ClassReader(b);
                    reader.accept(cn, 0);

                    ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
                    cn.accept(writer);

                    return writer.toByteArray();
                }
                return null;
            }
        }, true);
    }

    private static Map<String, String> parseArgs(String args) {
        Map<String, String> map = new HashMap<>();

        if (args == null || args.trim().isEmpty()) {
            return map;
        }
        String[] pairs = args.split(",");
        for (String pair : pairs) {
            String trimmed = pair.trim();
            if (trimmed.isEmpty()) continue;

            String[] kv = trimmed.split("=", 2);
            String key = kv[0].trim();
            String value = kv[1].trim();

            if (key.isEmpty() || value.isEmpty()) {
                System.err.println("[flow-agent] Ignoring empty key/value in agent argument: " + trimmed);
                continue;
            }
            map.put(key, value);
        }
        return map;
  }

  private static void printUsage(PrintStream out) {
    out.println(
        "[ASM-agent] Usage: target=<prefix[+prefix...]>,out=<dir>");
    out.println("  target   : '+'-separated list of class name prefixes to instrument (required)");
    out.println("  out      : output directory for flow files and method ID mapping (required)");
  }
}
