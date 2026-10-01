import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.SourceVersion;

import static java.nio.charset.StandardCharsets.ISO_8859_1;

/**
 * NewMod renames the template to the mod_* entries in gradle.properties, then deletes itself.
 * Run it once in the repo root with java NewMod.java. Git undoes it.
 */
public class NewMod {
    public static void main(String[] args) throws IOException {
        Properties mod = new Properties();
        try (var in = Files.newInputStream(Path.of("gradle.properties"))) {
            mod.load(in);
        }
        String id = mod.getProperty("mod_id");
        String name = mod.getProperty("mod_name");
        String pkg = mod.getProperty("mod_group") + "." + id;
        if (id.equals("examplemod") || name.equals("Example Mod")) fail("Set the mod_* entries in gradle.properties first.");
        if (!id.matches("[a-z][a-z0-9_]{1,63}")) fail("mod_id takes 2 to 64 lowercase letters, digits and _, and starts with a letter.");
        if (!SourceVersion.isName(pkg)) fail(pkg + " is not a Java package. Check mod_group.");
        // "Better HUD" names the classes BetterHUD, BetterHUDFabric and so on. A name that makes no class name gives way to the id.
        String type = pascal(name.matches("[A-Za-z][ -~]*") ? name : id);

        Pattern template = Pattern.compile("com([./])example\\1examplemod|ExampleMod|examplemod");
        UnaryOperator<String> rename = text -> template.matcher(text).replaceAll(match -> Matcher.quoteReplacement(
                match.group().equals("ExampleMod") ? type
                : match.group().equals("examplemod") ? id
                : pkg.replace(".", match.group(1))));

        List<Path> files = new ArrayList<>(List.of(Path.of("README.md")));
        for (String module : List.of("common", "fabric", "neoforge", "forge")) {
            Path src = Path.of(module, "src");
            if (!Files.isDirectory(src)) continue;
            try (var tree = Files.walk(src)) {
                tree.filter(Files::isRegularFile).forEach(files::add);
            }
        }
        for (Path file : files) {
            // Latin-1 keeps every byte, so UTF-8 text and binary files come through unharmed.
            String text = new String(Files.readAllBytes(file), ISO_8859_1);
            if (file.endsWith("README.md")) text = text.replaceAll("(?ms)^## New mod$.*?(?=^## )", "");
            Path target = Path.of(rename.apply(file.toString().replace(File.separatorChar, '/')));
            Files.createDirectories(target.toAbsolutePath().getParent());
            Files.write(target, rename.apply(text).getBytes(ISO_8859_1));
            if (target.equals(file)) continue;
            Files.delete(file);
            // The folders that the move emptied go too.
            for (File folder = file.toFile().getParentFile(); folder.delete(); folder = folder.getParentFile()) {
            }
        }
        Path license = Path.of("LICENSE");
        Files.writeString(license, Files.readString(license).replace("SiftMC", mod.getProperty("mod_author")));
        Files.delete(Path.of("NewMod.java"));
        System.out.println("The package is " + pkg + " and the classes are " + type + ", " + type + "Fabric, " + type
                + "NeoForge and " + type + "Forge. Check git diff, then reload Gradle in the IDE.");
    }

    private static String pascal(String words) {
        StringBuilder name = new StringBuilder();
        for (String word : words.split("[^A-Za-z0-9]+")) {
            name.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return name.toString();
    }

    private static void fail(String message) {
        System.err.println(message);
        System.exit(1);
    }
}
