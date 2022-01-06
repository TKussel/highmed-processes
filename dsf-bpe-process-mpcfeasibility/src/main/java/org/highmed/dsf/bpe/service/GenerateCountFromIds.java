package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsMpcFeasibility.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.MpcFeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.MpcFeasibilityQueryResults;
import org.highmed.dsf.bpe.variables.MpcFeasibilityQueryResultsValues;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;

public class GenerateCountFromIds extends AbstractServiceDelegate
{
	public GenerateCountFromIds(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		MpcFeasibilityQueryResults results = (MpcFeasibilityQueryResults) execution
				.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);

		List<MpcFeasibilityQueryResult> filteredResults = count(results.getResults());

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				MpcFeasibilityQueryResultsValues.create(new MpcFeasibilityQueryResults(filteredResults)));
	}

	private List<MpcFeasibilityQueryResult> count(List<MpcFeasibilityQueryResult> results)
	{
		return results.stream().map(this::count).collect(Collectors.toList());
	}

	protected MpcFeasibilityQueryResult count(MpcFeasibilityQueryResult result)
	{
		return MpcFeasibilityQueryResult.countResult(result.getOrganizationIdentifier(), result.getCohortId(),
				result.getResultSet().getRows().size());
	}
}
