package com.flowiq.agents.prreview.scanner;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;
import java.util.Set;

@Value
@Builder
public class SourceInventory {
    @Singular("serviceFile")
    List<String> serviceFiles;
    @Singular("controllerFile")
    List<String> controllerFiles;
    @Singular("repositoryFile")
    List<String> repositoryFiles;
    @Singular("dtoFile")
    List<String> dtoFiles;
    @Singular("pageObjectFile")
    List<String> pageObjectFiles;
    @Singular("schemaFile")
    List<String> schemaFiles;
    @Singular("testFile")
    List<String> testFiles;
    @Singular("serviceClass")
    Set<String> serviceClassNames;
    @Singular("pageClass")
    Set<String> pageClassNames;
    @Singular("dtoClass")
    Set<String> dtoClassNames;
}
