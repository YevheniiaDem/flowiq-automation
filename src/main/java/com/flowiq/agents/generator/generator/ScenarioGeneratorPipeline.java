package com.flowiq.agents.generator.generator;

import com.flowiq.agents.generator.config.SmartTestGeneratorConfig;
import com.flowiq.agents.generator.model.EndpointTestContext;
import com.flowiq.agents.generator.model.TestScenario;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class ScenarioGeneratorPipeline {

    private final List<ScenarioGenerator> generators;
    private final int maxPerEndpoint;

    public ScenarioGeneratorPipeline(SmartTestGeneratorConfig config) {
        this.maxPerEndpoint = config.maxScenariosPerEndpoint();
        this.generators = List.of(
                new PositiveScenarioGenerator(),
                new NegativeScenarioGenerator(),
                new BoundaryScenarioGenerator(),
                new AuthorizationScenarioGenerator(),
                new SecurityScenarioGenerator()
        );
    }

    public List<TestScenario> generate(List<EndpointTestContext> contexts) {
        List<TestScenario> all = new ArrayList<>();
        for (EndpointTestContext context : contexts) {
            List<TestScenario> endpointScenarios = new ArrayList<>();
            for (ScenarioGenerator generator : generators) {
                List<TestScenario> generated = generator.generate(context);
                log.debug("{} produced {} scenario(s) for {} {}",
                        generator.getClass().getSimpleName(), generated.size(),
                        context.getOperation().method(), context.getNormalizedPath());
                endpointScenarios.addAll(generated);
            }
            endpointScenarios.stream()
                    .sorted(Comparator.comparing(TestScenario::getPriority))
                    .limit(maxPerEndpoint)
                    .forEach(all::add);
        }
        return List.copyOf(new LinkedHashSet<>(all));
    }
}
