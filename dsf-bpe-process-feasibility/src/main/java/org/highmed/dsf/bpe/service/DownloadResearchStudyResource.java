package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.CODESYSTEM_HIGHMED_ORGANIZATION_TYPE_VALUE_MEDIC;
import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_PARTICIPATING_MEDIC;
import static org.highmed.dsf.bpe.ConstantsBase.NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER;
import static org.highmed.dsf.bpe.ConstantsFeasibility.BPMN_EXECUTION_VARIABLE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.ConstantsFeasibility.BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsFeasibility.BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE;
import static org.highmed.dsf.bpe.ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class DownloadResearchStudyResource extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(DownloadResearchStudyResource.class);

	private final OrganizationProvider organizationProvider;

	public DownloadResearchStudyResource(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper, OrganizationProvider organizationProvider)
	{
		super(clientProvider, taskHelper, readAccessHelper);

		this.organizationProvider = organizationProvider;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		super.afterPropertiesSet();

		Objects.requireNonNull(organizationProvider, "organizationProvider");
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = getCurrentTaskFromExecutionVariables();

		IdType researchStudyId = getResearchStudyId(task);
		FhirWebserviceClient client = getFhirWebserviceClientProvider().getLocalWebserviceClient();
		ResearchStudy researchStudy = getResearchStudy(researchStudyId, client);
		researchStudy = addMissingOrganizations(researchStudy, client);
		execution.setVariable(BPMN_EXECUTION_VARIABLE_RESEARCH_STUDY, researchStudy);

		boolean needsConsentCheck = getNeedsConsentCheck(task);
		execution.setVariable(BPMN_EXECUTION_VARIABLE_NEEDS_CONSENT_CHECK, needsConsentCheck);

		boolean needsRecordLinkage = getNeedsRecordLinkageCheck(task);
		execution.setVariable(BPMN_EXECUTION_VARIABLE_NEEDS_RECORD_LINKAGE, needsRecordLinkage);
	}

	private IdType getResearchStudyId(Task task)
	{
		Reference researchStudyReference = getTaskHelper()
				.getInputParameterReferenceValues(task, CODESYSTEM_HIGHMED_FEASIBILITY,
						CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE)
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("ResearchStudy reference is not set in task with id='"
						+ task.getId() + "', this error should " + "have been caught by resource validation"));

		return new IdType(researchStudyReference.getReference());
	}

	private ResearchStudy getResearchStudy(IdType researchStudyid, FhirWebserviceClient client)
	{
		try
		{
			return client.read(ResearchStudy.class, researchStudyid.getIdPart());
		}
		catch (Exception e)
		{
			logger.warn("Error while reading ResearchStudy with id {} from {}", researchStudyid.getIdPart(),
					client.getBaseUrl());
			throw e;
		}
	}

	private ResearchStudy addMissingOrganizations(ResearchStudy researchStudy, FhirWebserviceClient client)
	{
		List<String> identifiers = organizationProvider
				.getOrganizationsByType(CODESYSTEM_HIGHMED_ORGANIZATION_TYPE_VALUE_MEDIC)
				.flatMap(o -> o.getIdentifier().stream())
				.filter(i -> NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER.equals(i.getSystem())).map(i -> i.getValue())
				.collect(Collectors.toList());

		List<String> existingIdentifiers = researchStudy.getExtensionsByUrl(EXTENSION_HIGHMED_PARTICIPATING_MEDIC)
				.stream().filter(e -> e.getValue() instanceof Reference).map(e -> (Reference) e.getValue())
				.map(r -> r.getIdentifier().getValue()).collect(Collectors.toList());

		identifiers.removeAll(existingIdentifiers);

		if (!identifiers.isEmpty())
		{
			identifiers.forEach(identifier ->
			{
				logger.warn(
						"Adding missing organization with identifier='{}' to feasibility research study with id='{}'",
						identifier, researchStudy.getId());

				researchStudy.addExtension().setUrl(EXTENSION_HIGHMED_PARTICIPATING_MEDIC).setValue(
						new Reference().setType(ResourceType.Organization.name()).setIdentifier(new Identifier()
								.setSystem(NAMINGSYSTEM_HIGHMED_ORGANIZATION_IDENTIFIER).setValue(identifier)));

			});

			return update(researchStudy, client);
		}
		else
			return researchStudy;
	}

	private ResearchStudy update(ResearchStudy researchStudy, FhirWebserviceClient client)
	{
		try
		{
			return client.update(researchStudy);
		}
		catch (Exception e)
		{
			logger.warn("Error while updating ResearchStudy resoruce: " + e.getMessage(), e);
			throw e;
		}
	}

	private boolean getNeedsConsentCheck(Task task)
	{
		return getTaskHelper()
				.getFirstInputParameterBooleanValue(task, CODESYSTEM_HIGHMED_FEASIBILITY,
						CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK)
				.orElseThrow(() -> new IllegalArgumentException("NeedsConsentCheck boolean is not set in task with id='"
						+ task.getId() + "', this error should " + "have been caught by resource validation"));
	}

	private boolean getNeedsRecordLinkageCheck(Task task)
	{
		return getTaskHelper()
				.getFirstInputParameterBooleanValue(task, CODESYSTEM_HIGHMED_FEASIBILITY,
						CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE)
				.orElseThrow(
						() -> new IllegalArgumentException("NeedsRecordLinkage boolean is not set in task with id='"
								+ task.getId() + "', this error should " + "have been caught by resource validation"));
	}
}
