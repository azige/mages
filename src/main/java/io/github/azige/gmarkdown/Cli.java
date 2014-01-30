/*
 * Copyright 2014 Azige.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.azige.gmarkdown;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.*;

/**
 *
 * @author Azige
 */
public class Cli{

    public static void main(String[] args){

        Options options = new Options()
            .addOption("h", "help", false, "print this message")
            .addOption(OptionBuilder
                .hasArg()
                .withArgName("bundle")
                .withDescription("set the ResourceBundle")
                .create('r')
            )
            .addOption(OptionBuilder
                .hasArg()
                .withArgName("locale")
                .withDescription("set the locale")
                .create('l'))
            .addOption(OptionBuilder
                .hasArg()
                .withArgName("template")
                .withDescription("set the template")
                .create('t'))
            .addOption(OptionBuilder
            .hasArg()
            .withArgName("plugin dir")
            .withDescription("set the directory to load plugins")
            .create('p'));
        try{
            CommandLineParser parser = new BasicParser();
            CommandLine cl = parser.parse(options, args);

            if (cl.hasOption('h')){
                printHelp(System.out, options);
                return;
            }

            String[] fileArgs = cl.getArgs();
            if (fileArgs.length < 1){
                throw new ParseException("Missing input files.");
            }
            GMarkdownBuilder builder = new GMarkdownBuilder();
            if (cl.hasOption('t')){
                String template = cl.getOptionValue('t');
                try (Reader in = new BufferedReader(new InputStreamReader(new FileInputStream(template), "UTF-8"))){
                    builder.addPostFilter(new TemplateFilter(Util.readAll(in)));
                }
            }else{
                builder.addPostFilter(new TemplateFilter());
            }

            String resource = cl.getOptionValue('r');
            if (resource != null){
                System.setProperty("strings.resource", resource);
                String locale = cl.getOptionValue('l');
                if (locale != null){
                    System.setProperty("strings.locale", locale);
                }
            }
            builder.addPlugin(Util.loadPlugin("io.github.azige.gmarkdown.Strings"));

            File pluginDir;
            if (cl.hasOption('p')){
                pluginDir = new File(cl.getOptionValue('p'));
            }else{
                pluginDir = new File("./plugin");
            }

            for (Plugin p : Util.loadPluginsFromDirectory(pluginDir)){
                builder.addPlugin(p);
            }

            List<File> fileList = new LinkedList<>();
            for (String fileArg : fileArgs){
                if (fileArg.contains("*")){
                    fileArg = fileArg.replaceAll("\\.", "\\.").replaceAll("\\*", ".*");
                    File parent = new File(fileArg).getParentFile();
                    if (parent == null){
                        parent = new File(".");
                    }
                    final Pattern p = Pattern.compile(new File(fileArg).getName());
                    FileFilter filter = new FileFilter(){

                        @Override
                        public boolean accept(File pathname){
                            return p.matcher(pathname.getName()).matches();
                        }
                    };
                    File[] files = parent.listFiles(filter);
                    if (files != null){
                        fileList.addAll(Arrays.asList(files));
                    }
                }else{
                    fileList.add(new File(fileArg));
                }
            }

            GMarkdown gm = builder.build();

            for (File f : fileList){
                String result;
                try (Reader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"))){
                    result = gm.process(Util.readAll(in));
                }
                if (f.getName().contains(".")){
                    Matcher matcher = Pattern.compile(".+\\.").matcher(f.getName());
                    matcher.find();
                    f = new File(f.getParent(), matcher.group() + "html");
                }else{
                    f = new File(f.getParent(), f.getName() + ".html");
                }
                try (Writer output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"))){
                    output.write(result);
                }
            }
        }catch (ParseException ex){
            System.err.println(ex.getMessage());
            printHelp(System.err, options);
        }catch (IOException ex){
            System.err.println(ex);
        }
    }

    static void printHelp(PrintStream out, Options options){
        HelpFormatter hf = new HelpFormatter();
        PrintWriter pw = new PrintWriter(out);
        hf.printHelp(pw, hf.getWidth(), "gmarkdown [-r <bundle> [-l <locale>]] [-t <template>] [-p <plugin dir> <input files>",
            "Convert input files.", options, hf.getLeftPadding(), hf.getDescPadding(), null);
        pw.flush();
    }
}
