package org.highmed.dsf.bpe.service;

import static org.highmed.dsf.bpe.ConstantsBase.EXTENSION_HIGHMED_GROUP_ID;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsFeasibility;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.FeasibilityQueryResults;
import org.highmed.dsf.fhir.authorization.read.ReadAccessHelper;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class StoreResult extends AbstractServiceDelegate implements InitializingBean
{
	private static final Logger logger = LoggerFactory.getLogger(StoreResult.class);

	public StoreResult(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper,
			ReadAccessHelper readAccessHelper)
	{
		super(clientProvider, taskHelper, readAccessHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = getCurrentTaskFromExecutionVariables();
		FeasibilityQueryResults results = (FeasibilityQueryResults) execution
				.getVariable(ConstantsFeasibility.BPMN_EXECUTION_VARIABLE_QUERY_RESULTS);

		addOutputs(task, results);
	}

	private void addOutputs(Task task, FeasibilityQueryResults results)
	{
		results.getResults().forEach(result -> addOutput(task, result));
	}

	private void addOutput(Task task, FeasibilityQueryResult result)
	{
		if (result.isCohortSizeResult())
		{
			Task.TaskOutputComponent output = getTaskHelper().createOutputUnsignedInt(
					ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY,
					ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT,
					result.getCohortSize());
			output.addExtension(createCohortIdExtension(result.getCohortId()));
			task.addOutput(output);
		}
		else if (result.isIdResultSetUrlResult())
		{
			Task.TaskOutputComponent output = getTaskHelper().createOutput(
					ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY,
					ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_SINGLE_MEDIC_RESULT_REFERENCE,
					new Reference(result.getResultSetUrl()));
			output.addExtension(createCohortIdExtension(result.getCohortId()));
			task.addOutput(output);
		}
		else
		{
			logger.warn("Unexpected result (not a cohort-size or ResultSet URL result) for cohort with ID "
					+ result.getCohortId());
			throw new RuntimeException(
					"Unexpected result (not a cohort-size or ResultSet URL result) for cohort with ID "
							+ result.getCohortId());
		}
	}

	private Extension createCohortIdExtension(String cohortId)
	{
		return new Extension(EXTENSION_HIGHMED_GROUP_ID, new Reference(cohortId));
	}
}
