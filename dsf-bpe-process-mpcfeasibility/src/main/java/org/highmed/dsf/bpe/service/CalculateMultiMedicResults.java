package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsMpcFeasibility.BPMN_EXECUTION_VARIABLE_FINAL_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsMpcFeasibility.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.MpcFeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.MpcFeasibilityQueryResults;
import org.highmed.dsf.bpe.variables.FinalMpcFeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.FinalMpcFeasibilityQueryResults;
import org.highmed.dsf.bpe.variables.FinalMpcFeasibilityQueryResultsValues;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;

public class CalculateMultiMedicResults extends AbstractServiceDelegate
{
	public CalculateMultiMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		List<MpcFeasibilityQueryResult> results = ((MpcFeasibilityQueryResults) execution
				.getVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS)).getResults();

		List<FinalMpcFeasibilityQueryResult> finalResults = calculateResults(results);

		execution.setVariable(BPMN_EXECUTION_VARIABLE_FINAL_QUERY_RESULTS,
				FinalMpcFeasibilityQueryResultsValues.create(new FinalMpcFeasibilityQueryResults(finalResults)));
	}

	private List<FinalMpcFeasibilityQueryResult> calculateResults(List<MpcFeasibilityQueryResult> results)
	{
		Map<String, List<MpcFeasibilityQueryResult>> byCohortId = results.stream()
				.collect(Collectors.groupingBy(MpcFeasibilityQueryResult::getCohortId));

		return byCohortId.entrySet().stream()
				.map(e -> new FinalMpcFeasibilityQueryResult(e.getKey(),
						toInt(e.getValue().stream().filter(r -> r.getCohortSize() > 0).count()),
						toInt(e.getValue().stream().mapToLong(MpcFeasibilityQueryResult::getCohortSize).sum())))
				.collect(Collectors.toList());
	}

	private int toInt(long l)
	{
		if (l > Integer.MAX_VALUE)
			throw new IllegalArgumentException("long > " + Integer.MAX_VALUE);
		else
			return (int) l;
	}
}
