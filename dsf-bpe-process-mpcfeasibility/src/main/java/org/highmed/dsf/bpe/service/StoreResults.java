package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_GROUP_ID;
import static org.highmed.dsf.bpe.ConstantsMpcFeasibility.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsMpcFeasibility.CODESYSTEM_HIGHMED_MPCFEASIBILITY;
import static org.highmed.dsf.bpe.ConstantsMpcFeasibility.CODESYSTEM_HIGHMED_MPCFEASIBILITY_VALUE_SINGLE_MEDIC_RESULT;
import static org.highmed.dsf.bpe.ConstantsMpcFeasibility.CODESYSTEM_HIGHMED_MPCFEASIBILITY_VALUE_SINGLE_MEDIC_RESULT_REFERENCE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.MpcFeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.MpcFeasibilityQueryResults;
import org.highmed.dsf.bpe.variables.MpcFeasibilityQueryResultsValues;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.organization.OrganizationProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.UnsignedIntType;
import org.springframework.beans.factory.InitializingBean;

public class StoreResults extends AbstractServiceDelegate implements InitializingBean
{
	private final OrganizationProvider organizationProvider;

	public StoreResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
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
		MpcFeasibilityQueryResults results = (MpcFeasibilityQueryResults) execution
				.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);

		Task task = getCurrentTaskFromExecutionVariables();

		List<MpcFeasibilityQueryResult> extendedResults = new ArrayList<>();
		extendedResults.addAll(results.getResults());
		extendedResults.addAll(getResults(task, needsRecordLinkage));

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				MpcFeasibilityQueryResultsValues.create(new MpcFeasibilityQueryResults(extendedResults)));
	}

	private List<MpcFeasibilityQueryResult> getResults(Task task, boolean needsRecordLinkage)
	{
		TaskHelper taskHelper = getTaskHelper();
		Reference requester = task.getRequester();

		return taskHelper
				.getInputParameterWithExtension(task, CODESYSTEM_HIGHMED_MPCFEASIBILITY,
						CODESYSTEM_HIGHMED_MPCFEASIBILITY_VALUE_SINGLE_MEDIC_RESULT, EXTENSION_HIGHMED_GROUP_ID)
				.map(input ->
				{
					String cohortId = ((Reference) input.getExtension().get(0).getValue()).getReference();
					int cohortSize = ((UnsignedIntType) input.getValue()).getValue();

					return MpcFeasibilityQueryResult.countResult(requester.getIdentifier().getValue(), cohortId,
							cohortSize);
				}).collect(Collectors.toList());
	}
}
