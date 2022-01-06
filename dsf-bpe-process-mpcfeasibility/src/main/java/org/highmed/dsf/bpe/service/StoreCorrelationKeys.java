package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.BPMN_EXECUTION_VARIABLE_TARGETS;
import static org.highmed.dsf.bpe.ConstantsMpcFeasibility.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS;
import static org.highmed.dsf.bpe.ConstantsMpcFeasibility.CODESYSTEM_HIGHMED_MPCFEASIBILITY;
import static org.highmed.dsf.bpe.ConstantsMpcFeasibility.CODESYSTEM_HIGHMED_MPCFEASIBILITY_VALUE_PARTICIPATING_MEDIC_CORRELATION_KEY;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.MpcFeasibilityQueryResults;
import org.highmed.dsf.bpe.variables.MpcFeasibilityQueryResultsValues;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Target;
import org.highmed.dsf.fhir.variables.Targets;
import org.highmed.dsf.fhir.variables.TargetsValues;
import org.hl7.fhir.r4.model.Task;

public class StoreCorrelationKeys extends AbstractServiceDelegate
{
	public StoreCorrelationKeys(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = getCurrentTaskFromExecutionVariables();

		List<Target> targets = getTaskHelper()
				.getInputParameterStringValues(task, CODESYSTEM_HIGHMED_MPCFEASIBILITY,
						CODESYSTEM_HIGHMED_MPCFEASIBILITY_VALUE_PARTICIPATING_MEDIC_CORRELATION_KEY)
				.map(correlationKey -> Target.createBiDirectionalTarget("", "", correlationKey))
				.collect(Collectors.toList());

		execution.setVariable(BPMN_EXECUTION_VARIABLE_TARGETS, TargetsValues.create(new Targets(targets)));

		execution.setVariable(BPMN_EXECUTION_VARIABLE_QUERY_RESULTS,
				MpcFeasibilityQueryResultsValues.create(new MpcFeasibilityQueryResults(null)));
	}

}
