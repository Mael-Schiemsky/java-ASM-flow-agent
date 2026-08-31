# java-ASM-flow-agent
A Java agent that captures the control flow and the data flow of an application.
 
# compile agent first
```mvn
mvn clean package
```
We obtain in `target/` the asm-agent.jar agent.
 
```shell
# output in the terminal
java -javaagent:<java-agent-path.jar>=target="<my/package/path>",out="<output/trace/directory>" -jar <targer-java-jar> "<parameters>"
```
```shell
# output in a target file
java -javaagent:<java-agent-path.jar>=target="<my/package/path>",out="<output/trace/directory>" -jar <targer-java-jar> "<parameters>" > <path/to/target/file>
```
 
`<my/package/path>` -> the package path of the app you want to instrument.
 
After the `-jar` parameter please add the java paramter of the `jar` you want to instrument.
 
The trace output is only visible in the terminal.
 
# example
 
see an example of use in project `app_test_for_java_asm_agent` [link](GitHub - Mael-Schiemsky/app_test_for_java_asm_agent).

# read the output

use the Vscode extension 'ANSI Colors' to visualize the output trace with colors.
for this, the file containing the trace must be an .ansi file.

# Example 

see an example of use in project `app_test_for_java_asm_agent` [link](https://github.com/Mael-Schiemsky/app_test_for_java_asm_agent).


# How to read an output trace

Outputs trace are difficult to read. Although it depict a sequence of event, the lack of indentation can hide the structural aspect of the trace. This section gives some hints on how to read them. 

```
Agent loaded at startup
[EVENT-TYPE]:<METHOD-IDENFICATION-OR-DATA>
<DATA-VALUE>
<DATA-VALUES>
...
```

`[EVENT-TYPE]` are either 
- `[ENTER]`
- `[EXIT]`
- `[RETURN]`
- `[JUMP]` 
- `[SWITCH]`
- or `[PARAM]`. 

`[ENTER]` and `[EXIT]` are associated with a method identifier, a.k.a., fully qualify name (FQN).
`[JUMP]` and `[PARAM]` are associated with some specific data.

`[ENTER]` and `[EXIT]` tags are triggered when entering and exiting a method. Assume that anything printed after the last `[ENTER]` happen inside this method. Thus, for instance in the following trace, both `JUMP` instructions below to the last method entered, here`[ENTER] method: org/example/App#main([Ljava/lang/String;)V`. 

```
Agent loaded at startup
[ENTER] method: org/example/App#main([Ljava/lang/String;)V
[PARAM] type: [Ljava/lang/String;
[Ljava.lang.String;@1de0aca6
[ENTER] method: org/example/classes/model/PlusOne#<init>()V
[RETURN] value:
void
[EXIT] method: org/example/classes/model/PlusOne#<init>()V
[RETURN] value:
void
[JUMP] line: 27 
1
[JUMP] line: 31
0
```

Within `[ENTER] method: org/example/classes/model/PlusOne#increment()I`, `org/example/classes/model/PlusOne#increment()` is the FQN, while `I` refers to the return type of `increment()`.

In another example : `org/example/App#main([Ljava/lang/String;)V` the `main()` parameter is display as `Ljava/lang/String;`; the `L` refers to the type of this parameter ((see ASM documentation for the full list)[https://asm.ow2.io/asm4-guide.pdf]). Here `L` is an object type follows by its signature. 

`[PARAM]` usually follows an `[ENTER]` with parameters. In this example `[Ljava.lang.String;@1de0aca6` is linked to the parameter of `main()` and we print its memory reference `@1de0aca6`. 


*Jumps* are a type of JavaBytteCode (JBC) conditional instructions. Specific jumps are use in to compile any Java conditional structure (e.g., `if`, `switch`, `while`, `for`, etc). However, on encountering a *jump* during the JBC execution, we cannot determine which java structure it represents ([see the different JBC structure here](https://en.wikipedia.org/wiki/List_of_JVM_bytecode_instructions)). We choose to display the Java Line of Code (LoC) associated to where a jump occurs. By referring to the LoC and the last `[ENTER]` method, developer can retrieve the location of which java structure is linked to this jump. In a trace, this result in a set of at least two lines for each jump: 

```shell
[JUMP] line: X  # a java conditional structure occurs at line X
1 # this was the original parameter of this java conditional structure (can be object, string, int, bool, etc.)
```
Upon multiple parameters, each parameter occurs in a different line: 

```shell
[JUMP] line: 14
0 # value of paramater 1
4 # value of paramter 2
```

Below each [JUMP], we display the conditional values of each parameter requires by the original java structure. 
Multiple jumps can refer to the same line number, for instance `if(A > B && B>C)` is compiled as two different [JUMP], both referencing the LoC of the java `if`. 

`[RETURN]` tag are triggers when a method exits. Whether a method has an explicit `return` or not, we print the `return` value of the method, and we print `void` if no `return`.  

Finally, `[SWITCH]` represent `switch/case` java structure. 

First, discard the following information that only refers to the type of switch implemented: 
`switch_insn: 170` -> refers to `tableSwitch`
`switch_insn: 171` -> refers to `lookupSwitch`

```java
public static void tableSwitchInt() {
    int i = 3;
    switch (i) {
        case 1 -> System.out.println("[\u001B[3m"+ "APP" + "\u001B[0m] TABLESWITCH case : 1");
        case 2 -> System.out.println("[\u001B[3m"+ "APP" + "\u001B[0m] TABLESWITCH case : 2");
        case 3 -> System.out.println("[\u001B[3m"+ "APP" + "\u001B[0m] TABLESWITCH case : 3");
        default -> System.out.println("[\u001B[3m"+ "APP" + "\u001B[0m] TABLESWITCH default case");
    }
}
```

```shell
[ENTER] method: org/example/classes/example/SwitchCaseExample#tableSwitchInt()V
[SWITCH] switch_insn: 170, line: 6 #case 3
3 #value of parameter i 
[SWITCH] switch_insn: 170, line: 7 #default
3 #value of parameter i 
[EXIT] method: org/example/classes/example/SwitchCaseExample#tableSwitchInt()V
```
