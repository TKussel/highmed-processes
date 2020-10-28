package org.highmed.dsf.bpe.service;

import java.util.List;
import java.util.stream.Collectors;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.highmed.dsf.bpe.ConstantsBase;
import org.highmed.dsf.bpe.delegate.AbstractServiceDelegate;
import org.highmed.dsf.bpe.variables.ConstantsFeasibility;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResult;
import org.highmed.dsf.bpe.variables.FinalFeasibilityQueryResults;
import org.highmed.dsf.fhir.client.FhirWebserviceClientProvider;
import org.highmed.dsf.fhir.task.TaskHelper;
import org.highmed.dsf.fhir.variables.Output;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.ParameterComponent;
import org.hl7.fhir.r4.model.Task.TaskOutputComponent;
import org.hl7.fhir.r4.model.UnsignedIntType;

public class CheckMultiMedicResults extends AbstractServiceDelegate
{
	public CheckMultiMedicResults(FhirWebserviceClientProvider clientProvider, TaskHelper taskHelper)
	{
		super(clientProvider, taskHelper);
	}

	@Override
	protected void doExecute(DelegateExecution execution) throws Exception
	{
		Task task = (Task) execution.getVariable(ConstantsBase.VARIABLE_TASK);

		List<Output> errors = readFinalFeasibilityQueryErrors(task);
		FinalFeasibilityQueryResults results = readFinalFeasibilityQueryResults(task);
		results = checkResults(results);

		Task leadingTask = (Task) execution.getVariable(ConstantsBase.VARIABLE_LEADING_TASK);
		addResultOutputs(leadingTask, results);
		addErrorOutputs(leadingTask, errors);

		// This task is not automatically set to completed because it is an additional task
		// during the execution of the main process
		task.setStatus(Task.TaskStatus.COMPLETED);
		getFhirWebserviceClientProvider().getLocalWebserviceClient().withMinimalReturn().update(task);
	}

	private List<Output> readFinalFeasibilityQueryErrors(Task task)
	{
		return task.getInput().stream()
				.filter(in -> in.hasType() && in.getType().hasCoding() && ConstantsBase.CODESYSTEM_HIGHMED_BPMN
						.equals(in.getType().getCodingFirstRep().getSystem())
						&& ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE
						.equals(in.getType().getCodingFirstRep().getCode()))
				.map(in -> new Output(ConstantsBase.CODESYSTEM_HIGHMED_BPMN,
						ConstantsBase.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE, in.getValue().primitiveValue()))
				.collect(Collectors.toList());
	}

	private FinalFeasibilityQueryResults readFinalFeasibilityQueryResults(Task task)
	{
		List<FinalFeasibilityQueryResult> results = task.getInput().stream()
				.filter(in -> in.hasType() && in.getType().hasCoding()
						&& ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY
						.equals(in.getType().getCodingFirstRep().getSystem())
						&& ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT
						.equals(in.getType().getCodingFirstRep().getCode())).map(in -> toResult(task, in))
				.collect(Collectors.toList());
		return new FinalFeasibilityQueryResults(results);
	}

	private FinalFeasibilityQueryResult toResult(Task task, ParameterComponent in)
	{
		String cohortId = ((Reference) in.getExtensionByUrl(ConstantsFeasibility.EXTENSION_GROUP_ID_URI).getValue())
				.getReference();
		int participatingMedics = getParticipatingMedicsCountByCohortId(task, cohortId);
		int cohortSize = ((UnsignedIntType) in.getValue()).getValue();
		return new FinalFeasibilityQueryResult(cohortId, participatingMedics, cohortSize);
	}

	private int getParticipatingMedicsCountByCohortId(Task task, String cohortId)
	{
		return task.getInput().stream().filter(in -> in.hasType() && in.getType().hasCoding()
				&& ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY
				.equals(in.getType().getCodingFirstRep().getSystem())
				&& ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS_COUNT
				.equals(in.getType().getCodingFirstRep().getCode()) && cohortId
				.equals(((Reference) in.getExtensionByUrl(ConstantsFeasibility.EXTENSION_GROUP_ID_URI).getValue())
						.getReference())).mapToInt(in -> ((UnsignedIntType) in.getValue()).getValue()).findFirst()
				.getAsInt();
	}

	protected FinalFeasibilityQueryResults checkResults(FinalFeasibilityQueryResults results)
	{
		// TODO implement check for results
		// - criterias tbd
		return results;
	}

	private void addErrorOutputs(Task leadingTask, List<Output> errors)
	{
		errors.forEach(error -> leadingTask
				.addOutput(getTaskHelper().createOutput(error.getSystem(), error.getCode(), error.getValue())));
	}

	private void addResultOutputs(Task leadingTask, FinalFeasibilityQueryResults results)
	{
		results.getResults().forEach(result -> addResultOutput(leadingTask, result));
	}

	private void addResultOutput(Task leadingTask, FinalFeasibilityQueryResult result)
	{
		TaskOutputComponent output1 = getTaskHelper()
				.createOutputUnsignedInt(ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY,
						ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_MULTI_MEDIC_RESULT,
						result.getCohortSize());
		output1.addExtension(createCohortIdExtension(result.getCohortId()));
		leadingTask.addOutput(output1);

		TaskOutputComponent output2 = getTaskHelper()
				.createOutputUnsignedInt(ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY,
						ConstantsFeasibility.CODESYSTEM_HIGHMED_FEASIBILITY_VALUE_PARTICIPATING_MEDICS_COUNT,
						result.getParticipatingMedics());
		output2.addExtension(createCohortIdExtension(result.getCohortId()));
		leadingTask.addOutput(output2);
	}

	private Extension createCohortIdExtension(String cohortId)
	{
		return new Extension(ConstantsFeasibility.EXTENSION_GROUP_ID_URI, new Reference(cohortId));
	}
}
