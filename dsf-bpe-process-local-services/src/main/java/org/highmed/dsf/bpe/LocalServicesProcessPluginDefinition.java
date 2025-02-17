package org.highmed.dsf.bpe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.highmed.dsf.bpe.spring.config.FeasibilitySerializerConfig;
import org.highmed.dsf.bpe.spring.config.LocalServicesConfig;
import org.highmed.dsf.fhir.resources.AbstractResource;
import org.highmed.dsf.fhir.resources.ActivityDefinitionResource;
import org.highmed.dsf.fhir.resources.CodeSystemResource;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.dsf.fhir.resources.StructureDefinitionResource;
import org.highmed.dsf.fhir.resources.ValueSetResource;
import org.springframework.core.env.PropertyResolver;

import ca.uhn.fhir.context.FhirContext;

public class LocalServicesProcessPluginDefinition implements ProcessPluginDefinition
{
	public static final String VERSION = "0.5.0";

	private static final String DEPENDENCY_FEASIBILITY_VERSION = "0.5.0";
	private static final String DEPENDENCY_FEASIBILITY_NAME_AND_VERSION = "dsf-bpe-process-feasibility-0.5.0";

	@Override
	public String getName()
	{
		return "dsf-bpe-process-local-services";
	}

	@Override
	public String getVersion()
	{
		return VERSION;
	}

	@Override
	public Stream<String> getBpmnFiles()
	{
		return Stream.of("bpe/localServicesIntegration.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(LocalServicesConfig.class, FeasibilitySerializerConfig.class);
	}

	@Override
	public List<String> getDependencyNamesAndVersions()
	{
		return Arrays.asList(DEPENDENCY_FEASIBILITY_NAME_AND_VERSION);
	}

	@Override
	public ResourceProvider getResourceProvider(FhirContext fhirContext, ClassLoader classLoader,
			PropertyResolver resolver)
	{
		var aL = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-localServicesIntegration.xml");
		var sTL = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-local-services-integration.xml");

		var vF = ValueSetResource.dependency(DEPENDENCY_FEASIBILITY_NAME_AND_VERSION,
				"http://highmed.org/fhir/ValueSet/feasibility", DEPENDENCY_FEASIBILITY_VERSION);
		var cF = CodeSystemResource.dependency(DEPENDENCY_FEASIBILITY_NAME_AND_VERSION,
				"http://highmed.org/fhir/CodeSystem/feasibility", DEPENDENCY_FEASIBILITY_VERSION);

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map
				.of("highmedorg_localServicesIntegration/" + VERSION, Arrays.asList(aL, sTL, vF, cF));

		return ResourceProvider.read(VERSION, () -> fhirContext.newXmlParser().setStripVersionsFromReferences(false),
				classLoader, resolver, resourcesByProcessKeyAndVersion);
	}
}
