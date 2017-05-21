package com.mockrunner.gen.proc;

import java.util.List;

public class JavaLineAssembler
{
    private StringBuffer buffer;
    private String newLine;
    private int indentLevel;
    private String defaultMethodModifier;

    public JavaLineAssembler()
    {
        reset();
        newLine = System.getProperty("line.separator");
    }

    public void reset()
    {
        buffer = new StringBuffer();
        indentLevel = 0;
        defaultMethodModifier = "";
    }

    public String getResult()
    {
        return buffer.toString();
    }

    public StringBuffer getResultAsBuffer()
    {
        return new StringBuffer(getResult());
    }

    public void setIndentLevel(int indentLevel)
    {
        this.indentLevel = indentLevel;
    }

    public void setDefaultMethodModifier(String defaultMethodModifier)
    {
        this.defaultMethodModifier = defaultMethodModifier;
    }

    public void appendBlank()
    {
        buffer.append(" ");
    }

    public void appendBlank(int number)
    {
        for(int ii = 0; ii < number; ii++)
        {
            appendBlank();
        }
    }

    public void appendNewLine()
    {
        buffer.append(newLine);
    }

    public void appendNewLine(int number)
    {
        for(int ii = 0; ii < number; ii++)
        {
            appendNewLine();
        }
    }

    public void appendLeftBrace()
    {
        buffer.append("{");
    }

    public void appendRightBrace()
    {
        buffer.append("}");
    }

    public void appendLeftParenthesis()
    {
        buffer.append("(");
    }

    public void appendRightParenthesis()
    {
        buffer.append(")");
    }

    public void appendComma()
    {
        buffer.append(",");
    }

    public void appendIndent()
    {
        appendBlank(indentLevel * 4);
    }

    public void appendLine(String line)
    {
        if(null != line && line.length() > 0)
        {
            appendIndent();
            buffer.append(line);
            appendNewLine();
        }
    }

    public void appendCodeLines(String[] lines)
    {
        if(null == lines || lines.length <= 0)
        {
            appendNewLine();
        }
        else
        {
            for (String line : lines) {
                appendLine(line);
            }
        }
    }

    public void appendPackageInfo(String packageName)
    {
        if(null != packageName && packageName.length() > 0)
        {
            appendLine("package " + packageName + ";");
            appendNewLine();
        }
    }

    public void appendImport(String importLine)
    {
        appendLine("import " + importLine + ";");
    }

    public void appendImports(List<String> imports)
    {
        if(null == imports) return;
        for (String anImport : imports) {
            appendImport(anImport);
        }
    }

    public void appendClassDefintion(String className)
    {
        appendClassDefintion(className, "");
    }

    public void appendClassDefintion(String className, String superClass)
    {
        appendClassDefintion(className, "", superClass, null);
    }

    public void appendClassDefintion(String className, String[] interfaceDef)
    {
        appendClassDefintion(className, "", "", interfaceDef);
    }

    public void appendClassDefintion(String className, String modifier, String superClass, String[] interfaceDef)
    {
        if(null == className || className.length() <= 0) return;
        if(null == superClass || superClass.length() <= 0)
        {
            superClass = "";
        }
        else
        {
            superClass = " extends " + superClass;
        }
        String interfaceDefList = "";
        if(null != interfaceDef && interfaceDef.length > 0)
        {
            interfaceDefList = " implements " + prepareCommaSeparatedList(interfaceDef, null);
        }
        if(null == modifier)
        {
            modifier = "";
        }
        if(modifier.length() > 0)
        {
            modifier += " ";
        }
        appendLine("public " + modifier + "class " + className + superClass + interfaceDefList);
    }

    public void appendMemberDeclaration(String type, String name)
    {
        if(null == type || type.length() <= 0) return;
        if(null == name || name.length() <= 0) return;
        appendLine("private " + type + " " + name + ";");
    }

    public void appendConstructorDeclaration(String name)
    {
        appendConstructorDeclaration(name, null, null);
    }

    public void appendConstructorDeclaration(String name, String[] parameterTypes, String[] parameterNames)
    {
        appendConstructorDeclaration(name, parameterTypes, parameterNames, null);
    }

