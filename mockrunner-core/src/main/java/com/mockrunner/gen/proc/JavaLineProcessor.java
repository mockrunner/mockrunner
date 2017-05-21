package com.mockrunner.gen.proc;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import com.mockrunner.gen.proc.JavaLineParser.Block;
import com.mockrunner.gen.proc.JavaLineParser.Line;

public class JavaLineProcessor
{
    private JavaLineParser parser;

    public JavaLineProcessor()
    {
        parser = new JavaLineParser();
    }

    public void addLine(String line)
    {
        parser.addLine(line);
    }

    public void addBlock(String block)
    {
        parser.addBlock(block);
    }

    public void addLines(List<String> lines)
    {
        parser.addLines(lines);
    }

    public void addBlocks(List<String> blocks)
    {
        parser.addBlocks(blocks);
    }

    public String process(String source)
    {
        List<Line> result = parser.parse(source);
        LineNumberReader input = new LineNumberReader(new StringReader(source));
        StringWriter resultStringWriter = new StringWriter(source.length() + 100);
        PrintWriter output = new PrintWriter(resultStringWriter);
        for (Object aResult : result) {
            Line nextLine = (Line) aResult;
            int processUntil = nextLine.getLineNumber();
            dumpToOutputUntil(input, output, processUntil);
            handleLine(nextLine, input, output);
        }
        dumpToOutputUntilEnd(input, output);
        output.flush();
        return resultStringWriter.toString();
    }

    private void handleLine(Line line, LineNumberReader input, PrintWriter output)
    {
        try
        {
            StringBuffer nextLine = new StringBuffer(input.readLine());
            int firstNonWhitespace = 0;
            while(firstNonWhitespace < nextLine.length() && Character.isWhitespace(nextLine.charAt(firstNonWhitespace)))
            {
                firstNonWhitespace++;
            }
            if(firstNonWhitespace < nextLine.length())
            {
                if(line instanceof Block)
                {
                    nextLine.insert(firstNonWhitespace, "/*");
                    output.println(nextLine);
                    int processUntil = ((Block)line).getEndLineNumber();
                    dumpToOutputUntil(input, output, processUntil);
                    nextLine = new StringBuffer(input.readLine());
                    int lastNonWhitespace = nextLine.length() - 1;
                    while(lastNonWhitespace > 0 && Character.isWhitespace(nextLine.charAt(lastNonWhitespace)))
                    {
                        lastNonWhitespace--;
                    }
                    nextLine.insert(lastNonWhitespace + 1, "*/");
                    output.println(nextLine);
                }
                else
                {
                    nextLine.insert(firstNonWhitespace, "//");
                    output.println(nextLine);
                }
            }
        }
        catch(IOException exc)
        {
            throw new RuntimeException(exc.getMessage());
        }
    }

    private void dumpToOutputUntil(LineNumberReader input, PrintWriter output, int processUntil)
    {
        while(input.getLineNumber() < processUntil - 1)
        {
            try
            {
                output.println(input.readLine());
            }
            catch(IOException exc)
            {
                throw new RuntimeException(exc.getMessage());
            }
        }
    }

    private void dumpToOutputUntilEnd(LineNumberReader input, PrintWriter output)
    {
        String line = null;
        try
        {
            while(null != (line = input.readLine()))
            {
                output.println(line);
            }
        }
        catch (IOException exc)
        {
            throw new RuntimeException(exc.getMessage());
        }
    }
}
