package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.Constants.MIN_COHORT_DEFINITIONS;
import static org.highmed.dsf.bpe.Constants.MIN_PARTICIPATING_MEDICS;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.Constants;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.FhirResourcesList;
import org.hl7.fhir.r4.model.Group;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResearchStudy;

public class CheckFeasibilityResources extends AbstractServiceDelegate
{
	public CheckFeasibilityResources(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		ResearchStudy researchStudy = (ResearchStudy) execution.getVariable(Constants.VARIABLE_RESEARCH_STUDY);

		List<Group> cohorts = ((FhirResourcesList) execution.getVariable(Constants.VARIABLE_COHORTS))
				.getResourcesAndCast();

		checkNumberOfParticipatingMedics(researchStudy);
		checkFullyQualifiedCohortIds(cohorts);
		checkNumberOfCohortDefinitions(cohorts);
	}

	private void checkNumberOfParticipatingMedics(ResearchStudy researchStudy)
	{
		long medics = researchStudy.getExtensionsByUrl(Constants.EXTENSION_PARTICIPATING_MEDIC_URI).stream()
				.filter(e -> e.getValue() instanceof Reference).map(e -> (Reference) e.getValue())
				.map(r -> r.getIdentifier())
				.filter(i -> "http://highmed.org/fhir/NamingSystem/organization-identifier".equals(i.getSystem()))
				.map(i -> i.getValue()).distinct().count();

		if (medics < MIN_PARTICIPATING_MEDICS)
		{
			throw new RuntimeException(
					"Number of distinct participanting MeDICs is < " + MIN_PARTICIPATING_MEDICS + ", got " + medics);
		}
	}

	private void checkFullyQualifiedCohortIds(List<Group> cohorts)
	{
		if (cohorts.stream().anyMatch(g -> !g.getIdElement().hasBaseUrl()))
		{
			throw new RuntimeException("Not all cohorts have fully qualified ids (containing server base url)");
		}
	}

	private void checkNumberOfCohortDefinitions(List<Group> cohorts)
	{
		int size = cohorts.size();
		if (size < MIN_COHORT_DEFINITIONS)
		{
			throw new RuntimeException(
					"Number of defined cohorts is < " + MIN_COHORT_DEFINITIONS + ", got " + cohorts.size());
		}
	}
}
