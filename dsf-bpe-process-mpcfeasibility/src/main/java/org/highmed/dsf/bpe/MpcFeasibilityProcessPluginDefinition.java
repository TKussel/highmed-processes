package org.highmed.dsf.bpe;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.highmed.dsf.bpe.spring.config.FeasibilityConfig;
import org.highmed.dsf.bpe.spring.config.FeasibilitySerializerConfig;
import org.highmed.dsf.fhir.resources.AbstractResource;
import org.highmed.dsf.fhir.resources.ActivityDefinitionResource;
import org.highmed.dsf.fhir.resources.CodeSystemResource;
import org.highmed.dsf.fhir.resources.ResourceProvider;
import org.highmed.dsf.fhir.resources.StructureDefinitionResource;
import org.highmed.dsf.fhir.resources.ValueSetResource;
import org.springframework.core.env.PropertyResolver;

import ca.uhn.fhir.context.FhirContext;

public class MpcFeasibilityProcessPluginDefinition implements ProcessPluginDefinition
{
	public static final String VERSION = "0.0.1";

	@Override
	public String getName()
	{
		return "dsf-bpe-process-mpcfeasibility";
	}

	@Override
	public String getVersion()
	{
		return VERSION;
	}

	@Override
	public Stream<String> getBpmnFiles()
	{
		return Stream.of("bpe/requestMpcFeasibility.bpmn", "bpe/computeMpcFeasibility.bpmn", "bpe/executeMpcFeasibility.bpmn");
	}

	@Override
	public Stream<Class<?>> getSpringConfigClasses()
	{
		return Stream.of(MpcFeasibilityConfig.class, MpcFeasibilitySerializerConfig.class);
	}

	@Override
	public ResourceProvider getResourceProvider(FhirContext fhirContext, ClassLoader classLoader,
			PropertyResolver resolver)
	{
		var aCom = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-computeMpcFeasibility.xml");
		var aExe = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-executeMpcFeasibility.xml");
		var aReq = ActivityDefinitionResource.file("fhir/ActivityDefinition/highmed-requestMpcFeasibility.xml");

		var cF = CodeSystemResource.file("fhir/CodeSystem/highmed-mpcfeasibility.xml");

		var sTCom = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-compute-mpcfeasibility.xml");
		var sTErr = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-error-mpcfeasibility.xml");
		var sTExe = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-execute-mpcfeasibility.xml");
		var sTResM = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-multi-medic-result-mpcfeasibility.xml");
		var sTReq = StructureDefinitionResource.file("fhir/StructureDefinition/highmed-task-request-mpcfeasibility.xml");
		var sTResS = StructureDefinitionResource
				.file("fhir/StructureDefinition/highmed-task-single-medic-result-mpcfeasibility.xml");

		var vF = ValueSetResource.file("fhir/ValueSet/highmed-mpcfeasibility.xml");

		Map<String, List<AbstractResource>> resourcesByProcessKeyAndVersion = Map.of(
				"highmedorg_computeMpcFeasibility/" + VERSION, Arrays.asList(aCom, cF, sTCom, sTResS, vF),
				"highmedorg_executeMpcFeasibility/" + VERSION, Arrays.asList(aExe, cF, sTExe, vF),
				"highmedorg_requestMpcFeasibility/" + VERSION, Arrays.asList(aReq, cF, sTReq, sTResM, sTErr, vF));

		return ResourceProvider.read(VERSION, () -> fhirContext.newXmlParser().setStripVersionsFromReferences(false),
				classLoader, resolver, resourcesByProcessKeyAndVersion);
	}
}
