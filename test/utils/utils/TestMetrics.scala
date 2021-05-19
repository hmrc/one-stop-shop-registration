package utils

import com.kenshoo.play.metrics.Metrics
import com.codahale.metrics.MetricRegistry

class TestMetrics extends Metrics {
  override def defaultRegistry: MetricRegistry = new MetricRegistry
  override def toJson: String                  = ""
}