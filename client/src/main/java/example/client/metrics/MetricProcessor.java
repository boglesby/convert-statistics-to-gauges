package example.client.metrics;

import example.Metric;

@FunctionalInterface
public interface MetricProcessor {

  void process(String serverName, Metric metric);
}
