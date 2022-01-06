package org.highmed.dsf.bpe.variables;

import java.io.IOException;
import java.util.Objects;

import org.camunda.bpm.engine.impl.variable.serializer.PrimitiveValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.ValueFields;
import org.camunda.bpm.engine.variable.impl.value.UntypedValueImpl;
import org.highmed.dsf.bpe.variables.FinalMpcFeasibilityQueryResultValues.FinalMpcFeasibilityQueryResultValue;
import org.springframework.beans.factory.InitializingBean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FinalMpcFeasibilityQueryResultSerializer extends PrimitiveValueSerializer<FinalMpcFeasibilityQueryResultValue>
		implements InitializingBean
{
	private final ObjectMapper objectMapper;

	public FinalMpcFeasibilityQueryResultSerializer(ObjectMapper objectMapper)
	{
		super(FinalMpcFeasibilityQueryResultValues.VALUE_TYPE);

		this.objectMapper = objectMapper;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		Objects.requireNonNull(objectMapper, "objectMapper");
	}

	@Override
	public void writeValue(FinalMpcFeasibilityQueryResultValue value, ValueFields valueFields)
	{
		FinalMpcFeasibilityQueryResult result = value.getValue();
		try
		{
			if (result != null)
				valueFields.setByteArrayValue(objectMapper.writeValueAsBytes(result));
		}
		catch (JsonProcessingException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public FinalMpcFeasibilityQueryResultValue convertToTypedValue(UntypedValueImpl untypedValue)
	{
		return FinalMpcFeasibilityQueryResultValues.create((FinalMpcFeasibilityQueryResult) untypedValue.getValue());
	}

	@Override
	public FinalMpcFeasibilityQueryResultValue readValue(ValueFields valueFields, boolean asTransientValue)
	{
		byte[] bytes = valueFields.getByteArrayValue();

		try
		{
			FinalMpcFeasibilityQueryResult result = (bytes == null || bytes.length <= 0) ? null
					: objectMapper.readValue(bytes, FinalMpcFeasibilityQueryResult.class);
			return FinalMpcFeasibilityQueryResultValues.create(result);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