    public void appendConstructorDeclaration(String name, String[] parameterTypes, String[] parameterNames, String[] exceptions)
    {
        if(null == name || name.length() <= 0) return;
        StringBuffer buffer = new StringBuffer(30);
        buffer.append("public ");
        appendSignature(name, parameterTypes, parameterNames, exceptions, buffer);
        appendLine(buffer.toString());
    }

    public void appendMethodDeclaration(String name)
    {
        appendMethodDeclaration("void", name);
    }

    public void appendMethodDeclaration(String returnType, String name)
    {
        appendMethodDeclaration(returnType, name, null, null);
    }

    public void appendMethodDeclaration(String returnType, String name, String[] parameterTypes, String[] parameterNames)
    {
        appendMethodDeclaration(null, returnType, name, parameterTypes, parameterNames);
    }

    public void appendMethodDeclaration(String[] modifiers, String returnType, String name, String[] parameterTypes, String[] parameterNames)
    {
        appendMethodDeclaration(modifiers, returnType, name, parameterTypes, parameterNames, null);
    }

    public void appendMethodDeclaration(String[] modifiers, String returnType, String name, String[] parameterTypes, String[] parameterNames, String[] exceptions)
    {
        if(null == name || name.length() <= 0) return;
        if(null == returnType || returnType.length() <= 0)
        {
            returnType = "void";
        }
        StringBuffer buffer = new StringBuffer(30);
        if(null != defaultMethodModifier && defaultMethodModifier.length() > 0)
        {
            buffer.append(defaultMethodModifier).append(" ");
        }
        buffer.append(prepareModifierList(modifiers));
        buffer.append(returnType).append(" ");
        appendSignature(name, parameterTypes, parameterNames, exceptions, buffer);
        appendLine(buffer.toString());
    }

    private void appendSignature(String name, String[] parameterTypes, String[] parameterNames, String[] exceptions, StringBuffer buffer)
    {
        buffer.append(name);
        buffer.append("(");
        buffer.append(prepareCommaSeparatedList(parameterTypes, getParameterNameList(parameterTypes, parameterNames)));
        buffer.append(")");
        appendThrowsClause(exceptions, buffer);
    }

    private void appendThrowsClause(String[] exceptions, StringBuffer buffer)
    {
        if(null == exceptions || exceptions.length <= 0) return;
        String throwsClause = prepareCommaSeparatedList(exceptions, null);
        buffer.append(" throws ").append(throwsClause);
    }

    public void appendComment(String oneLineComment)
    {
        if(null == oneLineComment || oneLineComment.length() <= 0) return;
        appendLine("//" + oneLineComment);
    }

    public void appendBlockComment(String[] commentLines)
    {
        appendBlockComment(commentLines, "/*");
    }

    public void appendJavaDocComment(String[] commentLines)
    {
        appendBlockComment(commentLines, "/**");
    }

    private void appendBlockComment(String[] commentLines, String commentStart)
    {
        if(null == commentLines || commentLines.length <= 0) return;
        appendLine(commentStart);
        for (String commentLine : commentLines) {
            appendLine(" * " + commentLine);
        }
        appendLine(" */");
    }

    private String prepareModifierList(String[] modifiers)
    {
        if(null == modifiers) modifiers = new String[0];
        StringBuilder listBuffer = new StringBuilder(50);
        for (String modifier : modifiers) {
            listBuffer.append(modifier).append(" ");
        }
        return listBuffer.toString();
    }

    private String[] getParameterNameList(String[] types, String[] names)
    {
        if(null == types) types = new String[0];
        if(null == names) names = new String[0];
        if(names.length >= types.length) return names;
        String[] newNames = new String[types.length];
        System.arraycopy(names, 0, newNames, 0, names.length);
        for(int ii = 0; ii < types.length - names.length; ii++)
        {
            newNames[names.length + ii] = "param" + (names.length + ii);
        }
        return newNames;
    }

    private String prepareCommaSeparatedList(String[] types, String[] names)
    {
        if(null == types) types = new String[0];
        if(null == names) names = new String[0];
        StringBuilder listBuffer = new StringBuilder(50);
        for(int ii = 0; ii < types.length; ii++)
        {
            listBuffer.append(types[ii]);
            if(ii < names.length)
            {
                listBuffer.append(" ").append(names[ii]);
            }
            if(ii < types.length - 1)
            {
                listBuffer.append(", ");
            }
        }
        return listBuffer.toString();
    }
}
