package org.highmed.dsf.bpe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.highmed.dsf.bpe.spring.config.DataSharingSerializerConfig;
import org.highmed.dsf.bpe.spring.config.FeasibilityConfig;
import org.highmed.dsf.bpe.spring.config.FeasibilitySerializerConfig;
import org.highmed.dsf.fhir.resources.AbstractResource;
import org.highmed.dsf.fhir.resources.ActivityDefinitionResource;
import org.highmed.dsf.fhir.resources.CodeSystemResource;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.dsf.fhir.resources.StructureDefinitionResource;
import org.highmed.dsf.fhir.resources.ValueSetResource;

import ca.uhn.fhir.context.FhirContext;

public class FeasibilityProcessPluginDefinition implements ProcessPluginDefinition
{
	public static final String VERSION = "0.5.0";

	private static final String DEPENDENCY_DATA_SHARING_VERSION = "0.5.0";
	private static final String DEPENDENCY_DATA_SHARING_NAME_AND_VERSION = "dsf-bpe-process-data-sharing-0.5.0";

	@Override
	public String getName()
	{
		return "dsf-bpe-process-feasibility";
	}

	@Override
	public String getVersion()
	{
		return VERSION;
	}

	@Override
	public Stream<String> getBpmnFiles()
	{
		return Stream.of("bpe/requestFeasibility.bpmn", "bpe/computeFeasibility.bpmn", "bpe/executeFeasibility.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(FeasibilityConfig.class, FeasibilitySerializerConfig.class, DataSharingSerializerConfig.class);
	}

	@Override
	public List<String> getDependencyNamesAndVersions()
	{
		return Arrays.asList(DEPENDENCY_DATA_SHARING_NAME_AND_VERSION);
	}

	@Override
	public ResourceProvider getResourceProvider(FhirContext fhirContext, ClassLoader classLoader)
	{
		var aCom = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-computeFeasibility.xml");
		var aExe = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-executeFeasibility.xml");
		var aReq = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-requestFeasibility.xml");

		var cDS = CodeSystemResource
				.dependency(DEPENDENCY_DATA_SHARING_NAME_AND_VERSION, "http://highmed.org/fhir/CodeSystem/data-sharing",
						DEPENDENCY_DATA_SHARING_VERSION);

		var sTCom = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-compute-feasibility.xml");
		var sTErr = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-error-feasibility.xml");
		var sTExe = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-execute-feasibility.xml");
		var sTResM = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-multi-medic-result-feasibility.xml");
		var sTReq = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-request-feasibility.xml");
		var sTResS = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-single-medic-result-feasibility.xml");

		var vDS = ValueSetResource
				.dependency(DEPENDENCY_DATA_SHARING_NAME_AND_VERSION, "http://highmed.org/fhir/ValueSet/data-sharing",
						DEPENDENCY_DATA_SHARING_VERSION);

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map
				.of("computeFeasibility/" + VERSION, Arrays.asList(aCom, cDS, sTCom, sTResS, vDS),
						"executeFeasibility/" + VERSION, Arrays.asList(aExe, cDS, sTExe, vDS),
						"requestFeasibility/" + VERSION, Arrays.asList(aReq, cDS, sTReq, sTResM, sTErr, vDS));

		return ResourceProvider
				.read(VERSION, () -> fhirContext.newXmlParser().setStripVersionsFromReferences(false), classLoader,
						resourcesByProcessKeyAndVersion);
	}
}
