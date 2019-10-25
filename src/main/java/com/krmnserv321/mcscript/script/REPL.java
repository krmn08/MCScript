package com.krmnserv321.mcscript.script;

import com.krmnserv321.mcscript.script.eval.Environment;
import com.krmnserv321.mcscript.script.eval.ScriptError;
import com.krmnserv321.mcscript.script.eval.PublicEnvironment;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

public class REPL {
    private static final String PROMPT = ">> ";

    public void start(ClassLoader loader, InputStream in, PrintStream out, PrintStream err) {
        Scanner scanner = new Scanner(in);
        PublicEnvironment publicEnvironment = new PublicEnvironment(loader);
        Environment environment = new Environment(publicEnvironment);
        System.out.println("enter /exit to exit");
        while (true) {
            out.print(PROMPT);
            String line = scanner.nextLine();
            if (line.equals("/exit")) {
                break;
            }

            Parser parser = new Parser(new Lexer(line));
            Program program = parser.parseProgram();

            List<String> errors = parser.getErrors();

            if (!errors.isEmpty()) {
                err.println("parser errors:");
                errors.forEach(err::println);
                continue;
            }

            Object o = program.eval(environment);

            if (o instanceof ScriptError) {
                err.println(o);
            } else {
                out.println("result: " + o);
            }
        }
    }
}
