package com.mockrunner.gen.proc;

import java.util.List;

public interface AdapterProcessor
{
    void process(Class<?> module, List<String> excludedMethods);
    String getName();
    String getOutput();
}
