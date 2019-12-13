package org.highmed.dsf.bpe.plugin;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.context.ApplicationContext;

public class FeasibilityPlugin extends AbstractProcessEnginePlugin
{
	private static final String REQUEST_FILE = "requestSimpleFeasibility.bpmn";
	private static final String COMPUTATION_FILE = "computeSimpleFeasibility.bpmn";
	private static final String EXECUTION_FILE = "executeSimpleFeasibility.bpmn";

	public FeasibilityPlugin(ApplicationContext context)
	{
		super(context);
	}

	@Override
	public void postProcessEngineBuild(ProcessEngine processEngine)
	{
		BpmnModelInstance requestProcess = readAndValidateModel("/" + REQUEST_FILE);
		deploy(processEngine, REQUEST_FILE, requestProcess);

		BpmnModelInstance computationProcess = readAndValidateModel("/" + COMPUTATION_FILE);
		deploy(processEngine, COMPUTATION_FILE, computationProcess);

		BpmnModelInstance executionProcess = readAndValidateModel("/" + EXECUTION_FILE);
		deploy(processEngine, EXECUTION_FILE, executionProcess);
	}
}
