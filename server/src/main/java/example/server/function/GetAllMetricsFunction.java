package example.server.function;

import example.Metric;
import org.apache.geode.StatisticDescriptor;
import org.apache.geode.Statistics;
import org.apache.geode.StatisticsType;
import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.distributed.internal.InternalDistributedSystem;

import java.util.ArrayList;
import java.util.List;

public class GetAllMetricsFunction implements Function {

  private final InternalDistributedSystem system;

  public GetAllMetricsFunction() {
    this.system = (InternalDistributedSystem) CacheFactory.getAnyInstance().getDistributedSystem();
  }

  @Override
  public void execute(FunctionContext context) {
    List<Metric> allMetrics = new ArrayList<>();
    for (Statistics statistics : this.system.getStatisticsManager().getStatsList()) {
      StatisticsType statisticsType = statistics.getType();
      for (StatisticDescriptor descriptor : statisticsType.getStatistics()) {
        String statName = descriptor.getName();
        Metric metric = new Metric(statName, statistics.get(statName), statisticsType.getName(), statistics.getTextId());
        allMetrics.add(metric);
      }
    }
    context.getResultSender().lastResult(allMetrics);
  }

  @Override
  public String getId() {
    return getClass().getSimpleName();
  }
}
