package org.highmed.dsf.bpe.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.WebApplicationException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.BloomFilterConfig;
import org.highmed.dsf.fhir.variables.BloomFilterConfigValues;
import org.highmed.dsf.fhir.variables.FhirResourceValues;
import org.highmed.dsf.fhir.variables.FhirResourcesListValues;
import org.highmed.dsf.fhir.variables.Outputs;
import org.highmed.dsf.fhir.variables.OutputsValues;
import org.highmed.fhir.client.FhirWebserviceClient;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

public class DownloadFeasibilityResources extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(DownloadFeasibilityResources.class);

	private final OrganizationProvider organizationProvider;

	public DownloadFeasibilityResources(OrganizationProvider organizationProvider,
			FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
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
		Task task = (Task) execution.getVariable(Constants.VARIABLE_TASK);

		IdType researchStudyId = getResearchStudyId(task);
		FhirWebserviceClient client = getWebserviceClient(researchStudyId);

		ResearchStudy researchStudy = getResearchStudy(researchStudyId, client);
		execution.setVariable(Constants.VARIABLE_RESEARCH_STUDY, FhirResourceValues.create(researchStudy));

		Outputs outputs = (Outputs) execution.getVariable(Constants.VARIABLE_PROCESS_OUTPUTS);

		List<Group> cohortDefinitions = getCohortDefinitions(researchStudy, outputs, client);
		execution.setVariable(Constants.VARIABLE_COHORTS, FhirResourcesListValues.create(cohortDefinitions));
		execution.setVariable(Constants.VARIABLE_PROCESS_OUTPUTS, OutputsValues.create(outputs));

		String ttpIdentifier = getTtpIdentifier(researchStudy, client);
		execution.setVariable(Constants.VARIABLE_TTP_IDENTIFIER, ttpIdentifier);

		boolean needsConsentCheck = getNeedsConsentCheck(task);
		execution.setVariable(Constants.VARIABLE_NEEDS_CONSENT_CHECK, needsConsentCheck);

		boolean needsRecordLinkage = getNeedsRecordLinkageCheck(task);
		execution.setVariable(Constants.VARIABLE_NEEDS_RECORD_LINKAGE, needsRecordLinkage);

		if (needsRecordLinkage)
		{
			BloomFilterConfig bloomFilterConfig = getBloomFilterConfig(task);
			execution.setVariable(Constants.VARIABLE_BLOOM_FILTER_CONFIG,
					BloomFilterConfigValues.create(bloomFilterConfig));
		}
	}

	private IdType getResearchStudyId(Task task)
	{
		Reference researchStudyReference = getTaskHelper()
				.getInputParameterReferenceValues(task, Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
						Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_RESEARCH_STUDY_REFERENCE)
				.findFirst().get();

		return new IdType(researchStudyReference.getReference());
	}

	private FhirWebserviceClient getWebserviceClient(IdType researchStudyId)
	{
		if (researchStudyId.getBaseUrl() == null
				|| researchStudyId.getBaseUrl().equals(getFhirWebserviceClientProvider().getLocalBaseUrl()))
		{
			return getFhirWebserviceClientProvider().getLocalWebserviceClient();
		}
		else
		{
			return getFhirWebserviceClientProvider().getRemoteWebserviceClient(researchStudyId.getBaseUrl());
		}
	}

	private ResearchStudy getResearchStudy(IdType researchStudyid, FhirWebserviceClient client)
	{
		try
		{
			return client.read(ResearchStudy.class, researchStudyid.getIdPart());
		}
		catch (WebApplicationException e)
		{
			throw new ResourceNotFoundException("Error while reading ResearchStudy with id "
					+ researchStudyid.getIdPart() + " from " + client.getBaseUrl());
		}
	}

	private List<Group> getCohortDefinitions(ResearchStudy researchStudy, Outputs outputs, FhirWebserviceClient client)
	{
		List<Group> cohortDefinitions = new ArrayList<>();
		List<Reference> cohortDefinitionReferences = researchStudy.getEnrollment();

		cohortDefinitionReferences.forEach(reference ->
		{
			try
			{
				IdType type = new IdType(reference.getReference());
				Group group = client.read(Group.class, type.getIdPart());

				IdType groupId = new IdType(group.getId());
				group.setId(client.getBaseUrl() + groupId.getResourceType() + "/" + groupId.getIdPart());

				cohortDefinitions.add(group);
			}
			catch (WebApplicationException e)
			{
				String errorMessage = "Error while reading cohort definition with id " + reference.getReference()
						+ " from " + client.getBaseUrl();

				logger.info(errorMessage);
				outputs.addErrorOutput(errorMessage);
			}
		});

		return cohortDefinitions;
	}

	private String getTtpIdentifier(ResearchStudy researchStudy, FhirWebserviceClient client)
	{
		Extension ext = researchStudy
				.getExtensionByUrl("http://highmed.org/fhir/StructureDefinition/participating-ttp");
		Reference ref = (Reference) ext.getValue();
		return ref.getIdentifier().getValue();
	}

	private boolean getNeedsConsentCheck(Task task)
	{
		return getTaskHelper()
				.getFirstInputParameterBooleanValue(task, Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
						Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_CONSENT_CHECK)
				.orElseThrow(() -> new IllegalArgumentException("NeedsConsentCheck boolean is not set in task with id='"
						+ task.getId() + "', this error should " + "have been caught by resource validation"));
	}

	private boolean getNeedsRecordLinkageCheck(Task task)
	{
		return getTaskHelper()
				.getFirstInputParameterBooleanValue(task, Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
						Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_NEEDS_RECORD_LINKAGE)
				.orElseThrow(
						() -> new IllegalArgumentException("NeedsRecordLinkage boolean is not set in task with id='"
								+ task.getId() + "', this error should " + "have been caught by resource validation"));
	}

	private BloomFilterConfig getBloomFilterConfig(Task task)
	{
		return BloomFilterConfig.fromBytes(getTaskHelper()
				.getFirstInputParameterByteValue(task, Constants.CODESYSTEM_HIGHMED_FEASIBILITY,
						Constants.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_BLOOM_FILTER_CONFIG)
				.orElseThrow(() -> new IllegalArgumentException("BloomFilterConfig byte[] is not set in task with id='"
						+ task.getId() + "', this error should " + "have been caught by resource validation")));
	}
}
