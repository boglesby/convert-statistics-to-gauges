package example.client.metrics;

import example.Metric;
import example.client.function.OnServersFunctions;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MetricsProvider {

  @Autowired
  private OnServersFunctions functions;

  private final MeterRegistry registry;

  private final Map<String, Map<String, Number>> serverMetrics;

  private static final Logger logger = LoggerFactory.getLogger(MetricsProvider.class);

  public MetricsProvider(MeterRegistry registry, Environment environment) {
    this.registry = registry;
    this.serverMetrics = new ConcurrentHashMap<>();
    addCommonTags(environment);
    logger.info("Created {}", this);
  }

  private void addCommonTags(Environment environment) {
    String env = "unknown";
    try {
      env = InetAddress.getLocalHost().getHostName();
    } catch (Exception e) {}
    Tags tags = Tags.of("environment", env);
    this.registry.config().commonTags(tags);
  }

  protected double getServerMetric(String serverName, Metric metric) {
    double currentValue = 0;
    Map<String, Number> metrics = this.serverMetrics.get(serverName);
    if (metrics == null) {
      logger.warn("Metric server={}; category={}; type={}; metric={} cannot be retrieved since the server does not exist", serverName, metric.getCategory(), metric.getType(), metric.getName());
    } else {
      Number currentAtomicValue = metrics.get(metric.getMapName());
      if (currentAtomicValue == null) {
        logger.warn("Metric server={}; category={}; type={}; metric={} cannot be retrieved since the metric does not exist", serverName, metric.getCategory(), metric.getType(), metric.getName());
      } else {
        if (currentAtomicValue instanceof AtomicInteger) {
          AtomicInteger ai = (AtomicInteger) currentAtomicValue;
          currentValue = ai.get();
        } else if (currentAtomicValue instanceof AtomicLong && metric.getValue() instanceof Long) {
          AtomicLong al = (AtomicLong) currentAtomicValue;
          currentValue = al.get();
        } else if (currentAtomicValue instanceof AtomicLong && metric.getValue() instanceof Double) {
          AtomicLong al = (AtomicLong) currentAtomicValue;
          currentValue = Double.longBitsToDouble(al.get());
        }
      }
    }
    return currentValue;
  }

  @Scheduled(fixedDelay = 5000)
  protected void updateServerMetrics() {
    processServerMetrics((serverName, metric) -> updateServerMetric(serverName, metric));
  }

  protected void createGauge(String serverName, Metric metric) {
    Tags tags = Tags
      .of("member", serverName)
      .and("category", metric.getCategory())
      .and("type", metric.getType());
    Gauge
      .builder(metric.getName(), this, provider -> provider.getServerMetric(serverName, metric))
      .tags(tags)
      .register(this.registry);
    addServerMetric(serverName, metric);
    logger.info("Registered gauge for server={}; category={}; type={}; metric={}", serverName, metric.getCategory(), metric.getType(), metric.getName());
  }

  protected void addServerMetric(String serverName, Metric metric) {
    Number metricCounter = 0;
    if (metric.getValue() instanceof Integer) {
      metricCounter = new AtomicInteger((Integer) metric.getValue());
    } else if (metric.getValue() instanceof Long) {
      metricCounter = new AtomicLong((Long) metric.getValue());
    } else if (metric.getValue() instanceof Double) {
      long metricValueL = Double.doubleToLongBits((Double) metric.getValue());
      metricCounter = new AtomicLong(metricValueL);
    }
    addServerMetric(serverName, metric, metricCounter);
  }

  private void addServerMetric(String serverName, Metric metric, Number metricValue) {
    Map<String, Number> metrics = this.serverMetrics.get(serverName);
    if (metrics == null) {
      metrics = new ConcurrentHashMap<>();
      this.serverMetrics.put(serverName, metrics);
      logger.info("Created metrics map for server={}", serverName);
    }
    metrics.put(metric.getMapName(), metricValue);
  }

  private void updateServerMetric(String serverName, Metric metric) {
    Map<String, Number> metrics = this.serverMetrics.get(serverName);
    if (metrics == null) {
      createGauge(serverName, metric);
    } else {
      Number currentValue = metrics.get(metric.getMapName());
      if (currentValue == null) {
        createGauge(serverName, metric);
      } else {
        Number newValue = metric.getValue();
        if (newValue instanceof Integer && currentValue instanceof AtomicInteger) {
          AtomicInteger ai = (AtomicInteger) currentValue;
          Integer i = (Integer) newValue;
          ai.set(i);
        } else if (newValue instanceof Long && currentValue instanceof AtomicLong) {
          AtomicLong al = (AtomicLong) currentValue;
          Long l = (Long) newValue;
          al.set(l);
        } else if (newValue instanceof Double && currentValue instanceof AtomicLong) {
          AtomicLong al = (AtomicLong) currentValue;
          Long l = Double.doubleToLongBits((Double) newValue);
          al.set(l);
        }
      }
    }
  }

  public void processServerMetrics(MetricProcessor function) {
    Map<String, List<Metric>> allMetrics = Collections.EMPTY_MAP;
    try {
      allMetrics = (Map) this.functions.getAllMetrics();
    } catch (Exception e) {
      logger.warn("Caught the following exception attempting to retrieve metrics from the servers:", e);
    }
    for (Map.Entry<String, List<Metric>> metrics : allMetrics.entrySet()) {
      String serverName = metrics.getKey();
      for (Metric metric : metrics.getValue()) {
        function.process(serverName, metric);
      }
    }
  }
}
