package testutils

import com.codahale.metrics.MetricRegistry
import uk.gov.hmrc.play.bootstrap.metrics.Metrics


class TestMetrics extends Metrics {
  override def defaultRegistry: MetricRegistry = new MetricRegistry

}