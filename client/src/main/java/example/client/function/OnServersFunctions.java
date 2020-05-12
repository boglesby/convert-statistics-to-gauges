package example.client.function;

import org.springframework.data.gemfire.function.annotation.FunctionId;
import org.springframework.data.gemfire.function.annotation.OnServers;

@OnServers(resultCollector = "onServersFunctionsResultCollector")
public interface OnServersFunctions {

  @FunctionId("GetAllMetricsFunction")
  Object getAllMetrics();
}
