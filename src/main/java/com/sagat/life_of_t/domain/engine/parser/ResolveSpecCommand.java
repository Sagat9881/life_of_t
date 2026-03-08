package com.sagat.life_of_t.domain.engine.parser;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * CLI tool to resolve a visual spec and output flat XML.
 * <p>
 * Usage: {@code java ResolveSpecCommand <asset-specs-root> <spec-file> [output-file]}
 * <p>
 * If output-file is omitted, writes to stdout.
 * Example:
 * <pre>
 *   java ResolveSpecCommand asset-specs \
 *       asset-specs/characters/tanya/visual-specs.xml \
 *       resolved-tanya.xml
 * </pre>
 */
public class ResolveSpecCommand {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println(
                    "Usage: ResolveSpecCommand <asset-specs-root> <spec-file> [output-file]");
            System.exit(1);
        }

        Path root = Path.of(args[0]);
        Path specFile = Path.of(args[1]);

        VisualSpecResolver resolver = new VisualSpecResolver(root);

        OutputStream out = args.length >= 3
                ? new FileOutputStream(args[2])
                : System.out;

        try {
            resolver.resolveAndWrite(specFile, out);
        } finally {
            if (out != System.out) out.close();
        }
    }
}
