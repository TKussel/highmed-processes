package org.highmed.dsf.bpe.variables;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class FinalMpcFeasibilityQueryResultsValues
{
	public static interface FinalMpcFeasibilityQueryResultsValue extends PrimitiveValue<FinalMpcFeasibilityQueryResults>
	{
	}

	private static class FinalMpcFeasibilityQueryResultsValueImpl
			extends PrimitiveTypeValueImpl<FinalMpcFeasibilityQueryResults>
			implements FinalMpcFeasibilityQueryResultsValues.FinalMpcFeasibilityQueryResultsValue
	{
		private static final long serialVersionUID = 1L;

		public FinalMpcFeasibilityQueryResultsValueImpl(FinalMpcFeasibilityQueryResults value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class FinalMpcFeasibilityQueryResultsValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private FinalMpcFeasibilityQueryResultsValueTypeImpl()
		{
			super(FinalMpcFeasibilityQueryResults.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new FinalMpcFeasibilityQueryResultsValues.FinalMpcFeasibilityQueryResultsValueImpl(
					(FinalMpcFeasibilityQueryResults) value, VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new FinalMpcFeasibilityQueryResultsValues.FinalMpcFeasibilityQueryResultsValueTypeImpl();

	private FinalMpcFeasibilityQueryResultsValues()
	{
	}

	public static FinalMpcFeasibilityQueryResultsValues.FinalMpcFeasibilityQueryResultsValue create(
			FinalMpcFeasibilityQueryResults value)
	{
		return new FinalMpcFeasibilityQueryResultsValues.FinalMpcFeasibilityQueryResultsValueImpl(value, VALUE_TYPE);
	}
}
