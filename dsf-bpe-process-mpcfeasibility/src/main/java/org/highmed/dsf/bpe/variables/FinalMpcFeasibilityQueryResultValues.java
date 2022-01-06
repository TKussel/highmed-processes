package org.highmed.dsf.bpe.variables;

import java.util.Map;

import org.camunda.bpm.engine.variable.impl.type.PrimitiveValueTypeImpl;
import org.camunda.bpm.engine.variable.impl.value.PrimitiveTypeValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.PrimitiveValue;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class FinalMpcFeasibilityQueryResultValues
{
	public static interface FinalMpcFeasibilityQueryResultValue extends PrimitiveValue<FinalMpcFeasibilityQueryResult>
	{
	}

	private static class FinalMpcFeasibilityQueryResultValueImpl
			extends PrimitiveTypeValueImpl<FinalMpcFeasibilityQueryResult>
			implements FinalMpcFeasibilityQueryResultValues.FinalMpcFeasibilityQueryResultValue
	{
		private static final long serialVersionUID = 1L;

		public FinalMpcFeasibilityQueryResultValueImpl(FinalMpcFeasibilityQueryResult value, PrimitiveValueType type)
		{
			super(value, type);
		}
	}

	public static class FinalMpcFeasibilityQueryResultValueTypeImpl extends PrimitiveValueTypeImpl
	{
		private static final long serialVersionUID = 1L;

		private FinalMpcFeasibilityQueryResultValueTypeImpl()
		{
			super(FinalMpcFeasibilityQueryResult.class);
		}

		@Override
		public TypedValue createValue(Object value, Map<String, Object> valueInfo)
		{
			return new FinalMpcFeasibilityQueryResultValues.FinalMpcFeasibilityQueryResultValueImpl(
					(FinalMpcFeasibilityQueryResult) value, VALUE_TYPE);
		}
	}

	public static final PrimitiveValueType VALUE_TYPE = new FinalMpcFeasibilityQueryResultValues.FinalMpcFeasibilityQueryResultValueTypeImpl();

	private FinalMpcFeasibilityQueryResultValues()
	{
	}

	public static FinalMpcFeasibilityQueryResultValues.FinalMpcFeasibilityQueryResultValue create(
			FinalMpcFeasibilityQueryResult value)
	{
		return new FinalMpcFeasibilityQueryResultValues.FinalMpcFeasibilityQueryResultValueImpl(value, VALUE_TYPE);
	}
}
